package cvforge;

import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.List;

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
	
	
	/**
	 * Load OpenCV methods and native library.
	 * Load CVForgeExecuter and its methods.
	 * @param cvPath
	 * @throws Exception
	 */
	public static void initCVForgeExecuter(String cvPath, String dllPath, CVForgeClassLoader loader) throws Exception{		
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
		
		Method init = executer.getMethod("loadDll", String.class);
		init.setAccessible(true);
		init.invoke(null, dllPath);
	
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
