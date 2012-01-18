package sim.app.episim.model.visualization;


public class EpisimDrawInfo <T> {

	private T drawInfo;
	
	public EpisimDrawInfo(T info){
		this.drawInfo = info;
	}
	
	public T getDrawInfo(){ return this.drawInfo; }
}
