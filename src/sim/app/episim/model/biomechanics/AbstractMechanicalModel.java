package sim.app.episim.model.biomechanics;

import java.io.File;

import episiminterfaces.EpisimBioMechanicalModel;
import sim.app.episim.AbstractCell;
import sim.app.episim.model.initialization.AbstractBiomechanicalModelInitializer;


public abstract class AbstractMechanicalModel implements EpisimBioMechanicalModel{
	
	private AbstractCell cell;
	
	public AbstractMechanicalModel(AbstractCell cell){
		this.cell = cell;
	}
	
	protected AbstractCell getCell(){ return this.cell; }
	
	public abstract AbstractBiomechanicalModelInitializer getBiomechanicalModelInitializer();
	
	public abstract AbstractBiomechanicalModelInitializer getBiomechanicalModelInitializer(File modelInitializationFile);
	
	
		
}
