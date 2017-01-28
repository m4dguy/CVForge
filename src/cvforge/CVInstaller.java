package cvforge;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import ij.IJ;

import java.net.URL;
import java.net.URLClassLoader;

import reflectiontools.JarInspector;

/**
 * Utility functions for installing OpenCV jars and checking for installed OpenCV jars.
 */
public class CVInstaller {
	// filepath separator
	protected static final String SEP = CVForge.SEP;
	
	/**
	 * Add given path to java class path. 
	 * @param path Path to add.
	 */
	protected static void addClassPath(String path) throws Exception{					
		// TODO expose Executer classloader!
		// TODO hook Executer classloader in here!
		URLClassLoader loader = (URLClassLoader)IJ.getClassLoader();
		
		/*if(IJ.getClassLoader() instanceof URLClassLoader){
			IJ.showMessage("loader is urlloader");
		}else{
			IJ.showMessage("loader is " + IJ.getClassLoader().getClass().getName());
		}*/
		
		Class loaderClass = URLClassLoader.class;
		Method method = loaderClass.getDeclaredMethod("addURL", new Class[] {URL.class});
		method.setAccessible(true);
		
		File file = new File(path);
		method.invoke(loader, new Object[]{file.toURI().toURL()});
	}
	
    /**
     * Check for installed versions of OpenCV and return their names.
     * Looks in "JAVA_HOME/lib/ext" for opencv-xxx.jar files and lists them.
     * Null, if none installed.
     * @return Names/ version numbers of installed OpenCV versions.
     */
    public static String[] getInstalledOpenCV(){
    	File pluginDir;
    	if(CVForge.OS.contains("Windows")){
    		//IJ.showMessage(CVForge.getPluginPath());
    		pluginDir = new File(CVForge.PLUGINDIR);
    	}else{
    		pluginDir = new File("/usr/share/OpenCV/java/");
    	}
    	
        File[] cvJars = pluginDir.listFiles(new JarInspector.DefaultFilenameFilter("opencv"));
        
        String[] cvPaths = new String[cvJars.length];
        for(int i=0; i<cvJars.length; ++i)
        	cvPaths[i] = cvJars[i].getName();
        
        return cvPaths;
    }

    /**
     * Intstall jar from specified path in current active java directory.
     * This actually copies the OpenCV jar and library files into the jre execution directory.
     * Under regular conditions this would be a bad thing to do, but every package ImageJ comes with its own jre.
     * However, this means that running ImageJ with a jre other than the one deployed with it, can lead to undesirable behaviour.
     * Be sure that the jar installed by this method is in fact an OpenCV file!
     * @param cvPath Path to directory containing OpenCV jar and dll subdirectories.
     * @return false, if not successful.
     */
    public static boolean installOpenCV(String cvPath, CVForgeClassLoader loader){
    	File cvFile = new File(cvPath);
    	File cvDir = cvFile.getParentFile();
    	
    	File dstJar = new File(CVForge.PLUGINDIR + cvFile.getName());
    	
        String dllName = cvFile.getName().replace("opencv-", "opencv_java").replace(".jar", ".dll");
        File dllX86 = new File(cvDir.getPath() + SEP + "x86" + SEP + dllName);
        File dllX64 = new File(cvDir.getPath() + SEP + "x64" + SEP + dllName);
        
        File dstDirX86 = new File(dstJar.getParent() + SEP + "x86" + SEP);
		File dstDirX64 = new File(dstJar.getParent() + SEP + "x64" + SEP);
		
		File dstX86 = new File(dstDirX86.toPath() + SEP + dllName);
		File dstX64 = new File(dstDirX64.toPath() + SEP + dllName);
		
        try{ 
        	dstDirX86.mkdir();
        	dstDirX64.mkdir();
        	Files.copy(cvFile.toPath(), dstJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
        	Files.copy(dllX86.toPath(), dstX86.toPath(), StandardCopyOption.REPLACE_EXISTING);
        	Files.copy(dllX64.toPath(), dstX64.toPath(), StandardCopyOption.REPLACE_EXISTING);
        	//addClassPath(dstJar.getAbsolutePath());
        	//addClassPath(cvFile.getAbsolutePath());
        	//loader.addURL(dstJar.getAbsolutePath());
        	//loader.addURL(cvPath);
    	}catch( Exception e ){
    		IJ.showMessage("installation error");
        	return false;
    	}
        return true;
    }

    /**
     * Checks if given OpenCV directory contains OpenCV jar and x86/x64 subfolders.
     * The path must identify the top folder of an OpenCV directory downloaded from the official OpenCV websites.
     * The directory is expected to have the files/subfolders "build/java/opencv-xxx.jar", "build/java/x86", "build/java/x64".
     * @return File reference to java directory containing OpenCV jar and subfolders, null otherwise.
     */
    public static File checkForOpenCV(String path){
        // top level
        File topDir = new File(path);
        if(!topDir.isDirectory())
            return null;

        // crawl through directories
        File buildDir = JarInspector.findSubDirectory(topDir, "build");
        File javaDir = JarInspector.findSubDirectory(buildDir, "java");
        if(javaDir == null)
            return null;

        // ensure dll directories and jar exist (eeew, code duplication ahead)
        File[] x86Dir = javaDir.listFiles(new JarInspector.DefaultFilenameFilter("x86"));
        if(x86Dir.length != 1)
            return null;

        File[] x64Dir = javaDir.listFiles(new JarInspector.DefaultFilenameFilter("x64"));
        if(x64Dir.length != 1)
            return null;

        File[] cvJar = javaDir.listFiles(new JarInspector.DefaultFilenameFilter("opencv-"));
        if(cvJar.length != 1)
            return null;

        if(!cvJar[0].getName().endsWith("jar"))
            return null;

        return javaDir;
    }
}
