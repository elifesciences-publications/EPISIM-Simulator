package sim.app.episim.visualization.threedim;

import java.awt.Color;
import java.awt.color.ColorSpace;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Group;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;

import episiminterfaces.EpisimCellBehavioralModel;
import sim.app.episim.gui.EpisimAboutDialog;
import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.biomechanics.latticebased3Dr.LatticeBased3DModel;
import sim.app.episim.model.biomechanics.latticebased3Dr.LatticeBased3DModelGP;
import sim.app.episim.model.controller.ModelController;
import sim.display3d.Display3DHack;
import sim.portrayal.LocationWrapper;
import sim.portrayal3d.SimplePortrayal3D;
import sim.portrayal3d.simple.SpherePortrayal3D;
import sim.util.Int3D;


public class LatticeCellPortrayal3D extends SimplePortrayal3D {
	
	
	Transform3D transform;

   Appearance appearance;

 
   protected Node group;
       
   boolean pickable = true;
	
	private Sphere sphere;
	
	private static final double FACTOR = 0.7;
	
	private float standardCellRadius =1;
	
	
	private LatticeBased3DModelGP globalParameters;
	private PolygonAttributes polygonAttributes;
	
	public LatticeCellPortrayal3D(PolygonAttributes polygonAttributes)
   {
		//this(getCellAppearanceForColor(new Color(230, 130, 170),new Color(255,175,205), new Color(220,0,0)),true,false,scale,divisions);
		this(polygonAttributes, true,false);
   }
	
	public LatticeCellPortrayal3D(PolygonAttributes polygonAttributes,boolean generateNormals, boolean generateTextureCoordinates)
   {
		globalParameters = (LatticeBased3DModelGP)ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		standardCellRadius = (float)LatticeBased3DModelGP.hexagonal_radius;
		this.polygonAttributes = polygonAttributes;
		float transparencyFactor = 1.0f;
		if(getCurrentDisplay() instanceof Display3DHack){
		   	transparencyFactor = (float)((Display3DHack)getCurrentDisplay()).getModelSceneOpacity();
		}
		this.appearance = Episim3DAppearanceFactory.getCellAppearanceForColor(polygonAttributes,(new Color(255,160,160)),transparencyFactor);
	
		    
		
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
			EpisimCellBehavioralModel cbm = universalCell.getEpisimCellBehavioralModelObject();
			float transparencyFactor = 1.0f;
			if(getCurrentDisplay() instanceof Display3DHack){
			   	transparencyFactor = (float)((Display3DHack)getCurrentDisplay()).getModelSceneOpacity();
			}
		   shape.setAppearance(Episim3DAppearanceFactory.getCellAppearanceForColor(polygonAttributes,(universalCell.getCellColoring()),transparencyFactor));
			
		} 
		return j3dModel;
		
	}
	
	
  
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
