package sim.app.episim.charts.build;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sim.app.episim.CellType;
import sim.app.episim.TissueType;

public class EpisimChartImpl implements EpisimChart{
	
	private long id;
	private String title = "";
	private String xLabel = "";
	private String yLabel = "";
	private boolean antiAliasingEnabled = false;
	private boolean legendVisible = false;
	private boolean pdfPrintingEnabled = false;
	private int pdfPrintingFrequency = 0;
	
	private String baselineExpression = "";
	
	private Set<TissueType> tissueTypes;
	private Set<CellType> cellTypes;
	
	private Map<Long, EpisimChartSeries> seriesMap;
	
	public EpisimChartImpl(long id, Set<TissueType> tissueTypes, Set<CellType> cellTypes){
		this.id = id;
		this.tissueTypes = tissueTypes;
		this.cellTypes = cellTypes;
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
	
	public String getBaselineExpression() {
	
		return baselineExpression;
	}
	
	public void setBaselineExpression(String val) {
	
		this.baselineExpression = val;
	}

	
	public Set<TissueType> getTissueTypes() {
	
		return tissueTypes;
	}

	
	public Set<CellType> getCellTypes() {
	
		return cellTypes;
	}

	public void addEpisimChartSeries(EpisimChartSeries chartSeries) {

		this.seriesMap.put(chartSeries.getId(), chartSeries);
		
	}

	public List<EpisimChartSeries> getEpisimChartSeries() {

		
		return (List<EpisimChartSeries>)this.seriesMap.values();
	}

	public void removeChartSeries(long id) {

		this.seriesMap.remove(id);
		
	}
	
	
}
