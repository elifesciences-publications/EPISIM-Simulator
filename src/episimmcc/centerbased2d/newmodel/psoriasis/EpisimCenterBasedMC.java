package episimmcc.centerbased2d.newmodel.psoriasis;

import java.util.HashMap;

import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.biomechanics.centerbased2d.newmodel.CenterBased2DModel;
import sim.app.episim.model.biomechanics.centerbased2d.newmodel.CenterBased2DModelGP;
import sim.app.episim.model.biomechanics.centerbased2d.newmodel.psoriasis.PsoriasisCenterBased2DModelGP;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoExport;
import episimmcc.EpisimModelConnector.Hidden;
import episimmcc.EpisimModelConnector.Pairwise;



public class EpisimCenterBasedMC extends episimmcc.centerbased2d.newmodel.epidermis.EpisimCenterBasedMC {
	
	private static final String ID = "2015-03-10";
	private static final String NAME = "New Center Based Biomechanical Model - Psoriasis";

	
	
	public EpisimCenterBasedMC(){}
	
	@NoExport
	public void resetPairwiseParameters(){
		//this.contactArea.clear();
	}
	
	@Hidden
	@NoExport
	protected String getIdForInternalUse(){
		return ID;
	}
	
	@Hidden
	@NoExport
	public String getBiomechanicalModelName(){
		return NAME;
	}
	
	@NoExport
	public Class<? extends EpisimBiomechanicalModel> getEpisimBioMechanicalModelClass(){
		return CenterBased2DModel.class;
	}
	
	@NoExport
	public Class<? extends EpisimBiomechanicalModelGlobalParameters> getEpisimBioMechanicalModelGlobalParametersClass(){
		return PsoriasisCenterBased2DModelGP.class;
	}
	
	@NoExport
	public Class<? extends BiomechanicalModelInitializer> getEpisimBioMechanicalModelInitializerClass(){
		return CenterBasedMechModelInit.class;
	} 
}

