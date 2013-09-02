package sim.app.episim.model.biomechanics.hexagonbased.singlesurface.tumor.chemokine;

import sim.app.episim.model.biomechanics.hexagonbased.singlesurface.HexagonBasedMechanicalModelGP;


public class HexagonBasedMechanicalModelCytokineTumorGP extends HexagonBasedMechanicalModelGP {
	private int AL_SecretionCellDensityInPerc = 100;
	private int IM_SecretionCellDensityInPerc = 100;
	private int LM_SecretionCellDensityInPerc = 100;
	
   public int getAL_SecretionCellDensityInPerc() {
   
   	return AL_SecretionCellDensityInPerc;
   }
	
   public void setAL_SecretionCellDensityInPerc(int aL_SecretionCellDensityInPerc) {
   
   	AL_SecretionCellDensityInPerc = aL_SecretionCellDensityInPerc >=0 ? aL_SecretionCellDensityInPerc : AL_SecretionCellDensityInPerc;
   }
	
   public int getIM_SecretionCellDensityInPerc() {
   
   	return IM_SecretionCellDensityInPerc;
   }
	
   public void setIM_SecretionCellDensityInPerc(int iM_SecretionCellDensityInPerc) {
   
   	IM_SecretionCellDensityInPerc = iM_SecretionCellDensityInPerc>=0 ? iM_SecretionCellDensityInPerc : IM_SecretionCellDensityInPerc;
   }
	
   public int getLM_SecretionCellDensityInPerc() {
   
   	return LM_SecretionCellDensityInPerc;
   }
	
   public void setLM_SecretionCellDensityInPerc(int lM_SecretionCellDensityInPerc) {
   
   	LM_SecretionCellDensityInPerc = lM_SecretionCellDensityInPerc>=0 ? lM_SecretionCellDensityInPerc : LM_SecretionCellDensityInPerc;
   }
}