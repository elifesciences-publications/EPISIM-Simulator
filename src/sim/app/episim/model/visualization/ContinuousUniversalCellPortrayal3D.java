package sim.app.episim.model.visualization;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.HashMap;
import java.util.Iterator;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3d;

import sim.app.episim.UniversalCell;
import sim.app.episim.model.biomechanics.centerbased3d.CenterBased3DMechanicalModel;
import sim.field.SparseField;
import sim.portrayal.Portrayal;
import sim.portrayal3d.SimplePortrayal3D;
import sim.portrayal3d.continuous.ContinuousPortrayal3D;
import sim.util.Bag;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.EpisimPortrayal;


public class ContinuousUniversalCellPortrayal3D extends ContinuousPortrayal3D implements EpisimPortrayal {

	private final String NAME = "Epidermis";
	
	private PolygonAttributes polygonAttributes;
	public ContinuousUniversalCellPortrayal3D(){
		super();		
		polygonAttributes = new PolygonAttributes();
		polygonAttributes.setCapability(PolygonAttributes.ALLOW_CULL_FACE_READ);
		polygonAttributes.setCapability(PolygonAttributes.ALLOW_CULL_FACE_WRITE);
		polygonAttributes.setCapability(PolygonAttributes.ALLOW_MODE_READ);
		polygonAttributes.setCapability(PolygonAttributes.ALLOW_MODE_WRITE);
		polygonAttributes.setPolygonOffsetFactor(1.2f);
		setPortrayalForAll(new UniversalCellPortrayal3D(polygonAttributes));
	}
	public String getPortrayalName() {
		return NAME;
	}

	public Double getViewPortRectangle() {
		return new Rectangle2D.Double(0,0,0, 0);
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
	   	getLocationOfObjectAsVector3d(objects.objs[z], locationV3d);
	      tmpLocalT.setTranslation(locationV3d);
	      if(objects.objs[z] != null && objects.objs[z] instanceof UniversalCell){		   	
		   	manipulateTransformationRegardingCellSize((UniversalCell) objects.objs[z], tmpLocalT);
	   	}
	      
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
	   Vector3d locationV3d = new Vector3d();
	           
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
	   	tmpLocalT = new Transform3D(); 
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
	           getLocationOfObjectAsVector3d(fieldObj, locationV3d);
	           tmpLocalT.setTranslation(locationV3d);
	           
	           
	           	if(fieldObj != null && fieldObj instanceof UniversalCell){	  	   		
		  		   	manipulateTransformationRegardingCellSize((UniversalCell) fieldObj, tmpLocalT);		  		   	
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
	      	 tmpLocalT = new Transform3D();
	           Object fieldObj = newObjs.next();
	           if(fieldObj instanceof UniversalCell){
	         	  manipulateTransformationRegardingCellSize((UniversalCell)fieldObj, tmpLocalT);
	           }
	           locationV3d = getLocationOfObjectAsVector3d(fieldObj, locationV3d);
	           if (locationV3d != null) 
	               tmpLocalT.setTranslation(locationV3d);
	           
	           BranchGroup localBG = wrapModelForNewObject(fieldObj, tmpLocalT);                     
	           globalTG.addChild(localBG);
	      }
	   }
   }
	
	private void manipulateTransformationRegardingCellSize(UniversalCell cell, Transform3D trans){
		if(cell.getEpisimBioMechanicalModelObject() instanceof CenterBased3DMechanicalModel){
			CenterBased3DMechanicalModel mechModel = (CenterBased3DMechanicalModel) cell.getEpisimBioMechanicalModelObject();
			 double width = mechModel.getKeratinoWidth();
		 	 double height = mechModel.getKeratinoHeight();
		 	 double length = mechModel.getKeratinoLength();
		 	
			Vector3d scales = new Vector3d();
			trans.getScale(scales);
			scales.x*=(width/height);
			scales.y*=(height/height);
			scales.z*=(length/height);
			trans.setScale(scales);					
		}
	}

}