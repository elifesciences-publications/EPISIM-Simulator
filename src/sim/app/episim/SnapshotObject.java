package sim.app.episim;


public class SnapshotObject implements java.io.Serializable {
	
	
	public static final String WOUND = "Wound";
	public static final String EPIDERMIS = "Epidermis";
	public static final String CHARTS = "Charts";
	
	
	private static final long serialVersionUID = -4796696464984805544L;
	
	private String identifier;
	
	private Object  snapshotObject;
	
	public SnapshotObject(String identifier, Object  snapshotObject){
		this.identifier = identifier;
		this.snapshotObject = snapshotObject;
	}

	
	public String getIdentifier() {
	
		return identifier;
	}

	
	public Object getSnapshotObject() {
	
		return snapshotObject;
	}

}
