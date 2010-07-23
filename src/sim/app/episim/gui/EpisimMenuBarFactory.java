package sim.app.episim.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import sim.app.episim.datamonitoring.charts.ChartController;
import sim.app.episim.datamonitoring.dataexport.DataExportController;
import sim.app.episim.model.ModelController;
import sim.display.ConsoleHack;


public class EpisimMenuBarFactory {
	
	public enum EpisimMenu {
		FILE_MENU("File"), 
		CHART_MENU("Charting"), 
		DATAEXPORT_MENU("Data Export"), 
		INFO_MENU("Info"),
		WINDOWS_MENU("Windows");
		
		private String name;
		private EpisimMenu(String _name){ this.name = _name;}
		public String toString(){ return name;}
	
	}
	public enum EpisimMenuItem {
		
		SET_SNAPSHOT_PATH("Set Tissue-Snaphot-Path"),
		LOAD_SNAPSHOT("Load Tissue-Snaphot"),
		OPEN_MODEL_FILE("Open Episim-Cell-Model"),
		CLOSE_MODEL_FILE("Close Episim-Cell-Model"),
		BUILD_MODEL_ARCHIVE("Build Episim-Model-Archive"),
		
		EDIT_CHART_SET("Edit Chart-Set"),
		LOAD_CHART_SET("Load Chart-Set"),
		NEW_CHART_SET("New Chart-Set"),
		CLOSE_CHART_SET("Close Chart-Set"),
		SELECT_DEFAULT_CHARTS("Select Episim Default-Charts"),		
		
		EDIT_DATA_EXPORT("Edit Loaded Data-Export-Definition-Set"),
		LOAD_DATA_EXPORT("Load Data-Export-Definition-Set"),
		NEW_DATA_EXPORT("New Data-Export-Definition-Set"),
		CLOSE_DATA_EXPORT("Close Loaded Data-Export-Definition-Set"),
		
		ABOUT_MASON("About MASON"),
		
		AUTO_ARRANGE_WINDOWS("Auto-Arrange Windows");
		
		private String name;
		private EpisimMenuItem(String _name){ this.name = _name;}
		public String toString(){ return name;}
	}
		
	
	private JMenuBar menuBar;
	
	
	
	private EpidermisSimulator simulator;
	
	public EpisimMenuBarFactory(EpidermisSimulator simulator){
		if(simulator == null) throw new IllegalArgumentException("Epidermis Simulator must not be null");
		else this.simulator = simulator;
		
		menuBar = new JMenuBar();
		
		buildFileMenu(menuBar);		
		buildChartsMenu(menuBar);		
		buildDataExportMenu(menuBar);		
		buildWindowsMenu(menuBar);		
		buildInfoMenu(menuBar);
		
		simulator.getMainFrame().setJMenuBar(menuBar);
	}
	
	
	
	private void buildFileMenu(JMenuBar menuBar){
		//--------------------------------------------------------------------------------------------------------------
		//Men� File
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
		JMenuItem menuItemSetSnapshotPath = new JMenuItem(EpisimMenuItem.SET_SNAPSHOT_PATH.toString());
		menuItemSetSnapshotPath.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				simulator.setSnapshotPath();				
			}
			
		});
		JMenuItem menuItemLoadSnapshot = new JMenuItem(EpisimMenuItem.LOAD_SNAPSHOT.toString());
		menuItemLoadSnapshot.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				simulator.loadSnapshot();				
			}
			
		});
		JMenuItem menuItemBuild = new JMenuItem(EpisimMenuItem.BUILD_MODEL_ARCHIVE.toString());
		menuItemBuild.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				simulator.buildModelArchive();				
			}
			
		});
		
		menuItemBuild.setVisible(false);
		
		menuItemLoadSnapshot.setEnabled(true);
		
		fileMenu.add(menuItemOpen);
		fileMenu.add(menuItemSetSnapshotPath);
		fileMenu.add(menuItemLoadSnapshot);
		//fileMenu.addSeparator();
		fileMenu.add(menuItemBuild);
		fileMenu.addSeparator();
		fileMenu.add(menuItemClose);
		
		menuBar.add(fileMenu);
	}
	
	
	private void buildChartsMenu(JMenuBar menuBar){
		//--------------------------------------------------------------------------------------------------------------
		// Men� Charts
		//--------------------------------------------------------------------------------------------------------------
		
		JMenu chartMenu = new JMenu(EpisimMenu.CHART_MENU.toString());
		chartMenu.setEnabled(false);
		
		JMenuItem menuItemNewChartSet = new JMenuItem(EpisimMenuItem.NEW_CHART_SET.toString());
		menuItemNewChartSet.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				boolean success = ChartController.getInstance().showNewChartSetDialog(simulator.getMainFrame());
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
					ChartController.getInstance().showEditChartSetDialog(simulator.getMainFrame());
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
				ChartController.getInstance().showEditChartSetDialog(simulator.getMainFrame());
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
				
				ChartController.getInstance().showDefaultChartsSelectionDialog(simulator.getMainFrame());
				
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
		// Men� DataExport
		//--------------------------------------------------------------------------------------------------------------
		
		JMenu dataExportMenu = new JMenu(EpisimMenu.DATAEXPORT_MENU.toString());
		dataExportMenu.setEnabled(false);
		
		JMenuItem menuItemNewDataExport = new JMenuItem(EpisimMenuItem.NEW_DATA_EXPORT.toString());
		menuItemNewDataExport.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				boolean success = true;
				success = DataExportController.getInstance().showNewDataExportDefinitionSetDialog(simulator.getMainFrame());
				if(success){
					getEpisimMenuItem(EpisimMenuItem.EDIT_DATA_EXPORT).setEnabled(true);
					getEpisimMenuItem(EpisimMenuItem.CLOSE_DATA_EXPORT).setEnabled(true);
					getEpisimMenuItem(EpisimMenuItem.NEW_DATA_EXPORT).setEnabled(false);
					getEpisimMenuItem(EpisimMenuItem.LOAD_DATA_EXPORT).setEnabled(false);
					simulator.getStatusbar().setMessage("Loaded Data Export: "+ DataExportController.getInstance().getActLoadedDataExportsName());
				}
				
			}
			
		});
		
		JMenuItem menuItemLoadDataExport = new JMenuItem(EpisimMenuItem.LOAD_DATA_EXPORT.toString());
		menuItemLoadDataExport.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				boolean success = DataExportController.getInstance().loadDataExportDefinition(simulator.getMainFrame());
				
				if(success){ 
					DataExportController.getInstance().showEditDataExportDefinitionDialog(simulator.getMainFrame());
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
		
		JMenuItem menuItemEditDataExport = new JMenuItem(EpisimMenuItem.EDIT_DATA_EXPORT.toString());
		menuItemEditDataExport.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				DataExportController.getInstance().showEditDataExportDefinitionDialog(simulator.getMainFrame());
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
				simulator.getStatusbar().setMessage("");
			}
			
		});
		menuItemCloseDataExport.setEnabled(false);
		
		dataExportMenu.add(menuItemNewDataExport);
		dataExportMenu.add(menuItemLoadDataExport);
		dataExportMenu.add(menuItemEditDataExport);
		dataExportMenu.addSeparator();
		dataExportMenu.add(menuItemCloseDataExport);
		menuBar.add(dataExportMenu);
	}
	
	private void buildInfoMenu(JMenuBar menuBar){
		//--------------------------------------------------------------------------------------------------------------
		// Men� Info
		//--------------------------------------------------------------------------------------------------------------
		
		JMenu infoMenu = new JMenu(EpisimMenu.INFO_MENU.toString());
		
		JMenuItem menuItemAboutMason = new JMenuItem(EpisimMenuItem.ABOUT_MASON.toString());
		
		menuItemAboutMason.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				ConsoleHack.showAbout();
			}
			
		});
		
		infoMenu.add(menuItemAboutMason);
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