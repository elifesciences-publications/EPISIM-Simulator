package sim.app.episim.model.biomechanics.vertexbased;

import java.awt.Color;
import java.awt.Polygon;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import ec.util.MersenneTwisterFast;
import episiminterfaces.CellPolygonProliferationSuccessListener;

import sim.app.episim.model.biomechanics.vertexbased.GlobalBiomechanicalStatistics.GBSValue;
import sim.app.episim.model.biomechanics.vertexbased.VertexChangeEvent.VertexChangeEventType;
import sim.app.episim.model.biomechanics.vertexbased.simanneal.VertexForcesMinimizerSimAnneal;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EnhancedSteppable;
import sim.app.episim.util.ListenerAction;
import sim.engine.SimState;

public class CellPolygon implements VertexChangeListener, EnhancedSteppable{
	
 private static int nextId = 1;
 private final int id;
 private double x = 0;
 private double y = 0;
 private boolean isProliferating = false;
 private boolean isDying = false;
 private double preferredArea;
 
 private double originalPreferredArea = Double.NEGATIVE_INFINITY;
	
 private HashSet<Vertex> vertices;
 private HashSet<CellPolygonProliferationSuccessListener> cellProliferationAndApoptosisListener;
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
	cellProliferationAndApoptosisListener = new HashSet<CellPolygonProliferationSuccessListener>();
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


public void addProliferationAndApoptosisListener(CellPolygonProliferationSuccessListener listener){
	cellProliferationAndApoptosisListener.add(listener);
}

public void removeProliferationAndApoptosisListener(CellPolygonProliferationSuccessListener listener){
	cellProliferationAndApoptosisListener.remove(listener);
}

private void notifyAllCellProliferationAndApoptosisListener(ListenerAction<CellPolygonProliferationSuccessListener> action){
	for(CellPolygonProliferationSuccessListener listener :cellProliferationAndApoptosisListener){
		action.performAction(listener);
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



private void growForProliferation(double areaToGrow){
	if(this.originalPreferredArea == Double.NEGATIVE_INFINITY) this.originalPreferredArea = this.preferredArea;
	this.preferredArea += areaToGrow;
	
}

private void growToBecomeMature(double areaToGrow){
	
	this.preferredArea += areaToGrow;
	
}

private void relaxVertices(){
	if(this.preferredArea >0){
		Vertex[] cellVertices =	this.getUnsortedVertices();
		List<Vertex> verticesList = Arrays.asList(cellVertices);
		Collections.shuffle(verticesList);
		cellVertices = verticesList.toArray(new Vertex[verticesList.size()]);
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
	}
}


public boolean canDivide(){
	return getCurrentArea() >= ((2*originalPreferredArea)* VertexBasedMechanicalModelGlobalParameters.getInstance().getSize_percentage_cell_division()) 
	 									&& originalPreferredArea > 0;
}

private CellPolygon cellDivision(){
	sortedVertices = null;
	this.preferredArea /= 2;
	
	CellPolygon daughterCell =calculator.divideCellPolygon(this);
	if(daughterCell != null){
		daughterCell.preferredArea = this.preferredArea;
		daughterCell.originalPreferredArea = this.originalPreferredArea;
	}
	return daughterCell;
}

public Line[] getLinesOfCellPolygon(){
	int size = vertices.size();
	Vertex[] vSorted = getSortedVertices();
	Line[] cellPolLines = new Line[vSorted.length];
	//if(vSorted.length != size) System.out.println("Array und HashSet der Vertices im Cell Polygon stimmen nicht überein.");
	for(int i = 0; i < vSorted.length;i++){
		cellPolLines[i] = new Line(vSorted[i], vSorted[((i+1)%vSorted.length)]);
	}
	return cellPolLines;
}

private void checkProliferation(){
	if(isProliferating && canDivide()){
		GlobalBiomechanicalStatistics.getInstance().set(GBSValue.PREF_AREA_OVERHEAD, preferredArea - originalPreferredArea);
		final CellPolygon daughterCell = cellDivision();
		isProliferating = false;
		notifyAllCellProliferationAndApoptosisListener(new ListenerAction<CellPolygonProliferationSuccessListener>(){
					public void performAction(CellPolygonProliferationSuccessListener listener){
						listener.proliferationCompleted(CellPolygon.this, daughterCell);
					}
				});
		
	}
	else{ 
		if(isProliferating){ 
			growForProliferation(VertexBasedMechanicalModelGlobalParameters.getInstance().getGrowth_rate_per_sim_step());
		}
		else{
			if(this.preferredArea < this.originalPreferredArea){
				growToBecomeMature(VertexBasedMechanicalModelGlobalParameters.getInstance().getGrowth_rate_per_sim_step());
			}
			else this.originalPreferredArea = Double.NEGATIVE_INFINITY; 
		}
	}	
}

private void checkApoptosis(){
	if(isDying && this.preferredArea > 0){
		this.originalPreferredArea = Double.NEGATIVE_INFINITY;
		this.preferredArea -= VertexBasedMechanicalModelGlobalParameters.getInstance().getGrowth_rate_per_sim_step();
	}
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

public boolean isDying() {
	return isDying;
}


public void initializeApoptosis() {
	this.isDying = true;
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

protected void apoptosis(){
	notifyAllCellProliferationAndApoptosisListener(new ListenerAction<CellPolygonProliferationSuccessListener>(){
		public void performAction(CellPolygonProliferationSuccessListener listener){
			listener.apoptosisCompleted(CellPolygon.this);
		}
	});
}



public void setIsAlreadyCalculated(boolean val){this.isAlreadyCalculated = val;}
public boolean isAlreadyCalculated(){ return this.isAlreadyCalculated;}


public boolean hasContactToBasalLayer(){
	for(Vertex v :this.vertices){ 
		if(v.isAttachedToBasalLayer()) return true;
	}
	
	return false;
}

public boolean hasContactToCellThatIsAttachedToBasalLayer(){
	if(!this.hasContactToBasalLayer()){
		for(Vertex v :this.vertices){ 
			for(CellPolygon cell : v.getCellsJoiningThisVertex()){
				if(!cell.equals(this) && cell.hasContactToBasalLayer()) return true;
			}
		}
	}
	return false;
}

public void step(SimState state) {

	checkProliferation();
	checkApoptosis();
	relaxVertices();
	calculator.applyCellPolygonCheckPipeline(this);
	
}

public double getInterval() {	
	return 1;
}

public Polygon getPolygon(){
	Polygon p = new Polygon();		
	
	for(Vertex v : getSortedVertices()){	
		p.addPoint(v.getIntX(), v.getIntY());
		
	}
	return p;
}

}
