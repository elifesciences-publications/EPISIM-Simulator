package sim.app.episim.visualization.threedim;

import java.awt.Color;

import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;

import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters.MiscalleneousGlobalParameters3D;
import sim.display3d.Display3DHack;
import sim.portrayal3d.SimplePortrayal3D;


public class Episim3DAppearanceFactory {
	private static final double FACTOR = 0.7;
	
	public static Appearance getCellAppearanceForColor(Color color){
		return getCellAppearanceForColor(color, 1.0f);
	}
	
	public static Appearance getCellAppearanceForColor(Color color, float opacity){
		return getCellAppearanceForColor(null, color, opacity);
	}	
	
	public static Appearance getCellAppearanceForColor(PolygonAttributes polygonAttributes, Color color, float opacity){
		Appearance appearance = new Appearance();
		appearance.setRenderingAttributes(new RenderingAttributes());
      SimplePortrayal3D.setAppearanceFlags(appearance);      
      MiscalleneousGlobalParameters param = MiscalleneousGlobalParameters.getInstance();
      boolean optimizedGraphicsActivated =false;
      if(param instanceof MiscalleneousGlobalParameters3D && ((MiscalleneousGlobalParameters3D)param).getOptimizedGraphics()){	
			optimizedGraphicsActivated = true;
		}
      
      float[] hsbColor = new float[3];
      hsbColor= Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsbColor); 
      
      Color3f middleColor = getMiddleColor(hsbColor.clone());
      Color3f darkColor = getDarkColor(hsbColor.clone());
      Color3f brightColor = getBrightColor(hsbColor.clone());   
	   
      
  //    Color3f emissiveColor =new Color3f(new Color(0,0,0));
   //   Color3f specularColor =new Color3f(new Color(12,12,12));
   //   Material ma = new Material(darkColor, darkColor, middleColor, brightColor, 120f);
    
     // if(optimizedGraphicsActivated)ma = new Material(brightColor, emissiveColor, middleColor, specularColor, 7f);
      
      
	     
    Color3f ambientColor =new Color3f(new Color(255,204,204));
      Color3f diffuseColor =new Color3f(new Color(255,102,102));
      Color3f emissiveColor =new Color3f(new Color(0,0,0));
      Color3f specularColor =new Color3f(new Color(12,12,12));
      Material ma = new Material(ambientColor, emissiveColor, diffuseColor, specularColor, 7f);
	   ma.setCapability(Material.ALLOW_COMPONENT_READ);
	   ma.setCapability(Material.ALLOW_COMPONENT_WRITE);
	  
	   appearance.setMaterial(ma);	
	   
	  
	  
	   
      if (opacity < 1.0)  // partially transparent
      {
          TransparencyAttributes tta = new TransparencyAttributes(TransparencyAttributes.BLENDED, 1.0f - opacity); 
          tta.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
          tta.setCapability(TransparencyAttributes.ALLOW_VALUE_READ);
          appearance.setTransparencyAttributes(tta);
      }
     
     
      if(polygonAttributes != null) appearance.setPolygonAttributes(polygonAttributes);
      return appearance;
	}
	
	public static Appearance getCellAppearanceForColorNoMaterial(Color color){
		return getCellAppearanceForColor(color, 1.0f);
	}
	
	public static Appearance getCellAppearanceForColorNoMaterial(Color color, float opacity){
		return getCellAppearanceForColor(null, color, opacity);
	}	
	
	public static Appearance getCellAppearanceForColorNoMaterial(PolygonAttributes polygonAttributes, Color color, float opacity){
		Appearance appearance = new Appearance();
		appearance.setRenderingAttributes(new RenderingAttributes());
      SimplePortrayal3D.setAppearanceFlags(appearance);      
      MiscalleneousGlobalParameters param = MiscalleneousGlobalParameters.getInstance();
      boolean optimizedGraphicsActivated =false;
      if(param instanceof MiscalleneousGlobalParameters3D && ((MiscalleneousGlobalParameters3D)param).getOptimizedGraphics()){	
			optimizedGraphicsActivated = true;
		}
      
     
	     
    Color3f color3f =new Color3f(color);
     
	   
	  appearance.setColoringAttributes(new ColoringAttributes(color3f, ColoringAttributes.NICEST));
	  
	   
      if (opacity < 1.0)  // partially transparent
      {
          TransparencyAttributes tta = new TransparencyAttributes(TransparencyAttributes.BLENDED, 1.0f - opacity); 
          tta.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
          tta.setCapability(TransparencyAttributes.ALLOW_VALUE_READ);
          appearance.setTransparencyAttributes(tta);
      } 
      if(polygonAttributes != null) appearance.setPolygonAttributes(polygonAttributes);
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
      r = (r <=75) ? (75+r) : r;
      g = (g <=75) ? (75+g) : g;
      b = (b <=75) ? (75+b) : b;

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
	
}
