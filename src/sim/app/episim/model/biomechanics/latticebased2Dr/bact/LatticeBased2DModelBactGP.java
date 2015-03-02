package sim.app.episim.model.biomechanics.latticebased2Dr.bact;

import sim.app.episim.model.biomechanics.latticebased2Dr.LatticeBased2DModelGP;


public class LatticeBased2DModelBactGP extends LatticeBased2DModelGP{
	private boolean addErythrocyteColony=false;
	
	public boolean getAddErythrocyteColony(){ return addErythrocyteColony; }	
   public void setAddErythrocyteColony(boolean addErythrocyteColony) {
	   this.addErythrocyteColony = addErythrocyteColony;
   }
}
