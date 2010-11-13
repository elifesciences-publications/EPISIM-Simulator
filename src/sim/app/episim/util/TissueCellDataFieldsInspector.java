package sim.app.episim.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
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
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import sim.app.episim.CellType;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.model.ModelController;
import sim.app.episim.tissue.TissueType;
import episiminterfaces.*;

public class TissueCellDataFieldsInspector {
	
	public interface ParameterSelectionListener{
		void parameterWasSelected();
	}
	
	private enum ParameterLevel{TISSUE_LEVEL("Tissue Level"), CELLULAR_LEVEL("Cellular Level");
		private String level;
		private ParameterLevel(String level){			
			this.level = level;
		}
				
		public String toString(){ return level;}
	}
	
	
	private Set<String> nameRedundancyCheckSet;
	private Set<ParameterSelectionListener> parameterSelectionListener;
	
	private Set<String> overallVarOrConstantNameSet;
	private Map<String, EpisimCellType> cellTypesEnumMap;
	private Map<String, CellType> cellTypesClassesMap;
	private Map<String, EpisimDifferentiationLevel[]> diffLevelsMap;
	private Map<String, String> overallMethodCallMap;
	private Map<String, String> overallFieldCallMap;
	private Map<String, Method> overallMethodMap;
	private Map<String, Field> overallFieldMap;
	private Map<String, String> prefixMap;
	private SortedJList cellTypeList;
	private SortedJList diffLevelsList;
	private SortedJList cellParameterList;
	private SortedJList tissueParameterList;
	private SortedJList tissueConstantList;
	
	private Set<String> markerPrefixes;
	private Set<Class<?>> validTypes;
	
	private Set<Class<?>> requiredClasses;
	
	private TissueType inspectedTissue;
	
	private String actualSelectedParameterOrConstant = "";
	
	
	public TissueCellDataFieldsInspector(TissueType tissue, Set<String> markerPrefixes, Set<Class<?>> validTypes) {
			this.markerPrefixes = markerPrefixes;
			this.validTypes = validTypes;
			this.inspectedTissue = tissue;
					
			parameterSelectionListener = new HashSet<ParameterSelectionListener>();
			
			if(tissue == null) throw new IllegalArgumentException("Tissue was null!");
			buildCellTypesMaps();
			buildDiffLevelsMap();
			buildOverallVarNameSetMethodCallMapConstantNameSetAndFieldCallMap(this.cellTypesClassesMap);
			
			
			
	}
	
	private void buildCellTypesMaps(){
		this.cellTypesEnumMap = new HashMap<String, EpisimCellType>();
		this.cellTypesClassesMap = new HashMap<String, CellType>();
		for(EpisimCellType actCellType : ModelController.getInstance().getCellBehavioralModelController().getAvailableCellTypes()){	        
	        cellTypesEnumMap.put(actCellType.name(), actCellType);        
		}
		for(Class<? extends CellType> actType : this.inspectedTissue.getRegisteredCellTypes()){
	        CellType cellType  = null;
         try{
	         cellType = actType.newInstance();
	         cellTypesClassesMap.put(cellType.getCellName(), cellType);
         }
         catch (InstantiationException e){
	        ExceptionDisplayer.getInstance().displayException(e);
         }
         catch (IllegalAccessException e){
         	 ExceptionDisplayer.getInstance().displayException(e);
         }
	        
		}
	}
	
	private void buildDiffLevelsMap(){
		EpisimCellType[] cellTypes  = ModelController.getInstance().getCellBehavioralModelController().getAvailableCellTypes();
		this.diffLevelsMap = new HashMap<String, EpisimDifferentiationLevel[]>();
		for(EpisimCellType cellType : cellTypes){
			this.diffLevelsMap.put(cellType.name(), cellType.getDifferentiationLevel());
		}
	}
	
	private void buildOverallVarNameSetMethodCallMapConstantNameSetAndFieldCallMap(Map<String, CellType> cellTypes) {
		String parameterName = "";
		this.overallVarOrConstantNameSet = new HashSet<String>();
		this.requiredClasses = new HashSet<Class<?>>();
		this.overallMethodCallMap = new HashMap<String, String>();
		this.overallFieldCallMap = new HashMap<String, String>();
		this.overallMethodMap = new HashMap<String, Method>();
		this.overallFieldMap = new HashMap<String, Field>();
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
			for(Field actField : inspectedTissue.getContants()){
				processFieldForConstantNameSetAndFieldCallMap(this.inspectedTissue.getTissueName(), actField);
			}
		}
	}
	
	
	
	
	public void resetRequiredClasses(){ 
		
		this.requiredClasses.clear(); 
	}
	
	private void processMethodForVarNameSetAndMethodCallMap(String firstName, Method actMethod){
		String parameterName="";
		if(EpisimCellBehavioralModel.class.isAssignableFrom(actMethod.getDeclaringClass())) firstName += Names.CELLBEHAVIORALMODEL;
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
				String finalName = getRedundancyCheckedName(firstName + "." + parameterName, this.overallVarOrConstantNameSet);
				
				String methodCallName = Names.convertClassToVariable(actMethod.getDeclaringClass().getSimpleName()) + "."+ actMethod.getName()+"()";
				this.prefixMap.put(finalName.substring(Names.PREFIXLENGTH), finalName.substring(0, Names.PREFIXLENGTH));
				this.overallMethodCallMap.put(finalName,methodCallName);
				this.overallMethodMap.put(finalName,actMethod);
				this.overallVarOrConstantNameSet.add(finalName);
			}
		}
	}
	
	private void processFieldForConstantNameSetAndFieldCallMap(String firstName, Field actField){
		String constantName="";
		if(EpisimCellBehavioralModelGlobalParameters.class.isAssignableFrom(actField.getDeclaringClass())) firstName += Names.CELLBEHAVIORALMODEL;
		if(isValidReturnType(actField.getType())){
			constantName=actField.getName();
			
			if(actField.getType() != null){ 
				if(Double.TYPE.isAssignableFrom(actField.getType()) || Integer.TYPE.isAssignableFrom(actField.getType()) || Long.TYPE.isAssignableFrom(actField.getType())){ 
					firstName = Names.NUMBERPREFIX + firstName;
				}
				else if(Boolean.TYPE.isAssignableFrom(actField.getType())){ 
					firstName = Names.BOOLEANPREFIX + firstName;
				}
			}
			
			if(constantName != null){
				String finalName = getRedundancyCheckedName(firstName + "." + constantName, this.overallVarOrConstantNameSet);
				
				String fieldCallName = Names.convertClassToVariable(actField.getDeclaringClass().getSimpleName()) + "."+ actField.getName();
				this.prefixMap.put(finalName.substring(Names.PREFIXLENGTH), finalName.substring(0, Names.PREFIXLENGTH));
				this.overallFieldCallMap.put(finalName,fieldCallName);
				this.overallFieldMap.put(finalName,actField);
				this.overallVarOrConstantNameSet.add(finalName);
			}
		}
	}
	
	public void addParameterSelectionListener(ParameterSelectionListener listener){
		this.parameterSelectionListener.add(listener);
	}
	
	public void addRequiredClassForIdentifier(String identifier){
		Method method = overallMethodMap.get(identifier);
		if(method != null) this.requiredClasses.add(method.getDeclaringClass());
		else{
			Field field = overallFieldMap.get(identifier);
			if(field != null) this.requiredClasses.add(field.getDeclaringClass());
		}
	}
	
	public boolean checkIfIdentifierIsGlobalParameter(String identifier){
		Class<?> identifiersClass = overallMethodMap.get(identifier).getDeclaringClass();
		if(EpisimCellBehavioralModel.class.isAssignableFrom(identifiersClass)
				|| EpisimMechanicalModel.class.isAssignableFrom(identifiersClass)
				|| CellType.class.isAssignableFrom(identifiersClass)) return false;
		return true;		
	}
	
	public boolean checkIfIdentifierHasBooleanType(String identifier){
		return Boolean.TYPE.isAssignableFrom(overallMethodMap.get(identifier).getReturnType());
		
				
	}
	
	public boolean isIdentifier(String str){
		if(overallMethodMap.get(str) == null && overallFieldMap.get(str)==null) return false;
		else return true;
	}
	
	public Set<Class<?>> getRequiredClasses(){ return ObjectManipulations.cloneObject(this.requiredClasses);}
	
	private String getRedundancyCheckedName(String name, Set<String> nameSet){
		
		String newName = name;
		if(nameSet.contains(newName)){
			for(int i = 2; nameSet.contains(newName); i++) newName = name.concat(""+i);
		}
		return newName;
	}

	private String getParameterName(String paramName) {
		for(String actPrefix : markerPrefixes){
			if(paramName.startsWith(actPrefix)) return paramName.substring(actPrefix.length());
		}
		return null;
	}

	private void buildParametersOrConstantsList(SortedJList list, String cellOrTissueTypeName) {
		
		list.removeAll();
		for(String actName : overallVarOrConstantNameSet){
			if(((list == this.cellParameterList || list == this.tissueParameterList) && this.overallMethodMap.containsKey(actName))
					|| (list == this.tissueConstantList && this.overallFieldMap.containsKey(actName))){
				String[] subNames = actName.split("\\.");
				if(subNames.length >= 2 && subNames[0].substring(Names.PREFIXLENGTH).equals(cellOrTissueTypeName)){
					list.add(subNames[1]);
				}
				else if(subNames.length >= 2 && subNames[0].substring(Names.PREFIXLENGTH).equals(cellOrTissueTypeName+Names.CELLBEHAVIORALMODEL)){
					list.add(subNames[1]);
				}
			}
		}
	}
	
	private void buildDiffLevelsList(EpisimCellType cellType) {
		
		diffLevelsList.removeAll();
		for(EpisimDifferentiationLevel diffLevel : this.diffLevelsMap.get(cellType.name())){			
			diffLevelsList.add(diffLevel.name());
		}		

	}
	
	private void buildCellTypeList() {	
		this.cellTypeList.removeAll();
		for(String actCellTypeName : this.cellTypesEnumMap.keySet()){			
			this.cellTypeList.add(actCellTypeName);
		}
	}
	
	

	private boolean isValidReturnType(Class<?> cls) {
		return validTypes.contains(cls);
	}

	
	public JPanel getVariableListPanel(){
		
		JPanel variableMainPanel = new JPanel(new BorderLayout(10,10));
		variableMainPanel.add(buildTissueLevelAndCellLevelCombobox(), BorderLayout.NORTH);
		variableMainPanel.add(buildVariableListPanel(), BorderLayout.CENTER);
		
		variableMainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Model Parameters"),
		      BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		
		return variableMainPanel;
	}
	
	
	
	
	
	
	public Set<String> getOverallVarOrConstantNameSet(){ 		
		
		return this.overallVarOrConstantNameSet;
	}
	
	
	public boolean hasCellTypeConflict(Set<String> varNames){
		String foundCellTypeName = null;
		Set<String> cellTypeClassNames = new HashSet<String>();
		//Class-Based Check
		for(CellType actType: this.cellTypesClassesMap.values()) cellTypeClassNames.add(actType.getClass().getSimpleName());
		for(String actVarName:varNames){
			if(cellTypeClassNames.contains(firstLetterToUpperCase(getMethodOrFieldCallStrForVarOrConstantName(actVarName).split("\\.")[0]))){ 
				if(foundCellTypeName== null)foundCellTypeName=getMethodOrFieldCallStrForVarOrConstantName(actVarName).split("\\.")[0];
				else if(foundCellTypeName.equals(getMethodOrFieldCallStrForVarOrConstantName(actVarName).split("\\.")[0]))continue;
				else return true;
			}
		}
		
		
		
		//Celltypename-Based Check
		HashSet<String> cellTypeNameSet = new HashSet<String>();  
		for(String actVarName : varNames){
			String cellTypeName = removeCellBehavioralModel(actVarName.split("\\.")[0].substring(Names.PREFIXLENGTH));
			if(!cellTypeNameSet.contains(cellTypeName) && !this.inspectedTissue.getTissueName().equals(cellTypeName)){ 
				if(cellTypeNameSet.size() > 0){
					return true;
				}
				else cellTypeNameSet.add(cellTypeName);
			}
			
		}
		return false;
	}
	
	private String firstLetterToUpperCase(String text){
		if(text != null && text.length()>0){
			text = text.substring(0, 1).toUpperCase() + text.substring(1);
		}
		return text;
	}
	
	private String removeCellBehavioralModel(String name){
		if(name.endsWith(Names.CELLBEHAVIORALMODEL)){
			return name.substring(0, name.length()- Names.CELLBEHAVIORALMODEL.length());
		}
		return name;
	}

	public String getMethodOrFieldCallStrForVarOrConstantName(String varName){ 
		
		if(this.overallMethodCallMap.containsKey(varName))  return this.overallMethodCallMap.get(varName);
		else if(this.overallFieldCallMap.containsKey(varName))  return this.overallFieldCallMap.get(varName);
		return "";
	}
	
	private JPanel buildVariableListPanel() {
		
		Comparator<String> stringComparator = new Comparator<String>(){

			public int compare(String o1, String o2) {           
            return o1.compareTo(o2);
         }};
		diffLevelsList = new SortedJList(stringComparator);
		cellTypeList = new SortedJList(stringComparator);
		cellParameterList = new SortedJList(stringComparator);
		tissueParameterList = new SortedJList(stringComparator);
		tissueConstantList = new SortedJList(stringComparator);
		
		this.cellParameterList.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				if((cellParameterList.getSelectedIndex() != -1) && e.getClickCount() == 2){
					
					String name = cellTypeList.getSelectedValue() + "."  + cellParameterList.getSelectedValue();
					String alternName = cellTypeList.getSelectedValue()+Names.CELLBEHAVIORALMODEL + "."  + cellParameterList.getSelectedValue();
					
					if(!prefixMap.keySet().contains(name) &&prefixMap.keySet().contains(alternName)){
						
						actualSelectedParameterOrConstant = prefixMap.get(alternName) + alternName;
						
					}
					else if(prefixMap.keySet().contains(name) &&!prefixMap.keySet().contains(alternName)){
						
						actualSelectedParameterOrConstant = prefixMap.get(name) + name;
						
					}
					
					notifyAllParameterSelectionListener();
				}
			}
		});
	   
		this.tissueParameterList.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				if((tissueParameterList.getSelectedIndex() != -1) && e.getClickCount() == 2){
					
					String name = inspectedTissue.getTissueName() + "."  + tissueParameterList.getSelectedValue();
					actualSelectedParameterOrConstant = prefixMap.get(name) + name;
					notifyAllParameterSelectionListener();
				}
			}
		});
		
		this.tissueConstantList.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				if((tissueConstantList.getSelectedIndex() != -1) && e.getClickCount() == 2){
					
					String name = inspectedTissue.getTissueName() + "."  + tissueConstantList.getSelectedValue();
					String alternName = inspectedTissue.getTissueName()+Names.CELLBEHAVIORALMODEL + "."  + tissueConstantList.getSelectedValue();
					
					if(!prefixMap.keySet().contains(name) &&prefixMap.keySet().contains(alternName)){
						
						actualSelectedParameterOrConstant = prefixMap.get(alternName) + alternName;
						
					}
					else if(prefixMap.keySet().contains(name) &&!prefixMap.keySet().contains(alternName)){
						
						actualSelectedParameterOrConstant = prefixMap.get(name) + name;
						
					}
					
					notifyAllParameterSelectionListener();
				}
			}
		});
	   
		
		
		
		buildCellTypeList();
		
		
		final JPanel listPanel1 = new JPanel(new GridLayout(1, 2, 5, 5));
		final JPanel listPanel2 = new JPanel(new GridLayout(1, 2, 5, 5));
		final JPanel parametersAndConstantsPanel = new JPanel(new GridLayout(1, 2, 5, 5));
		final JPanel parametersPanel = new JPanel(new BorderLayout(0,0));
		
		JScrollPane diffLevelsScroll = new JScrollPane(diffLevelsList);
		JScrollPane cellListScroll = new JScrollPane(cellTypeList);
		JScrollPane parameterListScroll = new JScrollPane(parametersPanel);
		final JScrollPane constantListScroll = new JScrollPane(tissueConstantList);

		cellTypeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		diffLevelsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		cellParameterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tissueParameterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tissueConstantList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		
		diffLevelsList.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				if((diffLevelsList.getSelectedIndex() != -1) && e.getClickCount() == 2){
					//TODO: Implements double click on diffLevel
				}
			}
		});
		cellTypeList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {

				if((e.getValueIsAdjusting() != false) && cellTypeList.getSelectedIndex() != -1){
					buildParametersOrConstantsList(cellParameterList, cellTypesEnumMap.get(((String) cellTypeList.getSelectedValue())).name());
					buildDiffLevelsList(cellTypesEnumMap.get(((String) cellTypeList.getSelectedValue())));
					diffLevelsList.validate();
					diffLevelsList.repaint();
					parametersPanel.removeAll();
					parametersPanel.add(cellParameterList, BorderLayout.CENTER);
					parametersAndConstantsPanel.remove(constantListScroll);
					parametersAndConstantsPanel.validate();
					listPanel1.validate();
					listPanel1.repaint();
				}
			}

		});
		
		cellTypeList.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				if((cellTypeList.getSelectedIndex() != -1) && e.getClickCount() == 2){
					//TODO: Implements double click on cellType
				}
			}
		});
		
		tissueParameterList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {

				if((e.getValueIsAdjusting() != false) && tissueParameterList.getSelectedIndex() != -1){
					tissueConstantList.clearSelection();
				}
			}

		});
		tissueConstantList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {

				if((e.getValueIsAdjusting() != false) && tissueConstantList.getSelectedIndex() != -1){
					tissueParameterList.clearSelection();
				}
			}

		});
		
		

		cellParameterList.setToolTipText("double-click to select!");
		tissueParameterList.setToolTipText("double-click to select!");
		tissueConstantList.setToolTipText("double-click to select!");

		
		

		
		cellListScroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Cell Types"),
		      BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		diffLevelsScroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Differentiation Levels"),
		      BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		parameterListScroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Parameters"),
		      BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		constantListScroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Tissue Constants"),
		      BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		
		listPanel2.add(cellListScroll);
		listPanel2.add(diffLevelsScroll);
		listPanel1.add(listPanel2);
		parametersAndConstantsPanel.add(parameterListScroll);
		
		listPanel1.add(parametersAndConstantsPanel);		

		return listPanel1;

	}
	
	
	
	private JPanel buildTissueLevelAndCellLevelCombobox(){
		JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel comboPanel = new JPanel(new BorderLayout());
		JComboBox combo = new JComboBox(ParameterLevel.values());
		combo.setPreferredSize(new Dimension(200, 22));
		comboPanel.add(combo, BorderLayout.WEST);
		wrapperPanel.add(comboPanel);
		
		return wrapperPanel;
	}
	
	
	
	public String getActualSelectedParameter(){ return this.actualSelectedParameterOrConstant; }

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
	 
   
   private void notifyAllParameterSelectionListener(){
   	for(ParameterSelectionListener listener : this.parameterSelectionListener) listener.parameterWasSelected();
   }
   
}
