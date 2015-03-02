package sim.app.episim.model.biomechanics.latticebased2D.bact;

import sim.app.episim.model.biomechanics.latticebased2D.LatticeBased2DModelGP;


public class LatticeBased2DModelBactGP extends LatticeBased2DModelGP{
	private boolean addErythrocyteColony=false;
	
	public boolean getAddErythrocyteColony(){ return addErythrocyteColony; }	
   public void setAddErythrocyteColony(boolean addErythrocyteColony) {
	   this.addErythrocyteColony = addErythrocyteColony;
   }
}
