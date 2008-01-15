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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import sim.app.episim.CompileWizard;
import sim.app.episim.Epidermis;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.SnapshotObject;
import sim.app.episim.SnapshotReader;
import sim.app.episim.SnapshotWriter;
import sim.app.episim.charts.ChartController;
import sim.app.episim.charts.EpiSimCharts;
import sim.app.episim.model.BioChemicalModelController;
import sim.app.episim.visualization.WoundPortrayal2D;
import sim.display.Console;
import sim.display.ConsoleHack;
import sim.engine.Schedule;
import sim.portrayal.DrawInfo2D;
import sim.util.Double2D;


public class EpidermisSimulator extends JFrame{
	
	private JarFileChooser jarFileChoose;
	private TSSFileChooser tssFileChoose;
	
	
	
	private EpidermisGUIState epiUI;
	
	private boolean modelOpened = false;
	
	private JMenu fileMenu;
	private JMenuItem menuItemSetSnapshotPath;
	private JMenuItem menuItemLoadSnapshot;
	private JMenuItem menuItemOpen;
	private JMenuItem menuItemClose;
	private JMenuItem menuItemBuild;
	
	private JMenu chartMenu;
	private JMenuItem menuItemChartWizard;
	
	private JMenu infoMenu;
	private JMenuItem menuItemAboutMason;
	
	
	public EpidermisSimulator(){
		ExceptionDisplayer.getInstance().registerParentComp(this);
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e){
			
			ExceptionDisplayer.getInstance().displayException(e);
		}
		final EpidermisSimulator simulator = this;
		//--------------------------------------------------------------------------------------------------------------
		//Menü
		//--------------------------------------------------------------------------------------------------------------
		JMenuBar  menuBar = new JMenuBar();
		//--------------------------------------------------------------------------------------------------------------
		//Menü File
		//--------------------------------------------------------------------------------------------------------------
		fileMenu = new JMenu("File");
		menuItemOpen = new JMenuItem("Open EpiSimModel");
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
		
		menuItemClose = new JMenuItem("Close EpiSimModel");
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
		menuItemBuild = new JMenuItem("Build Episim-Model-Archive");
		menuItemBuild.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				buildModelArchive();
				
			}
			
		});
		
		menuItemLoadSnapshot.setEnabled(true);
		fileMenu.add(menuItemOpen);
		fileMenu.add(menuItemSetSnapshotPath);
		fileMenu.add(menuItemLoadSnapshot);
		fileMenu.addSeparator();
		fileMenu.add(menuItemBuild);
		fileMenu.addSeparator();
		fileMenu.add(menuItemClose);
		
		menuBar.add(fileMenu);
		
		//--------------------------------------------------------------------------------------------------------------
		// Menü Charts
		//--------------------------------------------------------------------------------------------------------------
		
		chartMenu = new JMenu("Charting");
		chartMenu.setEnabled(false);
		menuItemChartWizard = new JMenuItem("Chart Wizard");
		
		menuItemChartWizard.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				ChartController.getInstance().showChartCreationWizard(simulator);
			}
			
		});
		
		chartMenu.add(menuItemChartWizard);
		menuBar.add(chartMenu);
		
		
		//--------------------------------------------------------------------------------------------------------------
		
		
	
		//--------------------------------------------------------------------------------------------------------------
		// Menü Charts
		//--------------------------------------------------------------------------------------------------------------
		
		infoMenu = new JMenu("Info");
		
		menuItemAboutMason = new JMenuItem("About MASON");
		
		menuItemAboutMason.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				ConsoleHack.showAbout();
			}
			
		});
		
		infoMenu.add(menuItemAboutMason);
		menuBar.add(infoMenu);
		
		
		//--------------------------------------------------------------------------------------------------------------
		
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().setBackground(Color.LIGHT_GRAY);
		this.setJMenuBar(menuBar);
		
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
		File standartDir =new File("d:/");
		if(standartDir.exists())jarFileChoose.setCurrentDirectory(standartDir);
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
				epiUI = new EpidermisGUIState(this);
				this.validate();
				this.repaint();
				modelOpened = true;
				menuItemSetSnapshotPath.setEnabled(true);
				menuItemLoadSnapshot.setEnabled(false);
				menuItemBuild.setEnabled(false);
				chartMenu.setEnabled(true);
			}

		}
		
	}
	private void setSnapshotPath(){
		File file = null;

		if(tssFileChoose.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
			file = tssFileChoose.getSelectedFile();
			if(file != null){
			  this.setTitle("Epidermis Simulator"+ " - Snapshotpath: "+file.getAbsolutePath());
			  SnapshotWriter.getInstance().setSnapshotPath(file);
			  SnapshotWriter.getInstance().resetCounter();
			}
		}
		
	}
	public void loadSnapshot() {

		File file = null;
		boolean success = false;
		Epidermis epidermis = null;
		EpiSimCharts charts = null;
		List<Double2D> woundRegionCoordinates = null;
		java.awt.geom.Rectangle2D.Double[] deltaInfo = null;
		if(tssFileChoose.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
			file = tssFileChoose.getSelectedFile();
			if(file != null){
				List<SnapshotObject> snapshotobjects = SnapshotReader.getInstance().loadSnapshot(file);
				for(SnapshotObject sObj : snapshotobjects){
					if(sObj.getIdentifier().equals(SnapshotObject.EPIDERMIS)){
						epidermis = (Epidermis) sObj.getSnapshotObject();
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
						epiUI = new EpidermisGUIState(epidermis, this, true);
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
						menuItemBuild.setEnabled(false);
					}
				}

			}
		}
	}
	
	private void buildModelArchive(){
		CompileWizard wizard = new CompileWizard(this);
		
			wizard.showSelectFilesDialogs();
		
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
		menuItemBuild.setEnabled(true);
		chartMenu.setEnabled(false);
		SnapshotWriter.getInstance().clearListeners();
		SnapshotWriter.getInstance().resetCounter();
		 this.setTitle("Epidermis Simulator");
		  SnapshotWriter.getInstance().setSnapshotPath(null);
	}
	
	private void cleanUpContentPane(){
		Component[] comps = this.getContentPane().getComponents();
		for(int i = 0; i < comps.length; i++){
			if(!(comps[i] instanceof JMenuBar)) this.getContentPane().remove(i);
		}
			
	}

}
