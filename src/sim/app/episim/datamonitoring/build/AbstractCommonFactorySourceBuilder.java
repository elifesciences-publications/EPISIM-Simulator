package sim.app.episim.datamonitoring.build;

import java.util.HashSet;
import java.util.Set;

import sim.app.episim.model.AbstractCell;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.Names;
import sim.field.continuous.Continuous2D;
import episimexceptions.MissingObjectsException;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.monitoring.EpisimChart;


public abstract class AbstractCommonFactorySourceBuilder {
	
	protected StringBuffer factorySource;
	protected Set<Class<?>> requiredClasses;
	
	public AbstractCommonFactorySourceBuilder(){
		this.requiredClasses = new HashSet<Class<?>>();
		this.factorySource = new StringBuffer();
	}
	
	protected void appendHeader(){
		
		for(Class<?> actClass : this.requiredClasses){
			if(actClass.getCanonicalName().contains(".")) this.factorySource.append("import "+ actClass.getCanonicalName()+";\n");
		}
		
		this.factorySource.append("import java.util.*;\n");
		this.factorySource.append("import episiminterfaces.*;\n");
		this.factorySource.append("import episiminterfaces.calc.*;\n");
		this.factorySource.append("import episiminterfaces.monitoring.*;\n");
		this.factorySource.append("import episimexceptions.*;\n");
		this.factorySource.append("import episimfactories.*;\n");
		this.factorySource.append("import sim.engine.*;\n");
		this.factorySource.append("import sim.app.episim.util.*;\n");
		this.factorySource.append("import sim.app.episim.util.ObservedDataCollection.ObservedDataCollectionType;\n");
		this.factorySource.append("import sim.app.episim.util.ObservedDataCollection;\n");
		this.factorySource.append("import sim.app.episim.model.*;\n");		
	}
	
	protected void appendDataFields(){
		for(Class<?> actClass : this.requiredClasses){
			if(isRequiredClassNecessary(actClass)){
				this.factorySource.append("  private "+ actClass.getSimpleName()+ " "+Names.convertClassToVariable(actClass.getSimpleName())+ ";\n");
			}
		}
		this.factorySource.append("  private GenericBag<AbstractCell> allCells;\n");		
	}
	
	protected void appendRegisterMethod(){
		this.factorySource.append("public void registerNecessaryObjects(GenericBag<AbstractCell> allCells, Object[] objects) throws MissingObjectsException{\n");
		this.factorySource.append("  if(objects == null || allCells == null) throw new IllegalArgumentException(\"Objects to be registered for charting must not be null\");\n");
		this.factorySource.append("    this.allCells = allCells;\n");
		this.factorySource.append("  for(Object actObject: objects){\n");
		for(Class<?> actClass : this.requiredClasses){
			if(isRequiredClassNecessary(actClass)){
				this.factorySource.append("    if(actObject instanceof "+actClass.getSimpleName()+") this."+Names.convertClassToVariable(actClass.getSimpleName())+" = ("+actClass.getSimpleName()+") actObject;\n"); 
			}
		}
		this.factorySource.append("  }\n");
		this.factorySource.append("  checkForMissingObjects();\n");
		this.factorySource.append("  registerRequiredObjects();\n");
		this.factorySource.append("}\n");
	}
	
	protected void appendCheckForMissingObjectsMethod(){
		this.factorySource.append("private void checkForMissingObjects() throws MissingObjectsException {\n");
		this.factorySource.append("  boolean objectsMissing = false;\n");
		this.factorySource.append("  if(this.allCells == null) objectsMissing = true;\n");
		for(Class<?> actClass : this.requiredClasses){
			if(isRequiredClassNecessary(actClass)){
				this.factorySource.append("  if(this."+Names.convertClassToVariable(actClass.getSimpleName())+" == null) objectsMissing = true;\n"); 
			}
		}
		this.factorySource.append("  if(objectsMissing) throw new MissingObjectsException(\"Some of the required Objects for Charting are not registered."
				+" Please call again the registerNecessaryObjects-Method to register them!\");\n");
		this.factorySource.append("}\n");
	}
	
	protected void appendEnd(){
		
		this.factorySource.append("}\n");
	}
	
	protected boolean isRequiredClassNecessary(Class<?> actClass){
		return !EpisimCellBehavioralModel.class.isAssignableFrom(actClass)
				&& !EpisimBiomechanicalModel.class.isAssignableFrom(actClass)
		  		&& !AbstractCell.class.isAssignableFrom(actClass) 
		  		&& !EpisimCellType.class.isAssignableFrom(actClass)
		  		&& !EpisimDifferentiationLevel.class.isAssignableFrom(actClass);
	}

}
