package sim.app.episim.util;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
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
import sim.app.episim.tissue.TissueType;
import episiminterfaces.*;

public class TissueCellDataFieldsInspector {
	
	public interface ParameterSelectionListener{
		void parameterWasSelected();
	}
	
	
	private Set<String> nameRedundancyCheckSet;
	private Set<ParameterSelectionListener> parameterSelectionListener;
	
	private Set<String> overallVarNameSet;
	private Map<String, CellType> cellTypesMap;
	private Map<String, TissueType> tissueTypesMap;
	private Map<String, String> overallMethodCallMap;
	private Map<String, Method> overallMethodMap;
	private Map<String, String> prefixMap;
	private SortedJList cellTypeList;
	private SortedJList tissueTypeList;
	private SortedJList cellParameterList;
	private SortedJList tissueParameterList;
	private JPanel parametersPanel;
	private Set<String> markerPrefixes;
	private Set<Class<?>> validTypes;
	
	private Set<Class<?>> requiredClasses;
	
	private TissueType inspectedTissue;
	
	private String actualSelectedParameter = "";
	
	
	public TissueCellDataFieldsInspector(TissueType tissue, Set<String> markerPrefixes, Set<Class<?>> validTypes) {
			this.markerPrefixes = markerPrefixes;
			this.validTypes = validTypes;
			this.inspectedTissue = tissue;
			Comparator<String> stringComparator = new Comparator<String>(){

				public int compare(String o1, String o2) {
	           
	            return o1.compareTo(o2);
            }};
			tissueTypeList = new SortedJList(stringComparator);
			cellTypeList = new SortedJList(stringComparator);
			cellParameterList = new SortedJList(stringComparator);
			tissueParameterList = new SortedJList(stringComparator);
			
			this.cellParameterList.addMouseListener(new MouseAdapter() {

				public void mouseClicked(MouseEvent e) {

					if((cellParameterList.getSelectedIndex() != -1) && e.getClickCount() == 2){
						
						String name = cellTypeList.getSelectedValue() + "."  + cellParameterList.getSelectedValue();
						String alternName = cellTypeList.getSelectedValue()+Names.CELLDIFFMODEL + "."  + cellParameterList.getSelectedValue();
						
						if(!prefixMap.keySet().contains(name) &&prefixMap.keySet().contains(alternName)){
							
							actualSelectedParameter = prefixMap.get(alternName) + alternName;
							
						}
						else if(prefixMap.keySet().contains(name) &&!prefixMap.keySet().contains(alternName)){
							
							actualSelectedParameter = prefixMap.get(name) + name;
							
						}
						
						notifyAllParameterSelectionListener();
					}
				}
			});
		   
			this.tissueParameterList.addMouseListener(new MouseAdapter() {

				public void mouseClicked(MouseEvent e) {

					if((tissueParameterList.getSelectedIndex() != -1) && e.getClickCount() == 2){
						
						String name = tissueTypeList.getSelectedValue() + "."  + tissueParameterList.getSelectedValue();
						actualSelectedParameter = prefixMap.get(name) + name;
						notifyAllParameterSelectionListener();
					}
				}
			});
			
			
			
			
			
			parametersPanel = new JPanel(new BorderLayout(0,0));
			
			parameterSelectionListener = new HashSet<ParameterSelectionListener>();
			
			if(tissue == null) throw new IllegalArgumentException("Tissue was null!");
			buildCellTypesMap();
			buildTissueTypesMap();
			buildOverallVarNameSetAndMethodCallMap(this.cellTypesMap);
			buildTissueList();
			buildCellTypeList();
			
	}
	
	private void buildCellTypesMap(){
		this.cellTypesMap = new HashMap<String, CellType>();
		for(Class<?extends CellType> actCellType : this.inspectedTissue.getRegisteredCellTypes()){
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
	
	private void buildOverallVarNameSetAndMethodCallMap(Map<String, CellType> cellTypes) {
		String parameterName = "";
		this.overallVarNameSet = new HashSet<String>();
		this.requiredClasses = new HashSet<Class<?>>();
		this.overallMethodCallMap = new HashMap<String, String>();
		this.overallMethodMap = new HashMap<String, Method>();
		this.prefixMap = new HashMap<String, String>();
		Set<String> cellTypeNames = cellTypes.keySet();
		for(String actCellTypeName : cellTypeNames){
			CellType actClass = cellTypes.get(actCellTypeName);

			for(Method actMethod : actClass.getParameters()){
				processMethodForVarNameSetAndMethodCallMap(actCellTypeName, actMethod);
			}
		}
		if(this.inspectedTissue != null){
			for(Method actMethod : inspectedTissue.getParameters()){
				processMethodForVarNameSetAndMethodCallMap(this.inspectedTissue.getTissueName(), actMethod);
			}
		}
	}
	
	public void resetRequiredClasses(){ 
		
		this.requiredClasses.clear(); 
	}
	
	private void processMethodForVarNameSetAndMethodCallMap(String firstName, Method actMethod){
		String parameterName="";
		if(EpisimCellDiffModel.class.isAssignableFrom(actMethod.getDeclaringClass())) firstName += Names.CELLDIFFMODEL;
		if(isValidReturnType(actMethod.getReturnType())){
			parameterName=getParameterName(actMethod.getName());
			
			if(actMethod.getReturnType() != null){ 
				if(Double.TYPE.isAssignableFrom(actMethod.getReturnType()) || Integer.TYPE.isAssignableFrom(actMethod.getReturnType()) || Long.TYPE.isAssignableFrom(actMethod.getReturnType())){ 
					firstName = Names.NUMBERPREFIX + firstName;
				}
				else if(Boolean.TYPE.isAssignableFrom(actMethod.getReturnType())){ 
					firstName = Names.BOOLEANPREFIX + firstName;
				}
			}
			
			if(parameterName != null){
				String finalName = getRedundancyCheckedName(firstName + "." + parameterName);
				
				String methodCallName = Names.convertClassToVariable(actMethod.getDeclaringClass().getSimpleName()) + "."+ actMethod.getName()+"()";
				this.prefixMap.put(finalName.substring(Names.PREFIXLENGTH), finalName.substring(0, Names.PREFIXLENGTH));
				this.overallMethodCallMap.put(finalName,methodCallName);
				this.overallMethodMap.put(finalName,actMethod);
				this.overallVarNameSet.add(finalName);
			}
		}
	}
	
	public void addParameterSelectionListener(ParameterSelectionListener listener){
		this.parameterSelectionListener.add(listener);
	}
	
	public void addRequiredClassForIdentifier(String identifier){
		Method method = overallMethodMap.get(identifier);
		if(method != null) this.requiredClasses.add(method.getDeclaringClass());
	}
	
	public boolean checkIfIdentifierIsGlobalParameter(String identifier){
		Class<?> identifiersClass = overallMethodMap.get(identifier).getDeclaringClass();
		if(EpisimCellDiffModel.class.isAssignableFrom(identifiersClass)
				|| EpisimMechanicalModel.class.isAssignableFrom(identifiersClass)
				|| CellType.class.isAssignableFrom(identifiersClass)) return false;
		return true;		
	}
	
	public boolean checkIfIdentifierHasBooleanType(String identifier){
		return Boolean.TYPE.isAssignableFrom(overallMethodMap.get(identifier).getReturnType());
		
				
	}
	
	public boolean isIdentifier(String str){
		if(overallMethodMap.get(str) == null) return false;
		else return true;
	}
	
	public Set<Class<?>> getRequiredClasses(){ return ObjectManipulations.cloneObject(this.requiredClasses);}
	
	private String getRedundancyCheckedName(String name){
		
		String newName = name;
		if(overallVarNameSet.contains(newName)){
			for(int i = 2; overallVarNameSet.contains(newName); i++) newName = name.concat(""+i);
		}
		return newName;
	}

	private String getParameterName(String paramName) {
		
		for(String actPrefix : markerPrefixes){
			if(paramName.startsWith(actPrefix)) return paramName.substring(actPrefix.length());
		}
			return null;
	}

	private void buildParametersList(SortedJList list, String cellOrTissueTypeName) {

		list.removeAll();
		for(String actName : overallVarNameSet){
				String[] subNames = actName.split("\\.");
				if(subNames.length >= 2 && subNames[0].substring(Names.PREFIXLENGTH).equals(cellOrTissueTypeName)){
					list.add(subNames[1]);
				}
				else if(subNames.length >= 2 && subNames[0].substring(Names.PREFIXLENGTH).equals(cellOrTissueTypeName+Names.CELLDIFFMODEL)){
					list.add(subNames[1]);
				}
		}
		

	}
	
	private void buildTissueList() {
		
		tissueTypeList.removeAll();
		for(String actTissueName : this.tissueTypesMap.keySet()){			
			tissueTypeList.add(actTissueName);
		}
		
		

	}
	
	private void buildCellTypeList() {
	
		this.cellTypeList.removeAll();
		for(String actTissueName : this.cellTypesMap.keySet()){

			
			this.cellTypeList.add(actTissueName);

		}
		

	}
	
	

	private boolean isValidReturnType(Class<?> cls) {

		

		return validTypes.contains(cls);
	}

	
	public JPanel getVariableListPanel(){ return buildVariableListPanel();}
	
	public Set<String> getOverallVarNameSet(){ return this.overallVarNameSet;}
	
	
	public boolean hasCellTypeConflict(Set<String> varNames){
		String foundCellTypeName = null;
		Set<String> cellTypeClassNames = new HashSet<String>();
		//Class-Based Check
		for(CellType actType: this.cellTypesMap.values()) cellTypeClassNames.add(actType.getClass().getSimpleName().toLowerCase());
		for(String actVarName:varNames){
			if(cellTypeClassNames.contains(getMethodCallStrForVarName(actVarName).split("\\.")[0].toLowerCase())){ 
				if(foundCellTypeName== null)foundCellTypeName=getMethodCallStrForVarName(actVarName).split("\\.")[0].toLowerCase();
				else if(foundCellTypeName.equals(getMethodCallStrForVarName(actVarName).split("\\.")[0].toLowerCase()))continue;
				else return true;
			}
		}
		
		//Celltypename-Based Check
		HashSet<String> cellTypeNameSet = new HashSet<String>();  
		for(String actVarName : varNames){
			String cellTypeName = removeCellDiffModel(actVarName.split("\\.")[0].toLowerCase().substring(Names.PREFIXLENGTH));
			if(!cellTypeNameSet.contains(cellTypeName)){ 
				if(cellTypeNameSet.size() > 0){
					return true;
				}
				else cellTypeNameSet.add(cellTypeName);
			}
			
		}
		return false;
	}
	
	private String removeCellDiffModel(String name){
		if(name.endsWith(Names.CELLDIFFMODEL.toLowerCase())){
			return name.substring(0, name.length()- Names.CELLDIFFMODEL.length());
		}
		return name;
	}

	public String getMethodCallStrForVarName(String varName){ return this.overallMethodCallMap.get(varName);}
	
	private JPanel buildVariableListPanel() {

		final JPanel listPanel1 = new JPanel(new GridLayout(1, 2, 5, 5));
		final JPanel listPanel2 = new JPanel(new GridLayout(1, 2, 5, 5));
		

		cellTypeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tissueTypeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		cellParameterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tissueParameterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		tissueTypeList.addListSelectionListener(new ListSelectionListener() {
			
			public void valueChanged(ListSelectionEvent e){				
				if((e.getValueIsAdjusting() != false) && tissueTypeList.getSelectedIndex() != -1){
					buildCellTypeList();
					cellTypeList.validate();
					cellTypeList.repaint();
					buildParametersList(tissueParameterList, inspectedTissue.getTissueName());
					parametersPanel.removeAll();
					parametersPanel.add(tissueParameterList, BorderLayout.CENTER);
					listPanel1.validate();
					listPanel1.repaint();
				}				
			}
		});
		tissueTypeList.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				if((tissueTypeList.getSelectedIndex() != -1) && e.getClickCount() == 1){
					buildCellTypeList();
					cellTypeList.validate();
					cellTypeList.repaint();
					buildParametersList(tissueParameterList, inspectedTissue.getTissueName());
					parametersPanel.removeAll();
					parametersPanel.add(tissueParameterList, BorderLayout.CENTER);
					listPanel1.validate();
					listPanel1.repaint();
				}
			}
		});
		cellTypeList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {

				if((e.getValueIsAdjusting() != false) && cellTypeList.getSelectedIndex() != -1){

					buildParametersList(cellParameterList, cellTypesMap.get(((String) cellTypeList.getSelectedValue())).getCellName());
					parametersPanel.removeAll();
					parametersPanel.add(cellParameterList, BorderLayout.CENTER);
					listPanel1.validate();
					listPanel1.repaint();
				}
			}

		});
		
		

		cellParameterList.setToolTipText("double-click to select!");
		tissueParameterList.setToolTipText("double-click to select!");

		JScrollPane tissueListScroll = new JScrollPane(tissueTypeList);
		JScrollPane cellListScroll = new JScrollPane(cellTypeList);
		JScrollPane parameterListScroll = new JScrollPane(parametersPanel);

		tissueListScroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Tissue Types"),
		      BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		cellListScroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Cell Types"),
		      BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		parameterListScroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Parameters"),
		      BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		
		listPanel2.add(tissueListScroll);
		listPanel2.add(cellListScroll);
		listPanel1.add(listPanel2);
		listPanel1.add(parameterListScroll);

		listPanel1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Variables"),
		      BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		return listPanel1;

	}
	
	public String getActualSelectedParameter(){ return this.actualSelectedParameter; }

	/*
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
*/
	
   public Map<String, CellType> getCellTypesMap() {
   
   	return cellTypesMap;
   }

	
   public Map<String, TissueType> getTissueTypesMap() {
   
   	return tissueTypesMap;
   }
   
   private void notifyAllParameterSelectionListener(){
   	for(ParameterSelectionListener listener : this.parameterSelectionListener) listener.parameterWasSelected();
   }
   
}
