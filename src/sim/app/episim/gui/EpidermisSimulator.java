package sim.app.episim.gui;

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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import sim.app.episim.BioChemicalModelController;
import sim.app.episim.EpidermisClass;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.SnapshotObject;
import sim.app.episim.SnapshotReader;
import sim.app.episim.SnapshotWriter;
import sim.engine.Schedule;
import sim.portrayal.DrawInfo2D;
import sim.util.Double2D;


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
		jarFileChoose.setDialogTitle("Open EpiSim Model");
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
	public void loadSnapshot() {

		File file = null;
		boolean success = false;
		EpidermisClass epidermis = null;
		EpiSimCharts charts = null;
		List<Double2D> woundRegionCoordinates = null;
		java.awt.geom.Rectangle2D.Double[] deltaInfo = null;
		if(tssFileChoose.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
			file = tssFileChoose.getSelectedFile();
			if(file != null){
				List<SnapshotObject> snapshotobjects = SnapshotReader.getInstance().loadSnapshot(file);
				for(SnapshotObject sObj : snapshotobjects){
					if(sObj.getIdentifier().equals(SnapshotObject.EPIDERMIS)){
						epidermis = (EpidermisClass) sObj.getSnapshotObject();
						epidermis.setReloadedSnapshot(true);
					}
					else if(sObj.getIdentifier().equals(SnapshotObject.CHARTS)){
						charts = (EpiSimCharts) sObj.getSnapshotObject();
						EpiSimCharts.setInstance(charts);
					}
					else if(sObj.getIdentifier().equals(SnapshotObject.WOUND)){
						Object obj= null;
						if((obj=sObj.getSnapshotObject())instanceof List)
						                        woundRegionCoordinates = (List<Double2D>) obj;
						else deltaInfo = (java.awt.geom.Rectangle2D.Double[])sObj.getSnapshotObject();
						
					}
					
				}
				if(charts != null) SnapshotWriter.getInstance().addSnapshotListener(charts);
				if(epidermis != null) SnapshotWriter.getInstance().addSnapshotListener(epidermis);
				File file2 = null;

				if(jarFileChoose.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
					file2 = jarFileChoose.getSelectedFile();

					success = BioChemicalModelController.getInstance().loadModelFile(file2);
					if(SnapshotWriter.getInstance().getSnapshotPath() == null){
						JOptionPane.showMessageDialog(this, "Please specify snapshot path.", "Info",
								JOptionPane.INFORMATION_MESSAGE);
						setSnapshotPath();
						if(SnapshotWriter.getInstance().getSnapshotPath() == null)
							success = false;
					}
					// System.out.println(success);
					if(success){
						// EpiSimCharts.rebuildCharts();
						cleanUpContentPane();
						epidermis.setModelController(BioChemicalModelController.getInstance());
						epiUI = new EpidermisWithUIClass(epidermis, this, true);
						epiUI.setReloadedSnapshot(true);
						if(epiUI.getWoundPortrayalDraw() !=null){
						  if(woundRegionCoordinates!= null) epiUI.getWoundPortrayalDraw().setWoundRegionCoordinates(woundRegionCoordinates);
						  if(deltaInfo!= null && deltaInfo.length >=2) 
							  epiUI.getWoundPortrayalDraw().setDeltaInfo(new DrawInfo2D(deltaInfo[0], deltaInfo[1]) );
						  SnapshotWriter.getInstance().addSnapshotListener(epiUI.getWoundPortrayalDraw());
						}
						this.validate();
						this.repaint();
						modelOpened = true;
						menuItemSetSnapshotPath.setEnabled(true);
						menuItemLoadSnapshot.setEnabled(false);
					}
				}

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
