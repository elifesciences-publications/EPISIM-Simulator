package sim.app.episim.model.biomechanics.hexagonbased;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;


public abstract class AbstractHexagonBasedMechanicalModelGP implements EpisimBiomechanicalModelGlobalParameters, java.io.Serializable {
	
	public abstract double getInner_hexagonal_radius();
	public abstract double getOuter_hexagonal_radius();

}
