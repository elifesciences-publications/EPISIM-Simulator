package sim.app.episim.tissue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import sim.app.episim.CellType;
import sim.app.episim.datamonitoring.charts.ChartSetChangeListener;
import sim.engine.Schedule;
import sim.engine.SimStateHack;


public abstract class TissueType extends SimStateHack implements java.io.Serializable, ChartSetChangeListener{
	
	private List<Class<? extends CellType>> registeredCellTypes;
	
	public TissueType(long seed){ 
		super(new ec.util.MersenneTwisterFast(seed), new Schedule(1));
		registeredCellTypes = new ArrayList<Class<? extends CellType>>();
	}
	
	public abstract String getTissueName();
	
	
	public abstract List<Method> getParameters();
	
	public List <Class<? extends CellType>> getRegiseredCellTypes(){
		return this.registeredCellTypes;
	}
	
	public void registerCellType(Class<? extends CellType> celltype){
		this.registeredCellTypes.add(celltype);
	}
	
	

}
