package cvforge;

import javax.swing.JTextField;
import javax.swing.text.*;


/**
 * TextField accepting floating point numbers only.
 */
public class JNumberField extends JTextField {
	
	/**
	 * Construct JNumberField with start value 0.0
	 */
	public JNumberField(){
		this(0.0);
	}
	
	/**
	 * Construct JNumberField with given double start value.
	 * @param value
	 */
	public JNumberField(double value){
		super(""+value);
		this.addFocusListener(new java.awt.event.FocusAdapter(){
		    public void focusGained(java.awt.event.FocusEvent evt){selectAll();}
		    public void focusLost(java.awt.event.FocusEvent evt){}
		});
	}
	
	/**
	 * Internal document creator.
	 */
	protected Document createDefaultModel(){
		return new NumberDocument();
	 }
	
	/**
	 * Redundant.
	 * Check if currently entered value is valid double.
	 */
	public boolean isValid(){
		try{
			Double.parseDouble(getText());
			return true;
		}catch(Exception e){
			return false;
		}
	}
	
	/**
	 * Get parsed double value.
	 * @return Parsed double value.
	 */
	public double getValue(){
		try{
			return Double.parseDouble(getText());
	  	}catch(NumberFormatException e){
	  		return 0.0;
	  	}
	}
	
	/**
	 * Used internally to limit input to numbers.
	 */
	class NumberDocument extends PlainDocument{
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
		    if(str == null)
		    	return;
		    String oldString = getText(0, getLength());
		    String newString = oldString.substring(0, offs) + str + oldString.substring(offs);
		    try{
		    	Double.parseDouble(newString);
		    	super.insertString(offs, str, a);
		    }catch(NumberFormatException e){}
		}
	}
}
