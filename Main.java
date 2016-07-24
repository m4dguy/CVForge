
import java.util.Enumeration;
import java.util.Properties;

import cvforge.CVForgeFrame;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

/**
 *
 */
public class Main{
	public static void main(String[] args){
		Class<?> clazz = CVForgeFrame.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring(5, url.length() - clazz.getName().length() - 6);
		System.setProperty("plugins.dir", pluginsDir);

		System.out.println(pluginsDir);
		
		// start ImageJ
		new ImageJ();		
		
		// open the Clown sample
		//ImagePlus image = IJ.openImage("http://imagej.net/images/clown.jpg");
		
		String filename = "hund_color.jpg";
		//String filename = "hund.jpg";
		String path = System.getProperty("user.dir") + "\\" + filename;
		ImagePlus image = IJ.openImage(path);
		image.show();
		
		ImageProcessor ip = image.getProcessor(); 

		// run the plugin
		IJ.runPlugIn(clazz.getName(), "");
	}
}
