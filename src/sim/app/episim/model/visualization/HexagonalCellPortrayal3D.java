package sim.app.episim.model.visualization;

import java.awt.Color;
import java.awt.color.ColorSpace;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Material;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;

import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;

import sim.portrayal3d.simple.SpherePortrayal3D;
import sim.portrayal3d.simple.SpherePortrayal3DHack;


public class HexagonalCellPortrayal3D extends SpherePortrayal3DHack {
	
	private Sphere sphere;
	
	public HexagonalCellPortrayal3D()
   {
		this(1f);
   }    
	 		
	public HexagonalCellPortrayal3D(double scale)
   {
		 this(scale, 30);
		 
   }
	

	
	public HexagonalCellPortrayal3D( double scale, int divisions)
   {
		//this(getCellAppearanceForColor(new Color(230, 130, 170),new Color(255,175,205), new Color(220,0,0)),true,false,scale,divisions);
		this(getCellAppearanceForColor(new Color(30,0,250)),true,false,scale,divisions);
   }
	
	public HexagonalCellPortrayal3D(Appearance appearance, boolean generateNormals, boolean generateTextureCoordinates, double scale, int divisions)
   {
		    setAppearance(appearance);  
		    setScale(null, scale); 

		    this.sphere = new Sphere(0.5f, (generateNormals ? Primitive.GENERATE_NORMALS : 0) | 
        (generateTextureCoordinates ? Primitive.GENERATE_TEXTURE_COORDS : 0), 
        divisions, appearance);
		  AmbientLight ambientLight = new AmbientLight(new Color3f(.5f,.5f,.5f));
		  ambientLight.setInfluencingBounds(sphere.getBounds());
		  setShape3DFlags(sphere.getShape(Sphere.BODY));
		   /*BranchGroup branchGroup = new BranchGroup();
		   branchGroup.addChild(sphere);
		   branchGroup.addChild(ambientLight);*/
		//  
		
		  
		   group = sphere;
   }
	 
	public TransformGroup getModel(Object obj, TransformGroup j3dModel){
	   	return super.getModel(obj, j3dModel);
	}
	
	private static Appearance getCellAppearanceForColor(Color color){
		Appearance appearance = new Appearance();
      setAppearanceFlags(appearance);
      
      
      float[] hsbColor = new float[3];
      hsbColor= Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsbColor);
      
      
      
      //TODO complete color calculation
      Color bright = Color.getHSBColor(hsbColor[0],hsbColor[1] < 0.5f ? hsbColor[1] : hsbColor[1]*1.2f, hsbColor[2]);
      Color middle = Color.getHSBColor(hsbColor[0],hsbColor[1], hsbColor[2]*0.8f);
      Color dark = Color.getHSBColor(hsbColor[0],hsbColor[1], hsbColor[2]*0.6f);
      
      float[] c_bright = bright.getRGBComponents(null);
      float[] c_middle = middle.getRGBComponents(null);
      float[] c_dark = dark.getRGBComponents(null);
     
     
      
      Color3f middleColor = new Color3f(c_middle[0], c_middle[1], c_middle[2]);
      Color3f darkColor = new Color3f(c_dark[0], c_dark[1], c_dark[2]);
      Color3f brightColor = new Color3f(c_bright[0], c_bright[1], c_bright[2]);

      				

     
	   
	   Material ma = new Material(darkColor, darkColor, middleColor, brightColor, 120f);
	   ma.setCapability(Material.ALLOW_COMPONENT_READ);
	   ma.setCapability(Material.ALLOW_COMPONENT_WRITE);
	   appearance.setMaterial(ma);
      
	   
	   
      if (c_middle[3] < 1.0)  // partially transparent
          {
          TransparencyAttributes tta = new TransparencyAttributes(TransparencyAttributes.BLENDED, 1.0f - c_middle[3]); // duh, alpha's backwards
          tta.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
          tta.setCapability(TransparencyAttributes.ALLOW_VALUE_READ);
          appearance.setTransparencyAttributes(tta);
          }
      return appearance;
	}

}
