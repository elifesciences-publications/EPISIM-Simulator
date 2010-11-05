package sim.app.episim.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

import sim.app.episim.util.ByteArrayWriteListener;
import sim.app.episim.util.WriteEvent;


public class EpisimTextOut{
	
	private static EpisimTextOut instance = new EpisimTextOut();
	private String standardText;
	
	private JPanel simTextOutPanel;
	JTextPane textOutput;
	StringBuffer currentTextOnTextOut;
	private EpisimTextOut(){
		
		buildTextOutPanel();
		standardText="Episim Simulator version " + EpidermisSimulator.versionID + "<br>Simulation Text Output:<br><br>";
		currentTextOnTextOut = new StringBuffer();
		appendHTMLStartTags();
		this.currentTextOnTextOut.append(standardText);
		textOutput.setText(this.currentTextOnTextOut.toString());
		EpidermisSimulator.errorOutputStream.addWriteListener(new ByteArrayWriteListener()
	    {
	       
	       public void textWasWritten(WriteEvent event)
	       {
	          
	          Object source = event.getSource();
	          if(source instanceof ByteArrayOutputStream){
	         	 ByteArrayOutputStream stream = (ByteArrayOutputStream) source;
		      	 StringBuffer buffer = new StringBuffer();
		      	 buffer.append(new String(stream.toByteArray()));
		          print(buffer.toString(), Color.RED);
		          stream.reset(); 
	          }
	       }

			
	    });
		
		EpidermisSimulator.standardOutputStream.addWriteListener(new ByteArrayWriteListener()
	    {
	       
	       public void textWasWritten(WriteEvent event)
	       {	          
	          Object source = event.getSource();
	          if(source instanceof ByteArrayOutputStream){
	         	 ByteArrayOutputStream stream = (ByteArrayOutputStream) source;
		      	 StringBuffer buffer = new StringBuffer();
		      	 buffer.append(new String(stream.toByteArray()));
		          print(buffer.toString(), Color.BLUE);
		          stream.reset(); 
	          }
	       }

			
	    });
	
	}
	
	public static EpisimTextOut getEpisimTextOut(){
		return instance;
	}
	
	public void print(String text, Color c){
		currentTextOnTextOut.append("<span style=\"color:#"+Integer.toHexString(c.getRGB()).substring(2)+";\">" +formatText(text)+"</span>");
		textOutput.setText(currentTextOnTextOut.toString()+getHTMLClosingTags());
	}
	
	public void println(String text, Color c){
		print(text.concat("\n"), c);
	}
	
	
	
	
	
	public void clear(){
		this.currentTextOnTextOut = new StringBuffer();
		appendHTMLStartTags();
		this.currentTextOnTextOut.append(standardText);
		textOutput.setText(this.currentTextOnTextOut.toString());
	}
	public JPanel getEpisimTextOutPanel(){
		return this.simTextOutPanel;
	}
	
	private String formatText(String text){
		return text.replace(" ", "&nbsp;").replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;").replace("\n", "<br>");
	}
	
	private void buildTextOutPanel(){
		simTextOutPanel = new JPanel(new BorderLayout());
		
		textOutput = new JTextPane();
		textOutput.setContentType("text/html");
		//textOutput.setBorder(BorderFactory.createLoweredBevelBorder());
		textOutput.setFont(new Font("Courier", Font.PLAIN, 11));
		textOutput.setEditable(false);
		textOutput.setMargin(new Insets(3,3,3,3));
		JScrollPane areaScrollPane = new JScrollPane(textOutput);
		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		areaScrollPane.setPreferredSize(new Dimension(250, 175));
		
		simTextOutPanel.add(areaScrollPane, BorderLayout.CENTER);
		
		//Clear-Button
		
		JPanel buttonPanel = new JPanel(new BorderLayout(5,5));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

	         clear();
	         
         }});
		buttonPanel.add(clearButton, BorderLayout.EAST);
		simTextOutPanel.add(buttonPanel, BorderLayout.SOUTH);
	}
	private String getHTMLClosingTags(){
		return "</body></html>";
	}
	private void appendHTMLStartTags(){
		this.currentTextOnTextOut.append("<html><body style=\"font-family:'Courier New',Courier,monospace;\">");
	}
}
