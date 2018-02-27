package sim.app.episim.model.sbml;

import java.util.Collection;
import java.util.HashMap;

public class SBMLModelState {

	private HashMap<String, SBMLModelEntity> species;
	private HashMap<String, SBMLModelEntity> parameters;
	private HashMap<String, SBMLModelEntity> reactions;	

	public SBMLModelState(){
		this.species = new HashMap<String, SBMLModelEntity>();
		this.parameters = new HashMap<String, SBMLModelEntity>();
		this.reactions = new HashMap<String, SBMLModelEntity>();		
	}	

	public void addSpeciesValue(SBMLModelEntity _species){
		if(_species.name != null && _species.name.length() > 0)this.species.put(_species.name, _species);
	}	
	public void addParameterValue(SBMLModelEntity _parameter){
		if(_parameter.name != null && _parameter.name.length() > 0)this.parameters.put(_parameter.name, _parameter);
	}	
	public void addReactionValue(SBMLModelEntity _reaction){
		if(_reaction.name != null && _reaction.name.length() > 0)this.reactions.put(_reaction.name, _reaction);
	}

	public double getSpeciesValue(String name){
		return this.species.containsKey(name) ? this.species.get(name).value : 0;
	}
	public double getSpeciesConcentration(String name){
		return this.species.containsKey(name) ? this.species.get(name).concentration : 0;
	}
	public double getParameterValue(String name){
		return this.parameters.containsKey(name) ? this.parameters.get(name).value : 0;
	}
	public double getReactionValue(String name){
		return this.reactions.containsKey(name) ? this.reactions.get(name).value : 0;
	}	

	public Collection<SBMLModelEntity> getSpeciesValues(){ return this.species.values(); }
	public Collection<SBMLModelEntity> getParameterValues(){ return this.parameters.values(); }
	public Collection<SBMLModelEntity> getReactionValues(){ return this.reactions.values(); }


}
