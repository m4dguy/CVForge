package cvforge;

import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.gui.Roi;


public class InputHelpers {
    // declaration for parameter recognition
	protected static final String CVMAT = "org.opencv.core.Mat";
	protected static final int BOXSIZE = 5;
	
	/**
	 * Convert String to given primitive class type (String included).
	 * If conversion to primitive is not possible, return null.
	 * @param src String to be converted
	 * @param classType Class to which src is to be converted.
	 * @return Converted Primitive/ String.
	 */
	public static Object stringToPrimitive(String src, Class classType){
		if(classType == Boolean.TYPE){
			return Boolean.parseBoolean(src);
		}else if(classType == Byte.TYPE){
			return (byte)Float.parseFloat(src);
		}else if(classType == Short.TYPE){
			return (short)Float.parseFloat(src);
		}else if(classType == Integer.TYPE){
			return (int)Double.parseDouble(src);
		}else if(classType == Long.TYPE){
			return (long)Double.parseDouble(src);
		}else if(classType == Float.TYPE){
			return Float.parseFloat(src);
		} else if(classType == Double.TYPE){
			return Double.parseDouble(src);
		} else if(classType == String.class){
			return src;
		}
		return null;
	}
	
	/**
	 * Limit the length of the input String down to 15 characters.
	 * @param src String to be shortened.
	 * @return Shortened String.
	 */
	public static String limitLength(String src, int limit){
		if(src.length() <= limit)
			return src;
		else
			return src.substring(0, limit) + "...";		
	}
	
	// overloaded with limit 15.
	public static String limitLength(String src){
		return limitLength(src, 15);		
	}
	
	// TODO provide cache access
    public static JComboBox<String> createMatBox(){
    	String[] imageTitles = WindowManager.getImageTitles();
    	JComboBox<String> matBox = new JComboBox<String>(imageTitles);
    	matBox.setEditable(false);
    	matBox.setMaximumRowCount(BOXSIZE);
    	
    	ImageWindow win = WindowManager.getCurrentWindow();
    	if(win != null){
    		matBox.setSelectedItem(win.getName());
    	}
    	return matBox;
    }
    
    public static JComboBox<String> createRoiBox(){
    	String[] imageTitles = WindowManager.getImageTitles();
    	
    	ArrayList<String> elements = new ArrayList<String>(imageTitles.length); 
    	for(String s: imageTitles){
    		ImagePlus ip = WindowManager.getImage(s);
    		Roi roi = ip.getRoi();
    		if(roi != null){
    			elements.add(s);
    		}
    	}

    	JComboBox<String> roiBox = new JComboBox<String>(imageTitles);
    	roiBox.setEditable(false);
    	roiBox.setMaximumRowCount(BOXSIZE);
    	
    	ImageWindow win = WindowManager.getCurrentWindow();
    	if(win != null){
    		if(elements.contains(win.getTitle())){
    				roiBox.setSelectedItem(win.getName());
    		}
    	}
    	return roiBox;
    }
    
    
    
    /**
     * Create a JComboBox of boolean values.
     * @return JComboBox with boolean values.
     */
    public static JComboBox<String> createBoolBox(){
    	JComboBox<String> boolBox = new JComboBox<String>(new String[]{"true", "false"});
    	boolBox.setEditable(false);
    	return boolBox;
    }
    
    /**
     * Create a JComboBox referring to Objects in the cache.
     * Only classes of given Class are included.
     * @param classType Only classes of this Class are to be included in the resulting JComboBox.
     * @return Constructed JComboBox with references to cached Objects.
     */
	public static JComboBox<String> createCacheBox(Class classType){
		return CVForgeCache.createComboBox(classType);
	}
	
	/**
	 * Creates a JComboBox containing the simplified names of given classes.
	 * @param classes Classes to be used for JComboBox.
	 * @return Created JComboBox.
	 */
	public static JComboBox<String> createClassBox(Class[] classes){
		String[] classNames = new String[classes.length];
		for(int i=0; i<classes.length; ++i)
			classNames[i] = classes[i].getSimpleName();
		
		JComboBox<String> classBox = new JComboBox<String>(classNames);
		classBox.setEditable(false);
		classBox.setMaximumRowCount(BOXSIZE);
		return classBox;
	}
	
	/**
	 * Creates an a JComboBox or TextField, depending on the given Class.
	 * For everything other than Primitives, a JComboBox referring to the cache is created.
	 * @param classType Class for which the input element is to be created. 
	 * @return Created input element.
	 */
	public static JComponent createInputElement(Class classType){
		JComponent input;
		if(classType.isPrimitive()){
    		if(classType == boolean.class){
    			input = InputHelpers.createBoolBox();
    		}else{
    			input = new JNumberField();        			
    		}
        }else{
        	if(classType.getName() == CVMAT){
        		input = InputHelpers.createMatBox();
        	}else if(classType == String.class){
        		input = new JTextField("");
        	}else{
        		input = createCacheBox(classType);
        	}
    	}
		return input;
	}
	
	/**
	 * Extract the text from a JComponent.
	 * Only works for JTextField and JComboBox this far.
	 * @param comp JComponent from which the text is to be fetched.
	 * @return String extracted from the component.
	 */
	public static String getText(JComponent comp){		
		if(comp instanceof JTextField)
    		return ((JTextField)comp).getText();
		else if(comp instanceof JComboBox)
    		return ((JComboBox<String>)comp).getSelectedItem().toString();
    	
		return comp.toString();
	}
	
	/**
	 * Creates an Object of given Class from a component.
	 * Either converts text input into a String or a Primitive, or looks it up in the cache.
	 * @param comp JComponent to extract value from.
	 * @param classType Class to convert extracted value into.
	 * @return Created Object.
	 */
	public static Object createFromInput(JComponent comp, Class classType){
		String text = getText(comp);
		String className = classType.getName();
		Object converted = null;
		
    	// TODO check before getting named image if the named image does not exist, IJ creates it (no good!)
		if((classType.isPrimitive()) || (classType == String.class)){
    		converted = InputHelpers.stringToPrimitive(text, classType);
    	}else{
    		if(className == CVMAT){
        		converted = WindowManager.getImage(text).getProcessor(); 
    		}else{
    			converted = CVForgeCache.get(text);
    		}
    	}
		return converted;
	}
}
