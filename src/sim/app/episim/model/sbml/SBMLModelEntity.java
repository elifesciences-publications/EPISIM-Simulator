package sim.app.episim.model.sbml;


public class SBMLModelEntity{
	public String name;
	public double value;
	public double concentration;

	public SBMLModelEntity(String name, double value, double concentration){
		this.name = name;
		this.value = value;
		this.concentration = concentration;
	}
	public String toString(){
		return name+": "+concentration;
	}


}