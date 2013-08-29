package sim.app.episim.model.biomechanics.hexagonbased.singlesurface.bact;

import sim.app.episim.model.biomechanics.hexagonbased.singlesurface.HexagonBasedMechanicalModelGP;


public class HexagonBasedMechanicalModelBactGP extends HexagonBasedMechanicalModelGP{
	private boolean addErythrocyteColony=false;
	
	public boolean getAddErythrocyteColony(){ return addErythrocyteColony; }	
   public void setAddErythrocyteColony(boolean addErythrocyteColony) {
	   this.addErythrocyteColony = addErythrocyteColony;
   }
}
