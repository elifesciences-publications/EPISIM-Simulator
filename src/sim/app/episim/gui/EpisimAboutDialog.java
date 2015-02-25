package sim.app.episim.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import sim.app.episim.EpisimExceptionHandler;
import sim.engine.SimState;


public class EpisimAboutDialog{
	
	private JDialog dialog;
	private final int DIALOG_WIDTH = 650;
	private final int DIALOG_HEIGHT = 375;
	private final int BORDER_SIZE = 15;
	private final int IMAGE_WIDTH = 115;
	
	public EpisimAboutDialog(Frame owner){
		dialog = new JDialog(owner, "About EPISIM Simulator");
		dialog.getContentPane().setLayout(new BorderLayout());
		dialog.setSize(new Dimension(DIALOG_WIDTH,DIALOG_HEIGHT));
		dialog.setResizable(false);
		
		GridBagConstraints c = new GridBagConstraints(); 		
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0;
		c.insets = new Insets(0, 0, 0, 0);
		c.gridwidth = 1;	      
		JPanel panel = new JPanel(new GridBagLayout());  
	 	panel.setBorder(BorderFactory.createEmptyBorder(BORDER_SIZE,BORDER_SIZE,BORDER_SIZE,BORDER_SIZE));
	 	JScrollPane scroll = new JScrollPane(panel);
	 	scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	 	buildAbout(panel,c);
		dialog.getContentPane().add(scroll, BorderLayout.CENTER);
		JLabel iconLabel = new JLabel(new ImageIcon(ImageLoader.class.getResource("episim_about_picture.png")));
		dialog.getContentPane().add(iconLabel, BorderLayout.WEST);
	}
	
	private void addSpacer(JPanel panel, GridBagConstraints c, int size){
		 JLabel spacer = new JLabel(" ");
	    spacer.setFont(new Font("Dialog",0,size));
	    c.gridy += 1;
	    panel.add(spacer, c);
		
	}
	
	private void buildAbout(JPanel panel, GridBagConstraints c){		 	
	 	
		AboutHtmlEditor htmlPane = null;	   

	   	   
     
	   try{
	      htmlPane = new AboutHtmlEditor(ImageLoader.class.getResource("episim_about.html"));
	      c.gridy += 1;
		   panel.add(htmlPane,c);
		   addSpacer(panel, c, 25);
		   htmlPane = new AboutHtmlEditor(ImageLoader.class.getResource("copasi_about.html"));
	      c.gridy += 1;
		   panel.add(htmlPane,c);
		   htmlPane = new AboutHtmlEditor(ImageLoader.class.getResource("mason_about.html"));
	      c.gridy += 1;
		   panel.add(htmlPane,c);
      }
      catch (IOException e){
	     EpisimExceptionHandler.getInstance().displayException(e);
      }	     
	  	  
	   
	}
	
	public void showAboutDialog(){
	// if not on screen right now, move to center of screen
		  if (!dialog.isVisible())
		   {
			  if(dialog.getParent() == null){
					Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
					dialog.setLocation(((int)(screenDim.getWidth() /2) - (dialog.getWidth()/2)), 
					((int)(screenDim.getHeight() /2) - (dialog.getHeight()/2)));
				}
				else{
					Dimension parentDim = dialog.getParent().getSize();
					dialog.setLocation(((int)(dialog.getParent().getLocation().getX()+((parentDim.getWidth() /2) - (dialog.getWidth()/2)))), 
					((int)(dialog.getParent().getLocation().getY()+((parentDim.getHeight() /2) - (dialog.getHeight()/2)))));
				}
		   }
		  
		  // show it!
		  dialog.setVisible(true);
	}
	
	
	
	private class AboutHtmlEditor extends JEditorPane implements HyperlinkListener{		
		public AboutHtmlEditor(URL url) throws IOException{
			super(url);     
	      this.setOpaque(true);
	      this.setAlignmentX(Component.LEFT_ALIGNMENT);
	      this.setEditable(false);
	      this.setContentType("text/html");
	      this.setBackground((new JLabel()).getBackground());
	      this.addHyperlinkListener(this);
		}
		public Dimension getPreferredSize(){
			Dimension dim = super.getPreferredSize();
			return new Dimension(DIALOG_WIDTH-(3*BORDER_SIZE+IMAGE_WIDTH), (int)dim.getHeight());
		}

		public void hyperlinkUpdate(HyperlinkEvent e) {
			if(Desktop.isDesktopSupported()){
				Desktop desktop = Desktop.getDesktop();
				if(desktop.isSupported(Desktop.Action.BROWSE ) ) {
					if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED){
						try{
	                  desktop.browse(e.getURL().toURI());
                  }
                  catch (IOException e1){
	                 EpisimExceptionHandler.getInstance().displayException(e1);
                  }
                  catch (URISyntaxException e1){
                  	EpisimExceptionHandler.getInstance().displayException(e1);
                  }
					}					
				}
			}		   
	   }
	}

}
