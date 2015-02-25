package episimbiomechanics.centerbased.newversion.chemotaxis;

import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.biomechanics.centerbased.newversion.CenterBasedMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased.newversion.chemotaxis.CenterBasedChemotaxisMechanicalModelGP;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import episimbiomechanics.centerbased.newversion.chemotaxis.CenterBasedMechModelInit;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimCellType;
import episiminterfaces.NoExport;

public class EpisimCenterBasedMC extends episimbiomechanics.centerbased.newversion.EpisimCenterBasedMC {
	
	private static final String ID = "2013-10-04";
	private static final String NAME = "New Center Based Chemotaxis Biomechanical Model";	
	
	private double adhesionDefaultCell=0;
	private double adhesionTCell=0;
	private double adhesionSecretoryCell=0;
		
	private String nameCellTypeDefaultCell="";
	private String nameCellTypeTCell="";
	private String nameCellTypeSecretoryCell="";
		
	private String chemotacticField="";
	private double lambdaChem=1;	
	
	public EpisimCenterBasedMC(){}
	
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
		return CenterBasedMechanicalModel.class;
	}
	
	@NoExport
	public Class<? extends EpisimBiomechanicalModelGlobalParameters> getEpisimBioMechanicalModelGlobalParametersClass(){
		return CenterBasedChemotaxisMechanicalModelGP.class;
	}
	
	@NoExport
	public Class<? extends BiomechanicalModelInitializer> getEpisimBioMechanicalModelInitializerClass(){
		return CenterBasedMechModelInit.class;
	}
	
	public double getAdhesionDefaultCell(){   
   	return adhesionDefaultCell;
   }

	
   public void setAdhesionDefaultCell(double adhesionDefaultCell){   
   	this.adhesionDefaultCell = adhesionDefaultCell;
   }

	
   public double getAdhesionTCell(){   
   	return adhesionTCell;
   }

	
   public void setAdhesionTCell(double adhesionTCell) {   
   	this.adhesionTCell = adhesionTCell;
   }

	
   public double getAdhesionSecretoryCell() {   
   	return adhesionSecretoryCell;
   }

	
   public void setAdhesionSecretoryCell(double adhesionSecretoryCell){   
   	this.adhesionSecretoryCell = adhesionSecretoryCell;
   }
	
   public String getNameCellTypeDefaultCell(){   
   	return nameCellTypeDefaultCell;
   }

	
   public void setNameCellTypeDefaultCell(String nameCellTypeDefaultCell){   
   	this.nameCellTypeDefaultCell = nameCellTypeDefaultCell;
   }
	
   public String getNameCellTypeTCell(){   
   	return nameCellTypeTCell;
   }
	
   public void setNameCellTypeTCell(String nameCellTypeTCell) {   
   	this.nameCellTypeTCell = nameCellTypeTCell;
   }
	
   public String getNameCellTypeSecretoryCell() {   
   	return nameCellTypeSecretoryCell;
   }
	
   public void setNameCellTypeSecretoryCell(String nameCellTypeSecretoryCell) {   
   	this.nameCellTypeSecretoryCell = nameCellTypeSecretoryCell;
   }	
   
	@Hidden
	@NoExport
   public double getAdhesionFactorForCell(AbstractCell cell){
		EpisimCellType cellType = cell.getEpisimCellBehavioralModelObject().getCellType();
   	if(cellType.name().equals(getNameCellTypeDefaultCell())) return getAdhesionDefaultCell();
   	else if(cellType.name().equals(getNameCellTypeTCell())) return getAdhesionTCell();
   	else if(cellType.name().equals(getNameCellTypeSecretoryCell())) return getAdhesionSecretoryCell();	
   	return 0;
   }   
   
   public double getLambdaChem() {
	   
		 return lambdaChem;
	}
		
   public void setLambdaChem(double lambdaChem) {	   
	  	this.lambdaChem = lambdaChem;
	}
   
   public String getChemotacticField(){	   
   	return this.chemotacticField;
   }
	
   public void setChemotacticField(String chemotacticField){   
   	if(chemotacticField != null)this.chemotacticField=chemotacticField;
   }
   
}

