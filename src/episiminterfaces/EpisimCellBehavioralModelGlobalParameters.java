package episiminterfaces;



public interface EpisimCellBehavioralModelGlobalParameters{
	public static final int KTYPE_UNASSIGNED=0;
	public static final int KTYPE_NONUCLEUS=1;
	public static final int KTYPE_NIRVANA=2;
	public static final int STEMCELL=3;
	public static final int EARLYSPICELL=4;
	public static final int LATESPICELL=5;
	public static final int TACELL=6;
	public static final int GRANUCELL=7;
	
	public static final int KERATINOCYTE=8;
	
		
	int getCellCycleStem();
 	void setCellCycleStem(int val);
	
	int getCellCycleTA();
 	void setCellCycleTA(int val);
 	
 	int getMaxAge();
	void setMaxAge(int val);
	
	double getMinSigLipidsBarrier();
 	void setMinSigLipidsBarrier(double val);
 	
	void resetInitialGlobalValues();
	
	void setSnapshotValues(EpisimCellBehavioralModelGlobalParameters parametersObj);
	
}
