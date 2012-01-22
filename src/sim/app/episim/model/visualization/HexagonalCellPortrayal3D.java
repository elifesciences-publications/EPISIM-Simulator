package sim.app.episim.model.visualization;

import java.awt.Color;
import java.awt.color.ColorSpace;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Group;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
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

import sim.app.episim.model.biomechanics.hexagonbased3d.HexagonBased3DMechanicalModelGP;
import sim.app.episim.model.controller.ModelController;
import sim.portrayal.LocationWrapper;
import sim.portrayal3d.SimplePortrayal3D;
import sim.portrayal3d.simple.SpherePortrayal3D;


public class HexagonalCellPortrayal3D extends SimplePortrayal3D {
	
	
	Transform3D transform;

   Appearance appearance;

 
   protected Node group;
       
   boolean pickable = true;
	
	private Sphere sphere;
	
	private static final double FACTOR = 0.7;
	
	private float standardCellRadius =1;
	
	
	private HexagonBased3DMechanicalModelGP globalParameters;

	
	public HexagonalCellPortrayal3D()
   {
		//this(getCellAppearanceForColor(new Color(230, 130, 170),new Color(255,175,205), new Color(220,0,0)),true,false,scale,divisions);
		this(getCellAppearanceForColor(new Color(255,180,180)),true,false);
   }
	
	public HexagonalCellPortrayal3D(Appearance appearance, boolean generateNormals, boolean generateTextureCoordinates)
   {
		globalParameters = (HexagonBased3DMechanicalModelGP)ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		standardCellRadius = (float)HexagonBased3DMechanicalModelGP.outer_hexagonal_radius;
		this.appearance = appearance;
		    
		
		this.sphere = new Sphere(standardCellRadius, (generateNormals ? Primitive.GENERATE_NORMALS : 0) | 
        (generateTextureCoordinates ? Primitive.GENERATE_TEXTURE_COORDS : 0), 
        30, appearance);
		
		setShape3DFlags(sphere.getShape(Sphere.BODY));
		transform = new Transform3D();
		transform.setTranslation(new Vector3f(standardCellRadius,standardCellRadius*2,standardCellRadius));		 
		group = sphere;
   }
	 
	public TransformGroup getModel(Object obj, TransformGroup j3dModel){
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
	
	       int numShapes = numShapes();
	       for(int i = 0; i < numShapes; i++)
	       {
	           Shape3D shape = getShape(j3dModel, i);
	           shape.setAppearance(appearance);
	           if (pickable) setPickableFlags(shape);
	
	           // Store the LocationWrapper in the user data of each shape
	           shape.setUserData(pickI);
	       }
       }
   return j3dModel;
		//return super.getModel(obj, j3dModel);
	}
	
	private static Appearance getCellAppearanceForColor(Color color){
		Appearance appearance = new Appearance();
      setAppearanceFlags(appearance);      
      
      float[] hsbColor = new float[3];
      hsbColor= Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsbColor); 
      
      Color3f middleColor = getMiddleColor(hsbColor.clone());
      Color3f darkColor = getDarkColor(hsbColor.clone());
      Color3f brightColor = getBrightColor(hsbColor.clone());   
	   Material ma = new Material(darkColor, darkColor, middleColor, brightColor, 120f);
	   ma.setCapability(Material.ALLOW_COMPONENT_READ);
	   ma.setCapability(Material.ALLOW_COMPONENT_WRITE);
	   appearance.setMaterial(ma);	   
      if (color.getRGBComponents(null)[3] < 1.0)  // partially transparent
          {
          TransparencyAttributes tta = new TransparencyAttributes(TransparencyAttributes.BLENDED, 1.0f - color.getRGBComponents(null)[3]); // duh, alpha's backwards
          tta.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
          tta.setCapability(TransparencyAttributes.ALLOW_VALUE_READ);
          appearance.setTransparencyAttributes(tta);
          }
      return appearance;
	}
	
	private static Color3f getDarkColor(float[] hsbColor){
		Color resultingColor = Color.getHSBColor(hsbColor[0],hsbColor[1], hsbColor[2]);
		
		if(hsbColor[1] < 1f ||hsbColor[2] > 0.8f){
			resultingColor = darker(resultingColor);
			//resultingColor = darker(resultingColor);
		}		
		float[] c_result = resultingColor.getRGBComponents(null);
		return new Color3f(c_result[0], c_result[1], c_result[2]);
	}
	
	private static Color3f getMiddleColor(float[] hsbColor){
		Color resultingColor = Color.getHSBColor(hsbColor[0],hsbColor[1], hsbColor[2]);
		if(hsbColor[1] < 0.3f){
			resultingColor = darker(resultingColor);
		}
		
		if(hsbColor[1] > 0.9f || hsbColor[2] < 0.5f){
			resultingColor = brighter(resultingColor);
		}	
		
		float[] c_result = resultingColor.getRGBComponents(null);
		return new Color3f(c_result[0], c_result[1], c_result[2]);
	}
	private static Color3f getBrightColor(float[] hsbColor){
		Color resultingColor =Color.getHSBColor(hsbColor[0],hsbColor[1], hsbColor[2]);
		if(hsbColor[1] > 0.6f || hsbColor[2] < 0.5f){
			resultingColor = brighter(resultingColor);
			resultingColor = brighter(resultingColor);
		}		
		float[] c_result = resultingColor.getRGBComponents(null);
		return new Color3f(c_result[0], c_result[1], c_result[2]);
	}
	
	private static Color brighter(Color c) {
      int r = c.getRed();
      int g = c.getGreen();
      int b = c.getBlue();
      int alpha = c.getAlpha();
      r = r == 0 ? 100 : r;
      g = g == 0 ? 100 : g;
      b = b == 0 ? 100 : b;

      /* From 2D group:
       * 1. black.brighter() should return grey
       * 2. applying brighter to blue will always return blue, brighter
       * 3. non pure color (non zero rgb) will eventually return white
       */
      int i = (int)(1.0/(1.0-FACTOR));
      if ( r == 0 && g == 0 && b == 0) {
          return new Color(i, i, i, alpha);
      }
      if ( r > 0 && r < i ) r = i;
      if ( g > 0 && g < i ) g = i;
      if ( b > 0 && b < i ) b = i;

      return new Color(Math.min((int)(r/FACTOR), 255),
                       Math.min((int)(g/FACTOR), 255),
                       Math.min((int)(b/FACTOR), 255),
                       alpha);
  }
	
  private static Color darker(Color c) {
       return new Color(Math.max((int)(c.getRed()  *FACTOR), 0),
                        Math.max((int)(c.getGreen()*FACTOR), 0),
                        Math.max((int)(c.getBlue() *FACTOR), 0),
                        c.getAlpha());
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
  protected int numShapes() { return 1; }

}
