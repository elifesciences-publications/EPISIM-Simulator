package sim.app.episim;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import sim.engine.SimState;
import episiminterfaces.EpisimCellDiffModel;


public class DummyCell extends CellType{
	public DummyCell(){
	   this(-1, -1);
	   }
	public DummyCell(long identity, long motherIdentity) {

	   super(identity, motherIdentity);
	   // TODO Auto-generated constructor stub
   }
	public boolean isMitotic() { return true;}
	public int getAge(){ return 0;}
	@Override
   public String getCellName() {

	   // TODO Auto-generated method stub
	   return "DummyCellType";
   }
	@Override
   public Class<? extends EpisimCellDiffModel> getEpisimCellDiffModelClass() {

	   // TODO Auto-generated method stub
	   return null;
   }
	@Override
   public EpisimCellDiffModel getEpisimCellDiffModelObject() {

	   // TODO Auto-generated method stub
	   return null;
   }
	@Override
   public List<Method> getParameters() {
		List<Method> methods = new ArrayList<Method>();
		
		for(Method m : this.getClass().getMethods()){
			if((m.getName().startsWith("get") && ! m.getName().equals("getParameters")) || m.getName().startsWith("is")) methods.add(m);
		}
		
		return methods;
	   
   }
	@Override
   public void killCell() {

	   // TODO Auto-generated method stub
	   
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
	@Override
   public SimState getActSimState() {

	   // TODO Auto-generated method stub
	   return null;
   }
	

}
