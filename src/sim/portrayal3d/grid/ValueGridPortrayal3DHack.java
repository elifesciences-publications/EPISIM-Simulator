package sim.portrayal3d.grid;

import sim.field.grid.AbstractGrid2D;
import sim.field.grid.AbstractGrid3D;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.DoubleGrid3D;
import sim.field.grid.IntGrid2D;
import sim.field.grid.IntGrid3D;


public class ValueGridPortrayal3DHack extends ValueGridPortrayal3D{
	
	 public ValueGridPortrayal3DHack() 
    { 
    this("Value", 1); 
    }

	 public ValueGridPortrayal3DHack(String valueName) 
    { 
    this(valueName, 1); 
    }

	 public ValueGridPortrayal3DHack(double s) 
    { 
    this("Value", s); 
    }

	 public ValueGridPortrayal3DHack(String valueName, double scale) 
    { 
		 super(valueName, scale);
    }
	 
	 

}
