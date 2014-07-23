package sim.app.episim.datamonitoring.dataexport;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;





import javax.swing.JTextField;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.gui.EpisimProgressWindow;
import sim.app.episim.gui.EpisimSimulator;
import sim.app.episim.gui.ExtendedFileChooser;
import sim.app.episim.gui.ImageLoader;
import sim.app.episim.gui.EpisimProgressWindow.EpisimProgressWindowCallback;
import sim.app.episim.persistence.SimulationStateFile;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.tissue.TissueType;
import sim.app.episim.tissue.UniversalTissue;


public class TissueSnapshotDataExportDialog extends JDialog {

	private EpisimSimulator simulator;
	private boolean tissueSnapshotLoadSuccessful=false;
	private boolean tissueDataExportDefinitionLoadSuccessful=false;
	
	private JLabel loadSnaphotLabel = new JLabel("Load EPISIM tissue simulation export (.xml) file: ");
	private JLabel loadSnapshotCompletedLabel = new JLabel();
	private JTextField snapshotPathTextField = new JTextField();
	private JButton loadDataExportButton = new JButton(" Load ");
	   
	private JLabel loadDataExportLabel = new JLabel("Load EPISIM data-export-definition (.ede) file: ");
	private JLabel loadDataExportCompletedLabel = new JLabel();
	private JTextField edePathTextField = new JTextField();	   
	private JButton loadSnapshotButton = new JButton(" Load ");
	
	private JLabel exportDataLabel = new JLabel("Extract simulation data:");
	private JButton startButton = new JButton(" Start ");
	
	private UniversalTissue state;
	
	public TissueSnapshotDataExportDialog(final Frame owner, boolean modal) {

	   super(owner, "Data Extraction From Tissue Simulation Snaphot", modal);
	   
	   ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	   JPanel mainPanel =new JPanel();
	   mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
	   getContentPane().add(mainPanel, BorderLayout.CENTER);
	  
	   JLabel titleLabel = new JLabel("<html><strong>Extract pre-processed simulation data from a stored EPISIM tissue-simulation-export file using an already defined EPISIM data export definition set</strong></html>");
	   
	  
	   
	   Box box = Box.createHorizontalBox();
	   box.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
	   box.add(titleLabel);
	   mainPanel.add(box);
	   
	   
	   box = Box.createHorizontalBox();
	   box.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
	  
	   box.add(loadSnaphotLabel);
	   box.add(Box.createHorizontalGlue());
	   mainPanel.add(box);
	   
	   box = Box.createHorizontalBox();
	   box.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
	   
	   JPanel fieldPanel = new JPanel(new BorderLayout());	  
	   snapshotPathTextField.setEditable(false);
	   fieldPanel.setAlignmentY(Component.TOP_ALIGNMENT);
	   fieldPanel.add(snapshotPathTextField,BorderLayout.NORTH);
	   box.add(fieldPanel);
	   
	   box.add(Box.createRigidArea(new Dimension(10,0)));
	   box.add(Box.createHorizontalGlue());
	   
	  
	   loadSnapshotButton.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				if(simulator != null){					
					loadTissueSimulationSnapshot();					   
				}
			}
		});
	   loadSnapshotButton.setAlignmentY(Component.TOP_ALIGNMENT);
	   box.add(loadSnapshotButton);
	   loadSnapshotCompletedLabel.setIcon(new ImageIcon(ImageLoader.class.getResource("completed-icon.png")));
	   loadSnapshotCompletedLabel.setVisible(false);
	   loadSnapshotCompletedLabel.setAlignmentY(Component.TOP_ALIGNMENT);
	   box.add(loadSnapshotCompletedLabel);
	   mainPanel.add(box);
	     
	   box = Box.createHorizontalBox();
	   box.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
	   loadDataExportLabel.setEnabled(false);
	   box.add(loadDataExportLabel);
	   box.add(Box.createHorizontalGlue());
	   mainPanel.add(box);
	   
	   box = Box.createHorizontalBox();
	   box.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
	   
	   fieldPanel = new JPanel(new BorderLayout());
	   edePathTextField.setEnabled(false);
	   edePathTextField.setEditable(false);
	   fieldPanel.setAlignmentY(Component.TOP_ALIGNMENT);
	   fieldPanel.add(edePathTextField,BorderLayout.NORTH);
	   box.add(fieldPanel);
	   
	   box.add(Box.createRigidArea(new Dimension(10,0)));
	   box.add(Box.createHorizontalGlue());
	   
	   
	   loadDataExportButton.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				loadDataExportDefinition();
				
			}
		});
	   loadDataExportButton.setAlignmentY(Component.TOP_ALIGNMENT);
	   loadDataExportButton.setEnabled(false);
	   box.add(loadDataExportButton);
	   loadDataExportCompletedLabel.setIcon(new ImageIcon(ImageLoader.class.getResource("completed-icon.png")));
	   loadDataExportCompletedLabel.setVisible(false);
	   loadDataExportCompletedLabel.setAlignmentY(Component.TOP_ALIGNMENT);
	   box.add(loadDataExportCompletedLabel);
	   
	   mainPanel.add(box);
	   mainPanel.add(Box.createVerticalGlue());
	   
	   box = Box.createHorizontalBox();
	   box.add(Box.createHorizontalGlue());
	   
	   exportDataLabel.setEnabled(false);
	   startButton.setEnabled(false);
	   
	   startButton.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {			
				extractSimulationDataFromSnapshot();				
			}
		});
	   
	   JButton cancelButton = new JButton("Cancel");
	   cancelButton.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e){
				cancelDataExport();
			}
		});
	   box.add(exportDataLabel);
	   box.add(Box.createRigidArea(new Dimension(15,0)));
	   box.add(startButton);
	   box.add(Box.createRigidArea(new Dimension(10,0)));
	   box.add(cancelButton);
	   mainPanel.add(box);
	   this.addWindowListener(new WindowAdapter(){
	   	public void windowClosing(WindowEvent e){
	   		cancelDataExport();
	   	}
	   });
	   this.setResizable(false);
	   setSize(450, 250);
		validate();
   }
	
	public void showDialog(EpisimSimulator simulator){
		this.simulator = simulator;
		tissueSnapshotLoadSuccessful=false;
		tissueDataExportDefinitionLoadSuccessful=false;
		centerMe();
		setVisible(true);
	}
	
	private void centerMe(){
		if(this.getParent() == null){
			Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
			this.setLocation(((int)(screenDim.getWidth() /2) - (this.getWidth()/2)), 
			((int)(screenDim.getHeight() /2) - (this.getHeight()/2)));
		}
		else{
			Dimension parentDim = this.getParent().getSize();
			this.setLocation(((int)(this.getParent().getLocation().getX()+((parentDim.getWidth() /2) - (this.getWidth()/2)))), 
			((int)(this.getParent().getLocation().getY()+((parentDim.getHeight() /2) - (this.getHeight()/2)))));
		}
	}
	
	
	private void loadTissueSimulationSnapshot(){
		if(simulator!= null && simulator.getMainFrame() instanceof JFrame){
			EpisimProgressWindowCallback cb = new EpisimProgressWindowCallback() {											
				public void taskHasFinished() {
					if(tissueSnapshotLoadSuccessful){
						loadDataExportLabel.setEnabled(true);
						edePathTextField.setEnabled(true);
						loadDataExportButton.setEnabled(true);
						loadSnapshotButton.setEnabled(false);
						loadSnapshotButton.setVisible(false);
						loadSnapshotCompletedLabel.setVisible(true);
						snapshotPathTextField.setText(SimulationStateFile.getTissueExportPath().getAbsolutePath());
						state=new UniversalTissue(System.currentTimeMillis());
						if(state instanceof TissueType) TissueController.getInstance().registerTissue(((TissueType) state));
					}
				}
				public void executeTask() {
					ExtendedFileChooser chooser = new ExtendedFileChooser("xml");
					chooser.setDialogTitle("Open EPISIM Tissue-Export");
					if(ExtendedFileChooser.APPROVE_OPTION == chooser.showOpenDialog((JFrame)simulator.getMainFrame())){
						tissueSnapshotLoadSuccessful= simulator.loadSimulationStateFile(chooser.getSelectedFile(),true,true);						
					}
				}
			};
			 EpisimProgressWindow.showProgressWindowForTask(this, "Load EPISIM Tissue Simulation Export...", cb);
			
		}
	}
	
	private void extractSimulationDataFromSnapshot(){	
		EpisimProgressWindowCallback cb = new EpisimProgressWindowCallback() {					
			public void taskHasFinished() {
				cancelDataExport();
			}
		public void executeTask() {
			try{
				state.simulateASingleDataExtractionStepForDataExport();
				DataExportController.getInstance().simulationWasStopped();
				JOptionPane.showMessageDialog(TissueSnapshotDataExportDialog.this, "Extraction of the simulation data was successful!", "Extraction Completed", JOptionPane.INFORMATION_MESSAGE);
			}
			catch(Exception e){
				JOptionPane.showMessageDialog(TissueSnapshotDataExportDialog.this, "Error occurred: Extraction of the simulation data was not sucessful!", "Error", JOptionPane.ERROR_MESSAGE);
				ExceptionDisplayer.getInstance().displayException(e);
			}
		}
	};
	 EpisimProgressWindow.showProgressWindowForTask(this, "Extracting data from EPISIM tissue simulation export...", cb);
		
		
		
	}
	
	private void loadDataExportDefinition(){
		if(simulator.getMainFrame() instanceof JFrame){
			EpisimProgressWindowCallback cb = new EpisimProgressWindowCallback() {											
				public void taskHasFinished() {
					if(tissueDataExportDefinitionLoadSuccessful){
						snapshotPathTextField.setEnabled(true);
						edePathTextField.setText(DataExportController.getInstance().getCurrentlyLoadedDataExportDefinitionSet().getAbsolutePath());
						exportDataLabel.setEnabled(true);
						loadDataExportButton.setEnabled(false);
						loadDataExportButton.setVisible(false);
						loadDataExportCompletedLabel.setVisible(true);
						startButton.setEnabled(true);
					}
				}
				public void executeTask() {
					tissueDataExportDefinitionLoadSuccessful = DataExportController.getInstance().loadDataExportDefinition((JFrame)simulator.getMainFrame());
				}
			};
			 EpisimProgressWindow.showProgressWindowForTask(this, "Load EPISIM Data Export Definition...", cb);
			
			
			
		}
		
	}
	
	private void cancelDataExport(){
		simulator.closeModelAfterSnapshotDataExport();
		this.setVisible(false);
		this.dispose();
	}
}
