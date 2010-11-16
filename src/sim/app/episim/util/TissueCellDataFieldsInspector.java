package sim.app.episim.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import sim.app.episim.AbstractCell;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.datamonitoring.ExpressionEditorPanel.ExpressionType;
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
	private Map<String, EpisimDifferentiationLevel> diffLevelsEnumMap;
	private Map<String, AbstractCell> cellTypesClassesMap;
	private Map<String, String> cellTypesEnumClassesMap;
	private Map<String, EpisimDifferentiationLevel[]> diffLevelsMap;
	private Map<String, String> overallMethodCallMap;
	private Map<String, String> overallFieldCallMap;
	private Map<String, String> overallDiffLevelCallMap;
	private Map<String, String> overallCellTypeCallMap;
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
	
	private JComboBox levelSelectionCombo;
	
	private String actualSelectedParameterOrConstant = "";
	
	private Component parentComponent;
	
	
	private ExpressionType expressionType = ExpressionType.MATHEMATICAL_EXPRESSION;
	
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
		this.cellTypesClassesMap = new HashMap<String, AbstractCell>();
		this.cellTypesEnumClassesMap = new HashMap<String, String>();
		this.overallCellTypeCallMap = new HashMap<String, String>();
		for(EpisimCellType actCellType : ModelController.getInstance().getCellBehavioralModelController().getAvailableCellTypes()){	        
	        cellTypesEnumMap.put(actCellType.name(), actCellType);
	        overallCellTypeCallMap.put(actCellType.name(), actCellType.getClass().getSimpleName()+"."+actCellType.name());
		}
		for(EpisimCellType actType : this.inspectedTissue.getRegisteredCellTypes().keySet()){
	        AbstractCell cellType  = null;
         try{
	         cellType = this.inspectedTissue.getRegisteredCellTypes().get(actType).newInstance();
	         cellTypesClassesMap.put(actType.name(), cellType);
	         cellTypesEnumClassesMap.put(actType.name(), cellType.getCellName());
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
		this.diffLevelsEnumMap = new HashMap<String, EpisimDifferentiationLevel>();
		this.overallDiffLevelCallMap = new HashMap<String, String>();
		EpisimCellType[] cellTypes  = ModelController.getInstance().getCellBehavioralModelController().getAvailableCellTypes();
		this.diffLevelsMap = new HashMap<String, EpisimDifferentiationLevel[]>();
		for(EpisimCellType cellType : cellTypes){
			this.diffLevelsMap.put(cellType.name(), cellType.getDifferentiationLevel());
		}
		for(EpisimDifferentiationLevel actDiffLevel : ModelController.getInstance().getCellBehavioralModelController().getAvailableDifferentiationLevels()){
			diffLevelsEnumMap.put(actDiffLevel.name(), actDiffLevel);
			overallDiffLevelCallMap.put(actDiffLevel.name(), actDiffLevel.getClass().getSimpleName()+"."+actDiffLevel.name());
		}
	}
	
	private void buildOverallVarNameSetMethodCallMapConstantNameSetAndFieldCallMap(Map<String, AbstractCell> cellTypes) {
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
			AbstractCell actClass = cellTypes.get(actCellTypeName);

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
		if(EpisimCellBehavioralModel.class.isAssignableFrom(actMethod.getDeclaringClass())) firstName += Names.CELLBEHAVIORAL_MODEL;
		if(isValidReturnType(actMethod.getReturnType())){
			parameterName=getParameterName(actMethod.getName());
			
			if(actMethod.getReturnType() != null){ 
				if(Double.TYPE.isAssignableFrom(actMethod.getReturnType()) || Integer.TYPE.isAssignableFrom(actMethod.getReturnType()) || Long.TYPE.isAssignableFrom(actMethod.getReturnType())){ 
					firstName = Names.NUMBER_PREFIX + firstName;
				}
				else if(Boolean.TYPE.isAssignableFrom(actMethod.getReturnType())){ 
					firstName = Names.BOOLEAN_PREFIX + firstName;
				}
				else if(EpisimCellType.class.isAssignableFrom(actMethod.getReturnType())){ 
					firstName = Names.CELLTYPE_PREFIX + firstName;
				}
				else if(EpisimDifferentiationLevel.class.isAssignableFrom(actMethod.getReturnType())){ 
					firstName = Names.DIFFLEVEL_PREFIX + firstName;
				}
			}
			
			if(parameterName != null){
				String finalName = getRedundancyCheckedName(firstName + "." + parameterName, this.overallVarOrConstantNameSet);
				
				String methodCallName = Names.convertClassToVariable(actMethod.getDeclaringClass().getSimpleName()) + "."+ actMethod.getName()+"()";
				this.prefixMap.put(finalName.substring(Names.PREFIX_LENGTH), finalName.substring(0, Names.PREFIX_LENGTH));
				this.overallMethodCallMap.put(finalName,methodCallName);
				this.overallMethodMap.put(finalName,actMethod);
				this.overallVarOrConstantNameSet.add(finalName);
			}
		}
	}
	
	private void processFieldForConstantNameSetAndFieldCallMap(String firstName, Field actField){
		String constantName="";
		if(EpisimCellBehavioralModelGlobalParameters.class.isAssignableFrom(actField.getDeclaringClass())) firstName += Names.CELLBEHAVIORAL_MODEL;
		if(isValidReturnType(actField.getType())){
			constantName=actField.getName();
			
			if(actField.getType() != null){ 
				if(Double.TYPE.isAssignableFrom(actField.getType()) || Integer.TYPE.isAssignableFrom(actField.getType()) || Long.TYPE.isAssignableFrom(actField.getType())){ 
					firstName = Names.NUMBER_PREFIX + firstName;
				}
				else if(Boolean.TYPE.isAssignableFrom(actField.getType())){ 
					firstName = Names.BOOLEAN_PREFIX + firstName;
				}
			}
			
			if(constantName != null){
				String finalName = getRedundancyCheckedName(firstName + "." + constantName, this.overallVarOrConstantNameSet);
				
				String fieldCallName = Names.convertClassToVariable(actField.getDeclaringClass().getSimpleName()) + "."+ actField.getName();
				this.prefixMap.put(finalName.substring(Names.PREFIX_LENGTH), finalName.substring(0, Names.PREFIX_LENGTH));
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
			else{
				EpisimCellType cellType = cellTypesEnumMap.get(identifier);
				if(cellType!= null) this.requiredClasses.add(cellType.getClass());
				else{
					EpisimDifferentiationLevel diffLevel = diffLevelsEnumMap.get(identifier);
					if(diffLevel != null) this.requiredClasses.add(diffLevel.getClass());
				}
			}
		}
	}
	
	public boolean checkIfIdentifierIsGlobalParameter(String identifier){
		Class<?> identifiersClass = overallMethodMap.get(identifier).getDeclaringClass();
		if(EpisimCellBehavioralModel.class.isAssignableFrom(identifiersClass)
				|| EpisimMechanicalModel.class.isAssignableFrom(identifiersClass)
				|| AbstractCell.class.isAssignableFrom(identifiersClass)) return false;
		return true;		
	}
	
	public boolean checkIfIdentifierHasBooleanType(String identifier){
		return Boolean.TYPE.isAssignableFrom(overallMethodMap.get(identifier).getReturnType());		
				
	}
	
	public boolean checkIfIdentifierHasCellType(String identifier){
		return EpisimCellType.class.isAssignableFrom(overallMethodMap.get(identifier).getReturnType());		
				
	}
	
	public boolean checkIfIdentifierHasDiffLevelType(String identifier){
		return EpisimDifferentiationLevel.class.isAssignableFrom(overallMethodMap.get(identifier).getReturnType());		
				
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
				if(subNames.length >= 2 && subNames[0].substring(Names.PREFIX_LENGTH).equals(cellOrTissueTypeName)){
					list.add(subNames[1]);
				}
				else if(subNames.length >= 2 && subNames[0].substring(Names.PREFIX_LENGTH).equals(cellOrTissueTypeName+Names.CELLBEHAVIORAL_MODEL)){
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

	
	public JPanel getVariableListPanel(Component parent){
		
		this.parentComponent  = parent;
		this.expressionType = ExpressionType.MATHEMATICAL_EXPRESSION;
		JPanel variableMainPanel = new JPanel(new BorderLayout(10,10));
		variableMainPanel.add(buildTissueLevelAndCellLevelCombobox(), BorderLayout.NORTH);
		variableMainPanel.add(buildVariableListPanel(), BorderLayout.CENTER);
		
		variableMainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Model Parameters"),
		      BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		
		return variableMainPanel;
	}
	
	
	
	
	
	
	public Set<String> getOverallVarOrConstantNameSet(){
		HashSet<String> newOverallMap = new HashSet<String>();
		newOverallMap.addAll(overallVarOrConstantNameSet);
		newOverallMap.addAll(this.diffLevelsEnumMap.keySet());
		newOverallMap.addAll(this.cellTypesEnumMap.keySet());
		return newOverallMap;
	}
	
	
	public boolean hasCellTypeConflict(Set<String> varNames){
		String foundCellTypeName = null;
		Set<String> cellTypeClassNames = new HashSet<String>();
		//Class-Based Check
		for(AbstractCell actType: this.cellTypesClassesMap.values()) cellTypeClassNames.add(actType.getClass().getSimpleName());
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
			String cellTypeName = removeCellBehavioralModel(actVarName.split("\\.")[0].substring(Names.PREFIX_LENGTH));
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
		if(name.endsWith(Names.CELLBEHAVIORAL_MODEL)){
			return name.substring(0, name.length()- Names.CELLBEHAVIORAL_MODEL.length());
		}
		return name;
	}

	public String getMethodOrFieldCallStrForVarOrConstantName(String varName){ 
		
		if(this.overallMethodCallMap.containsKey(varName))  return this.overallMethodCallMap.get(varName);
		else if(this.overallFieldCallMap.containsKey(varName))  return this.overallFieldCallMap.get(varName);
		else if(this.overallCellTypeCallMap.containsKey(varName)) return this.overallCellTypeCallMap.get(varName);
		else if(this.overallDiffLevelCallMap.containsKey(varName)) return this.overallDiffLevelCallMap.get(varName);
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
		
		
		
		this.cellParameterList.addListSelectionListener(new ListSelectionListener(){

			public void valueChanged(ListSelectionEvent e) {

				if((e.getValueIsAdjusting() != false) && cellParameterList.getSelectedIndex() != -1){
	         	diffLevelsList.clearSelection();
	         }
	         
         }});
		
		
		this.cellParameterList.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				if((cellParameterList.getSelectedIndex() != -1) && e.getClickCount() == 2){					
					cellParameterSelected();
				}
			}
		});
	   
		this.tissueParameterList.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				if((tissueParameterList.getSelectedIndex() != -1) && e.getClickCount() == 2){
					tissueParameterSelected();				
				}
			}
		});
		
		this.tissueConstantList.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				if((tissueConstantList.getSelectedIndex() != -1) && e.getClickCount() == 2){					
					tissueConstantSelected();
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
					if(validTypes.contains(EpisimDifferentiationLevel.class)){
						if(expressionType==ExpressionType.MATHEMATICAL_EXPRESSION){
							
							if(parentComponent != null)JOptionPane.showMessageDialog(parentComponent, "A differentiation level cannot be used in a mathematical expression.","Info", JOptionPane.INFORMATION_MESSAGE);
						}
						else{
							actualSelectedParameterOrConstant = (String) diffLevelsList.getSelectedValue();
							notifyAllParameterSelectionListener();
						}
					}
					else{
						if(parentComponent != null){
							JOptionPane.showMessageDialog(parentComponent, "Differentiation levels cannot be used in this context.","Info", JOptionPane.INFORMATION_MESSAGE);
						}	
					}
				}
			}
		});
		diffLevelsList.addListSelectionListener(new ListSelectionListener(){

			public void valueChanged(ListSelectionEvent e) {
				if((e.getValueIsAdjusting() != false) && diffLevelsList.getSelectedIndex() != -1){	
					tissueParameterList.clearSelection();
					tissueConstantList.clearSelection();
					cellParameterList.clearSelection();
				}
	         
         }});
		
		
		cellTypeList.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				if((cellTypeList.getSelectedIndex() != -1) && e.getClickCount() == 2){
					if(validTypes.contains(EpisimCellType.class)){
						if(expressionType==ExpressionType.MATHEMATICAL_EXPRESSION){
							
							if(parentComponent != null)JOptionPane.showMessageDialog(parentComponent, "A cell type cannot be used in a mathematical expression.","Info", JOptionPane.INFORMATION_MESSAGE);
						}
						else{
							actualSelectedParameterOrConstant = (String) cellTypeList.getSelectedValue();
							notifyAllParameterSelectionListener();
						}
					}
					else{
						if(parentComponent != null){
							JOptionPane.showMessageDialog(parentComponent, "Cell types cannot be used in this context.","Info", JOptionPane.INFORMATION_MESSAGE);
						}	
					}
				}
			}
		});
		
		cellTypeList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {

				if((e.getValueIsAdjusting() != false) && cellTypeList.getSelectedIndex() != -1){					
					buildDiffLevelsList(cellTypesEnumMap.get(((String) cellTypeList.getSelectedValue())));
					diffLevelsList.validate();
					diffLevelsList.repaint();
					buildParametersOrConstantsList(cellParameterList, ((String) cellTypeList.getSelectedValue()));
					parametersPanel.removeAll();
					parametersPanel.add(cellParameterList, BorderLayout.CENTER);
					parametersAndConstantsPanel.remove(constantListScroll);
					parametersAndConstantsPanel.validate();
					listPanel1.validate();
					listPanel1.repaint();
					tissueParameterList.clearSelection();
					tissueConstantList.clearSelection();
				}
			}

		});
		
		tissueParameterList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {

				if((e.getValueIsAdjusting() != false) && tissueParameterList.getSelectedIndex() != -1){
					tissueConstantList.clearSelection();
					cellTypeList.clearSelection();
					diffLevelsList.clearSelection();
					diffLevelsList.removeAll();
				}
			}

		});
		tissueConstantList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {

				if((e.getValueIsAdjusting() != false) && tissueConstantList.getSelectedIndex() != -1){
					tissueParameterList.clearSelection();
					cellTypeList.clearSelection();
					diffLevelsList.clearSelection();
					diffLevelsList.removeAll();
				}
			}

		});
		
		levelSelectionCombo.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				if(e.getStateChange() == ItemEvent.SELECTED){
					if(e.getItem() == ParameterLevel.TISSUE_LEVEL){
						buildParametersOrConstantsList(tissueParameterList, inspectedTissue.getTissueName());
						buildParametersOrConstantsList(tissueConstantList, inspectedTissue.getTissueName());
						parametersPanel.removeAll();
						parametersPanel.add(tissueParameterList, BorderLayout.CENTER);
						parametersAndConstantsPanel.remove(constantListScroll);
						parametersAndConstantsPanel.add(constantListScroll);
						parametersAndConstantsPanel.validate();
						listPanel1.validate();
						listPanel1.repaint();
					}
					else if(e.getItem() == ParameterLevel.CELLULAR_LEVEL){
						if(cellTypeList.getSelectedIndex() == -1){
							cellTypeList.setSelectedIndex(0);
							buildDiffLevelsList(cellTypesEnumMap.get(((String) cellTypeList.getSelectedValue())));
							diffLevelsList.validate();
							diffLevelsList.repaint();
							listPanel1.validate();
							listPanel1.repaint();
							tissueParameterList.clearSelection();
							tissueConstantList.clearSelection();
						}
						buildParametersOrConstantsList(cellParameterList, ((String) cellTypeList.getSelectedValue()));
						parametersPanel.removeAll();
						parametersPanel.add(cellParameterList, BorderLayout.CENTER);
						parametersAndConstantsPanel.remove(constantListScroll);
						parametersAndConstantsPanel.validate();
						listPanel1.validate();
						listPanel1.repaint();
					}
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
		
		buildParametersOrConstantsList(tissueParameterList, inspectedTissue.getTissueName());
		buildParametersOrConstantsList(tissueConstantList, inspectedTissue.getTissueName());
		parametersPanel.removeAll();
		parametersPanel.add(tissueParameterList, BorderLayout.CENTER);
		parametersAndConstantsPanel.remove(constantListScroll);
		parametersAndConstantsPanel.add(constantListScroll);
		parametersAndConstantsPanel.validate();
		listPanel1.validate();
		listPanel1.repaint();
		
		if(cellTypeList.getModel().getSize() > 0){
			cellTypeList.setSelectedIndex(0);
			buildDiffLevelsList(cellTypesEnumMap.get(((String) cellTypeList.getSelectedValue())));
			diffLevelsList.validate();
			diffLevelsList.repaint();
			listPanel1.validate();
			listPanel1.repaint();
		}

		return listPanel1;

	}
	
	
	
	private JPanel buildTissueLevelAndCellLevelCombobox(){
		JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel comboPanel = new JPanel(new BorderLayout());
		levelSelectionCombo = new JComboBox(ParameterLevel.values());
		levelSelectionCombo.setPreferredSize(new Dimension(200, 22));
		comboPanel.add(levelSelectionCombo, BorderLayout.WEST);
		wrapperPanel.add(comboPanel);
		
		
		
		return wrapperPanel;
	}
	
	public void setExpressionType(ExpressionType expType){ this.expressionType = expType; }
	
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
   
   private void cellParameterSelected(){
   	String name = cellTypeList.getSelectedValue() + "."  + cellParameterList.getSelectedValue();
		String alternName = cellTypeList.getSelectedValue()+Names.CELLBEHAVIORAL_MODEL + "."  + cellParameterList.getSelectedValue();
		String selectedParamName ="";
		String prefix = null;
		
		if(!prefixMap.keySet().contains(name) &&prefixMap.keySet().contains(alternName)){
			prefix = prefixMap.get(alternName);
			selectedParamName =  prefix + alternName;			
		}
		else if(prefixMap.keySet().contains(name) &&!prefixMap.keySet().contains(alternName)){
			prefix = prefixMap.get(name);
			selectedParamName = prefix + name;			
		}  	
		if(checkIfParameterAllowedInExpression(prefix)){			
			actualSelectedParameterOrConstant = selectedParamName;
			notifyAllParameterSelectionListener();
		}
   }
   
   
   private void tissueParameterSelected(){
   	String name = inspectedTissue.getTissueName() + "."  + tissueParameterList.getSelectedValue();
   	String prefix = prefixMap.get(name); 	
   	if(checkIfParameterAllowedInExpression(prefix)){ 
	   	actualSelectedParameterOrConstant = prefix + name;
			notifyAllParameterSelectionListener();
   	}
   }
   
   private void tissueConstantSelected(){
   	String name = inspectedTissue.getTissueName() + "."  + tissueConstantList.getSelectedValue();
		String alternName = inspectedTissue.getTissueName()+Names.CELLBEHAVIORAL_MODEL + "."  + tissueConstantList.getSelectedValue();
		String prefix = null;
		String selectedConstantName = "";
		
		if(!prefixMap.keySet().contains(name) &&prefixMap.keySet().contains(alternName)){
			prefix = prefixMap.get(alternName);
			selectedConstantName = prefix + alternName;
			
		}
		else if(prefixMap.keySet().contains(name) &&!prefixMap.keySet().contains(alternName)){
			prefix = prefixMap.get(name);
			selectedConstantName = prefix + name;
			
		}			
   	if(checkIfParameterAllowedInExpression(prefix)){ 
   		actualSelectedParameterOrConstant = selectedConstantName;
			notifyAllParameterSelectionListener();
   	}
   }
   
   private boolean checkIfParameterAllowedInExpression(String prefix){
   	
   	if(prefix != null  && expressionType == ExpressionType.MATHEMATICAL_EXPRESSION && !prefix.equals(Names.NUMBER_PREFIX)){																
			if(parentComponent != null){
				if(prefix.equals(Names.BOOLEAN_PREFIX)){							
					JOptionPane.showMessageDialog(parentComponent, "A parameter of type boolean cannot be used in a mathematical expression.","Info", JOptionPane.INFORMATION_MESSAGE);
					
				}
				else if(prefix.equals(Names.CELLTYPE_PREFIX)){							
					JOptionPane.showMessageDialog(parentComponent, "The cell type cannot be used in a mathematical expression.","Info", JOptionPane.INFORMATION_MESSAGE);
				}
				else if(prefix.equals(Names.DIFFLEVEL_PREFIX)){							
					JOptionPane.showMessageDialog(parentComponent, "The differentiation level cannot be used in a mathematical expression.","Info", JOptionPane.INFORMATION_MESSAGE);
				}				
				return false;
			}
		}
   	return true;
   }
   
}
