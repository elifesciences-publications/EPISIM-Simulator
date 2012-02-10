package sim.app.episim.model.visualization;

import java.awt.Color;
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
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased3d.CenterBased3DMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased3d.CenterBased3DMechanicalModelGP;
import sim.app.episim.model.biomechanics.vertexbased.VertexBasedMechanicalModel;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.visualization.Episim3DAppearanceFactory;
import sim.display3d.Display3DHack;
import sim.portrayal.LocationWrapper;
import sim.portrayal3d.SimplePortrayal3D;

import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;

import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimDifferentiationLevel;


public class UniversalCellPortrayal3D extends SimplePortrayal3D {
	
	
	Transform3D transform;

   Appearance appearance;

 
   protected Node group;
       
   boolean pickable = true;
	
	private Sphere sphere;
	
	
	
	private float standardCellRadius =1;
	
	
	private CenterBased3DMechanicalModelGP globalParameters;
	private PolygonAttributes polygonAttributes;
	
	public UniversalCellPortrayal3D(PolygonAttributes polygonAttributes)
   {
		//this(getCellAppearanceForColor(new Color(230, 130, 170),new Color(255,175,205), new Color(220,0,0)),true,false,scale,divisions);
		this(polygonAttributes, true,false);
   }
	
	public UniversalCellPortrayal3D(PolygonAttributes polygonAttributes,boolean generateNormals, boolean generateTextureCoordinates)
   {
		globalParameters = (CenterBased3DMechanicalModelGP)ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		standardCellRadius = (float)(CenterBased3DMechanicalModel.INITIAL_KERATINO_WIDTH/2d);
		this.polygonAttributes = polygonAttributes;
			
		 float transparencyFactor = 1.0f;
		 if(getCurrentDisplay() instanceof Display3DHack){
		   	transparencyFactor = (float)((Display3DHack)getCurrentDisplay()).getModelSceneOpacity();
		 }
		this.appearance = Episim3DAppearanceFactory.getCellAppearanceForColor(this.polygonAttributes, new Color(255,160,160), transparencyFactor);
		    
		
		this.sphere = new Sphere(standardCellRadius, (generateNormals ? Primitive.GENERATE_NORMALS : 0) | 
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
			
			Color cellColor =getFillColor(universalCell);
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
  
  private Color getFillColor(UniversalCell kcyte){
  	int keratinoType=kcyte.getEpisimCellBehavioralModelObject().getDiffLevel().ordinal();                                
     int coloringType=MiscalleneousGlobalParameters.instance().getTypeColor();
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
       	  
             
           if((kcyte.getIsOuterCell()) && (coloringType==2)){red=0xF3; green=0xBE; blue=0x4E;}        
           boolean isMembraneCell = false;
           if(kcyte.getEpisimBioMechanicalModelObject() instanceof CenterBased3DMechanicalModel){
         	  isMembraneCell=((CenterBased3DMechanicalModel)kcyte.getEpisimBioMechanicalModelObject()).isMembraneCell();        
              if((((CenterBased3DMechanicalModel)kcyte.getEpisimBioMechanicalModelObject()).nextToOuterCell()) && (coloringType==2))
              {red=255; green=255; blue=255;}
              
           }
           
           if(isMembraneCell && (coloringType==2)){red=0xF3; green=0xFF; blue=0x4E;}                        
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
     
      if(coloringType>=4){ //Colors are calculated in the cellbehavioral model
        red=kcyte.getEpisimCellBehavioralModelObject().getColorR();
        green=kcyte.getEpisimCellBehavioralModelObject().getColorG();
        blue=kcyte.getEpisimCellBehavioralModelObject().getColorB();
      }
       
     // Limit the colors to 255
     green=(green>255)?255:((green<0)?0:green);
     red=(red>255)?255:((red<0)?0:red);
     blue=(blue>255)?255:((blue<0)?0:blue);
     
     if(kcyte.getIsTracked()) return Color.RED;
     return new Color(red, green, blue);
  }
}
