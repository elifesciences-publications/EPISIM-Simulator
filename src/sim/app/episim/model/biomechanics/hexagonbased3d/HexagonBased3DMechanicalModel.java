package sim.app.episim.model.biomechanics.hexagonbased3d;

import java.awt.Shape;
import java.awt.geom.GeneralPath;

import episimbiomechanics.EpisimModelConnector;
import sim.app.episim.AbstractCell;
import sim.app.episim.model.biomechanics.AbstractMechanicalModel;
import sim.app.episim.model.biomechanics.CellBoundaries;
import sim.app.episim.util.GenericBag;
import sim.portrayal.DrawInfo2D;
import sim.util.Double2D;


public class HexagonBased3DMechanicalModel extends AbstractMechanicalModel {

	@Override
   public void setEpisimModelConnector(EpisimModelConnector modelConnector) {

	   // TODO Auto-generated method stub
	   
   }

	@Override
   public GenericBag<AbstractCell> getRealNeighbours() {

	   // TODO Auto-generated method stub
	   return null;
   }

	@Override
   public Shape getPolygonCell() {

	   // TODO Auto-generated method stub
	   return null;
   }

	@Override
   public Shape getPolygonCell(DrawInfo2D info) {

	   // TODO Auto-generated method stub
	   return null;
   }

	@Override
   public Shape getPolygonNucleus() {

	   // TODO Auto-generated method stub
	   return null;
   }

	@Override
   public Shape getPolygonNucleus(DrawInfo2D info) {

	   // TODO Auto-generated method stub
	   return null;
   }

	@Override
   public CellBoundaries getCellBoundariesInMikron() {

	   // TODO Auto-generated method stub
	   return null;
   }

	@Override
   public void newSimStep(long simStepNumber) {

	   // TODO Auto-generated method stub
	   
   }

	@Override
   public double getX() {

	   // TODO Auto-generated method stub
	   return 0;
   }

	@Override
   public double getY() {

	   // TODO Auto-generated method stub
	   return 0;
   }

	@Override
   public double getZ() {

	   // TODO Auto-generated method stub
	   return 0;
   }

	@Override
   public void setLastDrawInfo2D(DrawInfo2D info) {

	   // TODO Auto-generated method stub
	   
   }

	@Override
   protected void removeCellsInWoundArea(GeneralPath woundArea) {

	   // TODO Auto-generated method stub
	   
   }

	@Override
   protected void clearCellField() {

	   // TODO Auto-generated method stub
	   
   }

	@Override
   public void removeCellFromCellField() {

	   // TODO Auto-generated method stub
	   
   }

	@Override
   public void setCellLocationInCellField(Double2D location) {

	   // TODO Auto-generated method stub
	   
   }

	@Override
   public Double2D getCellLocationInCellField() {

	   // TODO Auto-generated method stub
	   return null;
   }

	@Override
   protected Object getCellField() {

	   // TODO Auto-generated method stub
	   return null;
   }

	@Override
   protected void setReloadedCellField(Object cellField) {

	   // TODO Auto-generated method stub
	   
   }

	@Override
   public EpisimModelConnector getEpisimModelConnector() {

	   // TODO Auto-generated method stub
	   return null;
   }

	@Override
   protected void newSimStepGloballyFinished(long simStepNumber) {

	   // TODO Auto-generated method stub
	   
   }

}
