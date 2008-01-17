package sim.app.episim.model;


public interface EpisimCellDiffModelGlobalParameters {
	public static final int KTYPE_UNASSIGNED=0;
	public static final int KTYPE_STEM=1;
	public static final int KTYPE_BASAL=2;
	public static final int KTYPE_TA=3;
	public static final int KTYPE_SPINOSUM=4;
	public static final int KTYPE_LATESPINOSUM=5;        
	public static final int KTYPE_GRANULOSUM=7;
	public static final int KTYPE_RENAME=8;
	public static final int KTYPE_NONUCLEUS=9;
	public static final int KTYPE_NIRVANA=9;
	
	int getStemCycle_t();
 	void setStemCycle_t(int val);

	
	int getCellCycleTA();
 	void setCellCycleTA(int val);
 	

 	public int getMaxCellAge();
	public void setMaxCellAge(int val);
}
