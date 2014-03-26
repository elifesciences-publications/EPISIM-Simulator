package sim.app.episim.model.biomechanics.hexagonbased3d;

import java.util.HashMap;

import javax.vecmath.Vector3d;

import episiminterfaces.EpisimPortrayal;
import sim.app.episim.AbstractCell;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.biomechanics.vertexbased.geom.Line;
import sim.app.episim.model.visualization.HexagonalCellGridPortrayal3D;
import sim.field.grid.Grid3D;
import sim.field.grid.ObjectGrid3D;
import sim.field.grid.SparseGrid3D;
import sim.util.Bag;
import sim.util.Double3D;
import sim.util.Int3D;
import sim.util.IntBag;


public class HexagonalCellField3D{
	
	private ObjectGrid3D objectGridCellField;
	private SparseGrid3D sparseGridCellField;
	private HashMap<Long, Line3D> spreadingLineRegistry;
	
	public HexagonalCellField3D(int width, int height, int length){
		objectGridCellField = new ObjectGrid3D(width, height, length);
		sparseGridCellField = new SparseGrid3D(width, height, length);
		spreadingLineRegistry = new HashMap<Long, Line3D>();
	}
	
	public void setFieldLocationOfObject(Int3D location, AbstractCell cell){
		objectGridCellField.field[location.x][location.y][location.z] = cell;
		if(cell != null && spreadingLineRegistry.containsKey(cell.getID()))spreadingLineRegistry.remove(cell.getID());
		sparseGridCellField.setObjectLocation(cell, location);
	}
	
	public void setSpreadingLocationOfObject(Int3D fieldLocation, Int3D spreadingLocation, AbstractCell cell){
		if(objectGridCellField.field[spreadingLocation.x][spreadingLocation.y][spreadingLocation.z] != null){
			UniversalCell oldCell = (UniversalCell)objectGridCellField.field[spreadingLocation.x][spreadingLocation.y][spreadingLocation.z];
			if((cell != null && oldCell.getID() != cell.getID()) || cell==null)this.spreadingLineRegistry.remove(oldCell.getID());			
		}
		
		objectGridCellField.field[spreadingLocation.x][spreadingLocation.y][spreadingLocation.z] = cell;
		if(cell != null){
			Double3D fieldLocMikron = getLocationInMikron(fieldLocation);
			Double3D spreadingLocMikron = getLocationInMikron(spreadingLocation);
			this.spreadingLineRegistry.put(cell.getID(), new Line3D(new Vector3d(fieldLocMikron.x, fieldLocMikron.y, fieldLocMikron.z), 
																					  new Vector3d(spreadingLocMikron.x, spreadingLocMikron.y, spreadingLocMikron.z)));
		}
	}
	
	public boolean hasPotentialSpreadingLocationIntersectionWithOtherCell(Int3D fieldLocation, Int3D spreadingLocation){
		Double3D fieldLocMikron = getLocationInMikron(fieldLocation);
		Double3D spreadingLocMikron = getLocationInMikron(spreadingLocation);
		Line3D line = new Line3D(new Vector3d(fieldLocMikron.x, fieldLocMikron.y, fieldLocMikron.z), 
				  new Vector3d(spreadingLocMikron.x, spreadingLocMikron.y, spreadingLocMikron.z));
		for(Line3D otherLine : this.spreadingLineRegistry.values()){
			if(line.lineLineIntersect(otherLine, HexagonBased3DMechanicalModelGP.hexagonal_radius*0.5)) return true;
		}
		
		return false;
	}
	
	
	public EpisimPortrayal getCellFieldPortrayal(){
		HexagonalCellGridPortrayal3D portrayal = new HexagonalCellGridPortrayal3D(2*HexagonBased3DMechanicalModelGP.hexagonal_radius);
		portrayal.setField(sparseGridCellField);
		return portrayal;
	}
	
	public int getWidth(){
		return objectGridCellField.getWidth();
	}
	public int getHeight(){
		return objectGridCellField.getHeight();
	}
	public int getLength(){
		return objectGridCellField.getLength();
	}
	
	public void clear(){
		objectGridCellField.clear();
		sparseGridCellField.clear();
		spreadingLineRegistry.clear();
	}
	
	public Bag getNeighborsMaxDistance(int x, int y, int z, int dist, boolean toroidal, Bag result, IntBag xPos, IntBag yPos, IntBag zPos){
		return objectGridCellField.getMooreNeighbors(x, y, z, dist, toroidal ? Grid3D.TOROIDAL : Grid3D.BOUNDED, false, result, xPos, yPos, zPos);
	}
	public void getNeighborLocationsMaxDistance(int x, int y, int z, int dist, boolean toroidal, IntBag xPos, IntBag yPos, IntBag zPos){
		objectGridCellField.getMooreLocations( x, y, z, dist, toroidal ? Grid3D.TOROIDAL : Grid3D.BOUNDED, false, xPos, yPos, zPos );
	}
	
	
	public Bag getNeighborsHamiltonianDistance(int x, int y, int z, int dist, boolean toroidal, Bag result, IntBag xPos, IntBag yPos, IntBag zPos){
		return objectGridCellField.getVonNeumannNeighbors(x, y, z, dist, toroidal ? Grid3D.TOROIDAL : Grid3D.BOUNDED, false,result, xPos, yPos, zPos);
	}
	public final Object get(final int x, final int y, final int z){
		return objectGridCellField.get(x, y, z);
	}
	
	public int tx(int x){
		return objectGridCellField.tx(x);
	}
	public int ty(int y){
		return objectGridCellField.ty(y);
	}
	public int tz(int z){
		return objectGridCellField.tz(z);
	}
	
	public int stx(int x){
		return objectGridCellField.stx(x);
	}
	public int sty(int y){
		return objectGridCellField.sty(y);
	}
	public int stz(int z){
		return objectGridCellField.stz(z);
	}
	
	private Double3D getLocationInMikron(Int3D location){
		double x=-1, y =-1, z=-1;
		if(location !=null){
			double locX = (double) location.x;
			double locY = (double) location.y;
			double locZ = (double) location.z;
			x = HexagonBased3DMechanicalModelGP.hexagonal_radius + (locX)*(2d*HexagonBased3DMechanicalModelGP.hexagonal_radius);
			y = HexagonBased3DMechanicalModelGP.hexagonal_radius + (locY)*(2d*HexagonBased3DMechanicalModelGP.hexagonal_radius);
			z = HexagonBased3DMechanicalModelGP.hexagonal_radius + (locZ)*(2d*HexagonBased3DMechanicalModelGP.hexagonal_radius);
			
		}
		return new Double3D(x, y, z);
	}

}
