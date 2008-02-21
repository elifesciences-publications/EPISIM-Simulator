package sim;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import episiminterfaces.EpisimCellDiffModel;
import sim.app.episim.CellType;
import sim.engine.SimState;
import java.util.*;

public class Dummy extends CellType{

	@Override
   public String getCellName() {

	   
	   return "Dummy Cell Type";
   }

	@Override
   public Class<? extends EpisimCellDiffModel> getEpisimCellDiffModelClass() {


	   return null;
   }
	
	public int getBert(){ return 1;}
	public void getTiffy(){}
	public int getErnie(){ return 1;}
	
	@Override
   public List<Method> getParameters() {
		
	  	   return Arrays.asList(this.getClass().getMethods());
   }

	public void step(SimState state) {

	   // TODO Auto-generated method stub
	   
   }

	public void stop() {

	   // TODO Auto-generated method stub
	   
   }

	public double orientation2D() {

	   // TODO Auto-generated method stub
	   return 0;
   }

}
