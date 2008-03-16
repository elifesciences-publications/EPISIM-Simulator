package sim.app.episim.datamonitoring.dataexport;

import episiminterfaces.EpisimDataExportColumn;


public class EpisimDataExportColumnImpl implements EpisimDataExportColumn{
	
	private final long id;
	private String name = null;
	private String[] calculationExpression;
	
	public EpisimDataExportColumnImpl(long id){
		this.id = id;
	}
	

	public String[] getCalculationExpression() { return this.calculationExpression; }

	public long getId() { return id; }

	public String getName(){ return this.name; }
   
	public void setCalculationExpression(String[] exp) {

	   if(exp == null || exp.length < 2 || exp[0] == null  || exp[1] == null) 
	   	throw new IllegalArgumentException("This Calculation expression is null or does not contain proper caluculation expressions!");
	   else{
	   	this.calculationExpression = exp;
	   }
	}

	public void setName(String val) {
	   if(val != null && !val.trim().equals("")) this.name = val; 	   
   }

}
