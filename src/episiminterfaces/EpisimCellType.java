package episiminterfaces;


public interface EpisimCellType {
	public static final int KERATINOCYTE=0;	
	String name();
	int ordinal();
	EpisimDifferentiationLevel[] getDifferentiationLevel();
}
