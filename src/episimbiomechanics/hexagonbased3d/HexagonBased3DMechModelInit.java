package episimbiomechanics.hexagonbased3d;

import java.util.ArrayList;

import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimPortrayal;
import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.biomechanics.latticebased3D.LatticeBased3DModel;
import sim.app.episim.model.biomechanics.latticebased3D.LatticeBased3DModelGP;
import sim.app.episim.model.biomechanics.latticebased3D.LatticeCellField3D;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.tissue.TissueController;
import sim.util.Double3D;
import sim.util.Int3D;



public class HexagonBased3DMechModelInit extends BiomechanicalModelInitializer {
	private LatticeBased3DModelGP globalParameters;
	public HexagonBased3DMechModelInit(){
		super();
		TissueController.getInstance().getTissueBorder().loadNoMembrane();
		globalParameters = (LatticeBased3DModelGP)ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		
	}
	
	public HexagonBased3DMechModelInit(SimulationStateData simulationStateData){
		super(simulationStateData);		
	}


   protected ArrayList<UniversalCell> buildStandardInitialCellEnsemble() {
   	return regularInitialization();
   	//return testInitialization();
   }

   
   private  ArrayList<UniversalCell> regularInitialization() {
   	ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();
				
		int width = globalParameters.getNumber_of_columns();
		int height = globalParameters.getNumber_of_rows();
		int length= globalParameters.getNumber_of_columns();
		int delta = globalParameters.getNumber_of_initially_occupied_layers()/2;
		EpisimCellType[] cellTypes =ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableCellTypes();	
		for(int z = ((length/2)-delta); z < ((length/2)+delta); z++){
			for(int y = height-1 ;y > ((height-1)-delta); y--){			
				for(int x = ((width/2)-delta); x < ((width/2)+delta); x++){
					UniversalCell cell = new UniversalCell(null, null, true);
					((LatticeBased3DModel) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double3D(x, y, z));
					if(cellTypes.length >0) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[0]);
					standardCellEnsemble.add(cell);
				}
			}
		}
		for(int z = ((length/2)-delta); z < ((length/2)+delta); z++){
			for(int y = 0 ;y < delta; y++){			
				for(int x = ((width/2)-delta); x < ((width/2)+delta); x++){
					UniversalCell cell = new UniversalCell(null, null, true);
					((LatticeBased3DModel) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double3D(x, y, z));
					if(cellTypes.length >0) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[0]);
					standardCellEnsemble.add(cell);
				}
			}
		}
		if(globalParameters.getAddSecretingCellColony())addSekretionCellColony(standardCellEnsemble);
		return standardCellEnsemble;
   }
   
   private  ArrayList<UniversalCell> testInitialization() {
   	ArrayList<UniversalCell> standardCellEnsemble = new ArrayList<UniversalCell>();
		UniversalCell cell = new UniversalCell(null, null, true);
		LatticeBased3DModel mechModel =((LatticeBased3DModel) cell.getEpisimBioMechanicalModelObject());
		mechModel.setCellLocationInCellField(new Double3D(0, 0, 0));
		mechModel.setSpreadingLocation(new Int3D(1,1,1));
		standardCellEnsemble.add(cell);
		
		return standardCellEnsemble;
   }
   
   
   protected void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble) {
		// is not needed int this model
   }

	
   protected EpisimPortrayal getCellPortrayal() {
   	Object cellField = ModelController.getInstance().getBioMechanicalModelController().getCellField();
	   if(cellField instanceof LatticeCellField3D){
   	 return ((LatticeCellField3D) cellField).getCellFieldPortrayal();
	   }
	   return null;
   }

	
   protected EpisimPortrayal[] getAdditionalPortrayalsCellForeground() {

	   // TODO Auto-generated method stub
	   return new EpisimPortrayal[0];
   }

	
   protected EpisimPortrayal[] getAdditionalPortrayalsCellBackground() {
	   
	   return new EpisimPortrayal[0];
   }
   
   private void addSekretionCellColony(ArrayList<UniversalCell> standardCellEnsemble){
	
		EpisimCellType[] cellTypes =ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableCellTypes();	
		
		int width = (int) (globalParameters.getNumber_of_columns());
		int height = globalParameters.getNumber_of_rows();
		int length= (int) (globalParameters.getNumber_of_columns());
		int delta = (int)globalParameters.getNumber_of_initially_occupied_layers()/3;
		delta = delta==0 ? 1 : delta;
		for(int z = ((length/2)-delta); z < ((length/2)+delta); z++){
			for(int y = ((height/2)-delta);y < ((height/2)+delta); y++){			
				for(int x = ((width/2)-delta); x < ((width/2)+delta); x++){
					UniversalCell cell = new UniversalCell(null, null, true);
					((LatticeBased3DModel) cell.getEpisimBioMechanicalModelObject()).setCellLocationInCellField(new Double3D(x, y, z));
					if(cellTypes.length >1) cell.getEpisimCellBehavioralModelObject().setCellType(cellTypes[1]);
					standardCellEnsemble.add(cell);
				}
			}
		}
   }
   
}
