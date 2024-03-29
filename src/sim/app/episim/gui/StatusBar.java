package sim.app.episim.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;


import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import sim.app.episim.ModeServer;


public class StatusBar extends JPanel {
   
	private JLabel statusText;
	
   /** Creates a new instance of StatusBar */
   public StatusBar() {
       super();
       statusText = new JLabel();
       statusText.setPreferredSize(new Dimension(100, 16));
       statusText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
       setMessage("Ready");
       setLayout(new BorderLayout());
       setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1), BorderFactory.createEmptyBorder(2, 5, 2, 5)));
       add(statusText, BorderLayout.SOUTH);
   }
   
   public void setMessage(String message){
   	if(ModeServer.useMonteCarloSteps()){
			String line = message != null && message.trim().length() >0 ? " - ":" ";
			statusText.setText(" Monte Carlo Simulation Steps activated"+line+message);   
		}
   	else statusText.setText(" "+message);        
   }
   public String getMessage(){
   	return statusText.getText();        
   }
}