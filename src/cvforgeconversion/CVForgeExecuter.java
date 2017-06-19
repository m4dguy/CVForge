package cvforgeconversion;

import java.lang.reflect.Method;
import java.awt.Rectangle;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

import cvforge.CVForgeCache;

/**
 * Execution module for OpenCV methods.
 * This module has a dependency to CVForgeConverter and OpenCV.
 * It is self-contained to keep the CVForge core package clean and free of any direct dependency to OpenCV.
 */
public final class CVForgeExecuter {
	
	/**
	 * Must be called to load the OpenCV native library from current ClassLoader context.
	 * This method is located in this module since it is used by CVForge's own ClassLoader.
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
		// conversion from imagej to opencv
		Object[] argsConv = new Object[args.length];
		for(int i=0; i<args.length; ++i){
			if(args[i] instanceof ImageProcessor){
				Mat cvmat = CVForgeConverter.createCompatibleMat((ImageProcessor)args[i]);
				ImageProcessor ip = (ImageProcessor)args[i];
				CVForgeConverter.ij2cv(ip, cvmat);
				
				// extract region of interest
				Rectangle roi = ip.getRoi();
				Mat cvsubmat = new Mat(cvmat, new Rect(roi.x, roi.y, roi.width, roi.height));
				argsConv[i] = cvsubmat;
			}else{
				argsConv[i] = args[i];
			}
		}
		// method execution
		Object callResult = m.invoke(null, argsConv);
		// show result and add to cache if new object was created
		if((!cacheTarget.isEmpty()) && (callResult != null)){
			if(callResult instanceof Mat){
				ImageProcessor ip = CVForgeConverter.createCompatibleProcessor((Mat)callResult);
				CVForgeConverter.cv2ij((Mat)callResult, ip);
				ImagePlus image = new ImagePlus(cacheTarget, ip);
				image.show();
			}
			CVForgeCache.add(cacheTarget, callResult);
		}
		// backconversion from opencv to imagej		
		for(int i=0; i<args.length; ++i){
			if(argsConv[i] instanceof Mat){
				Mat cvmat = CVForgeConverter.createCompatibleMat((ImageProcessor)args[i]);
				
				ImageProcessor ip = (ImageProcessor)args[i];
				CVForgeConverter.cv2ij((Mat)argsConv[i], (ImageProcessor)args[i], ip.getRoi().x, ip.getRoi().y);
				argsConv[i] = cvmat;
				// free matrix memory
				//cvmat.release();
			}else{
				argsConv[i] = args[i];
			}
		}
	}
}
