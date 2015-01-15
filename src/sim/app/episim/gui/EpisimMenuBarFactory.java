package sim.app.episim.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.ModeServer;
import sim.app.episim.datamonitoring.charts.ChartController;
import sim.app.episim.datamonitoring.dataexport.DataExportController;
import sim.app.episim.datamonitoring.dataexport.TissueSnapshotDataExportDialog;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.persistence.SimulationStateFile;
import sim.app.episim.propfilegenerator.PropertyFileGeneratorWizard;
import sim.display.ConsoleHack;


public class EpisimMenuBarFactory {
	
	public enum EpisimMenu {
		FILE_MENU("File"), 
		CHART_MENU("Charting"), 
		DATAEXPORT_MENU("Data Export"), 
		INFO_MENU("Info"),
		WINDOWS_MENU("Windows"),
		PARAMETERS_SCAN("Parameter Scan");
		
		private String name;
		private EpisimMenu(String _name){ this.name = _name;}
		public String toString(){ return name;}
	
	}
	
	public enum EpisimMenuItem {		
		SET_SNAPSHOT_PATH("Set Tissue-Export-Path"),
		LOAD_SNAPSHOT("Load Tissue-Export"),
		OPEN_MODEL_FILE("Open EPISIM-Cell-Model"),
		CLOSE_MODEL_FILE("Close EPISIM-Cell-Model"),
		CLOSE_SIMULATOR("Close EPISIM Simulator"),
		
		EDIT_CHART_SET("Edit Chart-Set"),
		LOAD_CHART_SET("Load Chart-Set"),
		NEW_CHART_SET("New Chart-Set"),
		CLOSE_CHART_SET("Close Chart-Set"),
		SELECT_DEFAULT_CHARTS("Select EPISIM Default-Charts"),		
		
		EDIT_DATA_EXPORT("Edit Loaded Data-Export-Definition-Set"),
		LOAD_DATA_EXPORT("Load Data-Export-Definition-Set"),
		NEW_DATA_EXPORT("New Data-Export-Definition-Set"),
		CLOSE_DATA_EXPORT("Close Loaded Data-Export-Definition-Set"),
		DATA_EXPORT_SIMULATION_SNAPSHOT("Export Data from Simulation Snapshot"),
		
		ABOUT_EPISIM_SIMULATOR("About EPISIM Simulator"),
		UPDATE_EPISIM_SIMULATOR("Update EPISIM Simulator"),
		GENERATE_PARAMETER_FILES("Param-Scan File-Generator"),		
		AUTO_ARRANGE_WINDOWS("Auto-Arrange Windows");
		
		private String name;
		private EpisimMenuItem(String _name){ this.name = _name;}
		public String toString(){ return name;}
	}		
	
	private JMenuBar menuBar;	
	
	private EpisimSimulator simulator;
	private EpisimAboutDialog aboutDialog;
	private EpisimUpdateDialog updateDialog;
	
	public EpisimMenuBarFactory(EpisimSimulator simulator){
		if(simulator == null) throw new IllegalArgumentException("Epidermis Simulator must not be null");
		else this.simulator = simulator;
		
		menuBar = new JMenuBar();
		
		buildFileMenu(menuBar);		
		buildChartsMenu(menuBar);		
		buildDataExportMenu(menuBar);		
		buildParamScanMenu(menuBar);
		buildWindowsMenu(menuBar);		
		buildInfoMenu(menuBar);
		
		if(simulator.getMainFrame() instanceof JFrame){
			((JFrame)simulator.getMainFrame()).setJMenuBar(menuBar);
			aboutDialog = new EpisimAboutDialog(((JFrame)simulator.getMainFrame()));
			updateDialog = new EpisimUpdateDialog(((JFrame)simulator.getMainFrame()));
		}
	}
	
	
	
	private void buildFileMenu(JMenuBar menuBar){
		//--------------------------------------------------------------------------------------------------------------
		//Menü File
		//--------------------------------------------------------------------------------------------------------------
		
		JMenu fileMenu = new JMenu(EpisimMenu.FILE_MENU.toString());
		JMenuItem menuItemOpen = new JMenuItem(EpisimMenuItem.OPEN_MODEL_FILE.toString());
		menuItemOpen.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				if(ModelController.getInstance().isModelOpened()){
					int choice = JOptionPane.showConfirmDialog(simulator.getMainFrame(), "Do you really want to close the opened model?", "Close Model?", JOptionPane.YES_NO_OPTION);
					if(choice == JOptionPane.OK_OPTION){
						simulator.closeModel();
						simulator.openModel();
					}
				}
				else simulator.openModel();
				
			}
			
		});
		
		JMenuItem menuItemClose = new JMenuItem(EpisimMenuItem.CLOSE_MODEL_FILE.toString());
		menuItemClose.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				if(ModelController.getInstance().isModelOpened()) simulator.closeModel();				
			}
			
		});
		menuItemClose.setEnabled(false);
		
		JMenuItem menuItemCloseSimulator = new JMenuItem(EpisimMenuItem.CLOSE_SIMULATOR.toString());
		menuItemCloseSimulator.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				if(ModelController.getInstance().isModelOpened()){
					int choice = JOptionPane.showConfirmDialog(simulator.getMainFrame(), "Do you really want to close the opened model?", "Close Model?", JOptionPane.YES_NO_OPTION);
					if(choice == JOptionPane.OK_OPTION){
						simulator.closeModel();	
						simulator.close(0);
					}
				}
				else simulator.close(0);
				
			}
			
		});
		menuItemCloseSimulator.setEnabled(true);
		JMenuItem menuItemSetSnapshotPath = new JMenuItem(EpisimMenuItem.SET_SNAPSHOT_PATH.toString());
		menuItemSetSnapshotPath.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				simulator.setTissueExportPath();				
			}
			
		});
		JMenuItem menuItemLoadSnapshot = new JMenuItem(EpisimMenuItem.LOAD_SNAPSHOT.toString());
		menuItemLoadSnapshot.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				boolean loadTissueExport = false;
				if(ModelController.getInstance().isModelOpened()){
					int choice = JOptionPane.showConfirmDialog(simulator.getMainFrame(), "Do you really want to close the opened model?", "Close Model?", JOptionPane.YES_NO_OPTION);
					if(choice == JOptionPane.OK_OPTION){
						simulator.closeModel();
						loadTissueExport = true;
					}
				}
				else loadTissueExport = true;				
				
				if(loadTissueExport && simulator.getMainFrame() instanceof JFrame){					
					ExtendedFileChooser chooser = new ExtendedFileChooser("xml");
					chooser.setDialogTitle("Open EPISIM Tissue-Export");
					if(ExtendedFileChooser.APPROVE_OPTION == chooser.showOpenDialog((JFrame)simulator.getMainFrame())){
						simulator.loadSimulationStateFile(chooser.getSelectedFile(),true,false);						
					}
				}			
			}
			
		});
				
		menuItemLoadSnapshot.setEnabled(true);
		
		fileMenu.add(menuItemOpen);
		fileMenu.add(menuItemSetSnapshotPath);
		fileMenu.add(menuItemLoadSnapshot);
		//fileMenu.addSeparator();
		fileMenu.addSeparator();
		fileMenu.add(menuItemClose);
		fileMenu.addSeparator();
		fileMenu.add(menuItemCloseSimulator);
		
		menuBar.add(fileMenu);
	}
	
	
	private void buildChartsMenu(JMenuBar menuBar){
		//--------------------------------------------------------------------------------------------------------------
		// Menü Charts
		//--------------------------------------------------------------------------------------------------------------
		
		JMenu chartMenu = new JMenu(EpisimMenu.CHART_MENU.toString());
		chartMenu.setEnabled(false);
		
		JMenuItem menuItemNewChartSet = new JMenuItem(EpisimMenuItem.NEW_CHART_SET.toString());
		menuItemNewChartSet.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				boolean success = true;
				if(simulator.getMainFrame() instanceof JFrame) success = ChartController.getInstance().showNewChartSetDialog((JFrame)simulator.getMainFrame());
				if(success){
					getEpisimMenuItem(EpisimMenuItem.EDIT_CHART_SET).setEnabled(true);
					getEpisimMenuItem(EpisimMenuItem.CLOSE_CHART_SET).setEnabled(true);
					getEpisimMenuItem(EpisimMenuItem.NEW_CHART_SET).setEnabled(false);
					getEpisimMenuItem(EpisimMenuItem.LOAD_CHART_SET).setEnabled(false);
				}
				
			}
			
		});
		
		JMenuItem menuItemLoadChartSet = new JMenuItem(EpisimMenuItem.LOAD_CHART_SET.toString());
		menuItemLoadChartSet.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				boolean success = ChartController.getInstance().loadChartSet(simulator.getMainFrame());
				
				if(success){ 
					if(simulator.getMainFrame() instanceof JFrame)ChartController.getInstance().showEditChartSetDialog((JFrame)simulator.getMainFrame());
					getEpisimMenuItem(EpisimMenuItem.EDIT_CHART_SET).setEnabled(true);
					getEpisimMenuItem(EpisimMenuItem.CLOSE_CHART_SET).setEnabled(true);
					getEpisimMenuItem(EpisimMenuItem.NEW_CHART_SET).setEnabled(false);
					getEpisimMenuItem(EpisimMenuItem.LOAD_CHART_SET).setEnabled(false);
				}
				else{ 
					if(!ChartController.getInstance().isAlreadyChartSetLoaded()) getEpisimMenuItem(EpisimMenuItem.EDIT_CHART_SET).setEnabled(false);
				}
			}
			
		});
		
		JMenuItem menuItemEditChartSet = new JMenuItem(EpisimMenuItem.EDIT_CHART_SET.toString());
		menuItemEditChartSet.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				if(simulator.getMainFrame() instanceof JFrame) ChartController.getInstance().showEditChartSetDialog((JFrame)simulator.getMainFrame());
			}
			
		});
		menuItemEditChartSet.setEnabled(false);
		
		JMenuItem menuItemCloseChartSet = new JMenuItem(EpisimMenuItem.CLOSE_CHART_SET.toString());
		menuItemCloseChartSet.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				getEpisimMenuItem(EpisimMenuItem.NEW_CHART_SET).setEnabled(true);
				getEpisimMenuItem(EpisimMenuItem.LOAD_CHART_SET).setEnabled(true);
				getEpisimMenuItem(EpisimMenuItem.CLOSE_CHART_SET).setEnabled(false);
				getEpisimMenuItem(EpisimMenuItem.EDIT_CHART_SET).setEnabled(false);
				ChartController.getInstance().closeActLoadedChartSet();
				simulator.removeAllChartInternalFrames();
			}
			
		});
		menuItemCloseChartSet.setEnabled(false);
		
		JMenuItem menuItemSelectDefaultCharts = new JMenuItem(EpisimMenuItem.SELECT_DEFAULT_CHARTS.toString());
		menuItemSelectDefaultCharts.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				
				if(simulator.getMainFrame() instanceof JFrame) ChartController.getInstance().showDefaultChartsSelectionDialog((JFrame)simulator.getMainFrame());
				
			}
			
		});
		menuItemSelectDefaultCharts.setEnabled(true);
		
		chartMenu.add(menuItemNewChartSet);
		chartMenu.add(menuItemLoadChartSet);
		chartMenu.add(menuItemEditChartSet);
		chartMenu.addSeparator();
		chartMenu.add(menuItemCloseChartSet);
		chartMenu.addSeparator();
		chartMenu.add(menuItemSelectDefaultCharts);
		menuBar.add(chartMenu);
	}
	
	private void buildDataExportMenu(JMenuBar menuBar){
		//--------------------------------------------------------------------------------------------------------------
		// Menü DataExport
		//--------------------------------------------------------------------------------------------------------------
		
		JMenu dataExportMenu = new JMenu(EpisimMenu.DATAEXPORT_MENU.toString());
		dataExportMenu.setEnabled(true);
		
		JMenuItem menuItemNewDataExport = new JMenuItem(EpisimMenuItem.NEW_DATA_EXPORT.toString());
		menuItemNewDataExport.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				boolean success = true;
				if(simulator.getMainFrame() instanceof JFrame) success = DataExportController.getInstance().showNewDataExportDefinitionSetDialog((JFrame)simulator.getMainFrame());
				if(success){
					getEpisimMenuItem(EpisimMenuItem.EDIT_DATA_EXPORT).setEnabled(true);
					getEpisimMenuItem(EpisimMenuItem.CLOSE_DATA_EXPORT).setEnabled(true);
					getEpisimMenuItem(EpisimMenuItem.NEW_DATA_EXPORT).setEnabled(false);
					getEpisimMenuItem(EpisimMenuItem.LOAD_DATA_EXPORT).setEnabled(false);
					simulator.getStatusbar().setMessage("Loaded Data Export: "+ DataExportController.getInstance().getActLoadedDataExportsName());
				}
				
			}
			
		});
		menuItemNewDataExport.setEnabled(false);
		
		JMenuItem menuItemLoadDataExport = new JMenuItem(EpisimMenuItem.LOAD_DATA_EXPORT.toString());
		menuItemLoadDataExport.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				boolean success = true;
				if(simulator.getMainFrame() instanceof JFrame) success = DataExportController.getInstance().loadDataExportDefinition((JFrame)simulator.getMainFrame());
				
				if(success){ 
					if(simulator.getMainFrame() instanceof JFrame) DataExportController.getInstance().showEditDataExportDefinitionDialog((JFrame)simulator.getMainFrame());
					getEpisimMenuItem(EpisimMenuItem.EDIT_DATA_EXPORT).setEnabled(true);
					getEpisimMenuItem(EpisimMenuItem.CLOSE_DATA_EXPORT).setEnabled(true);
					getEpisimMenuItem(EpisimMenuItem.NEW_DATA_EXPORT).setEnabled(false);
					getEpisimMenuItem(EpisimMenuItem.LOAD_DATA_EXPORT).setEnabled(false);
					simulator.getStatusbar().setMessage("Loaded Data Export: "+ DataExportController.getInstance().getActLoadedDataExportsName());
				}
				else{ 
					if(!DataExportController.getInstance().isAlreadyDataExportSetLoaded()) getEpisimMenuItem(EpisimMenuItem.EDIT_DATA_EXPORT).setEnabled(false);
				}
			}
			
		});
		menuItemLoadDataExport.setEnabled(false);
		JMenuItem menuItemEditDataExport = new JMenuItem(EpisimMenuItem.EDIT_DATA_EXPORT.toString());
		menuItemEditDataExport.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				if(simulator.getMainFrame() instanceof JFrame) DataExportController.getInstance().showEditDataExportDefinitionDialog((JFrame)simulator.getMainFrame());
				simulator.getStatusbar().setMessage("Loaded Data Export: "+ DataExportController.getInstance().getActLoadedDataExportsName());
			}
			
		});
		menuItemEditDataExport.setEnabled(false);
		
		JMenuItem menuItemCloseDataExport = new JMenuItem(EpisimMenuItem.CLOSE_DATA_EXPORT.toString());
		menuItemCloseDataExport.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				getEpisimMenuItem(EpisimMenuItem.NEW_DATA_EXPORT).setEnabled(true);
				getEpisimMenuItem(EpisimMenuItem.LOAD_DATA_EXPORT).setEnabled(true);
				getEpisimMenuItem(EpisimMenuItem.CLOSE_DATA_EXPORT).setEnabled(false);
				getEpisimMenuItem(EpisimMenuItem.EDIT_DATA_EXPORT).setEnabled(false);
				DataExportController.getInstance().closeActLoadedDataExportDefinitonSet();
				simulator.getStatusbar().setMessage("Ready");
			}
			
		});
		menuItemCloseDataExport.setEnabled(false);
		
		JMenuItem menuItemDataExportSimulationSnaphot = new JMenuItem(EpisimMenuItem.DATA_EXPORT_SIMULATION_SNAPSHOT.toString());
		menuItemDataExportSimulationSnaphot.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				if(ModeServer.guiMode()){
					TissueSnapshotDataExportDialog dialog = new TissueSnapshotDataExportDialog((JFrame)simulator.getMainFrame(), true);
					dialog.showDialog(simulator);
				}
				/*getEpisimMenuItem(EpisimMenuItem.NEW_DATA_EXPORT).setEnabled(true);
				getEpisimMenuItem(EpisimMenuItem.LOAD_DATA_EXPORT).setEnabled(true);
				getEpisimMenuItem(EpisimMenuItem.CLOSE_DATA_EXPORT).setEnabled(false);
				getEpisimMenuItem(EpisimMenuItem.EDIT_DATA_EXPORT).setEnabled(false);
				DataExportController.getInstance().closeActLoadedDataExportDefinitonSet();
				simulator.getStatusbar().setMessage("Ready");*/
			}
			
		});
		menuItemDataExportSimulationSnaphot.setEnabled(true);
		
		dataExportMenu.add(menuItemNewDataExport);
		dataExportMenu.add(menuItemLoadDataExport);
		dataExportMenu.add(menuItemEditDataExport);
		dataExportMenu.addSeparator();
		dataExportMenu.add(menuItemCloseDataExport);
		dataExportMenu.addSeparator();
		dataExportMenu.add(menuItemDataExportSimulationSnaphot);
		menuBar.add(dataExportMenu);
	}
	
	private void buildInfoMenu(JMenuBar menuBar){
		//--------------------------------------------------------------------------------------------------------------
		// Menü Info
		//--------------------------------------------------------------------------------------------------------------
		
		JMenu infoMenu = new JMenu(EpisimMenu.INFO_MENU.toString());
		
		JMenuItem menuItemUpdateEpisimSimulator = new JMenuItem(EpisimMenuItem.UPDATE_EPISIM_SIMULATOR.toString());
		
		menuItemUpdateEpisimSimulator.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(simulator.getMainFrame() instanceof JFrame){
					updateDialog.showUpdateDialog();
				}				
			}			
		});		
		infoMenu.add(menuItemUpdateEpisimSimulator);
		
		
		infoMenu.addSeparator();
		
		JMenuItem menuItemAboutEpisimSimulator = new JMenuItem(EpisimMenuItem.ABOUT_EPISIM_SIMULATOR.toString());
		
		menuItemAboutEpisimSimulator.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(simulator.getMainFrame() instanceof JFrame){
					aboutDialog.showAboutDialog();
				}				
			}			
		});
		
		infoMenu.add(menuItemAboutEpisimSimulator);
		menuBar.add(infoMenu);		
		
	}
	
	private void buildWindowsMenu(JMenuBar menuBar)
	{
		JMenu windowsMenu = new JMenu(EpisimMenu.WINDOWS_MENU.toString());
		
		final JMenuItem menuItemAutoArrangeWindows = new JCheckBoxMenuItem(EpisimMenuItem.AUTO_ARRANGE_WINDOWS.toString());
		menuItemAutoArrangeWindows.setSelected(true);
		menuItemAutoArrangeWindows.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				simulator.setAutoArrangeWindows(menuItemAutoArrangeWindows.isSelected());
         }
		});
		windowsMenu.add(menuItemAutoArrangeWindows);
		menuBar.add(windowsMenu);
	}
	
	private void buildParamScanMenu(JMenuBar menuBar)
	{
		JMenu paramScanMenu = new JMenu(EpisimMenu.PARAMETERS_SCAN.toString());
		
		final JMenuItem menuItemParamFileGen = new JMenuItem(EpisimMenuItem.GENERATE_PARAMETER_FILES.toString());
		menuItemParamFileGen.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(simulator.getMainFrame() instanceof JFrame){
				
						new PropertyFileGeneratorWizard((JFrame)simulator.getMainFrame(), "Parameter Scan File Generator", false);
					
				}
				
         }
		});
		
		paramScanMenu.add(menuItemParamFileGen);
		paramScanMenu.setEnabled(false);
		menuBar.add(paramScanMenu);
	}	
	
	public JMenu getEpisimMenu(EpisimMenu menu){
		
		for(int i = 0; i < menuBar.getMenuCount(); i++){
			if(menuBar.getMenu(i).getText().equals(menu.toString())) return menuBar.getMenu(i);
		}
		return null;
	}
	
	public JMenuItem getEpisimMenuItem(EpisimMenuItem menuItem){
		for(int i = 0; i < menuBar.getMenuCount(); i++){
			JMenuItem item = getEpisimMenuItem(menuItem, menuBar.getMenu(i));
			if(item != null) return item;
		}
		return null;
	}
	
	private JMenuItem getEpisimMenuItem(EpisimMenuItem menuItem, JMenu menu){ 
		for(int i = 0; i < menu.getMenuComponentCount(); i++){			
			if(menu.getMenuComponent(i) instanceof JMenuItem 
					&& ((JMenuItem)menu.getMenuComponent(i)).getText().equals(menuItem.toString())) return  (JMenuItem)menu.getMenuComponent(i);
		}
		return null;
	}
   

}
