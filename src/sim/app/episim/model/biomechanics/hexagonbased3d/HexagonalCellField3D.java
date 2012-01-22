package sim.app.episim.model.biomechanics.hexagonbased3d;

import episiminterfaces.EpisimPortrayal;
import sim.app.episim.model.visualization.HexagonalCellGridPortrayal3D;
import sim.field.grid.ObjectGrid3D;
import sim.field.grid.SparseGrid3D;
import sim.util.Bag;
import sim.util.Int3D;
import sim.util.IntBag;


public class HexagonalCellField3D{
	
	private ObjectGrid3D objectGridCellField;
	private SparseGrid3D sparseGridCellField;
	
	public HexagonalCellField3D(int width, int height, int length){
		objectGridCellField = new ObjectGrid3D(width, height, length);
		sparseGridCellField = new SparseGrid3D(width, height, length);
	}
	
	public void setFieldLocationOfObject(Int3D location, Object obj){
		objectGridCellField.field[location.x][location.y][location.z] = obj;
		sparseGridCellField.setObjectLocation(obj, location);
	}
	
	public void setSpreadingLocationOfObject(Int3D location, Object obj){
		objectGridCellField.field[location.x][location.y][location.z] = obj;
	}
	
	
	public EpisimPortrayal getCellFieldPortrayal(){
		HexagonalCellGridPortrayal3D portrayal = new HexagonalCellGridPortrayal3D(2*HexagonBased3DMechanicalModelGP.outer_hexagonal_radius);
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
		objectGridCellField.clear();
	}
	
	public Bag getNeighborsMaxDistance(int x, int y, int z, int dist, boolean toroidal, Bag result, IntBag xPos, IntBag yPos, IntBag zPos){
		return objectGridCellField.getNeighborsMaxDistance(x, y, z, dist, toroidal, result, xPos, yPos, zPos);
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

}
