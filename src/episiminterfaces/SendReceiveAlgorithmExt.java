package episiminterfaces;

public interface SendReceiveAlgorithmExt extends SendReceiveAlgorithm {
	double sense(String ecDiffusionFieldName, EpisimCellBehavioralModel cell);	
}