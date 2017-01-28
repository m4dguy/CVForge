package reflectiontools;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.*;

/**
  * Reflection toolkit.
  * Inspector for jars and classes.
  */
public class JarInspector {

    // filter for jar files
    public final static class JarFilter implements FileFilter{
        public boolean accept(File file){
            return file.getName().endsWith("jar");
        }
    }

    // Filter for directories
    public final static class DirFilter implements FileFilter {
        public boolean accept(File file) { return file.isDirectory(); }
    }

    // Filter for files containing specified substring in their name
    public final static class DefaultFilenameFilter implements FileFilter {
        private String name;
        public DefaultFilenameFilter(String name){ this.name = name; }
        public boolean accept(File file) { return (file.getName().contains(name)); }
    }

    /**
     * Returns subdirectory of given name.
     * If the given File object is not a directory or null, null is returned.
     * If no directoy of given name is found, null is returned.
     * @param topDir Top directory to search.
     * @param dirName Name of wanted subdirectory.
     * @return File reference to subdirectory, null else.
     */
    public static File findSubDirectory(File topDir, String dirName){
        if(topDir == null || !topDir.isDirectory())
            return null;

        File[] subDirs = topDir.listFiles(new DirFilter());
        for(File d: subDirs){
            if(d.getName() == dirName){
                return d;
            }
        }
        return null;
    }

    /**
     * Get all jars inside designated directory.
     * @param path Path to directory.
     * @return ArrayList containing jars as File objects.
     */
    public static File[] getJars(String path){
        File dir = new File(path);
        File[] files = dir.listFiles(new JarFilter());
        return files;
    }

    /**
     * Retrieve all jars in designated directory.
     * @param path Path to directory.
     * @return List containing jars as JarFile objects.
     */
    public static JarFile[] getJarFiles(String path){
        File[] files = getJars(path);
        if(files == null)
            return null;

        JarFile[] jars = new JarFile[files.length];
        for (int i=0; i<files.length; ++i) {
            try{jars[i] = (new JarFile(files[i], true));}
            catch(Exception e){}
        }
        return jars;
    }

    // get entries in jar (exclude directories)
    public static ArrayList<JarEntry> getJarEntries(JarFile jar){
        return getJarEntries(jar, false);
    }

    /**
     * Get entries contained in JarFile.
     * @param jar JarFile to read jar entries from
     * @param includeDirectories Include directories in result list.
     * @return ArrayList of entries contained in JarFile.
     */
    public static ArrayList<JarEntry> getJarEntries(JarFile jar, boolean includeDirectories){
        ArrayList<JarEntry> res = new ArrayList<JarEntry>();
        Enumeration<JarEntry> entries = jar.entries();
        while(entries.hasMoreElements()){
            JarEntry entry = entries.nextElement();
            if(entry.isDirectory()) {
                if(includeDirectories)
                    res.add(entry);
            }else{
                res.add(entry);
            }
        }
        return res;
    }
    
    /**
     * Loads classes from designated jar file. Allows use of custom ClassLoader.
     * @param path Path to jar.
     * @param baseLoader Base ClassLoader to be used for loading. 
     * @return ArrayList containing loaded classes.
     */
    public static List<Class> loadClassesFromJar(String path, ClassLoader baseLoader) throws Exception{
        // TODO loader dies at method exit without passing loaded class info 
    	
    	File file = new File(path);
        ArrayList<Class> classes = new ArrayList<Class>();
        URLClassLoader loader;
        
        if(baseLoader == null)
        	loader = URLClassLoader.newInstance(new URL[]{file.toURI().toURL()});
        else
        	loader = URLClassLoader.newInstance(new URL[]{file.toURI().toURL()}, baseLoader);
        
        JarFile jar = new JarFile(file);
        List<JarEntry> entries = getJarEntries(jar, false);
        List<String> classEntries = new ArrayList<String>();
        
        for(JarEntry j: entries){
            if(j.getName().endsWith("class")) {
                String name = j.getName();
                name = name.substring(0, name.length()-6);
                classEntries.add(name.replace("/","."));
            }
        }
        
        for(String name: classEntries)
        	classes.add(loader.loadClass(name));
        
        return classes;
    }
    
    // overloaded version
    public static List<Class> loadClassesFromJar(String path) throws Exception{
        return loadClassesFromJar(path, null);
    }
    
    /**
     * Load methods into hashmap, where keys denote the method name.
     * @param path Path to jar file.
     * @param loader ClassLoader to use.
     * @return HashMap containing methods.
     */
    public static HashMap<String, Method> generateMethodCache(String path, ClassLoader loader) throws Exception {
        HashMap<String, Method> methodCache = new HashMap<String, Method>();
        List<Class> classes = loadClassesFromJar(path, loader);
        for (Class c: classes){
            Method[] methods = c.getMethods();
            for(Method m: methods){
            	String key = ClassInspector.infoString(m);            	
                methodCache.put(key, m);
            }
        }
        return methodCache;
    }
    
    // overloaded version
    public static HashMap<String, Method> generateMethodCache(String path) throws Exception {
        return generateMethodCache(path, null);
    }
    
    /**
     * Load clases into hashmap, where keys denote the class name.
     * @param path Path to jar file.
     * @param loader ClassLoader to use.
     * @return
     */
    public static HashMap<String, Class> generateClassCache(String path, ClassLoader loader) throws Exception {
        HashMap<String, Class> classCache = new HashMap<String, Class>();
        List<Class> classes = loadClassesFromJar(path, loader);
        for (Class c: classes){
            String key = c.getSimpleName();
            classCache.put(key, c);
        }
        return classCache;
    }
    
    // overloaded version
    public static HashMap<String, Class> generateClassCache(String path) throws Exception {
        return generateClassCache(path, null);
    }
    
    /**
     * Load classes with constructors to hashmap, where keys denote the class name.
     * @param path Path to jar file.
     * @return
     */
    public static HashMap<String, Class> generateConstructableClassCache(String path, ClassLoader loader) throws Exception {
        HashMap<String, Class> classCache = new HashMap<String, Class>();
        List<Class> classes = loadClassesFromJar(path, loader);
        for (Class c: classes){
        	if(c.getConstructors().length != 0){
        		String key = c.getSimpleName();
        		classCache.put(key, c);
        	}
        }
        return classCache;
    }
    
    // overloaded version
    public static HashMap<String, Class> generateConstructableClassCache(String path) throws Exception {
    	return generateConstructableClassCache(path, null);
    }
}
