package sim.app.episim1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


public class EpidermisSimulator extends JFrame{
	
	private JarFileChooser jarFileChoose;
	private TSSFileChooser tssFileChoose;
	
	private final String OPENMODEL = "Open Model";
	private final String CLOSEMODEL = "Close Model";
	
	private EpidermisWithUIClass epiUI;
	
	private boolean modelOpened = false;
	private JMenuItem menuItemSetSnapshotPath;
	private JMenuItem menuItemLoadSnapshot;

	
	public EpidermisSimulator(){
		ExceptionDisplayer.getInstance().registerParentComp(this);
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e){
			
			ExceptionDisplayer.getInstance().displayException(e);
		}
		final EpidermisSimulator simulator = this;
		
		//Menü
		JMenuBar  menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		JMenuItem menuItemOpen = new JMenuItem("Open EpiSimModel");
		menuItemOpen.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				if(modelOpened){
					int choice = JOptionPane.showConfirmDialog(simulator, "Do you really want to close the opened model?", "Close Model?", JOptionPane.YES_NO_OPTION);
					if(choice == JOptionPane.OK_OPTION){
						closeModel();
						openModel();
					}
				}
				else openModel();
				
			}
			
		});
		
		JMenuItem menuItemClose = new JMenuItem("Close EpiSimModel");
		menuItemClose.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				if(modelOpened) closeModel();
				
			}
			
		});
		menuItemSetSnapshotPath = new JMenuItem("Set Tissue-Snaphot-Path");
		menuItemSetSnapshotPath.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				setSnapshotPath();
				
			}
			
		});
		menuItemLoadSnapshot = new JMenuItem("Load Tissue-Snaphot");
		menuItemLoadSnapshot.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				loadSnapshot();
				
			}
			
		});
		menuItemLoadSnapshot.setEnabled(true);
		menu.add(menuItemOpen);
		menu.add(menuItemSetSnapshotPath);
		menu.add(menuItemLoadSnapshot);
		menu.addSeparator();
		menu.add(menuItemClose);
		
		menuBar.add(menu);
		
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().setBackground(Color.LIGHT_GRAY);
		this.getContentPane().add(menuBar, BorderLayout.NORTH);
		
		jarFileChoose= new JarFileChooser();
		tssFileChoose = new TSSFileChooser();
		
		this.setTitle("Epidermis Simulator");
		
		this.setPreferredSize(new Dimension((int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth(),
				(int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 30));
		
		
		this.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {

				if(epiUI != null){
					epiUI.closeConsole();
					System.exit(0);
				}
				else System.exit(0);

			}
		});
		this.pack();
		this.setVisible(true);
	}
	
	public static void main(String[] args){
		EpidermisSimulator episim = new EpidermisSimulator();
	}
	
	
	
	
	private void openModel(){
		File file = null;

		if(jarFileChoose.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
			file = jarFileChoose.getSelectedFile();

			boolean success = BioChemicalModelController.getInstance().loadModelFile(file);
			
			if(SnapshotWriter.getInstance().getSnapshotPath() == null){
				JOptionPane.showMessageDialog(this, "Please specify snapshot path.", "Info", JOptionPane.INFORMATION_MESSAGE);
				setSnapshotPath();
				if(SnapshotWriter.getInstance().getSnapshotPath() == null)success = false;
			}
			//System.out.println(success);
			if(success){
				EpiSimCharts.rebuildCharts();
				cleanUpContentPane();
				epiUI = new EpidermisWithUIClass(this);
				this.validate();
				this.repaint();
				modelOpened = true;
				menuItemSetSnapshotPath.setEnabled(true);
				menuItemLoadSnapshot.setEnabled(false);
			}

		}
		
	}
	private void setSnapshotPath(){
		File file = null;

		if(tssFileChoose.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
			file = tssFileChoose.getSelectedFile();
			if(file != null){
			  this.setTitle(getTitle()+ " - Snapshotpath: "+file.getAbsolutePath());
			  SnapshotWriter.getInstance().setSnapshotPath(file);
			}
		}
		
	}
	public void loadSnapshot(){
		File file = null;

		if(tssFileChoose.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
			file = tssFileChoose.getSelectedFile();
			if(file != null){
			  this.setTitle(getTitle()+ " - Snapshotpath: "+file.getAbsolutePath());
			  SnapshotReader.getInstance().loadSnapshot(file);
			}
		}
	}
	
	private void closeModel(){
		if(epiUI != null){
			
			epiUI.quit();
		}
		epiUI = null;
		System.gc();
		this.repaint();
		modelOpened = false;
		menuItemLoadSnapshot.setEnabled(true);
	}
	
	private void cleanUpContentPane(){
		Component[] comps = this.getContentPane().getComponents();
		for(int i = 0; i < comps.length; i++){
			if(!(comps[i] instanceof JMenuBar)) this.getContentPane().remove(i);
		}
			
	}

}
