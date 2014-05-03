package sim.app.episim.model.visualization;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.media.j3d.Appearance;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3d;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.UniversalCell;


import sim.app.episim.model.biomechanics.centerbased3d.adhesion.old.AdhesiveCenterBased3DMechanicalModelGP;
import sim.app.episim.model.biomechanics.centerbased3d.newversion.CenterBased3DMechanicalModelGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.visualization.Episim3DAppearanceFactory;
import sim.display3d.Display3DHack;
import sim.portrayal.LocationWrapper;
import sim.portrayal3d.SimplePortrayal3D;

import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimDifferentiationLevel;


public class UniversalCellPortrayal3D extends SimplePortrayal3D {
	
	
	Transform3D transform;

   Appearance appearance;

 
   protected Node group;
       
   boolean pickable = true;
	
	private Sphere sphere;
	
	private Sphere nucleusSphere;
	
	
	private float standardCellRadius =1;
	
	

	private PolygonAttributes polygonAttributes;
	
	public UniversalCellPortrayal3D(PolygonAttributes polygonAttributes)
   {
		//this(getCellAppearanceForColor(new Color(230, 130, 170),new Color(255,175,205), new Color(220,0,0)),true,false,scale,divisions);
		this(polygonAttributes, true,false);
   }
	
	public UniversalCellPortrayal3D(PolygonAttributes polygonAttributes,boolean generateNormals, boolean generateTextureCoordinates)
   {
		EpisimBiomechanicalModelGlobalParameters globalParameters = ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		EpisimCellBehavioralModelGlobalParameters cbGP = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters();
		if(globalParameters instanceof sim.app.episim.model.biomechanics.centerbased3d.CenterBased3DMechanicalModelGP)
		{
			standardCellRadius = (float)(sim.app.episim.model.biomechanics.centerbased3d.CenterBased3DMechanicalModel.INITIAL_KERATINO_HEIGHT/2d);
		}
		else if(globalParameters instanceof AdhesiveCenterBased3DMechanicalModelGP){
			try{
		      Field field = cbGP.getClass().getDeclaredField("BASAL_CELL_WIDTH");
		      standardCellRadius = (float)field.getDouble(cbGP);
		      standardCellRadius /=2;
	      }
	      catch (NoSuchFieldException e){
	      	ExceptionDisplayer.getInstance().displayException(e);
	      }
	      catch (SecurityException e){
	      	ExceptionDisplayer.getInstance().displayException(e);
	      }
	      catch (IllegalArgumentException e){
	      	ExceptionDisplayer.getInstance().displayException(e);
	      }
	      catch (IllegalAccessException e){
	      	ExceptionDisplayer.getInstance().displayException(e);
	      }	
		}
		else if(globalParameters instanceof CenterBased3DMechanicalModelGP){
			try{
		      Field field = cbGP.getClass().getDeclaredField("WIDTH_DEFAULT");
		      standardCellRadius = (float)field.getDouble(cbGP);
		      standardCellRadius /=2;
	      }
	      catch (NoSuchFieldException e){
	      	ExceptionDisplayer.getInstance().displayException(e);
	      }
	      catch (SecurityException e){
	      	ExceptionDisplayer.getInstance().displayException(e);
	      }
	      catch (IllegalArgumentException e){
	      	ExceptionDisplayer.getInstance().displayException(e);
	      }
	      catch (IllegalAccessException e){
	      	ExceptionDisplayer.getInstance().displayException(e);
	      }	
		}
		this.polygonAttributes = polygonAttributes;
			this.polygonAttributes.setBackFaceNormalFlip(true);
			
		 float transparencyFactor = 1.0f;
		 if(getCurrentDisplay() instanceof Display3DHack){
		   	transparencyFactor = (float)((Display3DHack)getCurrentDisplay()).getModelSceneOpacity();
		 }
		this.appearance = Episim3DAppearanceFactory.getCellAppearanceForColor(this.polygonAttributes, new Color(255,160,160), transparencyFactor);
	   		
		this.sphere = new Sphere(standardCellRadius, (generateNormals ? Sphere.GENERATE_NORMALS : 0) | 
        (generateTextureCoordinates ? Primitive.GENERATE_TEXTURE_COORDS : 0), 
        30, appearance);
		
		setShape3DFlags(sphere.getShape(Sphere.BODY));
		
		transform = new Transform3D();
	//	transform.setTranslation(new Vector3f(standardCellRadius,standardCellRadius,standardCellRadius));		 
		group = sphere;
   }
	 
	public TransformGroup getModel(Object obj, TransformGroup j3dModel){
		if(obj instanceof UniversalCell){
			UniversalCell universalCell = (UniversalCell) obj;
			
			if (j3dModel==null)
		   {
		       j3dModel = new TransformGroup();
		       j3dModel.setCapability(Group.ALLOW_CHILDREN_READ);
		       
		       // build a LocationWrapper for the object
		       LocationWrapper pickI = new LocationWrapper(obj, null, getCurrentFieldPortrayal());
		
		       Node g = (Node) (group.cloneTree(true));
		
		       if (transform != null)
		       {
		           TransformGroup tg = new TransformGroup();
		           tg.setTransform(transform);
		           tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		           tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		           tg.setCapability(Group.ALLOW_CHILDREN_READ);
		           tg.addChild(g);
		           g = tg;
		       }
		       j3dModel.addChild(g);
		
		       
		       Shape3D shape = getShape(j3dModel, 0);
		       shape.setAppearance(appearance);
		       
		       if (pickable) setPickableFlags(shape);
		       shape.setUserData(pickI);
		       
	       }
			
			
			Shape3D shape = getShape(j3dModel, 0);
		
			float transparencyFactor = 1.0f;
			if(getCurrentDisplay() instanceof Display3DHack){
			   	transparencyFactor = (float)((Display3DHack)getCurrentDisplay()).getModelSceneOpacity();
			}
			
			Color cellColor =universalCell.getCellColoring();
			shape.setAppearance(Episim3DAppearanceFactory.getCellAppearanceForColor(polygonAttributes, cellColor,transparencyFactor));			
		} 
		return j3dModel;
		
	}
	
	
	
	public PolygonAttributes polygonAttributes() { return appearance.getPolygonAttributes(); } // default
	public void polygonAttributes(PolygonAttributes att) { appearance.setPolygonAttributes(att); }
	
	
  
  public static void setShape3DFlags(Shape3D shape)
  {
	  shape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE); // may need to change the appearance (see below)
	  shape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
	  shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ); // may need to change the geometry (see below)
	  shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE); // may need to change the geometry (see below)
	  shape.clearCapabilityIsFrequent(Shape3D.ALLOW_APPEARANCE_READ);
	  shape.clearCapabilityIsFrequent(Shape3D.ALLOW_APPEARANCE_WRITE);
	  shape.clearCapabilityIsFrequent(Shape3D.ALLOW_GEOMETRY_READ);
	  shape.clearCapabilityIsFrequent(Shape3D.ALLOW_GEOMETRY_WRITE);
  }
  protected Shape3D getShape(TransformGroup j3dModel, int shapeIndex)
  {
	  Node n = j3dModel;
	  while(n instanceof TransformGroup)
	      n = ((TransformGroup)n).getChild(0);
	  Primitive p = (Primitive) n;
	  return p.getShape(shapeIndex);
  }  
 
}
