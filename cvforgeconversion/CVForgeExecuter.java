package cvforgeconversion;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

import org.opencv.core.Mat;

import cvforge.CVForgeCache;

import java.lang.reflect.Method;


public final class CVForgeExecuter {
	
	/**
	 * Must be called to load the OpenCV native library from current ClassLoader context.
	 * @param path Path to native library.
	 */
	public static void loadDll(String path){
		System.load(path);
	}
	
	/**
	 * Call this method by reflection.
	 * @param m Method to be called.
	 * @param args Arguments for method.
	 * @param cacheTarget Name to be used for caching. If empty String is given, the call result will not be added to CVForgeCache. 
	 * @throws Exception Exception thrown in case of invocation failure.
	 */
	public static void execute(Method m, Object[] args, String cacheTarget) throws Exception {
		Object[] argsConv = new Object[args.length];
		for(int i=0; i<args.length; ++i){
			if(args[i] instanceof ImageProcessor){
				Mat cvmat = CVForgeConverter.createCompatibleMat((ImageProcessor)args[i]);
				CVForgeConverter.ij2cv((ImageProcessor)args[i], cvmat);
				argsConv[i] = cvmat;
			}else{
				argsConv[i] = args[i];
			}
		}
		
		Object callResult = m.invoke(null, argsConv);
		
		if((!cacheTarget.isEmpty()) && (callResult != null)){
			if(callResult instanceof Mat){
				ImageProcessor ip = CVForgeConverter.createCompatibleProcessor((Mat)callResult);
				CVForgeConverter.cv2ij((Mat)callResult, ip);
				ImagePlus image = new ImagePlus(cacheTarget, ip);
				image.show();
			}
			CVForgeCache.add(cacheTarget, callResult);
		}
		
		for(int i=0; i<args.length; ++i){
			if(argsConv[i] instanceof Mat){
				Mat cvmat = CVForgeConverter.createCompatibleMat((ImageProcessor)args[i]);
				CVForgeConverter.cv2ij((Mat)argsConv[i], (ImageProcessor)args[i]);
				argsConv[i] = cvmat;
			}else{
				argsConv[i] = args[i];
			}
		}
	}
}
