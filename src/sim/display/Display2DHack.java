package sim.display;


public class Display2DHack extends Display2D {

	public Display2DHack(double width, double height, GUIState simulation, long interval) {

		super(width, height, simulation, interval);
		
		//no unnecessary Entry: Show Console in the popup
		if(popup != null && popup.getComponentCount()>1){
			popup.remove(0);
			popup.remove(0);
		}
		
	}
	
	

}
