package cvforge;

import reflectiontools.ClassInspector;
import reflectiontools.JarInspector;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;

import ij.IJ;

import java.awt.Dimension;
import java.awt.Point;
import java.io.CharArrayWriter;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;


public class CVForge {
	
	public static final String SEP = System.getProperty("file.separator");
	
    public static final String VERSION = "CVForge v0.5 (beta)";
    public static final String CONFIGFILE = "cvforge.config";              // location of config file
    
    //public static final String PLUGINDIR = System.getProperty("user.dir") + SEP + "plugins" + SEP;
    public static final String PLUGINDIR = getPluginPath();
    
	public static final String BITS = System.getProperty("sun.arch.data.model");		
	public static final String OS = System.getProperty("os.name");
	
	//protected Point defaultFramePos = new Point(0, 0);
	//protected Point defaultFrameSize = new Point(0,0);
    protected boolean verbose = true;
    protected String libPath = null;                                                // local path to active lib
    protected ArrayList<String> libsAvailable; 		                                // libs available for loading

    // buffered variables
    protected JTree libTree;   												// class and method tree for loaded lib
    protected HashMap<String, Class> classCache;							// mapping of strings to classes
    protected HashMap<String, Method> methodCache;                          // mapping of strings to methods
    protected HashMap<String, String> config;                               // config map

    //protected ClassLoader forgeLoader = ClassLoader.getSystemClassLoader();
    protected CVForgeClassLoader forgeLoader = new CVForgeClassLoader();
    
    /**
     * Call initialization.
     */
    public CVForge(){
    	init();
    }
    
    public static String getPluginPath(){
    	String dir = System.getProperty("plugins.dir");
    	dir = dir.replace("/", SEP);
    	dir = dir.replace("%20", " ");
    	
    	//dir += "plugins" + SEP;
    	//return dir.substring(1,  dir.length());
    	
    	dir += SEP + "plugins" + SEP;
    	return dir;
    }

    /**
     * Initialization by loading config, finding jars, generating cache.
     */
    public void init(){
        loadConfig(CONFIGFILE);
        
        libsAvailable = new ArrayList<String>();
        String[] foundJars = CVInstaller.getInstalledOpenCV();
        for(String found: foundJars)
        	libsAvailable.add(found);
        
        if((libPath == null) && (!libsAvailable.isEmpty())){
    		libPath = libsAvailable.get(0);
        }     
    }

    /**
     * Load config file
     * @param path
     */
    protected void loadConfig(String path){
        config = ConfigIO.loadConfig(path);
        
        // get and convert config values
        String verboseConfig = config.get("verbose");
        verboseConfig = (verboseConfig == null)? "true" : verboseConfig;
        verbose = Boolean.parseBoolean(verboseConfig);
        libPath = config.get("libPath");
        libsAvailable = new ArrayList<String>();
        for(Map.Entry<String,String> entry: config.entrySet()){
            if(entry.getKey().contains("installed-")) {
                libsAvailable.add(entry.getValue());
            }
        }
    }
    
    /**
     * Return the internal ClassLoader.
     * Use with caution, as modifications can potentially break IJ. 
     * @return Internal ClassLoader.
     */
    public CVForgeClassLoader getClassLoader(){
    	return forgeLoader;
    }
    
    /**
     * Loads the OpenCV jar identified by the argument.
     * Generate library tree and method cache on-the-fly.
     * @see generateMethodCache()
     * @see generateLibraryTree()
     * @param version Library version/ path to load.
     */
    public void loadOpenCV(String version) throws Exception{ 
    	String nativePath, jarPath, libName;
    	String bits = (BITS.equals("64"))? "64": "86"; 

    	classCache = new HashMap<String, Class>();
        methodCache = new HashMap<String, Method>();  
    	
    	boolean isWin = OS.contains("Windows");
    	boolean isLinux = OS.contains("Linux");
    	boolean isMac = OS.contains("Mac");
    	
    	try{
	    	if(isWin){
	    		nativePath = PLUGINDIR + "x" + bits + SEP;
	    		jarPath = PLUGINDIR;
	    		libName = new File(version).getName().replace("opencv-", "opencv_java").replace(".jar", ".dll");
	    	}else if(isLinux || isMac){
	    		nativePath = "/usr/lib/jni/";
	    		jarPath = "/usr/share/OpenCV/java/";
	    		libName = new File(version).getName().replace("opencv-", "libopencv_java").replace(".jar", ".so");
	    	}else{
	    		IJ.showMessage("Operating system not recognized.\nUnable to load native libraries.");
	    		return;
	    	}
	    	
	    	if((version != null)/* && libsAvailable.contains(version)*/){    		
	        	libPath = jarPath + version;
	            config.put("libPath", version);
	            
	            forgeLoader.addURL(libPath);
	            methodCache = JarInspector.generateMethodCache(libPath, forgeLoader);
	            classCache = JarInspector.generateConstructableClassCache(libPath, forgeLoader);
	        
	            List<Class> classes = JarInspector.loadClassesFromJar(libPath, forgeLoader);
	            if(!methodCache.isEmpty()){
	        		Executer.initCVForgeExecuter(libPath, (nativePath + libName), forgeLoader);
	        	}
	    	}
    	}catch(Exception e){}
    	generateLibraryTree();
    }

    /**
     * Generate a JTree representation of the library based on the methodCache;
     */
    protected void generateLibraryTree(){
        libTree = new JTree(new DefaultMutableTreeNode("No library loaded"));
        if(!methodCache.isEmpty()){        	
        	try{
        		libTree = LibTreeBuilder.generateLibTree(libPath, forgeLoader);
    		}catch(Exception e){
        		IJ.beep();
    			IJ.showStatus(e.toString());
				CharArrayWriter caw = new CharArrayWriter();
				PrintWriter pw = new PrintWriter(caw);
				e.printStackTrace(pw);
				IJ.log(caw.toString());
        	}
        }
    }
    
    /**
     * TODO
     * Load shards from plugin folder and hook them into the library tree.
     * 
     */
    public void loadShards(){
    	File pluginDir = new File(PLUGINDIR);
      	File[] shards = pluginDir.listFiles(new JarInspector.DefaultFilenameFilter(".shard.jar"));
      	FilteredTreeNode newRoot = new FilteredTreeNode("root");
      	newRoot.add((FilteredTreeNode)libTree.getModel().getRoot());
      	
       	for(File f: shards){
    		try{
    			String path = f.getAbsolutePath();
    			forgeLoader.addURL(path);		
    			HashMap<String, Method> shardCache = JarInspector.generateMethodCache(path, forgeLoader);
    			this.methodCache.putAll(shardCache);

    			// HACK
				JTree shardTree = LibTreeBuilder.generateLibTree(path, forgeLoader, true);
    			FilteredTreeNode shardRoot = (FilteredTreeNode)shardTree.getModel().getRoot();
    			newRoot.add(shardRoot);
    		}catch(Exception e){System.out.println(e);}
    	}
       	libTree = new JTree(new FilteredTreeModel(newRoot)); 
       	libTree.setRootVisible(false);
    }
    

    /**
     * Install and remember OpenCV jar.
     * The path to the jar will be stored in the config file once the plugin saves.
     * @see CVInstaller.installOpenCV()
     * @param path Path to OpenCV jar.
     */
    public void installOpenCV(String path){
        if(CVInstaller.installOpenCV(path, forgeLoader)){
        	String relPath = PLUGINDIR + path.substring(path.lastIndexOf(SEP)+1, path.length());
	        config.put("libPath", relPath);
	        
	        if(!libsAvailable.contains(relPath))
	        	libsAvailable.add(relPath);
        }else{
        	IJ.showMessage("installation of jar failed: " + path);
        }
    }

    /**
     * Dump config map in file defined by CONFIGPATH.
     */
    public void saveSettings(){
    	ConfigIO.writeConfig(config, CONFIGFILE);
    }

    /**
     * Get name of currently loaded OpenCV lib.
     * Returns null, if none loaded/ available.
     * @return local path to currently loaded library, null else.
     */
    public String activeLib(){
        return libPath;
    }
    
    /**
     * Get list of installed libraries.
     * @return Paths to known libraries. 
     */
    public ArrayList<String> availableLibs(){
    	return libsAvailable;
    }

    /**
     * Tree representation of loaded library and its methods.
     * @return Generated JTree, granted that a library has been loaded.
     */
    public JTree getLibraryTree(){
        return libTree;
    }

    /**
     * Mapping of method name to method.
     * @return Generated cache of methods, granted a library has been loaded.
     */
    public HashMap<String, Method> getMethodCache(){
        return methodCache;
    }
    
    /**
     * Mapping of class name to class.
     * @return Generated cache of methods, granted a library has been loaded.
     */
    public HashMap<String, Class> getClassCache(){
    	return classCache;
    }

    /**
     * Enable to show error log popups in ImageJ.
     * @param v Set to true, if logs should be shown.
     */
    public void setVerbose(boolean v){
    	config.put("verbose", Boolean.toString(v));
    	verbose = v;
    }
    
    /**
     * Show if verbose error logs are enabled.
     * @return True, if verbose messages enabled.
     */
    public boolean isVerbose(){
    	return verbose;
    }
    
    /**
     * Gets Frame position from config file.
     * @return Stored Frame position from earlier session.
     */
    public Point restoreWindowPosition(){
    	String x = config.get("winX");
    	String y = config.get("winY");
    	
    	if((x != null) && (y != null))
    		return new Point(Integer.parseInt(x), Integer.parseInt(y));
    	else
    		return new Point(0, 0);
    }
    
    /**
     * Gets Frame size from config file.
     * @return Stored Frame size from earlier session.
     */
    public Dimension restoreWindowSize(){
    	String width = config.get("winWidth");
    	String height = config.get("winHeight");
    	
    	if((width != null) && (height != null))
    		return new Dimension(Integer.parseInt(width), Integer.parseInt(height));
    	else
    		return new Dimension(0, 0);
    }
    
    /**
     * Save position and size of frame in config file.
     * @param pos Frame position to be stored.
     * @param size Frame size to be stored.
     */
    public void storeWindowDimensions(Point pos, Dimension size){
    	config.put("winX", ""+pos.x);
    	config.put("winY", ""+pos.y);
    	config.put("winWidth", ""+size.width);
    	config.put("winHeight", ""+size.height);
    }
}
