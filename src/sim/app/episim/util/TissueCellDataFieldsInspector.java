package sim.app.episim.util;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import sim.app.episim.CellType;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.TissueType;


public class TissueCellDataFieldsInspector {

	private Set<String> varNameSet;
	private Map<String, CellType> cellTypesMap;
	private Map<String, TissueType> tissueTypesMap;
	
	private JList cellTypeList;
	private JList tissueTypeList;
	private JList cellParameterList;
	private JList tissueParameterList;
	private JPanel parametersPanel;
	private Set<String> markerPrefixes;
	private Set<Class<?>> validTypes;
	
	private TissueType inspectedTissue;
	
	public TissueCellDataFieldsInspector(TissueType tissue, Set<String> markerPrefixes, Set<Class<?>> validTypes) {
			this.markerPrefixes = markerPrefixes;
			this.validTypes = validTypes;
			this.inspectedTissue = tissue;
			
			tissueTypeList = new JList();
			cellTypeList = new JList();
			cellParameterList = new JList();
			tissueParameterList = new JList();
			
			parametersPanel = new JPanel(new BorderLayout(0,0));
			if(tissue == null) throw new IllegalArgumentException("Tissue was null!");
			buildCellTypesMap();
			buildTissueTypesMap();
			buildVarNameSet(this.cellTypesMap);
			buildTissueList();
			buildCellTypeList();
			
	}
	
	private void buildCellTypesMap(){
		this.cellTypesMap = new HashMap<String, CellType>();
		for(Class<?extends CellType> actCellType : this.inspectedTissue.getRegiseredCellTypes()){
			try{
	        CellType cellType =  actCellType.newInstance();
	        cellTypesMap.put(cellType.getCellName(), cellType);
         }
         catch (InstantiationException e){
	         ExceptionDisplayer.getInstance().displayException(e);
         }
         catch (IllegalAccessException e){
         	ExceptionDisplayer.getInstance().displayException(e);
         }
		}
	}
	
	private void buildTissueTypesMap(){
		this.tissueTypesMap = new HashMap<String, TissueType>();
		if(this.inspectedTissue != null) tissueTypesMap.put(this.inspectedTissue.getTissueName(), this.inspectedTissue);

	}
	
	private void buildVarNameSet(Map<String, CellType> cellTypes) {

		this.varNameSet = new HashSet<String>();
		Set<String> cellTypeNames = cellTypes.keySet();
		for(String actCellTypeName : cellTypeNames){
			CellType actClass = cellTypes.get(actCellTypeName);

			for(Method actMethod : actClass.getParameters())
				varNameSet.add(actCellTypeName + "." + getParameterName(actMethod.getName()));
		}
	}

	private String getParameterName(String paramName) {
		
		for(String actPrefix : markerPrefixes){
			if(paramName.startsWith(actPrefix)) return paramName.substring(actPrefix.length());
		}
			return null;
	}

	private void buildParametersList(JList list, List<Method> methods) {

		DefaultListModel listModel = new DefaultListModel();
		for(Method actMethod : methods){

			if(isValidReturnType(actMethod.getReturnType()))
				listModel.addElement(getParameterName(actMethod.getName()));

		}
		list.setModel(listModel);

	}
	
	private void buildTissueList() {
		
		DefaultListModel listModel = new DefaultListModel();
		for(String actTissueName : this.tissueTypesMap.keySet()){

			
				listModel.addElement(actTissueName);

		}
		tissueTypeList.setModel(listModel);

	}
	
	private void buildCellTypeList() {
	
		DefaultListModel listModel = new DefaultListModel();
		for(String actTissueName : this.cellTypesMap.keySet()){

			
				listModel.addElement(actTissueName);

		}
		this.cellTypeList.setModel(listModel);

	}
	
	

	private boolean isValidReturnType(Class<?> cls) {

		

		return validTypes.contains(cls);
	}

	
	public JPanel getVariableListPanel(){ return buildVariableListPanel();}
	
	public Set<String> getVarNameSet(){ return this.varNameSet;}

	private JPanel buildVariableListPanel() {

		JPanel listPanel = new JPanel(new GridLayout(1, 3, 5, 5));
		

		cellTypeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tissueTypeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		cellParameterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tissueParameterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		tissueTypeList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {

				if((e.getValueIsAdjusting() != false) && tissueTypeList.getSelectedIndex() != -1){
					buildCellTypeList();
					cellTypeList.validate();
					cellTypeList.repaint();
					buildParametersList(tissueParameterList, inspectedTissue.getParameters());
					parametersPanel.removeAll();
					parametersPanel.add(tissueParameterList, BorderLayout.CENTER);
					parametersPanel.validate();
					parametersPanel.repaint();
				}
			}

		});
		cellTypeList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {

				if((e.getValueIsAdjusting() != false) && cellTypeList.getSelectedIndex() != -1){

					buildParametersList(cellParameterList, cellTypesMap.get(((String) cellTypeList.getSelectedValue())).getParameters());
					parametersPanel.removeAll();
					parametersPanel.add(cellParameterList, BorderLayout.CENTER);
					parametersPanel.validate();
					parametersPanel.repaint();
				}
			}

		});
		

		cellParameterList.setToolTipText("double-click to select!");
		tissueParameterList.setToolTipText("double-click to select!");

		JScrollPane tissueListScroll = new JScrollPane(tissueTypeList);
		JScrollPane cellListScroll = new JScrollPane(cellTypeList);
		JScrollPane parameterListScroll = new JScrollPane(parametersPanel);

		listPanel.add(tissueListScroll);
		listPanel.add(cellListScroll);
		listPanel.add(parameterListScroll);

		listPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Variables"),
		      BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		return listPanel;

	}

	
   public JList getCellParameterList() {
   
   	return cellParameterList;
   }
   public JList getCellTypeList() {
      
   	return cellTypeList;
   }
   public JList getTissueTypeList() {
      
   	return tissueTypeList;
   }
   public JList getTissueParameterList() {
      
   	return tissueParameterList;
   }

}
