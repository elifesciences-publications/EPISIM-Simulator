package sim.app.episim.model.biomechanics.vertexbased;

import java.awt.Color;
import java.util.HashSet;

import ec.util.MersenneTwisterFast;
import episiminterfaces.CellPolygonProliferationSuccessListener;

import sim.app.episim.model.biomechanics.vertexbased.GlobalBiomechanicalStatistics.GBSValue;
import sim.app.episim.model.biomechanics.vertexbased.VertexChangeEvent.VertexChangeEventType;
import sim.app.episim.model.biomechanics.vertexbased.simanneal.VertexForcesMinimizerSimAnneal;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EnhancedSteppable;
import sim.engine.SimState;

public class CellPolygon implements VertexChangeListener, EnhancedSteppable{
	
 private static int nextId = 1;
 private final int id;
 private double x = 0;
 private double y = 0;
 private boolean isProliferating = false;
 
 private double preferredArea;
 
 private double originalPreferredArea = Double.NEGATIVE_INFINITY;
	
 private HashSet<Vertex> vertices;
 private HashSet<CellPolygonProliferationSuccessListener> cellProliferationSuccessListener;
 private Vertex[] sortedVertices;
 private boolean isAlreadyCalculated;
 
 private boolean isVertexSortingDirty = true;
 
 private MersenneTwisterFast rand = new ec.util.MersenneTwisterFast(System.currentTimeMillis());
 private ConjugateGradientOptimizer conGradientOptimizer;
 private VertexForcesMinimizerSimAnneal simAnnealOptimizer;
 private CellPolygonCalculator calculator;
 
protected CellPolygon(double x, double y){
	id = nextId++;
	vertices = new HashSet<Vertex>();
	cellProliferationSuccessListener = new HashSet<CellPolygonProliferationSuccessListener>();
	this.x = x;
	this.y = y;	
	conGradientOptimizer = new ConjugateGradientOptimizer();
   simAnnealOptimizer = new VertexForcesMinimizerSimAnneal();
}

public CellPolygon(CellPolygonCalculator calculator){
	this(0, 0);
	if(calculator == null) throw new IllegalArgumentException("Cell Polygon Calculator must not be null");
	this.calculator = calculator;
	
}

public void setCellPolygonCalculator(CellPolygonCalculator calculator){
	if(calculator != null) this.calculator = calculator;
}


public void addProliferationSuccessListener(CellPolygonProliferationSuccessListener listener){
	cellProliferationSuccessListener.add(listener);
}

public void removeProliferationSuccessListener(CellPolygonProliferationSuccessListener listener){
	cellProliferationSuccessListener.remove(listener);
}

private void notifyAllCellProliferationSuccessListener(CellPolygon daughterCell){
	for(CellPolygonProliferationSuccessListener listener :cellProliferationSuccessListener){
		listener.proliferationCompleted(daughterCell);
	}
}


public void addVertex(Vertex v){
	if(v != null &&  !vertices.contains(v) && !v.isWasDeleted()){
		vertices.add(v);
		v.addVertexChangeListener(this);
		isVertexSortingDirty = true;
	}
}

public void removeVertex(Vertex v){
	if(vertices.contains(v)){
		vertices.remove(v);
		v.removeVertexChangeListener(this);
		isVertexSortingDirty = true;
	}
}

public int getId(){ return id;}


public int hashCode() {

	final int prime = 31;
	int result = 1;
	result = prime * result + id;
	return result;
}

public Vertex[] getUnsortedVertices(){ return vertices.toArray(new Vertex[vertices.size()]); }

/*public void sortVerticesWithGrahamScan(){
	GrahamScan scan = new GrahamScan();
	Vertex[] v =  vertices.toArray(new Vertex[vertices.size()]);
	int h = scan.computeHull(v);
	vertices.clear();
	for(Vertex ver : v) vertices.add(ver);	
	
	//System.out.println("No of vertices: " + v.length + "    No of Hull Points: " + h);
}

public Vertex[] getSortedVerticesUsingGrahamScan(){
	GrahamScan scan = new GrahamScan();
	Vertex[] v =  vertices.toArray(new Vertex[vertices.size()]);
	int h = scan.computeHull(v);
	return v;
}*/





private void growForProliferation(double areaToGrow){
	if(this.originalPreferredArea == Double.NEGATIVE_INFINITY) this.originalPreferredArea = this.preferredArea;
	this.preferredArea += areaToGrow;
	
}

public boolean canDivide(){
	return getCurrentArea() >= (2*originalPreferredArea) && originalPreferredArea > 0;
}

private CellPolygon cellDivision(){
	sortedVertices = null;
	this.preferredArea = this.originalPreferredArea;
	this.originalPreferredArea = Double.NEGATIVE_INFINITY;
	CellPolygon daughterCell =calculator.divideCellPolygon(this);
	if(daughterCell != null){
		daughterCell.setPreferredArea(this.preferredArea);
		
	}
	return daughterCell;
}


public Vertex[] getSortedVertices(){
	if(isVertexSortingDirty || sortedVertices == null) sortVerticesUsingTravellingSalesmanSimulatedAnnealing();
	return sortedVertices;
}

private void sortVerticesUsingTravellingSalesmanSimulatedAnnealing(){
	SimulatedAnnealingForOrderingVertices sim = new SimulatedAnnealingForOrderingVertices(getUnsortedVertices());
	sortedVertices = sim.sortVertices();	
	isVertexSortingDirty = false;
}

public void handleVertexChangeEvent(VertexChangeEvent event) {
	if(event.getType() == VertexChangeEventType.VERTEXDELETED){ 
		removeVertex(event.getSource());
		isVertexSortingDirty = true;
	}
	else if(event.getType() == VertexChangeEventType.VERTEXREPLACED){
		removeVertex(event.getSource());
		addVertex(event.getNewVertex());
		isVertexSortingDirty = true;
	}
	else if (event.getType() == VertexChangeEventType.VERTEXMOVED){
		isVertexSortingDirty = true;
	}
}


public int getNumberOfNeighbourPolygons(){
	return getNeighbourPolygons().length;
}


public CellPolygon[] getNeighbourPolygons(){
	HashSet<Integer> alreadyCheckedIds = new HashSet<Integer>();
	HashSet<CellPolygon> neighbourPolygonsSet = new HashSet<CellPolygon>();
	for(Vertex v: this.getUnsortedVertices()){
		if(v.getNumberOfCellsJoiningThisVertex() > 0){
			for(CellPolygon pol :v.getCellsJoiningThisVertex()){
				if(!alreadyCheckedIds.contains(pol.getId()) && pol.getId() != this.getId()){ 
					alreadyCheckedIds.add(pol.getId());
					neighbourPolygonsSet.add(pol);
				}
			}
		}
	}
	
	CellPolygon[] neighbourPolygonsArray = new CellPolygon[neighbourPolygonsSet.size()];
	
	return neighbourPolygonsSet.toArray(neighbourPolygonsArray);	
}


public double getCurrentArea(){
	return calculator.getCellArea(this);
}




public Color getFillColor() { 
	int neighbourNo = getNumberOfNeighbourPolygons();
	
		if(neighbourNo <= 3) return Color.WHITE;
		else if(neighbourNo == 4) return Color.GREEN;
		else if(neighbourNo == 5) return Color.YELLOW;
		else if(neighbourNo == 6) return Color.GRAY;
		else if(neighbourNo == 7) return Color.BLUE;
		else if(neighbourNo == 8) return Color.RED;
		else if(neighbourNo >= 9) return Color.PINK;
	
	 
	
	return Color.WHITE;
}



public boolean equals(Object obj) {

	if(this == obj)
		return true;
	
	if(obj == null)
		return false;
	
	if(getClass() != obj.getClass())
		return false;
	
	CellPolygon other = (CellPolygon) obj;
	
	if(id != other.id)
		return false;	
	
	return true;
}

protected double getX() { return x; }
protected void setX(double x) { this.x = x; }
protected double getY() { return y; }
protected void setY(double y){ this.y = y; }


public boolean isProliferating() {
	return isProliferating;
}


public void proliferate() {
	this.isProliferating = true;
}


public void resetCalculationStatusOfAllVertices(){
	for(Vertex v : this.getUnsortedVertices()){ 
		v.resetCalculationStatus();
	}
}


public double getPreferredArea() {
	return preferredArea;
}

public void setPreferredArea(double preferredArea) {
	this.preferredArea = preferredArea;
}

public void setIsAlreadyCalculated(boolean val){this.isAlreadyCalculated = val;}
public boolean isAlreadyCalculated(){ return this.isAlreadyCalculated;}

public void step(SimState state) {

	if(isProliferating && canDivide()){
		GlobalBiomechanicalStatistics.getInstance().set(GBSValue.PREF_AREA_OVERHEAD, preferredArea - originalPreferredArea);
		CellPolygon daughterCell = cellDivision();
		notifyAllCellProliferationSuccessListener(daughterCell);
		isProliferating = false;
	}
	else if(isProliferating) growForProliferation(VertexBasedMechanicalModelGlobalParameters.getInstance().getGrowth_rate_per_sim_step());
	
	
	Vertex[] cellVertices =	getSortedVertices();
	 int randomStartIndexVertices = rand.nextInt(cellVertices.length);
	
	for(int i = 0; i < cellVertices.length; i++){
		Vertex v = cellVertices[((i+randomStartIndexVertices)% cellVertices.length)];
		if(!v.isWasAlreadyCalculated()){					
			conGradientOptimizer.relaxVertex(v);			
			calculator.applyVertexPositionCheckPipeline(v);			
			v.commitNewValues();
			v.setWasAlreadyCalculated(true);
		}				
	}
	calculator.applyCellPolygonCheckPipeline(this);
	
}

public double getInterval() {	
	return 1;
}


}
