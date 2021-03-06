package cvforge;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.*;

import ij.plugin.frame.*;
import ij.*;


/**
 * Mainframe for library loading/ selection/ installation.
 * Serves as hub for executing methods.
 */
public class CVForgeFrame extends PlugInFrame implements ActionListener {
	
    public static CVForge FORGE;

    protected CVForgeCallFrame callFrame;
    protected CVForgeCacheFrame cacheFrame;
    protected CVForgeConstructorFrame conFrame;
    
    protected JTree libTree;
    protected HashMap<String, Method> methodCache;
    protected JScrollPane libTreePane;
    protected MenuBar menuBar;
    
    // input field for tree filtering
    protected JTextField textFieldFilter;
    protected JButton buttonFilter;
    
    public CVForgeFrame(){
        // window setup
        super(CVForge.VERSION);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) { pluginShutdown(); }
        });
        
        try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
        catch(Exception e){}
        setLayout(new BorderLayout());
		
        textFieldFilter = new JTextField("");
        textFieldFilter.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				filterTree(textFieldFilter.getText());
			}
        });
        
        buttonFilter = new JButton("filter");
        buttonFilter.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				filterTree(textFieldFilter.getText());
			}
        });
        
        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.add(textFieldFilter, BorderLayout.CENTER);
        filterPanel.add(buttonFilter, BorderLayout.EAST);
        add(filterPanel, BorderLayout.SOUTH);
        
        IJ.showStatus("initializing CVForge...");
        FORGE = new CVForge();
        callFrame = new CVForgeCallFrame();
        callFrame.addExternalButtonListener(this);
        conFrame = new CVForgeConstructorFrame();
        cacheFrame = new CVForgeCacheFrame();
        
        CVForgeCache.addListener(cacheFrame);
        CVForgeCache.addListener(callFrame);
        CVForgeCache.addListener(conFrame);
        String lib = FORGE.activeLib();
        
        if(lib != null){
        	switchJar(lib);
    	}else{
    		this.setSize(200, 300);
    	}
        
        setupMenubar();
        setResizable(true);  
    }

    /**
     * If there active lib found, load the tree and set up user interface.
     */
    public void loadLibraryTree(){
        libTree = FORGE.getLibraryTree();
        
        // keep GUI clean 
        if(libTreePane != null)
        	this.remove(libTreePane);
        	
    	libTreePane = new JScrollPane(libTree);
        setupTreeListener();
        this.add(libTreePane, BorderLayout.CENTER);
        this.textFieldFilter.setEditable(!methodCache.isEmpty());
        
        this.pack();
        Dimension winSize = FORGE.restoreWindowSize(); 
    	if((winSize.width != 0) && (winSize.height != 0)){
        	this.setSize(winSize);
    	}
        this.setLocation(FORGE.restoreWindowPosition());
    }

    /**
     * Initialize tree properties and add listener.
     */
    public void setupTreeListener(){
        libTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        libTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                        libTree.getLastSelectedPathComponent();
                if (node == null)
                    return;

                if (!node.children().hasMoreElements()) {
                    Method m = methodCache.get(node.getUserObject());
                    if (m != null){
                        callFrame.createParameterList(m);
                        callFrame.setVisible(true);
                    }
                }
            }
        });
    }
    
    /**
     * Saves plugin settings and shuts down smoothly.
     */
    public void pluginShutdown(){
        IJ.showStatus("shutting down CVForge");
        FORGE.storeWindowDimensions(this.getLocation(), this.getSize());
        FORGE.saveSettings();
        callFrame.dispose();
        cacheFrame.dispose();
        conFrame.dispose();
        this.dispose();
    }
    
    /**
     * Open up file dialog and install jar selected in JFileChooser.
     */
    public void installJar(){    	        	
    	this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    	JFileChooser chooser = new JFileChooser();
    	chooser.setFileFilter(new JarFilter());
    	int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION){
        	String path = chooser.getSelectedFile().getPath();
        	String name = chooser.getSelectedFile().getName();
        	FORGE.installOpenCV(path);
        	switchJar(name);
        	setupMenubar();
        }
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
    
    /**
     * Switch to jar defined by path.
     * @param path Path to opencv jar file.
     */
    public void switchJar(String path){
    	IJ.showStatus("loading opencv library: " + path);
    	
    	try{
    		FORGE.loadOpenCV(path);
    		FORGE.loadShards();
    		
	    	methodCache = FORGE.getMethodCache();
	    	loadLibraryTree();
	    	conFrame.setClassCache(FORGE.getClassCache());
	    	IJ.showStatus("library loaded: " + path);
    	}catch(Exception e) {
    		this.setSize(200, 300);
			IJ.beep();
			IJ.showStatus(e.toString());
			if(FORGE.isVerbose()){
				CharArrayWriter caw = new CharArrayWriter();
				PrintWriter pw = new PrintWriter(caw);
				e.printStackTrace(pw);
				IJ.log(caw.toString());
			}
		}
    	
    	conFrame.setVisible(false);
    	callFrame.setVisible(false);
    	cacheFrame.setVisible(false);
    }
    
    /**
     * Setup MenuBar and add installed jars to entries.
     */
    protected void setupMenubar(){
        menuBar = new MenuBar();
        Menu menuPlugin = new Menu("Plugin");
        menuBar.add(menuPlugin);
        
        if(CVForge.OS.contains("Windows")){
	        MenuItem itemInstallation = new MenuItem("Install");
	        itemInstallation.setShortcut(new MenuShortcut('I'));
	        itemInstallation.addActionListener(new ActionListener(){
	        	public void actionPerformed(ActionEvent e) {installJar();}
	        });  
	        menuPlugin.add(itemInstallation);
        }
	        
        ArrayList<String> installed = FORGE.availableLibs();
        
        Menu subMenuLoad = new Menu("Load");
        subMenuLoad.setEnabled(installed.size()!=0);
             
        for(String i: installed){
        	final String label = i.substring(i.lastIndexOf(CVForge.SEP)+1, i.length());
        	MenuItem subItemLoad = new MenuItem(label);
        	subItemLoad.addActionListener(new ActionListener(){
            	public void actionPerformed(ActionEvent e) {
            		FORGE.storeWindowDimensions(getLocation(), getSize());
            		switchJar(label);
        		}
            });
        	subMenuLoad.add(subItemLoad);
        }
        menuPlugin.add(subMenuLoad);
        
        	
        
        menuPlugin.addSeparator();
        MenuItem itemExit = new MenuItem("Exit");
        itemExit.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e) {pluginShutdown();}
        });
        menuPlugin.add(itemExit);   
     
        
        Menu menuTools = new Menu("Tools");
        
        MenuItem itemCache = new MenuItem("Cache");
        itemCache.setShortcut(new MenuShortcut('E'));
        itemCache.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e) {cacheFrame.setVisible(!cacheFrame.isVisible());}
        });
        menuTools.add(itemCache);
        
        MenuItem itemConstructor = new MenuItem("Constructor");
        itemConstructor.setShortcut(new MenuShortcut('R'));
        itemConstructor.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e) {conFrame.setVisible(!conFrame.isVisible());}
        });
    	itemConstructor.setEnabled(FORGE.activeLib()!=null);
    	menuTools.add(itemConstructor);		
        
    	menuTools.addSeparator();
    	
        MenuItem itemVerbose = new MenuItem("Toggle verbose errors");
        itemVerbose.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e) {
        		FORGE.setVerbose(!FORGE.isVerbose());
        	}
        });
        menuTools.add(itemVerbose);
        menuBar.add(menuTools);
        
        MenuItem itemFind = new MenuItem("Find Method");
        itemFind.setShortcut(new MenuShortcut('F'));
        itemFind.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e) {
        		textFieldFilter.requestFocusInWindow();
        		textFieldFilter.selectAll();
        	}
        });
        menuTools.add(itemFind);
        
        
        Menu menuHelp = new Menu("Help");
        MenuItem itemAbout = new MenuItem("About");
        itemAbout.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e) {showAbout();}
        });
        menuHelp.add(itemAbout);
        menuBar.add(menuHelp);
 
        setMenuBar(menuBar);
    }
    
    /**
     * Filter library tree leaves with filter.
     * Nodes containing filter term are kept.
     * Case insensitive!
     * @param filter Filter to use.
     */
    public void filterTree(String filter){    	
    	try{
	    	FilteredTreeModel model = (FilteredTreeModel)libTree.getModel();
	    	
	    	TreePath rootPath = new TreePath(model.getRoot());
	    	Enumeration<TreePath> expanded = libTree.getExpandedDescendants(rootPath);    	
	    	
	    	model.setFilter(filter);
	    	model.nodeChanged((TreeNode)model.getRoot());
	    	model.reload();
	    	
	    	while(expanded.hasMoreElements()){
	    		TreePath exp = expanded.nextElement();
				libTree.expandPath(exp);
	    	}
    	}catch(Exception e){}
    }
    
    /**
     * Simple about text.
     */
    public static void showAbout(){
    	IJ.showMessage(
			CVForge.VERSION + " is powered by OpenCV." + System.lineSeparator() +
			"For detailed information, please refer to" + System.lineSeparator() +
			"http://opencv.org/documentation.html" + System.lineSeparator() +
			 System.lineSeparator() +
			"For support and suggestions, contact send a mail to janmartens@live.de."  + System.lineSeparator() +
			"For bug reports, state each single step and send in the used picture."
		);
    }
    
    /**
     * Lock all images for filtering.
     */
    public static void lockAllImages(){
    	for(String title: WindowManager.getImageTitles()){
			ImagePlus imp = WindowManager.getImage(title);
			imp.lock();	
		}
    }
    
    /**
     * Unlock and update all images.
     */
    public static void unlockAllImages(){
    	for(String title: WindowManager.getImageTitles()){
			ImagePlus imp = WindowManager.getImage(title);
			imp.updateAndDraw();
			imp.unlock();
		}
    }
    
    /**
     * Launch method call.
     */
    public void actionPerformed(ActionEvent e){
		if ((WindowManager.getImageCount() == 0)) {
			IJ.beep();
			IJ.showStatus("no image available");
		}else{
			new Runner();			
		}
	}
    
    
    // non-blocking run of method through thread
    class Runner extends Thread {
		Runner() {
			super();
			setPriority(Math.max(getPriority()-2, MIN_PRIORITY));
			start();
		}
	
		public void run() {
			String cvmethod = callFrame.getActiveMethod().getName();
			IJ.showStatus("running " + cvmethod + "...");
			long startTime = System.currentTimeMillis();
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			lockAllImages();
			
			try {
				if(Recorder.record){
					String methodArgs = callFrame.getMethodArgs();
					Recorder.record("run", "CVForge", methodArgs);		
				}					
				Executer.executeMethod(callFrame.getActiveMethod(), callFrame.extractParameters(), callFrame.getReturnName());
				IJ.showStatus((System.currentTimeMillis()-startTime)+" milliseconds");
			} catch(OutOfMemoryError e) {
				IJ.outOfMemory(CVForge.VERSION);
			} catch(NullPointerException e){
				IJ.showMessage("CVForge Error", "One or more input parameters are undefined");
			} catch(Exception e) {
				IJ.beep();
				IJ.showStatus(e.toString());
				e.printStackTrace();
				if(FORGE.isVerbose()){
					CharArrayWriter caw = new CharArrayWriter();
					PrintWriter pw = new PrintWriter(caw);
					e.printStackTrace(pw);
					IJ.log(caw.toString());
				}
			}
			unlockAllImages();
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
    }
}
