package episimbiomechanics;


public class EpisimModelIntegrator {
	
	private static final String VERSION = "2010-05-13";
	
	private double lamda =0;
	private double kappa =0;
	private double gamma =0;
	
	
	public String getBiomechanicalModelId(){
		return VERSION;
	}
	
	public EpisimModelIntegrator(){}
	
	public double getLamda() {
	
		return lamda;
	}
	
	public void setLamda(double lamda) {
	
		this.lamda = lamda;
	}
	
	public double getKappa() {
	
		return kappa;
	}
	
	public void setKappa(double kappa) {
	
		this.kappa = kappa;
	}
	
	public double getGamma() {
	
		return gamma;
	}
	
	public void setGamma(double gamma) {
	
		this.gamma = gamma;
	}
	
	
}
