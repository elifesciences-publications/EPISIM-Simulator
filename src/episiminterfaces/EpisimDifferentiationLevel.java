package episiminterfaces;


public interface EpisimDifferentiationLevel {	
	public static final int STEMCELL=0;
	public static final int EARLYSPICELL=1;
	public static final int LATESPICELL=2;
	public static final int TACELL=3;
	public static final int GRANUCELL=4;
	
	String name();
	int ordinal();
}
