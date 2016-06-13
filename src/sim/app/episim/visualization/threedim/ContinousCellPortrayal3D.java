package sim.app.episim.visualization.threedim;

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

import sim.app.episim.EpisimExceptionHandler;


import sim.app.episim.EpisimProperties;
import sim.app.episim.model.DummyCell;
import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.biomechanics.centerbased3d.newmodel.CenterBased3DModel;
import sim.app.episim.model.biomechanics.centerbased3d.oldmodel.wound.AdhesiveCenterBased3DModelGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters.MiscalleneousGlobalParameters3D;
import sim.display3d.Display3DHack;
import sim.display3d.Display3DHack.ModelSceneCrossSectionMode;
import sim.portrayal.LocationWrapper;
import sim.portrayal3d.SimplePortrayal3D;

import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimDifferentiationLevel;

public class ContinousCellPortrayal3D extends SimplePortrayal3D {
	
	
	Transform3D transform;

   Appearance appearance;

 
  
   boolean pickable = true;
	
	private Sphere cellSphere;
	private Sphere innerCellSphere;	
	private Sphere nucleusSphere;
	private Sphere innerNucleusSphere;
	
	
	private float standardCellRadius =1;
	
	private boolean optimizedGraphicsActivated =false;
	private boolean drawInnerCellSurface = true;
	private PolygonAttributes polygonAttributes;
	private final Color nucleusColor = new Color(140,140,240);
	
	public ContinousCellPortrayal3D(PolygonAttributes polygonAttributes)
   {
		//this(getCellAppearanceForColor(new Color(230, 130, 170),new Color(255,175,205), new Color(220,0,0)),true,false,scale,divisions);
		this(polygonAttributes, true,false);
   }
	
	public ContinousCellPortrayal3D(PolygonAttributes polygonAttributes,boolean generateNormals, boolean generateTextureCoordinates)
   {
		EpisimBiomechanicalModelGlobalParameters globalParameters = ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		EpisimCellBehavioralModelGlobalParameters cbGP = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters();
		 MiscalleneousGlobalParameters param = MiscalleneousGlobalParameters.getInstance();
	      
	      if(param instanceof MiscalleneousGlobalParameters3D && ((MiscalleneousGlobalParameters3D)param).getOptimizedGraphics()){	
				optimizedGraphicsActivated = true;
			}
	      if(EpisimProperties.getProperty(EpisimProperties.DISPLAY_3D_DRAW_INNER_CELL_SURFACE) != null 
	      		&&EpisimProperties.getProperty(EpisimProperties.DISPLAY_3D_DRAW_INNER_CELL_SURFACE).equalsIgnoreCase(EpisimProperties.OFF)){
	      	drawInnerCellSurface=false;
	      }
		if(globalParameters instanceof sim.app.episim.model.biomechanics.centerbased3d.oldmodel.CenterBased3DModelGP)
		{
			standardCellRadius = (float)(sim.app.episim.model.biomechanics.centerbased3d.oldmodel.CenterBased3DModel.INITIAL_KERATINO_HEIGHT/2d);
		}
		else if(globalParameters instanceof AdhesiveCenterBased3DModelGP){
			try{
		      Field field = cbGP.getClass().getDeclaredField("BASAL_CELL_WIDTH");
		      standardCellRadius = (float)field.getDouble(cbGP);
		      standardCellRadius /=2;
	      }
	      catch (NoSuchFieldException e){
	      	EpisimExceptionHandler.getInstance().displayException(e);
	      }
	      catch (SecurityException e){
	      	EpisimExceptionHandler.getInstance().displayException(e);
	      }
	      catch (IllegalArgumentException e){
	      	EpisimExceptionHandler.getInstance().displayException(e);
	      }
	      catch (IllegalAccessException e){
	      	EpisimExceptionHandler.getInstance().displayException(e);
	      }	
		}
		else if( globalParameters instanceof sim.app.episim.model.biomechanics.centerbased3d.apicalmeristem.ApicalMeristemCenterBased3DModelGP
				|| globalParameters instanceof sim.app.episim.model.biomechanics.centerbased3d.fisheye.FishEyeCenterBased3DModelGP 
				|| globalParameters instanceof sim.app.episim.model.biomechanics.centerbased3d.newmodel.CenterBased3DMechanicalModelGP){
			try{
		      Field field = cbGP.getClass().getDeclaredField("WIDTH_DEFAULT");
		      standardCellRadius = (float)field.getDouble(cbGP);
		      standardCellRadius /=2;
	      }
	      catch (NoSuchFieldException e){
	      	EpisimExceptionHandler.getInstance().displayException(e);
	      }
	      catch (SecurityException e){
	      	EpisimExceptionHandler.getInstance().displayException(e);
	      }
	      catch (IllegalArgumentException e){
	      	EpisimExceptionHandler.getInstance().displayException(e);
	      }
	      catch (IllegalAccessException e){
	      	EpisimExceptionHandler.getInstance().displayException(e);
	      }	
		}
		this.polygonAttributes = polygonAttributes;
		this.polygonAttributes.setBackFaceNormalFlip(true);
			
		 float transparencyFactor = 1.0f;
		 if(getCurrentDisplay() instanceof Display3DHack){
		   	transparencyFactor = (float)((Display3DHack)getCurrentDisplay()).getModelSceneOpacity();
		 }
		this.appearance = Episim3DAppearanceFactory.getCellAppearanceForColor(this.polygonAttributes, new Color(255,160,160), transparencyFactor, true);
	   		
		this.cellSphere = new Sphere(standardCellRadius, (generateNormals ? Sphere.GENERATE_NORMALS : 0) |(generateTextureCoordinates ? Primitive.GENERATE_TEXTURE_COORDS : 0), 30, appearance);
		
		setShape3DFlags(cellSphere.getShape(Sphere.BODY));
		if(optimizedGraphicsActivated){
			if(drawInnerCellSurface){
				this.innerCellSphere = new Sphere(standardCellRadius*0.95f, (generateNormals ? Sphere.GENERATE_NORMALS : 0) |(generateTextureCoordinates ? Primitive.GENERATE_TEXTURE_COORDS : 0), 30, appearance);
				setShape3DFlags(innerCellSphere.getShape(Sphere.BODY));
			}			
			this.nucleusSphere = new Sphere((standardCellRadius/3f), (generateNormals ? Sphere.GENERATE_NORMALS : 0) |(generateTextureCoordinates ? Primitive.GENERATE_TEXTURE_COORDS : 0), 30, appearance);
			setShape3DFlags(nucleusSphere.getShape(Sphere.BODY));
			if(drawInnerCellSurface){
				this.innerNucleusSphere = new Sphere((standardCellRadius/3f)*0.95f, (generateNormals ? Sphere.GENERATE_NORMALS : 0) |(generateTextureCoordinates ? Primitive.GENERATE_TEXTURE_COORDS : 0), 30, appearance);
				setShape3DFlags(innerNucleusSphere.getShape(Sphere.BODY));
			}
		}
		
		
		transform = new Transform3D();
	//	transform.setTranslation(new Vector3f(standardCellRadius,standardCellRadius,standardCellRadius));		 
		
   }
	 
	public TransformGroup getModel(Object obj, TransformGroup j3dModel){
		if(obj instanceof UniversalCell){
			UniversalCell universalCell = (UniversalCell) obj;
			ModelSceneCrossSectionMode actCrossSectionMode=null;
			if(((Display3DHack)getCurrentDisplay()) != null){
				actCrossSectionMode = ((Display3DHack)getCurrentDisplay()).getModelSceneCrossSectionMode();
				
			}
			if (j3dModel==null)
		   {
		       j3dModel = new TransformGroup();
		       j3dModel.setCapability(Group.ALLOW_CHILDREN_READ);
		       
		       // build a LocationWrapper for the object
		       LocationWrapper pickI = new LocationWrapper(obj, null, getCurrentFieldPortrayal());
		
		      
		
		       if (transform != null)
		       {
		           TransformGroup tg = new TransformGroup();
		           tg.setTransform(transform);
		           tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		           tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		           tg.setCapability(Group.ALLOW_CHILDREN_READ);
		           tg.addChild(cellSphere.cloneTree(true));
		           if(optimizedGraphicsActivated){
			           if(drawInnerCellSurface)tg.addChild(innerCellSphere.cloneTree(true));
			           tg.addChild(nucleusSphere.cloneTree(true));
			           if(drawInnerCellSurface)tg.addChild(innerNucleusSphere.cloneTree(true));
		           }
		           j3dModel.addChild(tg);
		       }
		       else{
		      	 j3dModel.addChild(cellSphere.cloneTree(true));
		      	 if(optimizedGraphicsActivated){
		      		 if(drawInnerCellSurface)j3dModel.addChild(innerCellSphere.cloneTree(true));
		      		 j3dModel.addChild(nucleusSphere.cloneTree(true));
		      		 if(drawInnerCellSurface)j3dModel.addChild(innerNucleusSphere.cloneTree(true));
		      	 }
		       }
		   	 boolean isNucleated = true;
	      	 boolean isViable = true;
	      	 if(universalCell.getEpisimBioMechanicalModelObject() instanceof CenterBased3DModel){
	      		 isNucleated=((CenterBased3DModel)universalCell.getEpisimBioMechanicalModelObject()).hasNucleus();
	      		 isViable=((CenterBased3DModel)universalCell.getEpisimBioMechanicalModelObject()).hasViablility();
	      	 }
		       Shape3D shape = getShape(j3dModel, 0);
		       shape.setAppearance(appearance);		       
		       if (pickable) setPickableFlags(shape);
		       shape.setUserData(pickI);
		       
		       if(optimizedGraphicsActivated){
		      	 Appearance a =null;
		      	 if(drawInnerCellSurface){
			      	 Shape3D shapeInnerCell = getShape(j3dModel, 1);		      	 
			      	 a = Episim3DAppearanceFactory.getCellAppearanceForColorNoMaterial(polygonAttributes, Color.WHITE,1.0f);
			      	 if(a.getRenderingAttributes() !=null) a.getRenderingAttributes().setVisible(actCrossSectionMode != null && actCrossSectionMode != ModelSceneCrossSectionMode.DISABLED);
			      	 shapeInnerCell.setAppearance(a);
		      	 }
		      	 
		      	 int nucleusShapeIndex = drawInnerCellSurface ? 2 : 1;
		      	 Shape3D shapeNucleus = getShape(j3dModel, nucleusShapeIndex);		      	
		      	 a = Episim3DAppearanceFactory.getNucleusAppearance(polygonAttributes, 1.0f);      		
		      	 if(a.getRenderingAttributes() !=null){      		
		      		 a.getRenderingAttributes().setVisible(!(actCrossSectionMode != null && actCrossSectionMode != ModelSceneCrossSectionMode.DISABLED) && isNucleated);
		      	 }
		      	 shapeNucleus.setAppearance(a);  	 
		      	 if(drawInnerCellSurface){
			      	 Shape3D shapeInnerNucleus = getShape(j3dModel, 3);		      	
			      	 a = Episim3DAppearanceFactory.getCellAppearanceForColorNoMaterial(polygonAttributes, nucleusColor,1.0f);
			      	 if(a.getRenderingAttributes() !=null) a.getRenderingAttributes().setVisible(actCrossSectionMode != null && actCrossSectionMode != ModelSceneCrossSectionMode.DISABLED && isNucleated);
			      	 shapeInnerNucleus.setAppearance(a);
		      	 }
		       }
		       
	       }
			
			float transparencyFactor = 1.0f;
			if(getCurrentDisplay() instanceof Display3DHack){
			   	transparencyFactor = (float)((Display3DHack)getCurrentDisplay()).getModelSceneOpacity();
			}
			 boolean isNucleated = true;
			 boolean isViable = true;
      	 if(universalCell.getEpisimBioMechanicalModelObject() instanceof CenterBased3DModel){
      		 isNucleated=((CenterBased3DModel)universalCell.getEpisimBioMechanicalModelObject()).hasNucleus();
      		 isViable=((CenterBased3DModel)universalCell.getEpisimBioMechanicalModelObject()).hasViablility();
      	 }
			Color cellColor =universalCell.getCellColoring();			 
			Shape3D shapeCell = getShape(j3dModel, 0);
			shapeCell.setAppearance(Episim3DAppearanceFactory.getCellAppearanceForColor(polygonAttributes, cellColor,transparencyFactor, isViable));
			if(optimizedGraphicsActivated){
				Appearance a = null;
				if(drawInnerCellSurface){
					Shape3D shapeInnerCell = getShape(j3dModel, 1);
					a = Episim3DAppearanceFactory.getCellAppearanceForColorNoMaterial(polygonAttributes, cellColor,transparencyFactor);
					if(a.getRenderingAttributes() !=null)a.getRenderingAttributes().setVisible(actCrossSectionMode != null && actCrossSectionMode != ModelSceneCrossSectionMode.DISABLED);
					shapeInnerCell.setAppearance(a);
				}
				int nucleusShapeIndex = drawInnerCellSurface ? 2 : 1;
				Shape3D shapeNucleus = getShape(j3dModel, nucleusShapeIndex);
				a = Episim3DAppearanceFactory.getNucleusAppearance(polygonAttributes, transparencyFactor);
				if(a.getRenderingAttributes() !=null)a.getRenderingAttributes().setVisible(!(actCrossSectionMode != null && actCrossSectionMode != ModelSceneCrossSectionMode.DISABLED) &&isNucleated);
				shapeNucleus.setAppearance(a);
				if(drawInnerCellSurface){
					Shape3D shapeInnerNucleus = getShape(j3dModel, 3);
					a = Episim3DAppearanceFactory.getCellAppearanceForColorNoMaterial(polygonAttributes, nucleusColor,transparencyFactor);
					if(a.getRenderingAttributes() !=null)a.getRenderingAttributes().setVisible(actCrossSectionMode != null && actCrossSectionMode != ModelSceneCrossSectionMode.DISABLED&&isNucleated);
					shapeInnerNucleus.setAppearance(a);
				}
			}
		}
		else if (obj instanceof DummyCell){
			DummyCell dummyCell = (DummyCell) obj;
			ModelSceneCrossSectionMode actCrossSectionMode=null;
			if(((Display3DHack)getCurrentDisplay()) != null){
				actCrossSectionMode = ((Display3DHack)getCurrentDisplay()).getModelSceneCrossSectionMode();
				
			}
			if (j3dModel==null)
		   {
		       j3dModel = new TransformGroup();
		       j3dModel.setCapability(Group.ALLOW_CHILDREN_READ);
		       
		       // build a LocationWrapper for the object
		       LocationWrapper pickI = new LocationWrapper(obj, null, getCurrentFieldPortrayal());
		
		      
		
		       if (transform != null)
		       {
		           TransformGroup tg = new TransformGroup();
		           tg.setTransform(transform);
		           tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		           tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		           tg.setCapability(Group.ALLOW_CHILDREN_READ);
		           tg.addChild(cellSphere.cloneTree(true));
		           if(optimizedGraphicsActivated){
			           if(drawInnerCellSurface)tg.addChild(innerCellSphere.cloneTree(true));
			           tg.addChild(nucleusSphere.cloneTree(true));
			           if(drawInnerCellSurface)tg.addChild(innerNucleusSphere.cloneTree(true));
		           }
		           j3dModel.addChild(tg);
		       }
		       else{
		      	 j3dModel.addChild(cellSphere.cloneTree(true));
		      	 if(optimizedGraphicsActivated){
		      		 if(drawInnerCellSurface)j3dModel.addChild(innerCellSphere.cloneTree(true));
		      		 j3dModel.addChild(nucleusSphere.cloneTree(true));
		      		 if(drawInnerCellSurface)j3dModel.addChild(innerNucleusSphere.cloneTree(true));
		      	 }
		       }
		   	  Shape3D shape = getShape(j3dModel, 0);
		       shape.setAppearance(appearance);		       
		       if (pickable) setPickableFlags(shape);
		       shape.setUserData(pickI);
		       	       
	       }
			
			float transparencyFactor = 1.0f;
			if(getCurrentDisplay() instanceof Display3DHack){
			   	transparencyFactor = (float)((Display3DHack)getCurrentDisplay()).getModelSceneOpacity();
			}
			 
			Color cellColor = Color.YELLOW;
			Shape3D shapeCell = getShape(j3dModel, 0);
			shapeCell.setAppearance(Episim3DAppearanceFactory.getCellAppearanceForColor(polygonAttributes, cellColor,transparencyFactor, false));			
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
	  TransformGroup lastGroup=null;
	  while(n instanceof TransformGroup){
		  lastGroup=((TransformGroup)n);
	      n = ((TransformGroup)n).getChild(0);
	  }
	  Primitive p =null;
	  if(lastGroup!=null){
		  p = (Primitive) lastGroup.getChild(shapeIndex);
		  
	  }
	  else{
		  p = (Primitive) n;
		 
	  }
	  return p.getShape(0);
  }  
 
}
