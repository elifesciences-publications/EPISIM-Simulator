package sim.app.episim.charts;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sim.app.episim.CellType;
import sim.app.episim.TissueType;

public class EpisimChartImpl implements EpisimChart, java.io.Serializable{
	
	private long id;
	private String title = "";
	private String xLabel = "";
	private String yLabel = "";
	private boolean antiAliasingEnabled = false;
	private boolean legendVisible = false;
	private boolean pdfPrintingEnabled = false;
	private int pdfPrintingFrequency = 1;
	
	private File pdfPrintingPath = null;
	
	private String[] baselineExpression = null;
	
	private Map<String, TissueType> tissueTypesMap;
	private Map<String, CellType> cellTypesMap;
	
	private Map<Long, EpisimChartSeries> seriesMap;
	
	public EpisimChartImpl(long id, Map<String, TissueType> tissueTypes, Map<String, CellType> cellTypes){
		this.id = id;
		this.tissueTypesMap = tissueTypes;
		this.cellTypesMap = cellTypes;
		this.seriesMap = new HashMap<Long, EpisimChartSeries>();		
	}
	
	public long getId(){
		return this.id;
	}
	
	public int getPDFPrintingFrequency() {
		
		return pdfPrintingFrequency;
	}
	public String getTitle() {

		return title;
	}
	public String getXLabel() {

		return xLabel;
	}
	public String getYLabel() {

		return yLabel;
	}
	public boolean isAntialiasingEnabled() {

		return antiAliasingEnabled;
	}
	public boolean isLegendVisible() {

		return legendVisible;
	}
	public boolean isPDFPrintingEnabled() {

		
		return pdfPrintingEnabled;
	}
	public void setAntialiasingEnabled(boolean val) {

		this.antiAliasingEnabled = val;
		
	}
	public void setLegendVisible(boolean val) {

		this.legendVisible = val;
		
	}
	public void setPDFPrintingEnabled(boolean val) {

		this.pdfPrintingEnabled = val;
		
	}
	public void setPDFPrintingFrequency(int frequency) {

		this.pdfPrintingFrequency = frequency;
		
	}
	public void setTitle(String title) {

		this.title = title;
		
	}
	public void setXLabel(String label) {

		this.xLabel = label;
		
	}
	public void setYLabel(String label) {

		this.yLabel = label;
		
	}
	
	public String[] getBaselineExpression() {
	
		return baselineExpression;
	}
	
	public void setBaselineExpression(String[] val) {
	
		this.baselineExpression = val;
	}

	
	public Map<String,TissueType> getTissueTypesMap() {
	
		return tissueTypesMap;
	}

	
	public Map<String,CellType> getCellTypesMap() {
	
		return cellTypesMap;
	}

	public void addEpisimChartSeries(EpisimChartSeries chartSeries) {

		this.seriesMap.put(chartSeries.getId(), chartSeries);
		
	}

	public List<EpisimChartSeries> getEpisimChartSeries() {
					
		List<EpisimChartSeries> result = new ArrayList<EpisimChartSeries>();
		result.addAll(this.seriesMap.values());
		Collections.sort(result, new Comparator<EpisimChartSeries>(){

			public int compare(EpisimChartSeries o1, EpisimChartSeries o2) {
				if(o1 != null && o2 != null){
					if(o1.getId() < o2.getId()) return -1;
					else if(o1.getId() > o2.getId()) return +1;
    
					else return 0;
				}
				return 0;
         }

		});
		return result;
	}
	
	public EpisimChartSeries getEpisimChartSeries(long id){
		return this.seriesMap.get(id);
	}

	public void removeChartSeries(long id) {

		this.seriesMap.remove(id);
		
	}

	public File getPDFPrintingPath() {

	 
	   return this.pdfPrintingPath;
   }

	public void setPDFPrintingPath(File path) {

	  this.pdfPrintingPath = path;
	   
   }
	//Only a flat  copy
	public EpisimChart clone(){
		EpisimChart clone = new EpisimChartImpl(this.id, this.tissueTypesMap, this.cellTypesMap);
		clone.setAntialiasingEnabled(this.antiAliasingEnabled);
		clone.setBaselineExpression(this.baselineExpression.clone());
		clone.setLegendVisible(this.legendVisible);
		clone.setPDFPrintingEnabled(this.pdfPrintingEnabled);
		clone.setPDFPrintingFrequency(this.pdfPrintingFrequency);
		clone.setPDFPrintingPath(new File(this.pdfPrintingPath.getAbsolutePath()));
		clone.setTitle(this.title);
		clone.setXLabel(this.xLabel);
		clone.setYLabel(this.yLabel);
		for(EpisimChartSeries series: seriesMap.values()){
			clone.addEpisimChartSeries(series.clone());
		}
		return clone;
	}
	
}
