package cvforge;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;


/**
 * TextField accepting numbers only.
 */
public class QuickEditField extends JTextField implements KeyListener{
	
	public QuickEditField() {
		super("");
		this.addFocusListener(new java.awt.event.FocusAdapter() {
		    public void focusGained(java.awt.event.FocusEvent evt){selectAll();}
		    public void focusLost(java.awt.event.FocusEvent evt){}
		});
	}
	
 	public void keyPressed(KeyEvent e){
 		final int key = e.getKeyCode();
 		switch(key){
 			case KeyEvent.VK_ESCAPE:
 				this.selectAll();
 				break;
 			default:
 				break;
 			
 		}
 		
 	}
 	
 	public void keyReleased(KeyEvent e){}
 	public void keyTyped(KeyEvent e){}
}
