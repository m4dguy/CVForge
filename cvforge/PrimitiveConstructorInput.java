package cvforge;

import java.awt.FlowLayout;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JTextField;

public class PrimitiveConstructorInput extends JPanel{
	protected Constructor constructor;
	protected ArrayList<JTextField> inputs;
	
	public PrimitiveConstructorInput(){
		super();
		this.setLayout(new FlowLayout());
	}
	
	public void createParameterList(Constructor cons){
		constructor = cons;
		this.removeAll();
		Class[] classTypes = constructor.getParameterTypes();
		inputs.clear();
		for(Class c: classTypes){
			JTextField tf = new JTextField("");
			inputs.add(tf);
			this.add(tf);
		}
	}
	
	public Object getObject(){
		int elements = inputs.size();
		Class[] argTypes = constructor.getParameterTypes();
		Object[] params = new Object[elements];
		for(int i=0; i<elements; ++i)
			params[i] = InputHelpers.stringToPrimitive(inputs.get(i).getText(), argTypes[i]);
		try{
			return constructor.newInstance(params);	
		}catch(Exception e){
			return null;
		}
	}
}
