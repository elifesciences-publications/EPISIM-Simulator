package episiminterfaces;


public interface SendReceiveAlgorithm {
	
	void sendRegular(int propertycode, double requestedAmount, EpisimCellBehavioralModel cell, EpisimCellBehavioralModel[] neighbours);
	void sendModelCycle(int propertycode, double requestedAmount, EpisimCellBehavioralModel cell, EpisimCellBehavioralModel[] neighbours, int actNeighbour);
	
	void receiveRegular(int propertycode, double requestedAmount, EpisimCellBehavioralModel cell, EpisimCellBehavioralModel[] neighbours);
	void receiveModelCycle(int propertycode, double requestedAmount, EpisimCellBehavioralModel cell, EpisimCellBehavioralModel[] neighbours, int actNeighbour);
	
	void sendDF(String ecDiffusionFieldName, int propertycode, double amount, EpisimCellBehavioralModel cell);	
	void receiveDF(String ecDiffusionFieldName, int propertycode, double amount, EpisimCellBehavioralModel cell);
}
