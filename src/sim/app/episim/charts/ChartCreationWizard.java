package sim.app.episim.charts;

import java.awt.Frame;
import java.lang.reflect.Method;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;


public class ChartCreationWizard extends JDialog {
	
	private JList parameterList;
	
	public ChartCreationWizard(Frame owner, String title, boolean modal){
		super(owner, title, modal);
		parameterList = new JList();
		
		
		
	}
	
	
	public void showParameters(ChartMonitoredClass monitoredClass){
	
		DefaultListModel listModel = new DefaultListModel();
		for(Method actMethod :monitoredClass.getParameters())listModel.addElement(actMethod.getName().substring(3));
		parameterList.setModel(listModel);
		
	}
	

}
