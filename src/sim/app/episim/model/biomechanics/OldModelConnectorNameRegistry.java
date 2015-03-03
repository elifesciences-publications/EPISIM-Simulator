package sim.app.episim.model.biomechanics;

import java.util.HashMap;


public class OldModelConnectorNameRegistry {
	private static String [][] mccIDMapArray = new String[][]
			{	{"2010-05-13_episimbiomechanics.centerbased.EpisimCenterBasedMC","2010-05-13_episimmcc.centerbased2d.oldmodel.EpisimCenterBasedMC"},
				{"2013-08-22_episimbiomechanics.centerbased.newversion.epidermis.EpisimCenterBasedMC","2013-08-22_episimmcc.centerbased2d.newmodel.epidermis.EpisimCenterBasedMC"},
				{"2013-10-04_episimbiomechanics.centerbased.newversion.chemotaxis.EpisimCenterBasedMC","2013-10-04_episimmcc.centerbased2d.newmodel.chemotaxis.EpisimCenterBasedMC"},
				{"2013-03-13_episimbiomechanics.centerbased.adhesion.EpisimAdhesiveCenterBasedMC","2013-03-13_episimmcc.centerbased2d.oldmodel.wound.EpisimAdhesiveCenterBasedMC"},
				{"2012-02-05_episimbiomechanics.centerbased3d.EpisimCenterBased3DMC","2012-02-05_episimmcc.centerbased3d.oldmodel.EpisimCenterBased3DMC"},
				{"2014-04-10_episimbiomechanics.centerbased3d.newversion.chemotaxis.EpisimChemotaxisCenterBased3DMC","2014-04-10_episimmcc.centerbased3d.newmodel.chemotaxis.EpisimChemotaxisCenterBased3DMC"},
				{"2014-03-12_episimbiomechanics.centerbased3d.newversion.epidermis.EpisimEpidermisCenterBased3DMC","2014-03-12_episimmcc.centerbased3d.newmodel.epidermis.EpisimEpidermisCenterBased3DMC"},
				{"2015-01-27_episimbiomechanics.centerbased3d.newversion.fisheye.EpisimFishEyeCenterBased3DMC","2015-01-27_episimmcc.centerbased3d.fisheye.EpisimFishEyeCenterBased3DMC"},
				{"2013-03-27_episimbiomechanics.centerbased3d.adhesion.EpisimAdhesiveCenterBased3DMC","2013-03-27_episimmcc.centerbased3d.oldmodel.wound.EpisimAdhesiveCenterBased3DMC"},
				{"2012-07-09_episimbiomechanics.hexagonbased2d.singlesurface.EpisimHexagonBased2DSingleSurfaceMC","2012-07-09_episimmcc.latticebased2d.EpisimLatticeBased2DMC"},
				{"2012-03-26_episimbiomechanics.hexagonbased2d.singlesurface.tumormodel.simple.EpisimSimpleTumorModelMC","2012-03-26_episimmcc.latticebased2d.tumormodel.simple.EpisimSimpleTumorModelMC"},
				{"2013-08-29_episimbiomechanics.hexagonbased2d.singlesurface.tumormodel.chemokine.EpisimChemokineTumorModelMC","2013-08-29_episimmcc.latticebased2d.tumormodel.chemokine.EpisimChemokineTumorModelMC"},
				{"2012-11-11_episimbiomechanics.hexagonbased2d.singlesurface.populgrowth.EpisimPopulationGrowthMC","2012-11-11_episimmcc.latticebased2d.populgrowth.EpisimPopulationGrowthMC"},
				{"2012-07-05_episimbiomechanics.hexagonbased2d.singlesurface.bactmacro.EpisimBacteriaMacrophageModelMC","2012-07-05_episimmcc.latticebased2d.bactmacro.EpisimBacteriaMacrophageModelMC"},
				{"2012-01-16_episimbiomechanics.hexagonbased3d.EpisimHexagonBased3DMC","2012-01-16_episimmcc.latticebased3d.EpisimLatticeBased3DMC"},
				{"2011-01-07_episimbiomechanics.vertexbased.EpisimVertexBasedMC","2011-01-07_episimmcc.vertexbased2d.EpisimVertexBasedMC"}
			};
	
	private static HashMap<String, String> mccOldToNewMap = new HashMap<String, String>();
	static{
		for(int i = 0; i< mccIDMapArray.length; i++){
			mccOldToNewMap.put(mccIDMapArray[i][0], mccIDMapArray[i][1]);
		}
	}
	
	public static boolean isIDOfOldMCC(String id){ return mccOldToNewMap.containsKey(id);}
	
	public static String getNewIDOfMCC(String oldId){ return mccOldToNewMap.containsKey(oldId)? mccOldToNewMap.get(oldId) : "";}
}
