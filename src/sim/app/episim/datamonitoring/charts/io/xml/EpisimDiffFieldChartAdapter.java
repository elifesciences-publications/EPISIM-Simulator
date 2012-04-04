package sim.app.episim.datamonitoring.charts.io.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import episiminterfaces.monitoring.EpisimDiffFieldChart;

import sim.app.episim.datamonitoring.charts.EpisimDiffFieldChartImpl;


public class EpisimDiffFieldChartAdapter extends XmlAdapter<AdaptedEpisimDiffFieldChart, EpisimDiffFieldChart> implements java.io.Serializable{
	
   public EpisimDiffFieldChart unmarshal(AdaptedEpisimDiffFieldChart v) throws Exception {	  
   	EpisimDiffFieldChartImpl chart = new EpisimDiffFieldChartImpl(v.getId());
   	chart.setChartTitle(v.getTitle());
   	chart.setChartUpdatingFrequency(v.getChartUpdatingFrequency());
   	chart.setDiffusionFieldName(v.getDiffFieldName());
   	chart.setIsDirty(v.isDirty());
   	chart.setPNGPrintingEnabled(v.isPngPrintingEnabled());
   	chart.setPNGPrintingFrequency(v.getPngPrintingFrequency());
   	chart.setPNGPrintingPath(chart.getPNGPrintingPath());
   	return chart;
   }
	
   public AdaptedEpisimDiffFieldChart marshal(EpisimDiffFieldChart v) throws Exception {
   	AdaptedEpisimDiffFieldChart chart = new AdaptedEpisimDiffFieldChart();
   	chart.setId(v.getId());
   	chart.setChartUpdatingFrequency(v.getChartUpdatingFrequency());
   	chart.setDiffFieldName(v.getDiffusionFieldName());
   	chart.setDirty(v.isDirty());
   	chart.setPngPrintingEnabled(v.isPNGPrintingEnabled());
   	chart.setPngPrintingFrequency(v.getPNGPrintingFrequency());
   	chart.setPngPrintingPath(v.getPNGPrintingPath());
   	chart.setTitle(v.getChartTitle());
   	return chart;
   }

}
