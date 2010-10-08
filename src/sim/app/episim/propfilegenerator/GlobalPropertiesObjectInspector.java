package sim.app.episim.propfilegenerator;


import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import sim.app.episim.util.SortedJList;

public class GlobalPropertiesObjectInspector {
	
		public interface GlobalParameterSelectionListener{
			void parameterWasSelected();
		}
		
		
	
		private Set<GlobalParameterSelectionListener> parameterSelectionListener;	
		private SortedJList globalPropertiesList;		
		
		private Set<String> markerPrefixes;
		private Set<Class<?>> validTypes;		
		
		private String actualSelectedParameter = "";		
		private HashMap<String, Class<?>> globalParametersMap;
		private Object globalParametersObject;
		
		
		
		public GlobalPropertiesObjectInspector(Object globalParametersObject, Set<String> markerPrefixes, Set<Class<?>> validTypes) {
				this.markerPrefixes = markerPrefixes;
				this.validTypes = validTypes;
				this.globalParametersObject = globalParametersObject;						
				parameterSelectionListener = new HashSet<GlobalParameterSelectionListener>();				
				if(globalParametersObject == null) throw new IllegalArgumentException("Global Properties Object was null!");	
				buildGlobalParametersMap();
		}
		
		private JPanel buildGlobalParameterListPanel() {
			
			Comparator<String> stringComparator = new Comparator<String>(){

			public int compare(String o1, String o2) {
	           
	          return o1.compareTo(o2);
	      }};
	      globalPropertiesList = new SortedJList(stringComparator);
			
			
	         this.globalPropertiesList.addMouseListener(new MouseAdapter() {

				public void mouseClicked(MouseEvent e) {

					if((globalPropertiesList.getSelectedIndex() != -1) && e.getClickCount() == 2){
						
						actualSelectedParameter = ""+ globalPropertiesList.getSelectedValue();	
						
						notifyAllParameterSelectionListener();
					}
				}
			});	   
			if(globalParametersMap != null){				
				for(String param : this.globalParametersMap.keySet()) globalPropertiesList.add(param);				
			}
			
			
			final JPanel listPanel = new JPanel(new GridLayout(1, 2, 5, 5));			
			
			JScrollPane globalPropertiesListScroll = new JScrollPane(globalPropertiesList);			

			globalPropertiesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			globalPropertiesList.setToolTipText("double-click to select!");	

			
			
		
			listPanel.add(globalPropertiesListScroll);
			listPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Properties"),
			      BorderFactory.createEmptyBorder(5, 5, 5, 5)));

			return listPanel;

		}
		
		public String getActualSelectedGlobalParameter(){ return this.actualSelectedParameter; }
		public Class<?> getActualSelectedGlobalParameterType(){ return this.globalParametersMap.get(this.actualSelectedParameter); }
   
	   private void notifyAllParameterSelectionListener(){
	   	for(GlobalParameterSelectionListener listener : this.parameterSelectionListener) listener.parameterWasSelected();
	   }
	   
	   private void buildGlobalParametersMap(){
	   	this.globalParametersMap = new HashMap<String, Class<?>>();
	   	for(Method actMethod : this.globalParametersObject.getClass().getMethods()){
	   		if(actMethod.getReturnType() != null){ 
	   			if(isValidReturnType(actMethod.getReturnType())){
	   				String paramName = null;
	   				if((paramName = getParameterName(actMethod.getName())) != null){
	   					globalParametersMap.put(paramName, actMethod.getReturnType());
	   				}
	   			}
	   		}
	   	}
	   
	   }
	   
	   
	   private String getParameterName(String paramName) {
			for(String actPrefix : markerPrefixes){
				if(paramName.startsWith(actPrefix)) return paramName.substring(actPrefix.length());
			}
			return null;
		}

		private boolean isValidReturnType(Class<?> cls) {
			return validTypes.contains(cls);
		}
	   
	   public JPanel getGlobalParameterListPanel(){ return buildGlobalParameterListPanel();}
	   public void addGlobalParameterSelectionListener(GlobalParameterSelectionListener listener){ this.parameterSelectionListener.add(listener); }
}
