package sim.app.episim.datamonitoring;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;

import sim.app.episim.datamonitoring.calc.CalculationAlgorithmServer;
import sim.app.episim.util.Names;
import sim.app.episim.util.SortedJList;


public class CalculationAlgorithmSelectionPanel {
	
	public static final int ONLYONEDIMALGORITHMS = 1;
	public static final int ALLALGORITHMS = 2;
	
	
	public interface AlgorithmSelectionListener{
		void algorithmWasSelected();
		void noAlgorithmIsSelected();
	}
	
	
	private SortedJList algorithmList;
	private JTextField algorithmName;
	private JTextField algorithmType;
	private JTextPane algorithmDescription;
	private JPanel algorithmSelectionPanel;
	private JPanel algorithmDescriptionPanel;
	private Set<AlgorithmSelectionListener> listeners;
	
	
	private Map<Integer, CalculationAlgorithmDescriptor>calculationAlgorithmMap;
	private Map<String, Integer> calculationAlgorithmNameIDMap;
	
	
	private int role;
	
	public CalculationAlgorithmSelectionPanel(int role){
		this.role = role;
		listeners = new HashSet<AlgorithmSelectionListener>();
		algorithmSelectionPanel= new JPanel(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
	  	  
	   c.anchor =GridBagConstraints.NORTHWEST;
	   c.fill = GridBagConstraints.BOTH;
	   c.weightx = 0.3;
	   c.weighty = 0.4;
	   c.insets = new Insets(10,10,10,10);
	   c.gridwidth = GridBagConstraints.RELATIVE;
	   algorithmSelectionPanel.add(buildAlgorithmSelectionPanel(), c);
	 
	   // empty Panel as placeholder  
	   c.anchor =GridBagConstraints.NORTHWEST;
	   c.fill = GridBagConstraints.BOTH;
	   c.weightx = 0.7;
	   c.weighty = 0.4;
	   c.insets = new Insets(10,10,10,10);
	   c.gridwidth = GridBagConstraints.REMAINDER;
	   algorithmSelectionPanel.add(new JPanel(), c);
	   
	   
	   
	   c.anchor =GridBagConstraints.NORTHWEST;
	   c.fill = GridBagConstraints.BOTH;
	   c.weightx = 1;
	   c.weighty = 0.6;
	   c.insets = new Insets(10,10,10,10);
	   c.gridwidth = GridBagConstraints.REMAINDER;
	   algorithmDescriptionPanel = buildAlgorithmDescriptionPanel();
	   algorithmSelectionPanel.add(algorithmDescriptionPanel, c);
	   algorithmDescriptionPanel.setVisible(false);
      
   }
	public JPanel getCalculationAlgorithmSelectionPanel(){ return this.algorithmSelectionPanel; }
	
	private void buildDescriptorMap(List<CalculationAlgorithmDescriptor> descriptors){
		calculationAlgorithmMap = new HashMap<Integer, CalculationAlgorithmDescriptor>();
		calculationAlgorithmNameIDMap = new HashMap<String, Integer>();
		for(CalculationAlgorithmDescriptor descriptor : descriptors){
			if(this.role == ONLYONEDIMALGORITHMS){
				if(descriptor.getType() == CalculationAlgorithm.ONEDIMRESULT){
					calculationAlgorithmMap.put(descriptor.getID(), descriptor);
					calculationAlgorithmNameIDMap.put(getUniqueCalculationAlgorithmName(descriptor.getName()), descriptor.getID());
				}
			}
			else{
				calculationAlgorithmMap.put(descriptor.getID(), descriptor);
				calculationAlgorithmNameIDMap.put(getUniqueCalculationAlgorithmName(descriptor.getName()), descriptor.getID());
			}
		}
	}
	
	private String getUniqueCalculationAlgorithmName(String name){
		int i = 1;
		String tmpName = name;
		while(calculationAlgorithmNameIDMap.keySet().contains(tmpName)){
			tmpName = name + i;
			i++;
		}
		return tmpName;
	}
	
	
	private JPanel buildAlgorithmSelectionPanel(){
		JPanel algorithmSelectionListPanel = new JPanel(new BorderLayout());
		Comparator<String> stringComparator = new Comparator<String>(){
			public int compare(String o1, String o2) { return o1.compareTo(o2); }
		};
      algorithmList = new SortedJList(stringComparator);
      algorithmList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      buildDescriptorMap(CalculationAlgorithmServer.getInstance().getCalculationAlgorithmDescriptors());
      for(String name : this.calculationAlgorithmNameIDMap.keySet()) this.algorithmList.add(name);
      
      
      algorithmList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e){
				if(algorithmList.getSelectedIndex() != -1){
					if(algorithmList.getSelectedValue() instanceof String) updateDescription(((String) algorithmList.getSelectedValue()));
					algorithmDescriptionPanel.setVisible(true);
					notifyAllListeners(true);
				}
				
				if(algorithmList.getSelectedIndex() == -1){
					algorithmDescriptionPanel.setVisible(false);
					notifyAllListeners(false);
				}
			}

		});
      algorithmSelectionListPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Available Algorithms"),
		      BorderFactory.createEmptyBorder(5, 5, 5, 5)));
      algorithmSelectionListPanel.add(new JScrollPane(algorithmList), BorderLayout.CENTER);
            
      return algorithmSelectionListPanel;
	}
	
	
	private void updateDescription(String name){
		CalculationAlgorithmDescriptor descriptor = this.calculationAlgorithmMap.get(this.calculationAlgorithmNameIDMap.get(name));
		if(descriptor != null){
			this.algorithmName.setText(descriptor.getName());
			this.algorithmType.setText(Names.getCalculationAlgorithmTypeDescriptionForID(descriptor.getType()));
			this.algorithmDescription.setText(descriptor.getDescription());
		}
	}
	
	
	public void addAlgorithmSelectionListener(AlgorithmSelectionListener listener){
		this.listeners.add(listener);
	}
	
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(){
		if(algorithmList.getSelectedIndex() != -1){
			if(algorithmList.getSelectedValue() instanceof String) 
				return this.calculationAlgorithmMap.get(this.calculationAlgorithmNameIDMap.get((String) algorithmList.getSelectedValue()));
		}
		return null;
	}
	
	public void setSelectedAlgorithm(int id){
		String name = this.calculationAlgorithmMap.get(id).getName();
		updateDescription(name);
		for(int i = 0; i < this.algorithmList.getModel().getSize(); i++){
			Object obj = this.algorithmList.getModel().getElementAt(i);
			String listElem = null;
			if(obj instanceof String) listElem = (String) obj;
			if(listElem.equals(name)){
				this.algorithmList.setSelectedIndex(i);
				break;
			}
		}
		
	}
	
	private JPanel buildAlgorithmDescriptionPanel(){
		JPanel algorithmDescriptionPanel = new JPanel(new GridBagLayout());
		algorithmDescriptionPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Algorithm Description"),
		      BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		
		GridBagConstraints c = new GridBagConstraints();
	  	  
	   c.anchor =GridBagConstraints.NORTHWEST;
	   c.fill = GridBagConstraints.HORIZONTAL;
	   c.weightx = 0;
	   c.weighty = 0;
	   c.insets = new Insets(10,10,10,10);
	   c.gridwidth = GridBagConstraints.RELATIVE;
	   algorithmDescriptionPanel.add(new JLabel("Name:"), c);
	   
	   
	   this.algorithmName = new JTextField();
	   this.algorithmName.setEditable(false);
	   this.algorithmName.setEnabled(true);
	   this.algorithmName.setOpaque(false);
	   this.algorithmName.setBorder(null);
	   c.anchor =GridBagConstraints.NORTHWEST;
	   c.fill = GridBagConstraints.HORIZONTAL;
	   c.weightx = 1;
	   c.weighty = 0;
	   c.insets = new Insets(10,10,10,10);
	   c.gridwidth = GridBagConstraints.REMAINDER;
	   algorithmDescriptionPanel.add(this.algorithmName, c);
	   
	   
	   c.anchor =GridBagConstraints.NORTHWEST;
	   c.fill = GridBagConstraints.HORIZONTAL;
	   c.weightx = 0;
	   c.weighty = 0;
	   c.insets = new Insets(10,10,10,10);
	   c.gridwidth = GridBagConstraints.RELATIVE;
	   algorithmDescriptionPanel.add(new JLabel("Type:"), c);
	   
	   
	   this.algorithmType = new JTextField();
	   this.algorithmType.setEditable(false);
	   this.algorithmType.setEnabled(true);
	   this.algorithmType.setOpaque(false);
	   this.algorithmType.setBorder(null);
	   c.anchor =GridBagConstraints.NORTHWEST;
	   c.fill = GridBagConstraints.HORIZONTAL;
	   c.weightx = 1;
	   c.weighty = 0;
	   c.insets = new Insets(10,10,10,10);
	   c.gridwidth = GridBagConstraints.REMAINDER;
	   algorithmDescriptionPanel.add(this.algorithmType, c);
	   
	   
	   
	   
	   c.anchor =GridBagConstraints.NORTHWEST;
	   c.fill = GridBagConstraints.HORIZONTAL;
	   c.weightx = 0;
	   c.weighty = 1;
	   c.insets = new Insets(10,10,10,10);
	   c.gridwidth = GridBagConstraints.RELATIVE;
	   algorithmDescriptionPanel.add(new JLabel("Description:"), c);
	   
	   this.algorithmDescription = new JTextPane();
	   JScrollPane scroll = new JScrollPane(this.algorithmDescription);
	   scroll.setBorder(null);
		this.algorithmDescription.setEditable(false);
		this.algorithmDescription.setEnabled(true);
		this.algorithmDescription.setBorder(null);
		this.algorithmDescription.setOpaque(false);
		this.algorithmDescription.setFocusable(false);
		this.algorithmDescription.setMargin(new Insets(0,0,0,0));
	   c.anchor =GridBagConstraints.NORTHWEST;
	   c.fill = GridBagConstraints.BOTH;
	   c.weightx = 1;
	   c.weighty = 1;
	   c.insets = new Insets(10,10,10,10);
	   c.gridwidth = GridBagConstraints.REMAINDER;
	   algorithmDescriptionPanel.add(scroll, c);
		
		return algorithmDescriptionPanel;
	}
	
	
	
	private void notifyAllListeners(boolean algorithmSelected){
		if(algorithmSelected) for(AlgorithmSelectionListener listener : listeners) listener.algorithmWasSelected();
		else  for(AlgorithmSelectionListener listener : listeners) listener.noAlgorithmIsSelected();
	}
}
