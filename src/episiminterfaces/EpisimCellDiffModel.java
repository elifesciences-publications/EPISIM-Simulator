package episiminterfaces;


public interface EpisimCellDiffModel {
	
	double getCalSaturation();
 	void setCalSaturation(double val);
	 
 	double getLamellaSaturation();
 	void setLamellaSaturation(double val);

 	double getMinSigLipidsBarrier();
 	void setMinSigLipidsBarrier(double val);
 	
 	int getMaxCellAge();
	void setMaxCellAge(int val);

}
