package sim.app.episim;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import episimbiomechanics.EpisimModelConnector;
import episiminterfaces.CellDeathListener;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.NoExport;
import episiminterfaces.monitoring.CannotBeMonitored;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.model.biomechanics.AbstractMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased3d.CenterBased3DMechanicalModel;
import sim.app.episim.model.biomechanics.vertexbased.VertexBasedMechanicalModel;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.visualization.CellEllipse;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.tissue.TissueServer;
import sim.app.episim.util.GenericBag;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;


public abstract class AbstractCell implements Steppable, Stoppable, java.io.Serializable{
	
	
   private boolean isOuterCell=false;
   private boolean isBasalStatisticsCell=false; // for counting of growth fraction a wider range is necessary, not only membrane sitting cells
  
   private boolean inNirvana=false; // unvisible and without action: only ageing is active
   private final long id;
   private final AbstractCell motherCell;   // -1 means not yet set
   
   private static int cellCounter = 0;
   
   private boolean tracked = false;
      
   private Stoppable stoppable = null;
   
   private Set<CellDeathListener> cellDeathListeners;
      
   private EpisimCellBehavioralModel cellBehavioralModelObject;
   private EpisimBiomechanicalModel mechanicalModelObject;
   
   
   
   public AbstractCell(AbstractCell motherCell, EpisimCellBehavioralModel cellBehavioralModel){
   	inNirvana=false;
   	isOuterCell=false;
   	this.id = getNextCellId();
   	this.motherCell = ((motherCell == null) ? this : motherCell);   	
   	this.cellBehavioralModelObject = cellBehavioralModel;
   	final EpisimModelConnector modelConnector = ModelController.getInstance().getBioMechanicalModelController().getNewEpisimModelConnector();
   	if(cellBehavioralModel == null){ 
   		this.cellBehavioralModelObject = ModelController.getInstance().getCellBehavioralModelController().getNewEpisimCellBehavioralModelObject();
   	}
   	else{
   		this.cellBehavioralModelObject.setSendReceiveAlgorithm(ModelController.getInstance().getCellBehavioralModelController().getNewInstanceOfSendReceiveAlgorithm());
   	}
   	this.cellBehavioralModelObject.setId(this.id);
   	mechanicalModelObject =  ModelController.getInstance().getNewBioMechanicalModelObject(this);
   	this.mechanicalModelObject.setEpisimModelConnector(modelConnector);
   	this.cellBehavioralModelObject.setEpisimModelConnector(modelConnector);
   	this.cellBehavioralModelObject.setEpisimSbmlModelConnector(ModelController.getInstance().getNewEpisimSbmlModelConnector());
   
   	cellDeathListeners = new HashSet<CellDeathListener>();      
      cellDeathListeners.add(TissueController.getInstance().getActEpidermalTissue());      
      cellDeathListeners.add(GlobalStatistics.getInstance());
      
      
   }
	
   @CannotBeMonitored
   private synchronized static final long getNextCellId(){
   	cellCounter++;
   	return (System.currentTimeMillis() + cellCounter);
   }
   
	public abstract String getCellName();
	
	@NoExport
	public abstract List<Method> getParameters();
		
	@CannotBeMonitored
	public long getID() { return id; }   
	
	public boolean getIsBasalCell() { return isBasalStatisticsCell; }
	public void setIsBasalCell(boolean val){ this.isBasalStatisticsCell = val; }
	public boolean getIsOuterCell() { return isOuterCell; } 	
	public void setIsOuterCell(boolean isOuterCell) {	this.isOuterCell = isOuterCell; }	
	public long getMotherId(){ return this.motherCell != null ? this.motherCell.getID(): -1; }
	@NoExport
	public AbstractCell getMotherCell(){ return this.motherCell; }
	
	@CannotBeMonitored
	@NoExport
   public boolean getIsTracked(){ return tracked; }	
   public void setTracked(boolean tracked) {	this.tracked = tracked; }
   @NoExport
   public void setStoppable(Stoppable stopperparam){ this.stoppable = stopperparam;}
	   
   @CannotBeMonitored
   @NoExport
   public GenericBag<AbstractCell> getNeighbouringCells(){ return this.mechanicalModelObject.getDirectNeighbours(); }   
   public void stop(){}   
	public void removeFromSchedule(){ if(stoppable != null) stoppable.stop(); }
		
	@CannotBeMonitored @NoExport
	public EpisimCellBehavioralModel getEpisimCellBehavioralModelObject(){ return this.cellBehavioralModelObject; }
	
	@CannotBeMonitored @NoExport
   public Class<? extends EpisimCellBehavioralModel> getEpisimCellBehavioralModelClass(){ return this.cellBehavioralModelObject.getClass(); }	
   
	
	public void removeCellDeathListener(){ this.cellDeathListeners.clear(); }   
	public void addCellDeathListener(CellDeathListener listener){ this.cellDeathListeners.add(listener); }   
   
   public void killCell(){
   	 if(this.getEpisimBioMechanicalModelObject() instanceof AbstractMechanicalModel)((AbstractMechanicalModel)this.getEpisimBioMechanicalModelObject()).removeCellFromCellField();	
	  	 for(CellDeathListener listener: cellDeathListeners) listener.cellIsDead(this);	  	 
	  	 this.getEpisimCellBehavioralModelObject().setIsAlive(false);
	  	 removeFromSchedule();
   }   
   
   @CannotBeMonitored @NoExport
   public EpisimBiomechanicalModel getEpisimBioMechanicalModelObject(){ return this.mechanicalModelObject; }   
   public void step(SimState state) {		
		
   }
   
   public boolean equals(Object obj){
	   if(obj instanceof AbstractCell){
	   	return this.getID() == ((AbstractCell)obj).getID();
	   }
	   else return super.equals(obj);
   }
   
   
   public Color getCellColoring(){
   	return getFillColor(this);
   }
   
   private Color getFillColor(AbstractCell kcyte){
   	int keratinoType=kcyte.getEpisimCellBehavioralModelObject().getDiffLevel().ordinal();                                
      int coloringType=MiscalleneousGlobalParameters.getInstance().getTypeColor();
   	//
      // set colors
      //
                    
      int calculatedColorValue=0;  
     
      int red=255;         
      int green=0;
      int blue=0;
            
      if ((coloringType==1) || (coloringType==2))  // Cell type coloring
      {              
        	   if(keratinoType == EpisimDifferentiationLevel.STEMCELL){red=0x46; green=0x72; blue=0xBE;} 
        	   else if(keratinoType == EpisimDifferentiationLevel.TACELL){red=148; green=167; blue=214;}                             
        	   else if(keratinoType == EpisimDifferentiationLevel.EARLYSPICELL){red=0xE1; green=0x6B; blue=0xF6;}
        	   else if(keratinoType == EpisimDifferentiationLevel.LATESPICELL){red=0xC1; green=0x4B; blue=0xE6;}
        	   else if(keratinoType == EpisimDifferentiationLevel.GRANUCELL){red=204; green=0; blue=102;}
        	               
                       
            boolean isMembraneCell = false;
            boolean isOuterCell = kcyte.getIsOuterCell();
            boolean isNextToOuterCell = false;
            if(kcyte.getEpisimBioMechanicalModelObject() instanceof CenterBasedMechanicalModel){ 
            	isMembraneCell=((CenterBasedMechanicalModel)kcyte.getEpisimBioMechanicalModelObject()).isMembraneCell();
            	isNextToOuterCell = ((CenterBasedMechanicalModel)kcyte.getEpisimBioMechanicalModelObject()).nextToOuterCell();
        
            }
            if(kcyte.getEpisimBioMechanicalModelObject() instanceof CenterBased3DMechanicalModel){ 
            	isMembraneCell=((CenterBased3DMechanicalModel)kcyte.getEpisimBioMechanicalModelObject()).isMembraneCell();
            	isNextToOuterCell = ((CenterBased3DMechanicalModel)kcyte.getEpisimBioMechanicalModelObject()).nextToOuterCell();
            	
            }
            if(kcyte.getEpisimBioMechanicalModelObject() instanceof VertexBasedMechanicalModel) isMembraneCell=((VertexBasedMechanicalModel)kcyte.getEpisimBioMechanicalModelObject()).isMembraneCell();
           
            
            if(coloringType==2){
            	if(isMembraneCell){ red=230; green=255; blue=80;}
            	if(isNextToOuterCell){ red=255; green=255; blue=255;}
            	if(isOuterCell){ red=133; green=133; blue=133;}
            }
            
       }
       if (coloringType==3) // Age coloring
       {              
      	 Method m=null;
      	 double maxAge =0;
          try{
	          m = kcyte.getEpisimCellBehavioralModelObject().getClass().getMethod("_getMaxAge", new Class<?>[0]);
	          maxAge= (Double) m.invoke(kcyte.getEpisimCellBehavioralModelObject(), new Object[0]);
          }
          catch (Exception e){
	          ExceptionDisplayer.getInstance().displayException(e);
          }
          
      	 calculatedColorValue= (int) (250-250*kcyte.getEpisimCellBehavioralModelObject().getAge()/maxAge);
          red=255;
          green=calculatedColorValue;                        
          blue=calculatedColorValue;
          if(keratinoType== EpisimDifferentiationLevel.STEMCELL){ red=148; green=167; blue=214; } // stem cells do not age
       }
      
       if(coloringType==4){ //Colors are calculated in the cellbehavioral model
         red=kcyte.getEpisimCellBehavioralModelObject().getColorR();
         green=kcyte.getEpisimCellBehavioralModelObject().getColorG();
         blue=kcyte.getEpisimCellBehavioralModelObject().getColorB();
       }
        
      // Limit the colors to 255
      green=(green>255)?255:((green<0)?0:green);
      red=(red>255)?255:((red<0)?0:red);
      blue=(blue>255)?255:((blue<0)?0:blue);
      
      if(kcyte.getIsTracked() && MiscalleneousGlobalParameters.getInstance().getHighlightTrackedCells()) return Color.RED;
      return new Color(red, green, blue);
   }
	
}
