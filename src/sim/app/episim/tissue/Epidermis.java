package sim.app.episim.tissue;



import sim.app.episim.AbstractCell;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.UniversalCell;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.datamonitoring.charts.ChartController;
import sim.app.episim.datamonitoring.charts.DefaultCharts;
import sim.app.episim.datamonitoring.dataexport.DataExportController;

import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel;
import sim.app.episim.model.biomechanics.vertexbased.CellPolygonCalculator;
import sim.app.episim.model.biomechanics.vertexbased.CellPolygon;
import sim.app.episim.model.biomechanics.vertexbased.CellPolygonNetworkBuilder;
import sim.app.episim.model.controller.BiomechanicalModelController;
import sim.app.episim.model.controller.CellBehavioralModelController;
import sim.app.episim.model.controller.MiscalleneousGlobalParameters;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.snapshot.SnapshotListener;
import sim.app.episim.snapshot.SnapshotObject;
import sim.app.episim.snapshot.SnapshotWriter;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EnhancedSteppable;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.TysonRungeCuttaCalculator;
import sim.engine.*;
import sim.engine.SimStateHack.TimeSteps;
import sim.util.*;
import sim.field.continuous.*;

//Charts
import org.jfree.chart.JFreeChart;

//PDF Writer + ELSE
import java.awt.*; 
import java.awt.geom.*; 

//PDF Writer
import java.io.*;       
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.lowagie.text.*;  
import com.lowagie.text.pdf.*;  

import episimexceptions.MissingObjectsException;
import episiminterfaces.CellDeathListener;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.monitoring.CannotBeMonitored;

public class Epidermis extends TissueType implements CellDeathListener
{

//	---------------------------------------------------------------------------------------------------------------------------------------------------
// CONSTANTS
//--------------------------------------------------------------------------------------------------------------------------------------------------- 
		
	public final String NAME ="Epidermis";
	
//---------------------------------------------------------------------------------------------------------------------------------------------------
// VARIABLES
//--------------------------------------------------------------------------------------------------------------------------------------------------- 
	

	

	private Continuous2D cellContinous2D;
	private Continuous2D basementContinous2D;
	private Continuous2D rulerContinous2D;
	private Continuous2D gridContinous2D;
   

	
	// Percentage
	
	private transient List<EnhancedSteppable> chartSteppables = null;
	
	private transient List<EnhancedSteppable> dataExportSteppables = null;
	
	
	
	
	
//---------------------------------------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------------------------------------------------------- 
	 
 /** Creates a EpidermisClass simulation with the given random number seed. */
 public Epidermis(long seed)
 {
     super(seed);
     
    
     
     SnapshotWriter.getInstance().addSnapshotListener(this);
     EpisimCellType[] cellTypes = ModelController.getInstance().getCellBehavioralModelController().getAvailableCellTypes();
     if(cellTypes!= null){
   	  for(EpisimCellType epiType : cellTypes) this.registerCellType(epiType, UniversalCell.class); // Currently the same class is used for all modeled cell types
     }
     
     ChartController.getInstance().setChartMonitoredTissue(this);
     DataExportController.getInstance().setDataExportMonitoredTissue(this);
     ChartController.getInstance().registerChartSetChangeListener(this);
     DataExportController.getInstance().registerDataExportChangeListener(this);
 
		
		//TODO: plus 2 Korrektur überprüfen
		cellContinous2D = new Continuous2D(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getNeighborhood_µm() / 1.5, 
				TissueController.getInstance().getTissueBorder().getWidth() + 2, 
				TissueController.getInstance().getTissueBorder().getHeight());
		basementContinous2D = new Continuous2D(TissueController.getInstance().getTissueBorder().getWidth() + 2, 
				TissueController.getInstance().getTissueBorder().getWidth() + 2, 
				TissueController.getInstance().getTissueBorder().getHeight());
		rulerContinous2D = new Continuous2D(TissueController.getInstance().getTissueBorder().getWidth()+2,
				TissueController.getInstance().getTissueBorder().getWidth()+2,
				TissueController.getInstance().getTissueBorder().getHeight());
	   gridContinous2D = new Continuous2D(TissueController.getInstance().getTissueBorder().getWidth()+2,
	   		TissueController.getInstance().getTissueBorder().getWidth()+2,
	   		TissueController.getInstance().getTissueBorder().getHeight());
 }
 
 public void checkMemory(){
	 // Memory Management
    if (GlobalStatistics.getInstance().getActualNumberKCytes()>getAllCells().size()-50) // for safety -50
        getAllCells().resize(getAllCells().size()+500); // alloc 500 in advance
 }
 
 
 void printChartToPDF( JFreeChart chart, int width, int height, String fileName )
 {
     // call: printChartToPDF( EpidermisClass.createChart(), 500, 500, "test.pdf" );
	 try
	     {
	     Document document = new Document(new com.lowagie.text.Rectangle(width,height));
	     PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
	     document.addAuthor("EPISIM Simulator");
	     document.open();
	     PdfContentByte cb = writer.getDirectContent();
	     PdfTemplate tp = cb.createTemplate(width, height); 
	     Graphics2D g2 = tp.createGraphics(width, height, new DefaultFontMapper());
	     Rectangle2D rectangle2D = new Rectangle2D.Double(0, 0, width, height); 
	     chart.draw(g2, rectangle2D);
	     g2.dispose();
	     cb.addTemplate(tp, 0, 0);
	     document.close();
	     writer.close();
	     }
	 catch( Exception e )
	     {
	     e.printStackTrace();
	     }
 }
 
 
	private void seedInitiallyAvailableCells(){
		 for(UniversalCell cell : ModelController.getInstance().getInitialCellEnsemble()){
					Stoppable stoppable = schedule.scheduleRepeating(cell, SchedulePriority.CELLS.getPriority(), 1);
					cell.setStoppable(stoppable);
		 }	
	}
 
 
 
 public void start() {

		super.start();
		ChartController.getInstance().newSimulationRun();
		DataExportController.getInstance().newSimulationRun();
		
		if(this.chartSteppables != null){
			for(EnhancedSteppable steppable: this.chartSteppables){
		   	schedule.scheduleRepeating(steppable, SchedulePriority.DATAMONITORING.getPriority(), steppable.getInterval());
		   }
		}
		
		if(this.dataExportSteppables != null){
			for(EnhancedSteppable steppable: this.dataExportSteppables){
		   	schedule.scheduleRepeating(steppable, SchedulePriority.DATAMONITORING.getPriority(), steppable.getInterval());
		   }
		}
		
		GlobalStatistics.getInstance().reset(true);
		EnhancedSteppable globalStatisticsSteppable = GlobalStatistics.getInstance().getUpdateSteppable(getAllCells());
		schedule.scheduleRepeating(globalStatisticsSteppable, SchedulePriority.STATISTICS.getPriority(), globalStatisticsSteppable.getInterval());
		
			
		
			
			
		 basementContinous2D.clear();
		 rulerContinous2D.clear();
		 gridContinous2D.clear();
						
	    basementContinous2D.setObjectLocation("DummyObjektForDrawingTheBasementMembrane", new Double2D(50, 50));
	    rulerContinous2D.setObjectLocation("DummyObjektForDrawingTheRuler", new Double2D(50, 50));
	    gridContinous2D.setObjectLocation("DummyObjektForDrawingTheGrid", new Double2D(50, 50));
			
     
   

	     
	     
	     if(!isReloadedSnapshot()){
	   	  cellContinous2D.clear();
	   	  getAllCells().clear();
	   	  seedInitiallyAvailableCells();
	     }
		  else{
							 
			     for(AbstractCell cell: getAllCells()){		   	  
			   		
			   		schedule.scheduleRepeating(cell, SchedulePriority.CELLS.getPriority(), 1);
			   		if(cell instanceof UniversalCell){
			   			UniversalCell kcyte = (UniversalCell) cell;
			   			
			   			kcyte.removeCellDeathListener();
			   			kcyte.addCellDeathListener(this);
			   			kcyte.addCellDeathListener(GlobalStatistics.getInstance());
			   		}
			   		
			     }
			}
	     
	     
	     
        
 //////////////////////////////////////        
 // CELL STATISTICS & Updating OUTER SURFACE CELLS
 //////////////////////////////////////  

 // the agent that updates the isOuterSurface Flag for the surface exposed cells
     Steppable airSurface = new Steppable()
    {
             public void step(SimState state)
             {
                 int MAX_XBINS=300; // for every 3 x coordinates one bin
                 AbstractCell[] xLookUp=new AbstractCell[MAX_XBINS];                                         
                 double [] yLookUp=new double[MAX_XBINS]; // Concentrations *10 = 0 to 200
                 boolean [] LookUpUsed=new boolean[MAX_XBINS]; 
                 for (int k=0; k< MAX_XBINS; k++)
                 {
                     yLookUp[k]=9999.9; // deepest value, all coming are above
                     xLookUp[k]=null;
                 }
                 
                
                                     
                                 
                 
                 for (int i=0; i<getAllCells().size(); i++)
                 {
                     // iterate through all cells and determine the KCyte with lowest Y at bin
                     AbstractCell act=(AbstractCell)getAllCells().get(i);
                     if (act.isInNirvana()) continue;
                     // is a living cell..
                     
                     
                     
                   //  if (act.isBasalStatisticsCell()) actualBasalStatisticsCells++;
                     
                     //act.isOuterCell=false; // set new default 
                     Double2D loc=cellContinous2D.getObjectLocation(act);
                     
                     int xbin=(int)loc.x / CenterBasedMechanicalModel.GINITIALKERATINOWIDTH;
                     if (xLookUp[xbin]==null) 
                     {
                         xLookUp[xbin]=act;                            
                         yLookUp[xbin]=loc.y;
                     }
                     else
                         if (loc.y<yLookUp[xbin]) 
                         {
                             xLookUp[xbin]=act;
                             yLookUp[xbin]=loc.y;
                         }
                     
                     
                     
                 }            

                 for (int k=0; k< MAX_XBINS; k++)
                 {
                     if ((xLookUp[k]==null) || (xLookUp[k].getEpisimCellBehavioralModelObject().getDiffLevel().ordinal()==EpisimDifferentiationLevel.STEMCELL)) continue; // stem cells cannot be outer cells (Assumption)                        
                     xLookUp[k].setIsOuterCell(true);
                 }
                 
                 

             }
     };
     // Schedule the agent to update is Outer Flag     
     schedule.scheduleRepeating(airSurface,SchedulePriority.TISSUE.getPriority(),1);
     
     
     
     
    
 	}
 	
 	
 

	public void removeCells(GeneralPath path){
	Iterator<AbstractCell> iter = getAllCells().iterator();
	Map<Long, Double2D> map = new HashMap<Long, Double2D>();
	List<AbstractCell> livingCells = new LinkedList<AbstractCell>();
		int i = 0;
		while(iter.hasNext()){
			AbstractCell cell = iter.next();
			if(cell.getEpisimBioMechanicalModelObject() instanceof CenterBasedMechanicalModel){
				
				//TODO: Diese Ausnahme eleminieren und verallgemeinern
				CenterBasedMechanicalModel mechModel = (CenterBasedMechanicalModel) cell.getEpisimBioMechanicalModelObject();
				if(path.contains(mechModel.getCellEllipseObject().getLastDrawInfo2D().draw.x, mechModel.getCellEllipseObject().getLastDrawInfo2D().draw.y)&&
						cell.getEpisimCellBehavioralModelObject().getDiffLevel().ordinal() != EpisimDifferentiationLevel.STEMCELL){  
					cell.killCell();					 
					i++;
				}
				else{
					 livingCells.add(cell);
					 map.put(cell.getID(), this.cellContinous2D.getObjectLocation(cell));
				}
			}
		}
		
		this.getAllCells().clear();
		this.cellContinous2D.clear();
		for(AbstractCell cell: livingCells){
			this.getAllCells().add(cell);
			this.cellContinous2D.setObjectLocation(cell, map.get(cell.getID()));
		}
		
	}




 
	
	
	
//---------------------------------------------------------------------------------------------------------------------------------------------------
//GETTER-METHODS
//--------------------------------------------------------------------------------------------------------------------------------------------------- 
	@CannotBeMonitored
	public Continuous2D getBasementContinous2D() { return basementContinous2D; }
	@CannotBeMonitored
	public Continuous2D getCellContinous2D() { return cellContinous2D; }
	@CannotBeMonitored
	public Continuous2D getGridContinous2D() { return gridContinous2D; }
	@CannotBeMonitored
	public Continuous2D getRulerContinous2D() { return rulerContinous2D; }

	@CannotBeMonitored
	public String getTissueName() {return NAME;}
	
	
	
	//complex-Methods------------------------------------------------------------------------------------------------------------------
	public List<SnapshotObject> collectSnapshotObjects() {
		
		List<SnapshotObject> list = super.collectSnapshotObjects();		
		list.add(new SnapshotObject(SnapshotObject.CELLCONTINUOUS, this.cellContinous2D));

		return list;
	}  
	
	
//---------------------------------------------------------------------------------------------------------------------------------------------------
//SETTER-METHODS
//--------------------------------------------------------------------------------------------------------------------------------------------------- 
	
	
	
	
	public void setBasementContinous2D(Continuous2D basementContinous2D) { this.basementContinous2D = basementContinous2D; }	
	public void setCellContinous2D(Continuous2D cellContinous2D) { this.cellContinous2D = cellContinous2D; }	
	
	
	//	complex-Methods------------------------------------------------------------------------------------------------------------------
	
	
	public List<Method> getParameters() {
		List<Method> methods = new ArrayList<Method>();
		 
		 for(Method m : ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getClass().getMethods()){
				if((m.getName().startsWith("get") || m.getName().startsWith("is"))&& m.getAnnotation(CannotBeMonitored.class)==null) methods.add(m);
		 }
		 for(Method m : ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getClass().getMethods()){
				if((m.getName().startsWith("get") || m.getName().startsWith("is"))&& m.getAnnotation(CannotBeMonitored.class)==null) methods.add(m);
		 }
		 for(Method m : this.getClass().getMethods()){
				if((m.getName().startsWith("get") || m.getName().startsWith("is"))&& m.getAnnotation(CannotBeMonitored.class)==null) methods.add(m);
		 }
	   
		return methods;
	}
	
	public List<Field> getContants() {	
		List<Field> fields = new ArrayList<Field>();
		for(Field field : ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getClass().getFields()){
	   		if(!field.getDeclaringClass().isInterface()) fields.add(field);
		}
		for(Field field : ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getClass().getFields()){
   		if(!field.getDeclaringClass().isInterface()) fields.add(field);
		}
		
		return fields;
	}



	public void chartSetHasChanged() {

		try{
			if(getAllCells() != null && this.cellContinous2D != null 
					&& ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters() != null
					&& ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters() != null){
		      this.chartSteppables = ChartController.getInstance().getChartSteppablesOfActLoadedChartSet(getAllCells(), this.cellContinous2D, new Object[]{
		      		ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters(), 
		      		ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters(), 
		      		this});
		   }
      }
      catch (MissingObjectsException e){
	     ExceptionDisplayer.getInstance().displayException(e);
      }
		}



	public void cellIsDead(AbstractCell cell) {
		super.cellIsDead(cell);
		this.cellContinous2D.remove(cell);		
	}
	
	


	public void dataExportHasChanged() {

	   try{
	   	if(getAllCells() != null && this.cellContinous2D != null 
					&& ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters() != null
					&& ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters() != null){
	      this.dataExportSteppables = DataExportController.getInstance().getDataExportSteppablesOfActLoadedDataExport(getAllCells(), this.cellContinous2D, new Object[]{
	      	ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters(), 
	      	ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters(), 
	      	  	this});
	   	}
      }
      catch (MissingObjectsException e){
      	 ExceptionDisplayer.getInstance().displayException(e);
      }
	   
   }
}




