package reflectiontools;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ClassInspector {	
	
    // Filter for class files
    public final static class ClassFilter implements FileFilter {
        public boolean accept(File file){
            return file.getName().endsWith("class");
        }
    }

    /**
     * Load single class identified by its path.
     * @param path Path to class.
     * @return Loaded class, or null if loading failed.
     */
    public static Class loadClass(String path){
        File file = new File(path);
        Class loadedClass = null;
        try {
            URLClassLoader loader = new URLClassLoader(new URL[]{file.toURI().toURL()});
            loadedClass = Class.forName(path, true, loader);
        }catch(Exception e){System.out.println(e);}
        return loadedClass;
    }

    /**
     * Load all classes from a designated directory.
     * @param path Directory path.
     * @return List of loaded classes.
     */
    public static List<Class> loadClasses(String path){
        File file = new File(path);
        ArrayList<Class> classes = new ArrayList<Class>();
        File[] classFiles = getClasses(path);
        List<String> classNames = new ArrayList<String>();
        try {
            URLClassLoader loader = new URLClassLoader(new URL[]{file.toURI().toURL()});
            for(File cf: classFiles){
                if(cf.getName().endsWith("class")) {
                    String name = cf.getName();
                    name = name.substring(0, name.length()-6);
                    classNames.add(name);
                }
            }
            for(String name: classNames) {
                classes.add(Class.forName(name, true, loader));
            }

            loader.close();
        }catch(Exception e){}
        return classes;
    }

    /**
     * Get all class files in directory.
     * @param path Path to directory.
     * @return List of all files with .class file suffix.
     */
    public static File[] getClasses(String path){
        File dir = new File(path);
        File[] files = dir.listFiles(new ClassFilter());
        return files;
    }

    /**
     * Construct String containing method type and parameter types. 
     * @param m Method of interest.
     * @return String containing method type and parameter types.
     */
    public static String infoString(Method m){ 	
    	String result = m.getReturnType().getSimpleName();
		result += " " + m.getName()+ "(";
    	for(Class paramType: m.getParameterTypes())
    		result += paramType.getSimpleName() + ", ";            	
    	if(m.getParameters().length != 0)
    		result = result.substring(0, result.length()-2);
    	result += ")";
    	return result;
    }
    
    /**
     * Gets all constructors that only require primitive types.
     * @param src Class to inspect.
     * @return Array of primitive constructors.
     */
    public static Constructor[] getPrimitiveConstructors(Class src){
    	ArrayList<Constructor> primitives = new ArrayList<Constructor>();
    	Constructor[] cons = src.getConstructors();
    	for(Constructor c: cons){
    		boolean primitiveCon = true;
    		Class[] params = c.getParameterTypes();
    		for(Class p: params){
    			if(!p.isPrimitive()){
    				primitiveCon = false;
    				break;
    			}
    		}
    		if(primitiveCon){
    			primitives.add(c);
    		}
    	}
    	return (Constructor[])primitives.toArray();
	}
    
    
    // check if a method is static
    public static boolean isStatic(Method m){
        return (m.getModifiers() & Modifier.STATIC) != 0;
    }

    // check if a method is public
    public static boolean isPublic(Method m){
        return (m.getModifiers() & Modifier.PUBLIC) != 0;
    }

    // check if method is void (no shit)
    public static boolean isVoid(Method m){
        return (m.getReturnType().equals(Void.TYPE));
    }
}
