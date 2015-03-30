package episimmcc.latticebased3d.frenchflag;

import sim.app.episim.model.biomechanics.latticebased3d.LatticeBased3DModel;
import sim.app.episim.model.biomechanics.latticebased3d.LatticeBased3DModelGP;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoExport;
import episimmcc.latticebased3d.EpisimLatticeBased3DMC;


public class EpisimFrenchFlagModel3DMC extends EpisimLatticeBased3DMC {
	
	private static final String ID = "2015-03-30";
	private static final String NAME = "Simple French Flag 3D Model";
	
		
	@Hidden
	@NoExport
	protected String getIdForInternalUse() {
		return ID;
	}
	@Hidden
	@NoExport
	public String getBiomechanicalModelName() {
		return NAME;
	}
	
	@NoExport
	public Class<? extends EpisimBiomechanicalModel> getEpisimBioMechanicalModelClass(){
		return LatticeBased3DModel.class;
	}
	@NoExport
	public Class<? extends BiomechanicalModelInitializer> getEpisimBioMechanicalModelInitializerClass(){
		return EpisimFrenchFlag3DModelInit.class;
	}
	@NoExport
	public Class<? extends EpisimBiomechanicalModelGlobalParameters> getEpisimBioMechanicalModelGlobalParametersClass(){
		return LatticeBased3DModelGP.class;
	}
}
