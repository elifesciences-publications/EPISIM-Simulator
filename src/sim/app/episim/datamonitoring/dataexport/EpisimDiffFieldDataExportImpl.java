package sim.app.episim.datamonitoring.dataexport;

import java.io.File;

import sim.app.episim.util.ObjectManipulations;

import episiminterfaces.monitoring.EpisimDiffFieldDataExport;


public class EpisimDiffFieldDataExportImpl implements EpisimDiffFieldDataExport, java.io.Serializable {
	
	private long id;
	private String name;
	private String diffusionFieldName;
	private File csvFilePath;
	private int dataExportFrequncyInSimulationSteps = 1;
	private boolean isDirty = false;
	
	public EpisimDiffFieldDataExportImpl(long id){
		this.id = id;
	}

	public long getId() {
		return this.id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
	
	public void setDiffusionFieldName(String name) {
		this.diffusionFieldName = name;
	}

	public String getDiffusionFieldName() {
		return this.diffusionFieldName;
	}

	public void setCSVFilePath(File path) {
		this.csvFilePath = path;
	}

	public File getCSVFilePath() {
		return this.csvFilePath;
	}

	public void setDataExportFrequencyInSimulationSteps(int frequency) {
		this.dataExportFrequncyInSimulationSteps = frequency;
	}

	public int getDataExportFrequncyInSimulationSteps() {
		return this.dataExportFrequncyInSimulationSteps;
	}

	public void setIsDirty(boolean value) {
		this.isDirty = value;
	}

	public boolean isDirty() {
		return this.isDirty;
	}

	public EpisimDiffFieldDataExportImpl clone(){
		EpisimDiffFieldDataExportImpl newExport = ObjectManipulations.cloneObject(this);
		return newExport;
	}
	
}