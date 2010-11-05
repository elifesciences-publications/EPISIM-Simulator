package episimbiomechanics;

import episiminterfaces.EpisimMechanicalModel;


public abstract class EpisimModelIntegrator implements EpisimMechanicalModel, java.io.Serializable{
	public abstract String getBiomechanicalModelId();
}
