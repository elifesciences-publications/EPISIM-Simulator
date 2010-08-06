package sim.app.episim.snapshot;


public class SnapshotObject implements java.io.Serializable {
	
	
	public static final String WOUND = "Wound";
	public static final String CELL = "Cell";
	public static final String CELLCONTINUOUS = "CellContinuous";
	public static final String TIMESTEPS = "TimeSteps";
	public static final String CHARTS = "Charts";
	public static final String MECHANICALMODELGLOBALPARAMETERS = "MechanicalModelGlobalParameters";
	public static final String MISCALLENEOUSGLOBALPARAMETERS = "MiscalenneousGlobalParameters";
	public static final String CELLBEHAVIORALMODELGLOBALPARAMETERS = "CellBehavioralModelGlobalParameters";
	
	
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
