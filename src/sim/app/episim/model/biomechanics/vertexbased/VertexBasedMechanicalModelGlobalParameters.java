package sim.app.episim.model.biomechanics.vertexbased;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import sim.app.episim.ExceptionDisplayer;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;


public class VertexBasedMechanicalModelGlobalParameters implements EpisimBiomechanicalModelGlobalParameters, java.io.Serializable {
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface NotInStatisticsHeaderString{}	
	
	/**
    * 
    */
	@NotInStatisticsHeaderString()
   private static final long serialVersionUID = -2526221430357457145L;
   
	private double kappa = 650;
	private double lambda = 15000;
	private double lambda_high_factor = 1;//2;
	private double lambda_low_factor = 1;//0.4;
	private double gamma = 2500;
	private double pref_perimeter_factor = 1; //could be 1 or zero to take the preferred perimeter of a cell into account or not
	private double size_percentage_cell_division = 0.7;
	private double min_edge_length_percentage = 0.25;
	private double growth_rate_per_sim_step = 10;
	
	//----------------------------------------------------------------------------------------------------------------------
	// Other required Parameters
	//----------------------------------------------------------------------------------------------------------------------
		@NotInStatisticsHeaderString
		private int basalAmplitude_�m = 40; // depth of an undulation
		@NotInStatisticsHeaderString
		private int basalOpening_�m = 250; // width of undulation at the middle
		@NotInStatisticsHeaderString
		private double width = 140;
		@NotInStatisticsHeaderString
		private double neighborhood_�m= 10.0;
	//----------------------------------------------------------------------------------------------------------------------
	
	
	@NotInStatisticsHeaderString
	private static VertexBasedMechanicalModelGlobalParameters instance = new VertexBasedMechanicalModelGlobalParameters();
	
	public VertexBasedMechanicalModelGlobalParameters() {
	}
	
	public String getStatisticsHeaderString(){ return buildStatisticsHeaderString(); }

	public static VertexBasedMechanicalModelGlobalParameters getInstance(){ return instance; }
	
	private String buildStatisticsHeaderString(){
		StringBuffer headerStringBuffer = new StringBuffer();
		Field[] declaredFields = this.getClass().getDeclaredFields();
		
		for(Field f : declaredFields){
			if(f.getAnnotation(NotInStatisticsHeaderString.class) == null){
				headerStringBuffer.append(f.getName());
				headerStringBuffer.append(";");
			}
		}
		headerStringBuffer.append("\n");
		for(Field f : declaredFields){
			if(f.getAnnotation(NotInStatisticsHeaderString.class) == null){
				try{
	            headerStringBuffer.append(f.get(this));
            }
            catch (IllegalArgumentException e){
	            ExceptionDisplayer.getInstance().displayException(e);
            }
            catch (IllegalAccessException e){
            	 ExceptionDisplayer.getInstance().displayException(e);
            }
				headerStringBuffer.append(";");
			}
		}
		
		return headerStringBuffer.toString();
	}
	
	public int getBasalAmplitude_�m() { return basalAmplitude_�m; }	
   public void setBasalAmplitude_�m(int basalAmplitude_�m) { this.basalAmplitude_�m = basalAmplitude_�m; }
	
   public int getBasalOpening_�m() { return basalOpening_�m; }	
   public void setBasalOpening_�m(int basalOpening_�m) { this.basalOpening_�m = basalOpening_�m; }
	
   public double getWidth() {	return width; }	
   public void setWidth(double width) { this.width = width; }
	
   public double getNeighborhood_�m() { return neighborhood_�m; }
   public void setNeighborhood_�m(double neighborhood_�m) { this.neighborhood_�m = neighborhood_�m; }
	
   public double getKappa() {
   
   	return kappa;
   }



	
   public void setKappa(double kappa) {
   
   	this.kappa = kappa;
   }



	
   public double getLambda() {
   
   	return lambda;
   }



	
   public void setLambda(double lambda) {
   
   	this.lambda = lambda;
   }



	
   public double getLambda_high_factor() {
   
   	return lambda_high_factor;
   }



	
   public void setLambda_high_factor(double lambda_high_factor) {
   
   	this.lambda_high_factor = lambda_high_factor;
   }



	
   public double getLambda_low_factor() {
   
   	return lambda_low_factor;
   }



	
   public void setLambda_low_factor(double lambda_low_factor) {
   
   	this.lambda_low_factor = lambda_low_factor;
   }



	
   public double getGamma() {
   
   	return gamma;
   }



	
   public void setGamma(double gamma) {
   
   	this.gamma = gamma;
   } 

	
   public double getGrowth_rate_per_sim_step() {
   
   	return growth_rate_per_sim_step;
   }

	
   public void setGrowth_rate_per_sim_step(double growth_rate_per_sim_step) {
   
   	this.growth_rate_per_sim_step = growth_rate_per_sim_step;
   }

	
   public double getPref_perimeter_factor() {
   
   	return pref_perimeter_factor;
   }

	
   public void setPref_perimeter_factor(double pref_perimeter_factor) {
   
   	this.pref_perimeter_factor = pref_perimeter_factor;
   }

	
   public double getSize_percentage_cell_division() {
   
   	return size_percentage_cell_division;
   }

	
   public void setSize_percentage_cell_division(double size_percentage_cell_division) {
   
   	this.size_percentage_cell_division = size_percentage_cell_division;
   }

	
   public double getMin_edge_length_percentage() {
   
   	return min_edge_length_percentage;
   }

	
   public void setMin_edge_length_percentage(double min_edge_length_percentage) {
   
   	this.min_edge_length_percentage = min_edge_length_percentage;
   }
}
