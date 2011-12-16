package sim.util.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;


public class LabelledListHack extends LabelledList {
	public LabelledListHack(){
		super();
	}
	public LabelledListHack(String label){
		super(label);
	}
	public void setInsets(Insets insets){
		gbc.insets = insets;		
	}
	
	 public void add(Component farLeft, Component left, Component center, Component right, Component farRight)
    {
    
    gbc.gridy = y;
    
    if (farLeft!=null)
        {
        gbc.gridx = 0; 
        gbc.weightx = 0; 
        gbc.anchor=GridBagConstraints.WEST; 
        gbc.fill=GridBagConstraints.NONE; 
        gbc.gridwidth = 1;
        gridbag.setConstraints(farLeft,gbc);
        consolePanel.add(farLeft);
        }
    
    if (left!=null)
        {
        gbc.gridx = 1; 
        gbc.weightx = 0; 
        gbc.anchor=GridBagConstraints.EAST; 
        gbc.fill=GridBagConstraints.NONE; 
        gbc.gridwidth = 1; 
        gridbag.setConstraints(left,gbc);
        consolePanel.add(left);
        }

    if (center!=null)
        {
        gbc.gridx = 2; 
        gbc.weightx = 0; 
        gbc.anchor=GridBagConstraints.CENTER; 
        gbc.fill=GridBagConstraints.NONE; 
        gbc.gridwidth = 1; 
        gridbag.setConstraints(center,gbc);
        consolePanel.add(center);
        }

    if (right!=null)
        {
        gbc.gridx = 3; 
        gbc.weightx = 1; 
        gbc.anchor=GridBagConstraints.WEST; 
        gbc.fill=GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1; 
        gridbag.setConstraints(right,gbc);
        consolePanel.add(right);
        }
    
    if (farRight!=null)
        {
        gbc.gridx = 4; 
        gbc.weightx = 0; 
        gbc.anchor=GridBagConstraints.EAST; 
        gbc.fill=GridBagConstraints.NONE; 
        gbc.gridwidth = GridBagConstraints.REMAINDER; 
        gridbag.setConstraints(farRight,gbc);
        consolePanel.add(farRight);
        }
    
    // increment the count
    y++;
    }

}
