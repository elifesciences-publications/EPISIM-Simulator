package sim.app.episim.model.visualization;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;


import episiminterfaces.EpisimPortrayal;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.biomechanics.hexagonbased3d.HexagonBased3DMechanicalModel;
import sim.app.episim.model.biomechanics.hexagonbased3d.HexagonBased3DMechanicalModelGP;
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


public class HexagonalCellGridPortrayal3D extends SparseGridPortrayal3D implements EpisimPortrayal{
	private static final String NAME = "Epithelial Cells";
	
	private float standardCellRadius=0.5f;
	
	public HexagonalCellGridPortrayal3D(double scale){
		super();
		setPortrayalForAll(new HexagonalCellPortrayal3D());
		
		standardCellRadius = (float)HexagonBased3DMechanicalModelGP.outer_hexagonal_radius;
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
	   	 setCellTranslation(objects.objs[z], tmpLocalT);
	       globalTG.addChild(wrapModelForNewObject(objects.objs[z], tmpLocalT));                     
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

            p3d.setCurrentFieldPortrayal(this);
            TransformGroup localTG2 = p3d.getModel(fieldObj, localTG);
            tmpLocalT = new Transform3D();
            setCellTranslation(fieldObj, tmpLocalT);
           
           
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
	            setCellTranslation(fieldObj, tmpLocalT);
	            
	            BranchGroup localBG = wrapModelForNewObject(fieldObj, tmpLocalT);                     
	            globalTG.addChild(localBG);
	        }      
      }
   }
	 
	private void setCellTranslation(Object fieldObject,  Transform3D trans){		
		if(fieldObject instanceof UniversalCell){
			UniversalCell cell = (UniversalCell) fieldObject;
			Int3D location = ((HexagonBased3DMechanicalModel) cell.getEpisimBioMechanicalModelObject()).getFieldLocation();
      	trans.setTranslation(new Vector3f((float)(standardCellRadius + (2f*location.x*standardCellRadius)),
  	      		 (float)(standardCellRadius + (2f*location.y*standardCellRadius)),
  	      		 (float)(standardCellRadius + (2f*location.z*standardCellRadius))));
      }         
	}
	 
	
	public String getPortrayalName(){
		return NAME;
	}

	public Rectangle2D.Double getViewPortRectangle() {
		return new Rectangle2D.Double(0d,0d,0d,0d);
	}

}
