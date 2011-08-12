package sim.app.episim.model.controller;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import binloc.ProjectLocator;

import episimexceptions.ModelCompatibilityException;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.SendReceiveAlgorithm;


import sendreceive.StandardSendReceiveAlgorithm;
import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.initialization.CellBehavioralModelInitializer;
import sim.app.episim.util.GlobalClassLoader;




public class CellBehavioralModelController implements java.io.Serializable{
	
	private static final String SEND_RECEIVE_PACKAGENAME = "sendreceive";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2406025736169916469L;
	private static CellBehavioralModelController instance;
	private CellBehavioralModel cellBehavioralModel;
	
	
	private boolean caching = true;
	
	private File actLoadedCellBehavioralFile;
	private Class<? extends SendReceiveAlgorithm> sendReceiveAlgorithmClass;
	private File sendReceivePackagePath;
	
	private CellBehavioralModelController(){
		if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_SEND_RECEIVE_ALGORITHM) != null 
				&& EpisimProperties.getProperty(EpisimProperties.SIMULATOR_SEND_RECEIVE_ALGORITHM).length() >0){
			try{
				sendReceivePackagePath = new File (ProjectLocator.getBinPath().getAbsolutePath() + System.getProperty("file.separator") +SEND_RECEIVE_PACKAGENAME);
	         if(sendReceivePackagePath!=null){
	         	
	         	 GlobalClassLoader.getInstance().registerURL(sendReceivePackagePath.toURI().toURL());
	              
	 				for(File file: getSendReceiveClassFiles()){					
	 		           Class<?> loadedClass = GlobalClassLoader.getInstance().loadClass(SEND_RECEIVE_PACKAGENAME+"."+file.getName().substring(0, (file.getName().length()-".class".length())));
	 		           if(SendReceiveAlgorithm.class.isAssignableFrom(loadedClass) 
	 		         		  && loadedClass.getCanonicalName().endsWith(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_SEND_RECEIVE_ALGORITHM))){
	 		         	   this.sendReceiveAlgorithmClass = (Class<? extends SendReceiveAlgorithm>) loadedClass;
	 		           }
	 				}
	         }
	         if(sendReceiveAlgorithmClass==null) sendReceiveAlgorithmClass = StandardSendReceiveAlgorithm.class;
         }
         catch (URISyntaxException e){
	        ExceptionDisplayer.getInstance().displayException(e);
         }
         catch (ClassNotFoundException e){
         	ExceptionDisplayer.getInstance().displayException(e);
         }
         catch (MalformedURLException e){
         	ExceptionDisplayer.getInstance().displayException(e);
         }
		}
		else{
			sendReceiveAlgorithmClass = StandardSendReceiveAlgorithm.class;
		}
	}
	
	private List<File> getSendReceiveClassFiles() throws URISyntaxException{
		
		LinkedList<File> foundClassFiles = new LinkedList<File>();		
		
      if(sendReceivePackagePath != null && sendReceivePackagePath.isDirectory()){
      	for(File file : sendReceivePackagePath.listFiles()){
      		if(file != null && file.isFile() && file.getName() != null && file.getName().endsWith(".class")){
      			foundClassFiles.add(file);
      		}
      	}
      }
      return foundClassFiles;
	}
	
	protected synchronized static CellBehavioralModelController getInstance(){
		if(instance == null) instance = new CellBehavioralModelController();
		return instance;
	}
	
	public void reloadCellBehavioralModelGlobalParametersObject(EpisimCellBehavioralModelGlobalParameters parametersObject){
		if(cellBehavioralModel != null) cellBehavioralModel.reloadCellBehavioralModelGlobalParametersObject(parametersObject);
	}	
	
	protected boolean loadModelFile(File modelFile) throws ModelCompatibilityException{
		
			CellBehavioralModelJarClassLoader jarLoader = null;
         try{
	         jarLoader = new CellBehavioralModelJarClassLoader(modelFile.toURI().toURL());
         }
         catch (MalformedURLException e){
	        ExceptionDisplayer.getInstance().displayException(e);
         }
			if(jarLoader.isDiffModel()){
				cellBehavioralModel = new CellBehavioralModel(jarLoader.getModelClass(EpisimCellBehavioralModel.class), 
																	jarLoader.getGlobalParametersObject());
				this.actLoadedCellBehavioralFile = modelFile;
				return true;
			}
			else{
				this.actLoadedCellBehavioralFile = null;
				throw new ModelCompatibilityException("Model ist not a compatible Differentiation Model");
			}
		
		
		
	}
	public SendReceiveAlgorithm getNewInstanceOfSendReceiveAlgorithm(){
		try{
	      return this.sendReceiveAlgorithmClass.newInstance();
      }
      catch (InstantiationException e){
	     ExceptionDisplayer.getInstance().displayException(e);
      }
      catch (IllegalAccessException e){
      	 ExceptionDisplayer.getInstance().displayException(e);
      }
      return null;
	}
	
	public File getActLoadedModelFile(){
		return this.actLoadedCellBehavioralFile;
	}
	
	public EpisimCellBehavioralModel getNewEpisimCellBehavioralModelObject(){
	
		EpisimCellBehavioralModel model = cellBehavioralModel.getNewEpisimCellBehavioralModelObject();
		model.setSendReceiveAlgorithm(getNewInstanceOfSendReceiveAlgorithm());
		return model;		
	}
	
	protected EpisimCellBehavioralModelGlobalParameters getEpisimCellBehavioralModelGlobalParameters(){
		return cellBehavioralModel.getEpisimCellBehavioralModelGlobalParameters();
	}
	
	public CellBehavioralModelInitializer getCellBehavioralModelInitializer(){
		return this.cellBehavioralModel.getCellBehavioralModelInitializer();
	}
	
	public CellBehavioralModelInitializer getCellBehavioralModelInitializer(File modelInitializationFile){
		return this.cellBehavioralModel.getCellBehavioralModelInitializer(modelInitializationFile);
	}
	
	public EpisimCellType[] getAvailableCellTypes(){ return getEpisimCellBehavioralModelGlobalParameters().getAvailableCellTypes(); }
	public EpisimCellType getCellTypeForOrdinal(int ordinal){ return getEpisimCellBehavioralModelGlobalParameters().getCellTypeForOrdinal(ordinal); }
	
	public EpisimDifferentiationLevel[] getAvailableDifferentiationLevels(){ return getEpisimCellBehavioralModelGlobalParameters().getAvailableDifferentiationLevels(); }
	public EpisimDifferentiationLevel getDifferentiationLevelForOrdinal(int ordinal){ return getEpisimCellBehavioralModelGlobalParameters().getDifferentiationLevelForOrdinal(ordinal); }
	
	
	public void resetInitialGlobalValues(){
		try{
			cellBehavioralModel.resetInitialGlobalValues();
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
		}
		
	}
	
	
}
