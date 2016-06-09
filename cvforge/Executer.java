package cvforge;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import ij.IJ;
import reflectiontools.JarInspector;

// handle with care!
public final class Executer {
	
	protected static final String SEP = CVForge.SEP;
	protected static final Class[] SIG = {Method.class, Object[].class, String.class};
	
	protected static Class executer; 
	protected static Method execute;
	
	protected static URLClassLoader classLoader;
	protected static ClassLoader systemLoader = ClassLoader.getSystemClassLoader();
	
	protected static final String EXECUTERNAME = "CVForgeExecuter";
	protected static final String CONVERTERJAR = "CVForgeConversion.jar";
	
	/*public static URLClassLoader getClassLoader(){
		if(classLoader == null){
			classLoader = URLClassLoader.newInstance(new URL[]{new File(path).toURI().toURL()}); 
		}
		return classLoader;
	}*/
	
	/**
	 * Add path to internal classloader.
	 * @param path Path of class to add.
	 * @throws Exception Thrown, if loading and adding the class fails.
	 */
	/*protected static void addToClassLoader(String path) throws Exception{				
		if(classLoader == null){
			//ClassLoader parentLoader = IJ.getClassLoader();
			ClassLoader parentLoader = ClassLoader.getSystemClassLoader();
			classLoader = URLClassLoader.newInstance(new URL[]{new File(path).toURI().toURL()}, parentLoader);
			//classLoader = URLClassLoader.newInstance(new URL[]{new File(path).toURI().toURL()});

			Class ijclass = IJ.class;
			Method loaderSetter = ijclass.getDeclaredMethod("setClassLoader", new Class[]{ClassLoader.class});
			loaderSetter.setAccessible(true);
			
			//IJ.showMessage("loaderSetter" + loaderSetter.toString());
			loaderSetter.invoke(null, classLoader);
			
			JarFile jar = new JarFile(path);
	        List<JarEntry> entries = JarInspector.getJarEntries(jar, false);
	        List<String> classEntries = new ArrayList<String>();
	        for(JarEntry j: entries){
	            if(j.getName().endsWith("class")) {
	                String name = j.getName();
	                name = name.substring(0, name.length()-6);
	                classEntries.add(name.replace("/","."));
	            }
	        }
	        for(String name: classEntries){
	        	Class.forName(name, true, classLoader);
	        }

		}else{
			Class loaderClass = URLClassLoader.class;
			Method addMethod = loaderClass.getDeclaredMethod("addURL", new Class[] { URL.class });
			addMethod.setAccessible(true);
			addMethod.invoke(classLoader, new Object[] { new URL(path) });
		}
	}*/
	
	/**
	 * Load OpenCV methods 
	 * Load CVForgeExecuter and execute method.
	 * @param cvPath
	 * @throws Exception
	 */
	public static void initCVForgeExecuter(String cvPath, CVForgeClassLoader loader) throws Exception{
		final String path = CVForge.PLUGINDIR + CONVERTERJAR;
		loader.addURL(path);
		List<Class> classes = JarInspector.loadClassesFromJar(path, loader);
		for(Class c: classes){
			if(c.getSimpleName().equals(EXECUTERNAME)){
				executer = c;
				IJ.register(c);
			}
		}
		
		if(executer == null)
			throw new ClassNotFoundException("No Execution/Conversion module found.");
			
		IJ.register(executer);
		execute = executer.getMethod("execute", SIG);
		execute.setAccessible(true);
	}
	
	/**
	 * Execute given OpenCV method.
	 * @param m Method to execute.
	 * @param args Arguments for method.
	 * @param cacheTarget Destination for cache. Only relevant if method has return type.
	 * @throws Exception Thrown, if execution fails.
	 */
	public static void executeMethod(Method m, Object[] args, String cacheTarget) throws Exception{ 
		execute.invoke(null, m, args, cacheTarget);
	}
	
	/**
	 * Check if CVForgeExecuter class and execute method loaded.  
	 * @return true, if CVForgeExecuter class and execute method loaded.
	 */
	public static boolean ready(){
		return (executer != null) && (execute != null);
	}
}
