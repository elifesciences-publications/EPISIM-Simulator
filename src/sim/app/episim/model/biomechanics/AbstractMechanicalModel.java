package sim.app.episim.model.biomechanics;

import java.io.File;

import episiminterfaces.EpisimBiomechanicalModel;
import sim.app.episim.AbstractCell;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;


public abstract class AbstractMechanicalModel implements EpisimBiomechanicalModel{
	
	private AbstractCell cell;
	
	public AbstractMechanicalModel(AbstractCell cell){
		this.cell = cell;
	}
	
	protected AbstractCell getCell(){ return this.cell; }	
	
	public abstract BiomechanicalModelInitializer getBiomechanicalModelInitializer();	
	public abstract BiomechanicalModelInitializer getBiomechanicalModelInitializer(File modelInitializationFile);	

}
