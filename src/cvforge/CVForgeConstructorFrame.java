package cvforge;

import javax.swing.*;

import ij.IJ;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.Map.Entry;

/**
 * Provide cache for objects and notify listeners if new objects have been added.
 */
public class CVForgeConstructorFrame extends JDialog implements ActionListener, CacheListener{
	
	protected Class templateClass;
	protected Constructor[] constructors;
    
	protected ArrayList<JComponent[]> components = new ArrayList<JComponent[]>();    
    protected JTabbedPane tabs = new JTabbedPane();
    
    protected JComboBox<String> classBox;
    protected JPanel outputPanel = new JPanel();
    protected JLabel outputLabel = new JLabel(" name: ");
    protected JTextField outputField = new JTextField();
    protected JButton createButton = new JButton("create");
    
    // TODO ordering of set entries (SortedSet?)
    protected HashMap<String, Class> classCache;
    
	public CVForgeConstructorFrame() {
        // window setup
		setTitle("CVForge Constructor");
        addWindowListener(new WindowAdapter() {
            @Override
			public void windowClosing(WindowEvent evt) {
                dispose();
            }
        });
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        int frameWidth = 300;
        int frameHeight = 500;
        setSize(frameWidth, frameHeight);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (d.width - getSize().width) / 2;
        int y = (d.height - getSize().height) / 2;
        this.setLayout(new BorderLayout());
        setLocation(x, y);

        this.add(tabs, BorderLayout.CENTER);
        outputPanel.setLayout(new GridLayout(1,3));
        outputPanel.add(outputLabel);
        outputPanel.add(outputField);
        outputPanel.add(createButton);
        createButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {createObject();}
        });
        this.add(outputPanel, BorderLayout.SOUTH);        
        
        classBox = new JComboBox<String>(); 
		classBox.setEditable(false);
		classBox.setMaximumRowCount(7);
		classBox.addActionListener(this);
		
        this.add(classBox, BorderLayout.NORTH); 
        setAlwaysOnTop(true);
        setResizable(true);
        setVisible(false);
    }
	
	/**
	 * Set the cache of available classes.
	 * These classes will be displayed in the JComboBox at the Frame's top.
	 * The cache will also be used to lookup class properties.
	 * @param classes The new classes.
	 */
	public void setClassCache(HashMap<String, Class> classes){
        classCache = classes;
        classBox.removeAll();
        
		for(Entry<String, Class> entry: classCache.entrySet())
			classBox.addItem(entry.getValue().getSimpleName());
	}
    
	/**
	 * Create GUI elements for the constructors of this Class.
	 * @param templateClass Class for whose constructors the GUI elements are to be created.
	 */
    public void createConstructorLists(Class template){
    	templateClass = template; 
        constructors = templateClass.getConstructors();
    	components.clear();
        
    	tabs.removeAll();
	    for(Constructor c: constructors){
	    	Parameter[] params = c.getParameters();
		    int elements = params.length;
		    
		    JPanel tab = new JPanel();
	    	tab.setLayout(new GridLayout(elements, 3));
	    	JComponent[] subComponents = new JComponent[elements];
	    	
		    String signature = "(";
		    int i=0;
		    for(Parameter p: params){    	    	
		        JComponent element = InputHelpers.createInputElement(p.getType());

		    	JLabel typeName = new JLabel(p.getType().getSimpleName());
		        JLabel paramName = new JLabel(p.getName());		        
		        
		        signature += p.getType().getSimpleName() + ", ";
		        subComponents[i++] = element;
		        tab.add(typeName);
		        tab.add(paramName);
		        tab.add(element);
		    }
		    components.add(subComponents);
		    int end = signature.lastIndexOf(',');
		    end = (end==-1)? signature.length() : end;
		    signature  = signature.substring(0, end) + ")";
		    tabs.addTab(signature, tab);
	    }
	    this.pack();
    }
    
    /**
     * Get parameters from textfields.
     * @return Array of objects (Integer, Double, String, ...) with suitable paramters.
     */
    public Object[] extractParameters(){
    	Parameter[] params = getActiveConstructor().getParameters();
    	Object[] extracted = new Object[params.length];
    	int active = tabs.getSelectedIndex();
    	
    	JComponent[] activeComponents = components.get(active);
        for(int i=0; i<activeComponents.length; ++i){
            Class classType = params[i].getType();
            extracted[i] = InputHelpers.createFromInput(activeComponents[i], classType);
        }
        return extracted;
    }
    
    /**
     * Determine which Constructor is now in use.
     * @return Constructor which is currently active.
     */
    public Constructor getActiveConstructor(){
    	return constructors[tabs.getSelectedIndex()];
    }
    
    /**
     * Creates an Object based on the parameters extracted from the GUI.
     * Adds this Object immediately to the cache.
     */
    public void createObject(){
    	try{
    		Constructor activeConstructor = getActiveConstructor();
			Object[] params = extractParameters();			
			Object obj = activeConstructor.newInstance(params);
			CVForgeCache.add(outputField.getText(), obj);
    	}catch(Exception ex){IJ.showStatus(ex.toString());}
    }
    
    /**
     * Called, if JComboBox item changed.
     */
    @Override
	public void actionPerformed(ActionEvent ev){
    	String className = (String)classBox.getSelectedItem();
    	Class classType = classCache.get(className);
    	createConstructorLists(classType);
    }

	public void cacheChanged(){
		createConstructorLists(templateClass);
	}
    
}
