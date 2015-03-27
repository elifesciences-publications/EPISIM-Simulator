package episimmcc.latticebased2d.frenchflag;

import sim.app.episim.model.biomechanics.latticebased2d.LatticeBased2DModel;
import sim.app.episim.model.biomechanics.latticebased2d.LatticeBased2DModelGP;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoExport;
import episimmcc.EpisimModelConnector.Hidden;
import episimmcc.latticebased2d.EpisimLatticeBased2DMC;
import episimmcc.latticebased2d.tumormodel.simple.EpisimSimpleTumorModelInit;


public class EpisimFrenchFlagModelMC extends EpisimLatticeBased2DMC {
		
		private static final String ID = "2015-03-24";
		private static final String NAME = "Simple French Flag Model";
		
			
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
			return LatticeBased2DModel.class;
		}
		@NoExport
		public Class<? extends BiomechanicalModelInitializer> getEpisimBioMechanicalModelInitializerClass(){
			return EpisimFrenchFlagModelInit.class;
		}
		@NoExport
		public Class<? extends EpisimBiomechanicalModelGlobalParameters> getEpisimBioMechanicalModelGlobalParametersClass(){
			return LatticeBased2DModelGP.class;
		}
}
