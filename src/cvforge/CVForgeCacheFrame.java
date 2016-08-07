package cvforge;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;

/**
 * Provide cache for objects.
 *
 */
public class CVForgeCacheFrame extends JDialog implements CacheListener {
	protected JScrollPane scrollPane;
	protected JPanel mainPanel; 	
	protected JButton clearButton;
	
	public CVForgeCacheFrame() {
        // window setup
		setTitle("CVForge Cache");
        addWindowListener(new WindowAdapter() {
            @Override
			public void windowClosing(WindowEvent evt) {
                dispose();
            }
        });
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        int frameWidth = 300;
        int frameHeight = 200;
        setSize(frameWidth, frameHeight);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (d.width - getSize().width) / 2;
        int y = (d.height - getSize().height) / 2;
        setLayout(new BorderLayout());
        setLocation(x, y);
        
        clearButton = new JButton("clear cache");
        clearButton.addActionListener(new ActionListener(){
        	@Override
			public void actionPerformed(ActionEvent e) {CVForgeCache.clear();}
        });
        add(clearButton, BorderLayout.SOUTH);
        
        mainPanel = new JPanel();
        scrollPane = new JScrollPane();
        createCacheList();
        scrollPane.add(mainPanel);
        scrollPane.setViewportView(mainPanel);
        add(scrollPane, BorderLayout.CENTER); 
        
        setAlwaysOnTop(true);
        setResizable(true);
        setVisible(false);
    }
	
	
	// TODO enable edit buttons
	// TODO empty cache label bug
	public void createCacheList(){
		mainPanel.removeAll();
		if(CVForgeCache.isEmpty()){
		    mainPanel.setLayout(new BorderLayout());
		    mainPanel.add(new JLabel("  no cached elements  "), BorderLayout.CENTER);
		}else{
		    GridLayout layout = new GridLayout(CVForgeCache.size(), 4);
		    mainPanel.setLayout(layout);
		    for(Map.Entry<String, Object> entry: CVForgeCache.getEntries()){ 
		    	final String key = entry.getKey();
		    	String type = entry.getValue().getClass().getSimpleName();
		    	String content = InputHelpers.limitLength(entry.getValue().toString());
		    	JLabel elementType = new JLabel(type);
		    	JLabel elementName = new JLabel(key);
		    	JLabel elementContent = new JLabel(content);
		        JButton elementEdit = new JButton("edit");
		        JButton elementDelete = new JButton("remove");
		        
		        //elementEdit.addActionListener(arg0);
		        elementDelete.addActionListener(new ActionListener(){
		        	@Override
					public void actionPerformed(ActionEvent e) {
		        		CVForgeCache.remove(key);
	        		}
		        });
		        
		        mainPanel.add(elementType);
		        mainPanel.add(elementName);
		        mainPanel.add(elementContent);
		        //mainPanel.add(elementEdit);
		        mainPanel.add(elementDelete);
		    }
		}
	    pack();
    }
	
	/**
	 * Update GUI elements.
	 * @see CacheListener
	 */
	@Override
	public void cacheChanged(){
		createCacheList();	
		setVisible(true);
	}
}
