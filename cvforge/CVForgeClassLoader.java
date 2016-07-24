package cvforge;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import ij.IJ;
import ij.io.PluginClassLoader;

/**
 * Used to replace ImageJ PluginClassLoader.
 * Keeps classinformation up-to-date.
 * Has accessable method for adding new classes, thus bypassing reflections
 */
public class CVForgeClassLoader extends PluginClassLoader{

	protected static final String PLUGINDIR = CVForge.PLUGINDIR;
	
	/**
	 * Initialize by loading imagej plugin dir. 
	 */
	CVForgeClassLoader(){
		this(PLUGINDIR);
	}
	
	/**
	 * Initialize loader and get classes from defined directory.
	 * @param path Path with classes for initilization.
	 */
	CVForgeClassLoader(String path){
		super(path);
	}
	
	public void loadNativeLib(String path){		
		System.load(path);
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
		
	// Overloaded version for url.
	public void addURL(URL url){
		super.addURL(url);
	}
}
