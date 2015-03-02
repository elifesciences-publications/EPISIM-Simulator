package sim.app.episim.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.SimStateChangeListener;
import sim.app.episim.model.biomechanics.vertexbased2Dr.geom.CellPolygon;
import sim.app.episim.model.biomechanics.vertexbased2Dr.geom.Vertex;
import sim.app.episim.visualization.AbstractCellEllipse;
import sim.app.episim.visualization.CellEllipse;


public class CellEllipseIntersectionCalculationRegistry implements SimStateChangeListener, ClassLoaderChangeListener {
	
	private long actSimulationStep = 0;
	
	private Set<String> alreadyCalculatedCells;
	
	private Set<String> intersectingCells;
	
	private static final char SEPARATORCHAR = ';';
	
	private Map<Long, AbstractCellEllipse> cellEllipseRegistry;
	private Map<Long, CellPolygon> cellPolygonRegistry;
	
	private static Semaphore sem = new Semaphore(1);
	private CellEllipseIntersectionCalculationRegistry(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		alreadyCalculatedCells = new HashSet<String>();
		intersectingCells = new HashSet<String>();
		cellEllipseRegistry = new HashMap<Long, AbstractCellEllipse>();
		cellPolygonRegistry = new HashMap<Long, CellPolygon>();
		
	}
	
	private static CellEllipseIntersectionCalculationRegistry instance;
	
	public static CellEllipseIntersectionCalculationRegistry getInstance(){ 
		if(instance==null){
			try{
	         sem.acquire();
	         instance = new CellEllipseIntersectionCalculationRegistry();				
				sem.release();
         }
         catch (InterruptedException e){
	        EpisimExceptionHandler.getInstance().displayException(e);
         }
				
		}	
		return instance; 
	}
	
	public void addCellEllipseIntersectionCalculation(long idCell1, long idCell2){
		this.alreadyCalculatedCells.add(buildStringId(idCell1, idCell2));
		this.alreadyCalculatedCells.add(buildStringId(idCell2, idCell1));
	}
	
	public void registerCellEllipse(AbstractCellEllipse ellipse){
		cellEllipseRegistry.put(ellipse.getId(), ellipse);
	}
	
	public AbstractCellEllipse getCellEllipse(long id){
		return cellEllipseRegistry.get(id);
	}
	
	public void registerCellPolygonByCellEllipseId(long cellEllipseId, CellPolygon polygon){
		cellPolygonRegistry.put(cellEllipseId, polygon);
	}
	
	public CellPolygon getCellPolygonByCellEllipseId(long id){
		return cellPolygonRegistry.get(id);
	}
	
	
	public void addIntersectionCellEllipses(long idCell1, long idCell2){
		this.intersectingCells.add(buildStringId(idCell1, idCell2));
		this.intersectingCells.add(buildStringId(idCell2, idCell1));
	}
	
	
	
	public boolean isAreadyCalculated(long idCell1, long idCell2, long actSimStep){
		 return checkCondition(idCell1, idCell2, actSimStep, alreadyCalculatedCells);			
	}
	
	public boolean doCellIntersect(long idCell1, long idCell2, long actSimStep){
		 return checkCondition(idCell1, idCell2, actSimStep, intersectingCells);			
	}
	
	private boolean checkCondition(long idCell1, long idCell2, long actSimStep, Set<String> conditionSet){
		if(actSimStep > actSimulationStep){
			actSimulationStep = actSimStep;
			conditionSet.clear();
			cellPolygonRegistry.clear();
		}
		else if(actSimStep < actSimulationStep){
			throw new IllegalStateException("The current Sim Step is: " + actSimulationStep + " The submitted Sim Step to calculate was: " + actSimStep);
		}
		
		return conditionSet.contains(buildStringId(idCell1, idCell2)) || conditionSet.contains(buildStringId(idCell2, idCell1));		
	}
	
	public int getNeighbourNumber(long cellId){
		String id = ""+cellId+SEPARATORCHAR;
		int neighbourNo = 0;
		for(String idString : intersectingCells){
			if(idString.startsWith(id)) neighbourNo++;
		}
			
		return neighbourNo;	
	}
	
	private String buildStringId(long cellId1, long cellId2){
		StringBuffer stringBuilder = new StringBuffer();
		stringBuilder.append(cellId1);
		stringBuilder.append(SEPARATORCHAR);
		stringBuilder.append(cellId2);		
		return stringBuilder.toString(); 	
	}
	
	public void reset(){
		actSimulationStep = 0;
		this.alreadyCalculatedCells.clear();
		this.intersectingCells.clear();
		
	}

	public void simulationWasStarted() {

	   reset();
	  
   }
	
	public Vertex[] getAllCellEllipseVertices(){
		HashSet<Vertex> vertices = new HashSet<Vertex>();
		for(CellPolygon pol: this.cellPolygonRegistry.values()){
			vertices.addAll(Arrays.asList(pol.getUnsortedVertices())); 
		}
		Vertex[] verticesArray = new Vertex[vertices.size()];
		//System.out.println("There are " + vertices.size() + "Vertices");
		return vertices.toArray(verticesArray);
		
	}
	
	public CellPolygon[] getAllCellPolygons(){
	  CellPolygon[] cellPols = new CellPolygon[this.cellPolygonRegistry.values().size()];
	  return this.cellPolygonRegistry.values().toArray(cellPols);
	}
	
	
	public void simulationWasStopped() {
	   reset();
	   cellEllipseRegistry.clear();
	   cellPolygonRegistry.clear();
   }
	
	public void simulationWasPaused(){}

	
   public void classLoaderHasChanged() {
		instance = null;	   
   }

}
