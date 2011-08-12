package episiminterfaces;


public interface SendReceiveAlgorithm {
	
	void sendRegular(int propertycode, double requestedAmount, boolean toNeighbour, EpisimCellBehavioralModel cell, EpisimCellBehavioralModel[] neighbours);
	void sendModelCycle(int propertycode, double requestedAmount, boolean toNeighbour, EpisimCellBehavioralModel cell, EpisimCellBehavioralModel[] neighbours, int actNeighbour);
	
	void receiveRegular(int propertycode, double requestedAmount, boolean fromNeighbour, EpisimCellBehavioralModel cell, EpisimCellBehavioralModel[] neighbours);
	void receiveModelCycle(int propertycode, double requestedAmount, boolean fromNeighbour, EpisimCellBehavioralModel cell, EpisimCellBehavioralModel[] neighbours, int actNeighbour);
}
