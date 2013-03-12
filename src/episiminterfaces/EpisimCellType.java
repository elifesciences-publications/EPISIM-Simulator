package episiminterfaces;

public interface EpisimCellType {
	String name();
	int ordinal();
	EpisimDifferentiationLevel[] getDifferentiationLevel();
}
