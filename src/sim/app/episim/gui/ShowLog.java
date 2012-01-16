package sim.app.episim.gui;

import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import sim.app.episim.persistence.ImportLog.ImportEntryNode;

public class ShowLog extends JFrame {
	
	JScrollPane scrollPane = null;
	JTextArea logArea = null;
	ImportEntryNode log = null;
	
	public ShowLog(ImportEntryNode processedLog) {
		super();
		this.log = processedLog;
		scrollPane = new JScrollPane();
		logArea = new JTextArea();
		scrollPane.setViewportView(logArea);
		this.add(scrollPane);
		if(log!=null){
			logArea.setFont(Font.getFont(Font.MONOSPACED));
			logArea.setText(log.toString());
		}
		this.setSize(300, 600);
		this.setVisible(true);
	}
}
