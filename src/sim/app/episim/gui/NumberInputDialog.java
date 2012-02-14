package sim.app.episim.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import sim.util.gui.NumberTextField;


public class NumberInputDialog {
	
	private static JDialog dialog;
	
	private static Double result ;
	private static boolean okPressed = false;
	private NumberInputDialog(){}
	
	public static Double showDialog(Frame parent, String title, String label, Double defaultValue){
		
		dialog = new JDialog(parent, title, true);
		okPressed = false;
		
		Box numberFieldPanel = Box.createVerticalBox();
		 numberFieldPanel.add(Box.createRigidArea(new Dimension(10, 20)));
		result = defaultValue != null ? defaultValue: new Double(0);
		NumberTextField numberField = new NumberTextField(label, result.doubleValue(), 1, 0.02) 
       {       
			 public double newValue(double newValue)
          {				        
				 result = new Double(newValue);
				 return newValue;  
          }
       };
       numberField.setSize(10, 30);
       numberFieldPanel.add(numberField);
       numberFieldPanel.add(Box.createRigidArea(new Dimension(10, 20)));
       Box buttonPanel = Box.createHorizontalBox();
       buttonPanel.add(Box.createHorizontalGlue());
       JButton okButton = new JButton("   OK   ");
       okButton.addActionListener(new ActionListener() {		
		
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(true);
				dialog.dispose();
				okPressed=true;
			}
		});
       buttonPanel.add(okButton);
       JButton cancelButton = new JButton(" Cancel ");
       cancelButton.addActionListener(new ActionListener() {		
    		
 			public void actionPerformed(ActionEvent e) {
 				dialog.setVisible(true);
 				dialog.dispose();
 			}
 		});
      buttonPanel.add(Box.createHorizontalStrut(25));
      buttonPanel.add(cancelButton);
      buttonPanel.add(Box.createHorizontalGlue());
      JPanel contentPane = (JPanel) dialog.getContentPane();
      contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      dialog.getContentPane().add(numberFieldPanel, BorderLayout.CENTER);
      dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		dialog.setSize(400,130);
		dialog.setResizable(false);
		dialog.setLocation(parent.getLocation().x + (parent.getWidth()/2) - (dialog.getWidth()/2), 
				parent.getLocation().y + (parent.getHeight()/2) - (dialog.getHeight()/2));
		dialog.setVisible(true);
		return okPressed ? result : null;
	}

}
