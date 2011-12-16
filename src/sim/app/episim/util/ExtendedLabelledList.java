package sim.app.episim.util;

import java.awt.Insets;

import sim.util.gui.LabelledListHack;


public class ExtendedLabelledList extends LabelledListHack {
	
	 public ExtendedLabelledList(){
		 super();
	 }
	 public ExtendedLabelledList(String label){
		 super(label);
	 }
	 
	 public void setInsets(Insets insets){
		super.setInsets(insets);
	 }
	 
}
