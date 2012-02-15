package sim.app.episim.datamonitoring.charts;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import sim.util.gui.NumberTextField;


public class ChartImageResolutionDialog {
	
	private static JDialog dialog;
	
	private static int[] result ;
	private static boolean okPressed = false;
	private ChartImageResolutionDialog(){}
	
	public static int[] showDialog(Frame parent, int defaultWidth, int defaultHeight){
		defaultWidth = defaultWidth <= 0 ? 1 : defaultWidth;
		defaultHeight = defaultHeight <= 0 ? 1 : defaultHeight;
		dialog = new JDialog(parent, "Enter Resolution", true);
		okPressed = false;
		result = new int[]{defaultWidth, defaultHeight};
		Box numberFieldPanel = Box.createVerticalBox();
		numberFieldPanel.add(Box.createRigidArea(new Dimension(10, 10)));
		
		NumberTextField widthNumberField = new NumberTextField("Width in px:   ", defaultWidth, 1, 50) 
       {       
			 public double newValue(double newValue)
          {				        
				 newValue = newValue <= 0 ? 1:((int) newValue);
				 result[0] = (int) newValue;
				 return newValue;  
          }
       };
       widthNumberField.setSize(10, 30);
       numberFieldPanel.add(widthNumberField);
       
       numberFieldPanel.add(Box.createVerticalStrut(10));

 		  NumberTextField heightNumberField = new NumberTextField("Height in px:  ", defaultHeight, 1, 50) 
        {       
 			 public double newValue(double newValue)
           {				        
 				 newValue = newValue <= 0 ? 1:((int) newValue);
 				 result[1] = (int) newValue;
 				 return newValue;  
           }
        };
        heightNumberField.setSize(10, 30);
        numberFieldPanel.add(heightNumberField);    
       
       numberFieldPanel.add(Box.createRigidArea(new Dimension(10, 10)));
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
		dialog.setSize(400,150);
		dialog.setResizable(false);
		dialog.setLocation(parent.getLocation().x + (parent.getWidth()/2) - (dialog.getWidth()/2), 
				parent.getLocation().y + (parent.getHeight()/2) - (dialog.getHeight()/2));
		dialog.setVisible(true);
		return okPressed ? result : null;
	}

}
