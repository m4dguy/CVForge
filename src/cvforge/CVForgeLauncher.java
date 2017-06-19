package cvforge;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import ij.IJ;
import ij.Macro;
import ij.WindowManager;
import ij.plugin.PlugIn;

import cvforge.CVForgeCache;

/**
 * Launcher module creating either CVForgeFrame instance or setting up headless mode.
 * CVForgeFrame is created in normal use case. 
 * Headless mode directly acts on submitted arguments and is used for macros.
 * In both cases a OpenCV is loaded and methods are created and cached.
 */
public class CVForgeLauncher implements PlugIn{
	
	// macro parameters
	protected String methodName;
	protected String[] methodArgs;
	
	// reference to CVForge object
	public static CVForgeFrame CVFORGEFRAME;
	
	/**
	 * Inherited from PlugIn interface.
	 */
	public void run(String arg){
		String ops = Macro.getOptions();
		
		// launch forge
		if(CVFORGEFRAME == null){
			CVFORGEFRAME = (CVForgeFrame)IJ.runPlugIn(CVForgeFrame.class.getName(), "");
		}
		
		// extract arguments
		if(ops != null){
			String[] args = extractArgs(ops);
			methodName = args[0];
			methodArgs = new String[args.length-1];
			for(int i=1; i<args.length; ++i){
				methodArgs[i-1] = args[i];
			}	
		}else{
			CVFORGEFRAME.setVisible(true);
		}
		
		// if arguments exist, call filter
		if((methodName != null) && (methodArgs != null)){			
			Method method = getMethod(methodName, methodArgs);
			Object[] args = convertArguments(method, methodArgs);
			
			long startTime = System.currentTimeMillis();
			IJ.showStatus("running " + methodName + "...");
			CVForgeFrame.lockAllImages();
			try{
				Executer.executeMethod(method, args, method.getReturnType().getName());
				IJ.showStatus((System.currentTimeMillis()-startTime)+" milliseconds");
			} catch(OutOfMemoryError e) {
				IJ.outOfMemory(CVForge.VERSION);
			} catch(Exception e) {
				IJ.beep();
				IJ.showStatus(e.toString());
				e.printStackTrace();
				CharArrayWriter caw = new CharArrayWriter();
				PrintWriter pw = new PrintWriter(caw);
				e.printStackTrace(pw);
				IJ.log(caw.toString());
			}
			CVForgeFrame.unlockAllImages();
		}
		
	}
	
	/**
	 * Get method corresponding to name and argument signature.
	 * @param methodName Name of the method. 
	 * @param methodArgs String representations of the argumetns.
	 * @return
	 */
	protected Method getMethod(String methodName, String[] methodArgs){
		Method res = null;
		HashMap<String, Method> methodCache = CVFORGEFRAME.FORGE.methodCache; 
		Set<String> keySet = methodCache.keySet();
		
		// find methods with matching names
		ArrayList<Method> matches = new ArrayList<Method>();
		for(String key: keySet){
			if(key.contains(methodName)){
				matches.add(methodCache.get(key));
			}
		}

		// choose method with matching parameter signature
		for(Method m: matches){
			if(m.getParameterCount() == methodArgs.length){
				res = m;
			}
		}
		return res;
	}
	
	/**
	 * Returns arguments for method call as array.
	 * First element of array is method name itself.
	 * @param arg Argument string from which single arguments are extracted.
	 * @return Arguments 
	 */
	protected String[] extractArgs(String arg){
		String[] args = arg.split(" ");
		String[] res = new String[args.length];
		
		for(int i=0; i<args.length; ++i){
			String[] splits = args[i].split("=");
			res[i] = splits[1];
		}
		
		return res;
	}

	/**
	 * TODO refac, CVCallFrame is doing something similar.
	 * Convert the arguments in preparation for method call.
	 * @param method Method to be called.
	 * @param args Arguments to be converted to suitable method parameters.
	 * @return Converted Objects for method call.
	 */
	protected Object[] convertArguments(Method method, String[] args){
		Object[] conv = new Object[args.length];
		
		Class[] params = method.getParameterTypes();
		for(int i=0; i<args.length; ++i){
			String arg = args[i];
			Class param = params[i];
			if(param.isPrimitive()){
				conv[i] = InputHelpers.stringToPrimitive(arg, param);
			}else{
				if(param.getName().equals(InputHelpers.CVMAT)){
					if(arg.equals("[currentWindow]")){
						arg = WindowManager.getCurrentWindow().getTitle();
						conv[i] = WindowManager.getImage(arg).getProcessor();
					}
				}
				// if object is cached, do lookup
				if(arg.startsWith("[cache:")){
					String key = arg.substring(7, arg.length()-2);
					conv[i] = CVForgeCache.get(key);
					if(conv[i] == null){
						IJ.showMessage("CVForge Error", "Cache lookup for named object \"" + key + "\" failed!");
					}
				}
			}
		}
		return conv;
	}
	
}
