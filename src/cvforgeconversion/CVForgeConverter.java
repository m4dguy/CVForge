package cvforgeconversion;

import java.awt.Rectangle;
import ij.process.ImageProcessor;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.gui.Roi;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.CvType;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;


/**
 * Converters for ImageJ objects and OpenCV objects.
 * These converters provide the "glue" necessary for ImageJ/OpenCV interop.
 * This class is kept in a self-contained package to keep the CVForge core package clean and free of any direct dependency to OpenCV.
 */
public final class CVForgeConverter{	
	// predefinitions
	protected static final int CV_8UC3 = 16;
	protected static final int CV_8U = CvType.CV_8U;
	protected static final int CV_16U = CvType.CV_16U;
	protected static final int CV_32S = CvType.CV_32S;
	protected static final int CV_32F = CvType.CV_32F;
	
	// get corresponding cvtype of imageprocessor
	/*public static int toCvType(ImageProcessor ip){
		final int channels = ip.getNChannels();
		final int depth = ip.getBitDepth()/8;
		return CvType.makeType(depth, channels);
	}*/
	
	/**
	 * TODO replace this by upper method
	 * Get OpenCV Mat type corresponding to given ImageProcessor.
	 * @param ip
	 * @return
	 */
	public static int toCvType(ImageProcessor ip){
		switch(ip.getBitDepth()){
			case 8:
				return CvType.CV_8U;	
			case 16:
				return CvType.CV_16U ;
			case 24:
				return CvType.CV_8UC3;
			case 32: 
				return CvType.CV_32F;
			default: 
				return -1;
		}
	}
    
    /**
     * Creates a Mat which has the same size and type as the ImageProcessor.
     * @param ip ImageProcessor serving as a template for conversion.
     * @return Mat object with properties corresponding to the ImageProcessor.
     */
    public static Mat createCompatibleMat(ImageProcessor ip){
		final int width = ip.getWidth();
		final int height = ip.getHeight();
		return Mat.zeros(height, width, toCvType(ip));
    }
    
    /**
     * Creates an ImageProcessor which has the same size and type as the Mat.
     * @param cvmat Mat serving as a template for conversion.
     * @return ImageProcessor object with properties corresponding to the Mat.
     */
    public static ImageProcessor createCompatibleProcessor(Mat cvmat){
    	final int width = cvmat.width();
		final int height = cvmat.height();	
    	
    	ImageProcessor ip = null;
    	switch(cvmat.type()){
		case CvType.CV_8U: 
			ip = new ByteProcessor(width, height);
			break;
		case CvType.CV_16U: 
			ip = new ShortProcessor(width, height);
			break;
		case CvType.CV_32F: 
			ip = new FloatProcessor(width, height);
			break;
		case CV_8UC3:
			ip = new ColorProcessor(width, height);
			break;				
		default:
			throw new RuntimeException("Unsupported image type " + CvType.typeToString(cvmat.type()));
    	}
    	return ip;
    }
    
    /**
     * Convert Rect to Roi.
     * @param cvrect Source OpenCV Rect object.
     * @param roi Target ImageJ Roi object.
     */
    public static void cv2ij(Rect cvrect, Roi roi){	
    	roi = new Roi(cvrect.x, cvrect.y, cvrect.width, cvrect.height);
    }
    
    /**
     * Convert Roi to Rect.
     * @param roi Source ImageJ Roi object.
     * @param cvrect Target OpenCV Rect object.
     */
    public static void ij2cv(Roi roi, Rect cvrect){	
    	cvrect = new Rect((int)roi.getXBase(), (int)roi.getYBase(), (int)roi.getFloatWidth(), (int)roi.getFloatHeight());
    }
    
    /**
     * Convert Roi to Rect.
     * @param roi Source ImageJ Roi object.
     * @param cvrect Target OpenCV Rect object.
     */
    public static void ij2cv(Roi roi, Size size){	
    	size = new Size(roi.getFloatWidth(), roi.getFloatHeight());
    }
    
    /**
     * Convert Roi to Rect.
     * @param roi Source ImageJ Roi object.
     * @param cvrect Target OpenCV Rect object.
     */
    public static void ij2cv(Roi roi, RotatedRect cvrect){	
    	double angle = roi.getAngle();
    	Point center = new Point(roi.getXBase(), roi.getYBase());
    	Size size = new Size(roi.getFloatWidth(), roi.getFloatHeight());
    	cvrect = new RotatedRect(center, size, angle);
    }
        
    /**
     * Convert ImageProcessor to Mat.
     * Resize and reformat Mat if necessary.
     * @param ip Input ImageProcessor.
     * @param cvmat Mat to which which ip data is loaded.
     * @throws RuntimeException In case that ImagePlus is of unknown or incompatible type.
     */
    public static void ij2cv(ImageProcessor ip, Mat cvmat){		
    	switch(ip.getBitDepth()){
			case 8:
				cvmat.put(0, 0, (byte[])ip.getPixels());
				break;
			case 16:
				cvmat.put(0, 0, (short[])ip.getPixels());
				break;
			case 24: 
				toColorMat((ColorProcessor)ip, cvmat);
				break;
			case 32: 
				cvmat.put(0, 0, (float[])ip.getPixels());
				break;
			default: 
				throw new RuntimeException("Unsupported image type: " + ip.getClass().getSimpleName());
		}
    	// TODO roi extraction fails! we dont get a submatrix!
    	// extract ROI
    	Rectangle ijRoi = ip.getRoi();
		Rect roi = new Rect(ijRoi.x, ijRoi.y, ijRoi.width, ijRoi.height);
		cvmat = new Mat(cvmat, roi);
    }
    
    /**
     * Convert Mat to ImageProcessor.
     * Resize ImpageProcessor if necessary.
     * @param cvmat Input Mat.
     * @param ip ImageProcessor in which to load cvmat.
     * @param offsetX x origin of offset.
     * @param offsetY y origin of offset.
     * @throws RuntimeException In case that Mat is of unknown or incompatible type.
     */
    public static void cv2ij(Mat cvmat, ImageProcessor ip, int offsetX, int offsetY){		
    	final int type = cvmat.type();
    	final int channels = cvmat.channels();
    	switch(channels){
			case 1:
				toGrayProcessor(cvmat, ip, offsetX, offsetY);
				break;
			case 3:
				toColorProcessor(cvmat, (ColorProcessor)ip, offsetX, offsetY);
				break;	
			default:
				throw new RuntimeException("Unsupported image type " + CvType.typeToString(type));
		}
	}
    
    /**
     * Convert Mat to ImageProcessor.
     * Resize ImpageProcessor if necessary.
     * @param cvmat Input Mat.
     * @param ip ImageProcessor in which to load cvmat.
     * @throws RuntimeException In case that Mat is of unknown or incompatible type.
     */
    public static void cv2ij(Mat cvmat, ImageProcessor ip){
    	cv2ij(cvmat, ip, 0, 0);
    }
        
    /**
     * Helper for conversion of ColorProcessor to Mat.
     * Data layouts are different which is why we need to manually iterate over the underlying arrays.
     * @param ip ColorProcessor to convert.
     * @param cvmat converted Mat
     */
    protected static void toColorMat(ColorProcessor ip, Mat cvmat){
    	final int width = ip.getWidth();
    	final int height = ip.getHeight();
    	for(int y=0; y<height; ++y){
    		for(int x=0; x<width; ++x){
				int pix = ip.get(y*width+x);
				int r = (pix & 0xff0000) >> 16;
				int g = (pix & 0xff00) >> 8;
				int b = (pix & 0xff);
				byte[] element = {(byte)b,(byte)g,(byte)r};
				cvmat.put(y, x, element);
    		}
    	}
    }
    
	/**
	 * Conversion method for generic gray-value ImageProcessors.
	 * Covers special case if cvmat is actually a submatrix of ip.
     * @param cvmat Mat to convert.
     * @param ip converted ImageProcessor
     * @param offsetX offset for copying data
     * @param offsetY offset for copying data
     */
    protected static void toGrayProcessor(Mat cvmat, ImageProcessor ip, int offsetX, int offsetY){
    	final int width = cvmat.width();
    	final int height = cvmat.height();
    	for(int y=0; y<height; ++y){
    		for(int x=0; x<width; ++x){
    			double[] pix = cvmat.get(y, x);
    			int[] conv = {(int)pix[0], (int)pix[0], (int)pix[0]};
				ip.putPixel(x+offsetX, y+offsetY, conv);
    		}
    	}
    }
    
    /**
	 * Conversion method for generic gray-value ImageProcessors.
	 * Covers special case if cvmat is actually a submatrix of ip.
     * Offset values define are to be used if cvmat is actually a submatrix.
     * @param cvmat Mat to convert.
     * @param ip converted ImageProcessor
     */
    protected static void toGrayProcessor(Mat cvmat, ImageProcessor ip){
    	toGrayProcessor(cvmat, ip, 0, 0);
    }
    
    /**
     * Helper for conversion of Mat to ColorProcessor.
     * Data layouts are different which is why we need to manually iterate over the underlying arrays.
     * Offset values define are to be used if cvmat is actually a submatrix.
     * @param cvmat Mat to convert.
     * @param ip converted ColorProcessor
     * @param offsetX offset for copying data
     * @param offsetY offset for copying data
     */
    protected static void toColorProcessor(Mat cvmat, ColorProcessor ip, int offsetX, int offsetY){
    	final int width = cvmat.width();
    	final int height = cvmat.height();
    	for(int y=0; y<height; ++y){
    		for(int x=0; x<width; ++x){
    			double[] data = cvmat.get(y, x);    				
				int r = (int)data[2];
				int g = (int)data[1];
				int b = (int)data[0];
				ip.putPixel(x+offsetX, y+offsetY, ((r<<16)|(g<<8)|b));
    		}
    	}
    }
    
    /**
     * Overloaded method, if cvmat is not a submatrix.
     * @param cvmat Mat to convert.
     * @param ip converted ColorProcessor.
     */
    protected static void toColorProcessor(Mat cvmat, ColorProcessor ip){
    	toColorProcessor(cvmat, ip, 0, 0);
    }
    
    //TODO complete and integrate this! the same needs to be done for MatOfPoints and others!!
    /**
     * Convert raw array to ImageJ result table.
     * @param array
     * @param table
     */
    public static void toResultTable(float[][] array, ResultsTable table){
    	table = new ResultsTable();
    	for(int i=0; i<array.length; ++i){
    		table.incrementCounter();
    		for(int j=0; j<array[i].length; ++j){
    			table.addValue("slice", array[i][j]);
    		}
    	}
    	Analyzer.setResultsTable(table);
    	table.show("Results");
    }
}
