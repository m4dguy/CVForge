package cvforge;

import javax.swing.*;

import ij.WindowManager;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;


/**
 * Frame providing a visual interface for setting parameters of a method call.
 * This Frame does not call the method by itself, but rather only provides a user interface for calling it.
 */
public class CVForgeCallFrame extends JDialog implements CacheListener{

    // declaration for parameter extraction
	protected Method activeMethod;
	protected ArrayList<JComponent> components = new ArrayList<JComponent>();
    protected JTextField outputField = new JTextField();
    protected JLabel outputLabel = new JLabel(" name for cache: ");
    
    protected JPanel elementPanel = new JPanel();
    protected JButton callButton = new JButton("call"); 
    
    public CVForgeCallFrame() {
        // window setup
        addWindowListener(new WindowAdapter() {
        	@Override
			public void windowClosing(WindowEvent evt) {dispose();}
        });
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        int frameWidth = 500;
        int frameHeight = 500;
        setSize(frameWidth, frameHeight);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (d.width - getSize().width) / 2;
        int y = (d.height - getSize().height) / 2;
        this.setLayout(new BorderLayout());
        this.add(elementPanel, "Center");
        this.add(callButton, "South");

        setLocation(x, y);
        setAlwaysOnTop(true);
        setResizable(false);
        setVisible(false);
    }
     
    /**
     * Create GUI elements for parameters.
     * Also adds a field for naming, if the method has a return value which can be cached.
     * @param method Method for which a GUI is supposed to be created.
     */
    public void createParameterList(Method method){
	    activeMethod = method;
    	
    	this.setTitle(method.getName());
	    elementPanel.removeAll();
	    components.clear();
	    
	    Parameter[] params = method.getParameters();
	    boolean hasReturnValue = hasReturnValue();
	    int elements = hasReturnValue? (params.length+1) : (params.length);
	    elementPanel.setLayout(new GridLayout(elements, 2));
	    
	    for(Parameter p: params){    	    	
	        JLabel label = new JLabel(" " + p.getName() + " (" + p.getType().getSimpleName() + ") ");
	        
	        JComponent element = InputHelpers.createInputElement(p.getType());       
	        components.add(element);
	        elementPanel.add(label);
	        elementPanel.add(element);
	    }
	    
	    if(hasReturnValue){
	    	elementPanel.add(outputLabel);
	    	elementPanel.add(outputField);
	    }	    
	    this.pack();
    }
    
    /**
     * Creates an argument String for macro recording.
     * @return String with macro arguments.
     */
    public String getMethodArgs(){
    	String args = "method=" + this.getActiveMethod().getName();
    	for(int i=0; i<components.size(); ++i){
        	JComponent comp = components.get(i);
        	String arg = InputHelpers.getText(comp);
        	String currentName = WindowManager.getCurrentWindow().getTitle();
        	
        	if(arg.equals(currentName)){
        		arg = "[currentWindow]";
        	}
			args += " arg" + i + "=" + arg;
    	}
    	return args;
    }
    
    /**
     * Get parameters from JComponents.
     * @return Array of objects (Integer, Double, String, ...) which are suitable parameters.
     */
    public Object[] extractParameters(){
        Parameter[] params = activeMethod.getParameters();
    	Object[] extracted = new Object[params.length];
        for(int i=0; i<components.size(); ++i){
        	JComponent comp = components.get(i);
            Class classType = params[i].getType();
    		extracted[i] = InputHelpers.createFromInput(comp, classType);
        }
        return extracted;
    }

    /**
     * Get currently assigned method.
     * @return Method assigned to this CVForgeCallFrame.
     */
    public Method getActiveMethod(){
    	return activeMethod;
    }
    
    /**
     * Determine if method is void or has return type.
     * @return false, if the active method is void.
     */
    public boolean hasReturnValue(){
    	return !(activeMethod.getReturnType().equals(Void.TYPE));
    }
    
    /**
     * Gets the name which will be assigned to the returned object for caching.
     * Empty String, if the active method is of type void.
     * @return Name for caching the return value.
     */
    public String getReturnName(){
    	if(hasReturnValue())
    		return outputField.getText();
    	else
    		return "";
    }
    
    /**
     * Add external listener to detect if call button has been pressed.
     * @param listener ActionListener to assign to button.
     */
    public void addExternalButtonListener(ActionListener listener){
    	callButton.addActionListener(listener);
    }
    
	public void cacheChanged(){
		createParameterList(activeMethod);
	}
}
