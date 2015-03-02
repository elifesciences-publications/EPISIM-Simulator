package sim.app.episim.visualization.threedim;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;






import episiminterfaces.EpisimPortrayal;
import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.biomechanics.latticebased3d.LatticeBased3DModel;
import sim.app.episim.model.biomechanics.latticebased3d.LatticeBased3DModelGP;
import sim.display3d.Display3DHack;
import sim.field.SparseField;
import sim.portrayal.Portrayal;
import sim.portrayal3d.SimplePortrayal3D;
import sim.portrayal3d.grid.ObjectGridPortrayal3D;
import sim.portrayal3d.grid.SparseGridPortrayal3D;
import sim.portrayal3d.simple.CubePortrayal3D;
import sim.portrayal3d.simple.SpherePortrayal3D;
import sim.util.Bag;
import sim.util.Int3D;


public class LatticeCellFieldPortrayal3D extends SparseGridPortrayal3D implements EpisimPortrayal{
	private static final String NAME = "Epithelial Cells";
	
	private float standardCellRadius=0.5f;
	
	private final double VISUALIZATIONSCALINGFACTOR = 1.2;
	private final double VISUALIZATION_SPREADING_THICKNESS_SCALING_FACTOR = 1.3;
	private PolygonAttributes polygonAttributes;
	public LatticeCellFieldPortrayal3D(double scale){
		super();
		polygonAttributes = new PolygonAttributes();
		polygonAttributes.setCapability(PolygonAttributes.ALLOW_CULL_FACE_READ);
		polygonAttributes.setCapability(PolygonAttributes.ALLOW_CULL_FACE_WRITE);
		polygonAttributes.setCapability(PolygonAttributes.ALLOW_MODE_READ);
		polygonAttributes.setCapability(PolygonAttributes.ALLOW_MODE_WRITE);
		polygonAttributes.setPolygonOffsetFactor(1.2f);
		setPortrayalForAll(new LatticeCellPortrayal3D(polygonAttributes));
		
		standardCellRadius = (float)LatticeBased3DModelGP.hexagonal_radius;
		
	}
	
	public TransformGroup createModel()
   {
	   SparseField field = (SparseField)(this.field);
	   Vector3d locationV3d = new Vector3d();
	   TransformGroup globalTG = new TransformGroup(); 
	   globalTG.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
	   globalTG.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
	   globalTG.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
	
	   if (field==null) return globalTG;
	   Bag objects = field.getAllObjects();
	   Transform3D tmpLocalT = new Transform3D();
	   
	   for(int z = 0; z<objects.numObjs; z++)
	   {
	   	 tmpLocalT = new Transform3D();
	   	 if(objects.objs[z] instanceof UniversalCell){
	   		 LatticeBased3DModel mechModel = ((LatticeBased3DModel) ((UniversalCell)objects.objs[z]).getEpisimBioMechanicalModelObject());
	   		 mechModel.addSpreadingCellRotationAndTranslation(tmpLocalT);
	   			Vector3d scales =new Vector3d();
            	tmpLocalT.getScale(scales);
            	if(!mechModel.isSpreading())scales.scale(VISUALIZATIONSCALINGFACTOR);
            	else{
            		rescaleSpreadingCells(scales);
            	}
            	tmpLocalT.setScale(scales);
	   		 mechModel.addCellTranslation(tmpLocalT);
	   	 }
	       globalTG.addChild(wrapModelForNewObject(objects.objs[z], tmpLocalT));                     
	   }
	   if(getCurrentDisplay() instanceof Display3DHack){
	   	Display3DHack disp = (Display3DHack) getCurrentDisplay();
	   	if(disp.getModelClip() != null){
	   		disp.getModelClip().addScope(globalTG);
	   	}
	   }
	   return globalTG;
   }
	
	 public void updateModel(TransformGroup globalTG)
    {
	    SparseField field = (SparseField)(this.field);
	    if (field==null) return;
	    Bag b = field.getAllObjects();
	    HashMap hm = new HashMap();
	    Transform3D tmpLocalT = new Transform3D();
   
            
    // put all objects into hm
    for(int i=0;i<b.numObjs;i++)
        hm.put(b.objs[i],b.objs[i]);

    // update children if they're still in the field,
    // else remove the children if they appear to have left.
    // We use a hashmap to efficiently mark out the children
    // as we delete them and update them
    
    // build a Bag of children to remove
    Bag toRemove = new Bag();
            
    // for each child in the array...
    for(int t = 0; t < globalTG.numChildren(); t++)
    {
        BranchGroup localBG = (BranchGroup)(globalTG.getChild(t));
                                                                    
        // get the object represented by the child
        Object fieldObj = localBG.getUserData();
                    
        // try to remove the object from hm.  Returns null if it wasn't there.
        if(hm.remove(fieldObj) != null) 
            {
            // object still in the field.
            // Do an update on the child.
            // we can pull this off because sparse fields are not allowed to contain null -- Sean
            TransformGroup localTG = (TransformGroup)localBG.getChild(0);
            Portrayal p = getPortrayalForObject(fieldObj);
            if(! (p instanceof SimplePortrayal3D))
                throw new RuntimeException("Unexpected Portrayal " + p + " for object " + 
                    fieldObj + " -- expecting a SimplePortrayal3D");
            SimplePortrayal3D p3d = (SimplePortrayal3D)p;
            if(p3d instanceof LatticeCellPortrayal3D){
            	
            }
            p3d.setCurrentFieldPortrayal(this);
            TransformGroup localTG2 = p3d.getModel(fieldObj, localTG);
            tmpLocalT = new Transform3D();
            if(fieldObj instanceof UniversalCell){
            	LatticeBased3DModel mechModel = ((LatticeBased3DModel) ((UniversalCell)fieldObj).getEpisimBioMechanicalModelObject());
            	mechModel.addSpreadingCellRotationAndTranslation(tmpLocalT);
            	Vector3d scales =new Vector3d();
            	tmpLocalT.getScale(scales);
            	if(!mechModel.isSpreading()) scales.scale(VISUALIZATIONSCALINGFACTOR);
            	else{
            		rescaleSpreadingCells(scales);
            	}
            	tmpLocalT.setScale(scales);
            	mechModel.addCellTranslation(tmpLocalT);
            }
           
           
            localTG2.setTransform(tmpLocalT);

            if(localTG != localTG2)
            {
                localTG2.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
                localTG2.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
                                            
                BranchGroup newlocalBG = new BranchGroup();
                newlocalBG.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
                newlocalBG.setCapability(BranchGroup.ALLOW_DETACH);
                newlocalBG.setUserData(fieldObj);
                newlocalBG.addChild(localTG2);
                                            
                globalTG.setChild(newlocalBG, t);
             }

        }
        else  // object is no longer in the field -- remove it from the scenegraph
            toRemove.add(localBG);
     }

    // Now remove elements
    	for(int i = 0; i < toRemove.numObjs; i++)       // Ugh, this is truly awful
        globalTG.removeChild((Node)toRemove.objs[i]);  // O(n), yuck yuck yuck.    But we have to do this because Java3D has no efficient way around it.  Even removeAllChildren just does a for-loop and removes each child in turn (O(n^2)!!  Who are these dufuses?)
    
    // The remaining objects in hm must be new.  We add them to the scenegraph.
    // But first, we should check to see if hm is empty.
    
    	if (!hm.isEmpty())
      {
	        Iterator newObjs = hm.values().iterator();  // yuck, inefficient
	        while(newObjs.hasNext())
	        {
	            Object fieldObj = newObjs.next();
	            tmpLocalT = new Transform3D();
	            if(fieldObj instanceof UniversalCell){
	            	
	            	LatticeBased3DModel mechModel = ((LatticeBased3DModel) ((UniversalCell)fieldObj).getEpisimBioMechanicalModelObject());
	            	mechModel.addSpreadingCellRotationAndTranslation(tmpLocalT);
	            	Vector3d scales =new Vector3d();
	            	tmpLocalT.getScale(scales);
	            	if(!mechModel.isSpreading())scales.scale(VISUALIZATIONSCALINGFACTOR);
	            	tmpLocalT.setScale(scales);
	            	mechModel.addCellTranslation(tmpLocalT);
	            }
	            
	            BranchGroup localBG = wrapModelForNewObject(fieldObj, tmpLocalT);                     
	            globalTG.addChild(localBG);
	        }      
      }
   }
	 
	 public PolygonAttributes polygonAttributes() { return polygonAttributes; } // default
	
	 
	public void rescaleSpreadingCells(Vector3d scales){
		double maxScale = Double.NEGATIVE_INFINITY;
		int maxScaleIndex = -1;
		double[] scalesArray = new double[3];
		scales.get(scalesArray);
		for(int i = 0; i < scalesArray.length; i++){
			if(scalesArray[i] > maxScale){ 
				maxScale = scalesArray[i];
				maxScaleIndex = i;
			}
		}
		for(int i = 0; i < scalesArray.length; i++){
			if(i != maxScaleIndex){ 
				scalesArray[i] *= VISUALIZATION_SPREADING_THICKNESS_SCALING_FACTOR;
			}
		}
		scales.set(scalesArray);
	}
	 
	
	public String getPortrayalName(){
		return NAME;
	}

	public Rectangle2D.Double getViewPortRectangle() {
		return new Rectangle2D.Double(0d,0d,0d,0d);
	}

}
