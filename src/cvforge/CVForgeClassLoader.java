package cvforge;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import ij.IJ;

/**
 * Used to replace ImageJ PluginClassLoader.
 * Keeps classinformation up-to-date. Has method for adding new classes.
 * Mainly used to avoid "jar hell" problem,
 * where multiple definitions of opencv may not be loaded simultaneously. 
 */
public class CVForgeClassLoader extends URLClassLoader{

	protected static final String PLUGINDIR = CVForge.PLUGINDIR;
	
	/**
	 * Initialize by loading imagej plugin dir. 
	 */
	CVForgeClassLoader(){
		super(new URL[0], ClassLoader.getSystemClassLoader());
		//this.loadIJ();
	}
		
	/**
	 * Hook definitions for ImageJ classes into loader.
	 */
	public void loadIJ(){		
		String ijPath = PLUGINDIR.substring(0, PLUGINDIR.length()-1);
		ijPath = ijPath.substring(0, ijPath.lastIndexOf(CVForge.SEP));
		try{this.addURL(new URL(ijPath + "ij.jar"));}
		catch(Exception e){IJ.showMessage("IJ load failed");}
	}
	
	/**
	 * Add classes in path to defined classes.
	 * Should point to jar file.
	 * @param path Path to jar.  
	 * @return true, if loading successful.
	 */
	public void addURL(String path) throws MalformedURLException{
		File f = new File(path);
        this.addURL(f.toURI().toURL());
	}
		
	/**
	 * Add classes in url to defined classes.
	 * Should point to jar file.
	 * @param url URL to jar.  
	 * @return true, if loading successful.
	 */
	public void addURL(URL url){
		super.addURL(url);
	}
}
