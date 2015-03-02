package sim.app.episim.model.biomechanics.latticebased2Dr;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;


public abstract class AbstractLatticeBased2DModelGP implements EpisimBiomechanicalModelGlobalParameters, java.io.Serializable {
	
	public abstract double getInner_hexagonal_radius();
	public abstract double getOuter_hexagonal_radius();

}
