package sim.app.episim.visualization;

import sim.SimStateServer;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.snapshot.SnapshotListener;
import sim.app.episim.snapshot.SnapshotObject;
import sim.app.episim.snapshot.SnapshotWriter;
import sim.app.episim.tissue.TissueController;
import sim.field.continuous.Continuous2D;
import sim.portrayal.*;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.util.*;

import java.util.*;
import java.util.List;

import java.awt.*;
import java.awt.geom.*;

import episiminterfaces.EpisimPortrayal;


public class WoundPortrayal2D extends ContinuousPortrayal2D implements SnapshotListener, EpisimPortrayal {
	
	private final String NAME = "Wound Region";

	/**
	 * 
	 */
	private static final long serialVersionUID = -569327606127370200L;

	private List<Double2D> woundRegionCoordinates = new ArrayList<Double2D>();

	private boolean closeWoundRegionPath = false;

	private double width;

	private double height;

	private DrawInfo2D lastActualInfo;

	private DrawInfo2D deltaInfo;

	private GeneralPath polygon;

	private boolean refreshInfo = true;

 	public WoundPortrayal2D() {
 		 EpisimGUIState guiState = SimStateServer.getInstance().getEpisimGUIState();
 		 
 		if(guiState != null){ 			
			this.width = guiState.EPIDISPLAYWIDTH + (2*guiState.DISPLAYBORDER);
			this.height = guiState.EPIDISPLAYHEIGHT + (2*guiState.DISPLAYBORDER);
 		}
		SnapshotWriter.getInstance().addSnapshotListener(this);
		 Continuous2D field = new Continuous2D(TissueController.getInstance().getTissueBorder().getWidthInMikron() + 2, 
					TissueController.getInstance().getTissueBorder().getWidthInMikron() + 2, 
					TissueController.getInstance().getTissueBorder().getHeightInMikron());
	  	 
	  	 field.setObjectLocation("DummyObject", new Double2D(50, 50));
	  	 this.setField(field);
	}

	private void createPolygon(DrawInfo2D info) {

		{

			polygon = new GeneralPath();
			((GeneralPath) polygon).moveTo(lastActualInfo.clip.getMinX() + woundRegionCoordinates.get(0).x - getDeltaX(),
					lastActualInfo.clip.getMinY() + woundRegionCoordinates.get(0).y - getDeltaY());
			for(Double2D coord : woundRegionCoordinates){
				polygon.lineTo(lastActualInfo.clip.getMinX() - getDeltaX() + coord.x, lastActualInfo.clip.getMinY() - getDeltaY() + coord.y);
			}
			if(closeWoundRegionPath)
				polygon.closePath();
		}
	}

	// assumes the graphics already has its color set
	public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {

		if(info != null){
			lastActualInfo = info;
			graphics.setColor(Color.red);

			graphics.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

			graphics = (Graphics2D) graphics.create();

			if(woundRegionCoordinates.size() > 1)
				createPolygon(info);

			else
				polygon = null;

			if(polygon != null && lastActualInfo.clip.contains(polygon.getBounds2D())){

				graphics.draw(polygon);
			}

		}

	}

	public void addMouseCoordinate(Double2D double2d) {

		deltaInfo = lastActualInfo;
		if(double2d != null && lastActualInfo != null){
			Double2D newDouble2d = new Double2D(double2d.x - lastActualInfo.clip.getMinX(), double2d.y
					- lastActualInfo.clip.getMinY());

			woundRegionCoordinates.add(newDouble2d);
		}
	}

	public void closeWoundRegionPath(boolean closewoundRegionPath) {

		refreshInfo = false;
		this.closeWoundRegionPath = closewoundRegionPath;

	}

	public GeneralPath getWoundRegion() {

		return polygon;
	}

	public void clearWoundRegionCoordinates() {

		woundRegionCoordinates.clear();
	}

	private double getDeltaX() {

		if((lastActualInfo.clip.width+1) < width){
			return lastActualInfo.clip.getMinX() - deltaInfo.clip.getMinX();
		}
		else
			return 0;
	}

	private double getDeltaY() {

		if((lastActualInfo.clip.height+1) < height){
			return lastActualInfo.clip.getMinY() - deltaInfo.clip.getMinY();
		}
		else
			return 0;
	}

	public List<SnapshotObject> collectSnapshotObjects() {

		List<SnapshotObject> list = new ArrayList<SnapshotObject>();
		if(woundRegionCoordinates.size() > 0){
			list.add(new SnapshotObject(SnapshotObject.WOUND, woundRegionCoordinates));
			list.add(new SnapshotObject(SnapshotObject.WOUND, new java.awt.geom.Rectangle2D.Double[] { deltaInfo.draw, deltaInfo.clip }));
		}
		return list;
	}

	public void setWoundRegionCoordinates(List<Double2D> woundRegionCoordinates) {

		this.woundRegionCoordinates.clear();
		this.woundRegionCoordinates = woundRegionCoordinates;
		this.closeWoundRegionPath = true;
	}

	public void setDeltaInfo(DrawInfo2D deltaInfo) {

		this.deltaInfo = deltaInfo;
	}

	public String getPortrayalName() {
	   return NAME;
   }
	
	public Rectangle2D.Double getViewPortRectangle() {
 		EpisimGUIState guiState = SimStateServer.getInstance().getEpisimGUIState();	   
 	   if(guiState != null)return new Rectangle2D.Double(guiState.DISPLAYBORDER, guiState.DISPLAYBORDER, guiState.EPIDISPLAYWIDTH, guiState.EPIDISPLAYHEIGHT);
 	   else return new Rectangle2D.Double(0,0,0, 0);
    }

}