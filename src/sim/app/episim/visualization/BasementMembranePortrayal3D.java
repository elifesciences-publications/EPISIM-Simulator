package sim.app.episim.visualization;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.media.j3d.Appearance;
import javax.media.j3d.Group;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import episiminterfaces.EpisimPortrayal;

import sim.app.episim.tissue.StandardMembrane.StandardMembrane3DCoordinates;
import sim.app.episim.tissue.TissueController;
import sim.display3d.Display3DHack;
import sim.portrayal3d.SimplePortrayal3D;


public class BasementMembranePortrayal3D extends SimplePortrayal3D implements EpisimPortrayal{
	private final String NAME = "Basement Membrane";  
	private Appearance appearance;
	private PolygonAttributes polygonAttributes;
	public BasementMembranePortrayal3D(){
		
		polygonAttributes = new PolygonAttributes();
		polygonAttributes.setCapability(PolygonAttributes.ALLOW_CULL_FACE_READ);
		polygonAttributes.setCapability(PolygonAttributes.ALLOW_CULL_FACE_WRITE);
		polygonAttributes.setCapability(PolygonAttributes.ALLOW_MODE_READ);
		polygonAttributes.setCapability(PolygonAttributes.ALLOW_MODE_WRITE);
		polygonAttributes.setPolygonOffsetFactor(1.2f);
		
		float transparencyFactor = 1.0f;
		if(getCurrentDisplay() instanceof Display3DHack){
		   	transparencyFactor = (float)((Display3DHack)getCurrentDisplay()).getModelSceneOpacity();
		}
		this.appearance = Episim3DAppearanceFactory.getCellAppearanceForColor(this.polygonAttributes, new Color(255,165,65), transparencyFactor);
	}
	
	public TransformGroup getModel(Object obj, TransformGroup tg){
		if(tg==null)
      {
	      TransformGroup modelTG = new TransformGroup();
	      modelTG.setCapability(Group.ALLOW_CHILDREN_READ);
	      if(TissueController.getInstance().getTissueBorder().isStandardMembraneLoaded()){
	      	StandardMembrane3DCoordinates standardMembraneCoordinates = TissueController.getInstance().getTissueBorder().getStandardMembraneCoordinates3D(true);
	      	Point3f[] membraneCoordinatesA = standardMembraneCoordinates.coordinates;
	      	Point3f[] membraneCoordinatesB = generateLowerFace(membraneCoordinatesA);
	      	Vector3f[] normalsA = generateNormals(membraneCoordinatesA, false);
	      	Vector3f[] normalsB = generateNormals(membraneCoordinatesB, true);
	      	
	      	
	      	Point3f[] frontFace = generateSideFace(standardMembraneCoordinates.frontCoordinates);
	      	Point3f[] backFace = generateSideFace(standardMembraneCoordinates.backCoordinates);
	      	Vector3f[] normalsFront = generateNormals(frontFace, true);
	      	Vector3f[] normalsBack = generateNormals(backFace, false);
	      	
	      	Point3f[] sideFaceLeft = generateSideFace(standardMembraneCoordinates.leftCoordinates);
	      	Point3f[] sideFaceRight = generateSideFace(standardMembraneCoordinates.rightCoordinates);
	      	Vector3f[] normalsLeft = generateNormals(sideFaceLeft, false);
	      	Vector3f[] normalsRight = generateNormals(sideFaceRight, true);
	      	
	         QuadArray quadArray = new QuadArray((membraneCoordinatesA.length+membraneCoordinatesB.length + frontFace.length + backFace.length + sideFaceLeft.length+sideFaceRight.length), QuadArray.COORDINATES | QuadArray.NORMALS); 
	         int startIndex=0;
	         int stopIndex = membraneCoordinatesA.length;
	         for(int i = startIndex; i < stopIndex; i++){
	      	  quadArray.setCoordinate(i,membraneCoordinatesA[i]);
	      	  quadArray.setNormal(i, normalsA[i]);
	      	  
	         }
	         
	         startIndex += membraneCoordinatesA.length;
	         stopIndex += membraneCoordinatesB.length;	         
	         for(int i =  startIndex; i < stopIndex; i++){
		      	  quadArray.setCoordinate(i,membraneCoordinatesB[i-startIndex]);
		      	  quadArray.setNormal(i, normalsB[i-startIndex]);
		      	  
		      }
	         
	         startIndex += membraneCoordinatesB.length;
	         stopIndex += frontFace.length;	         
	         for(int i =  startIndex; i < stopIndex; i++){
		      	  quadArray.setCoordinate(i,frontFace[i-startIndex]);
		      	  quadArray.setNormal(i, normalsFront[i-startIndex]);		      	  
		      }
	         
	         startIndex += frontFace.length;
	         stopIndex += backFace.length;	         
	         for(int i =  startIndex; i < stopIndex; i++){
		      	  quadArray.setCoordinate(i,backFace[i-startIndex]);
		      	  quadArray.setNormal(i, normalsBack[i-startIndex]);		      	  
		      }
	         
	         startIndex += backFace.length;
	         stopIndex += sideFaceLeft.length;	         
	         for(int i =  startIndex; i < stopIndex; i++){
		      	  quadArray.setCoordinate(i,sideFaceLeft[i-startIndex]);
		      	  quadArray.setNormal(i, normalsLeft[i-startIndex]);		      	  
		      }
	         
	         startIndex += sideFaceLeft.length;
	         stopIndex += sideFaceRight.length;	         
	         for(int i =  startIndex; i < stopIndex; i++){
		      	  quadArray.setCoordinate(i,sideFaceRight[i-startIndex]);
		      	  quadArray.setNormal(i, normalsRight[i-startIndex]);		      	  
		      }
	         
	         
	         Shape3D s = new Shape3D(quadArray,appearance);
	         modelTG.addChild(s);
	      }
	      if(getCurrentDisplay() instanceof Display3DHack){
		   	Display3DHack disp = (Display3DHack) getCurrentDisplay();
		   	if(disp.getModelClip() != null){
		   		disp.getModelClip().addScope(modelTG);
		   	}
		   }
	      return modelTG;
      }
		else return tg;
	}
	
	private Vector3f[] generateNormals(Point3f[] membraneCoordinates, boolean negateVector){
		Vector3f[] normals = new Vector3f[membraneCoordinates.length];
		if(membraneCoordinates.length % 4 ==0){
			for(int i = 0; i < normals.length; i+=4){
				Point3f a = membraneCoordinates[i];
				Point3f b = membraneCoordinates[i+1];
				Point3f c = membraneCoordinates[i+3];
				Vector3f dirVect1 = new Vector3f(b.x-a.x, b.y-a.y,b.z-a.z);
				Vector3f dirVect2 = new Vector3f(c.x-a.x, c.y-a.y,c.z-a.z);
				Vector3f normal = new Vector3f();
				normal.cross(dirVect1, dirVect2);				
				normal.normalize();
				if(negateVector) normal.negate();
				normals[i]= normal;
				normals[i+1]= normal;
				normals[i+2]= normal;
				normals[i+3]= normal;
				
			}
		}
		return normals;
	}
	private Point3f[] generateLowerFace(Point3f[] points){
		Point3f[] lowerFace = new Point3f[points.length];
		final float height =1;
		for(int i = 0; i < lowerFace.length; i++){
			lowerFace[i] = new Point3f(points[i].x,points[i].y-height,points[i].z);
		}
		
		return lowerFace;
	}
	
	private Point3f[] generateSideFace(Point3f[] points){
		ArrayList<Point3f> pointsSide = new ArrayList<Point3f>();
		final float height =1;
		for(int i = 0; i < (points.length-1); i++){
			pointsSide.add(new Point3f(points[i]));
			pointsSide.add(new Point3f(points[i].x,points[i].y-height,points[i].z));
			pointsSide.add(new Point3f(points[i+1].x,points[i+1].y-height,points[i+1].z));
			pointsSide.add(new Point3f(points[i+1]));
		}		
		return pointsSide.toArray(new Point3f[pointsSide.size()]);		
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

	
   public String getPortrayalName() {
	   return NAME;
   }

	
   public Rectangle2D.Double getViewPortRectangle() {	   
	   return new Rectangle2D.Double(0d, 0d, 0d, 0d);
   }

}
