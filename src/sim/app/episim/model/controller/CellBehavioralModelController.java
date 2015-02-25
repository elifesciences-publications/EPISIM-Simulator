package sim.app.episim.model.controller;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import binloc.ProjectLocator;
import episimexceptions.ModelCompatibilityException;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.SendReceiveAlgorithm;


import sendreceive.StandardSendReceiveAlgorithm;
import sim.app.episim.EpisimProperties;
import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.cellbehavior.CellBehavioralModelFacade;
import sim.app.episim.model.cellbehavior.CellBehavioralModelJarClassLoader;
import sim.app.episim.model.initialization.CellBehavioralModelInitializer;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GlobalClassLoader;
import sim.app.episim.model.cellbehavior.CellBehavioralModelFacade.StandardCellType;
import sim.app.episim.model.cellbehavior.CellBehavioralModelFacade.StandardDiffLevel;


public class CellBehavioralModelController implements java.io.Serializable, ClassLoaderChangeListener{
	
	private static final String SEND_RECEIVE_PACKAGENAME = "sendreceive";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2406025736169916469L;
	private static CellBehavioralModelController instance = new CellBehavioralModelController();
	private CellBehavioralModelFacade cellBehavioralModel;
	
	
	private boolean caching = true;
	
	private File actLoadedCellBehavioralFile;
	private Class<? extends SendReceiveAlgorithm> sendReceiveAlgorithmClass;
	private File sendReceivePackagePath;
	
	private String checkedStandardModelFileName = null;
	private boolean isStandardKeratinocyteModel = false;
	
	private static Semaphore sem = new Semaphore(1);
	
	
	private CellBehavioralModelController(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		if(EpisimProperties.getProperty(EpisimProperties.MODEL_SEND_RECEIVE_ALGORITHM) != null 
				&& EpisimProperties.getProperty(EpisimProperties.MODEL_SEND_RECEIVE_ALGORITHM).length() >0){
			try{
				sendReceivePackagePath = new File (ProjectLocator.getBinPath().getAbsolutePath() + System.getProperty("file.separator") +SEND_RECEIVE_PACKAGENAME);
	         if(sendReceivePackagePath!=null){
	         	
	         	 GlobalClassLoader.getInstance().registerURL(sendReceivePackagePath.toURI().toURL());
	              
	 				for(File file: getSendReceiveClassFiles()){					
	 		           Class<?> loadedClass = GlobalClassLoader.getInstance().loadClass(SEND_RECEIVE_PACKAGENAME+"."+file.getName().substring(0, (file.getName().length()-".class".length())));
	 		           if(SendReceiveAlgorithm.class.isAssignableFrom(loadedClass) 
	 		         		  && loadedClass.getCanonicalName().endsWith(EpisimProperties.getProperty(EpisimProperties.MODEL_SEND_RECEIVE_ALGORITHM))){
	 		         	   this.sendReceiveAlgorithmClass = (Class<? extends SendReceiveAlgorithm>) loadedClass;
	 		           }
	 				}
	         }
	         if(sendReceiveAlgorithmClass==null) sendReceiveAlgorithmClass = StandardSendReceiveAlgorithm.class;
         }
         catch (URISyntaxException e){
	        EpisimExceptionHandler.getInstance().displayException(e);
         }
         catch (ClassNotFoundException e){
         	EpisimExceptionHandler.getInstance().displayException(e);
         }
         catch (MalformedURLException e){
         	EpisimExceptionHandler.getInstance().displayException(e);
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
	
	protected static CellBehavioralModelController getInstance(){
		if(instance==null){
			try{
	         sem.acquire();
	         instance = new CellBehavioralModelController();				
				sem.release();
         }
         catch (InterruptedException e){
	        EpisimExceptionHandler.getInstance().displayException(e);
         }
				
		}
		return instance;
	}
	
		
	protected boolean loadModelFile(File modelFile) throws ModelCompatibilityException{
			checkedStandardModelFileName = null;
			isStandardKeratinocyteModel = false;
			CellBehavioralModelJarClassLoader jarLoader = null;
         try{
	         jarLoader = new CellBehavioralModelJarClassLoader(modelFile.toURI().toURL());
         }
         catch (MalformedURLException e){
	        EpisimExceptionHandler.getInstance().displayException(e);
         }
			if(jarLoader.isDiffModel()){
				cellBehavioralModel = new CellBehavioralModelFacade(jarLoader.getModelClass(EpisimCellBehavioralModel.class), 
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
	     EpisimExceptionHandler.getInstance().displayException(e);
      }
      catch (IllegalAccessException e){
      	 EpisimExceptionHandler.getInstance().displayException(e);
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
		return cellBehavioralModel != null ? cellBehavioralModel.getEpisimCellBehavioralModelGlobalParameters() : null;
	}
	
	public CellBehavioralModelInitializer getCellBehavioralModelInitializer(){
		return this.cellBehavioralModel.getCellBehavioralModelInitializer();
	}
	
	public CellBehavioralModelInitializer getCellBehavioralModelInitializer(SimulationStateData simStateData){
		return this.cellBehavioralModel.getCellBehavioralModelInitializer(simStateData);
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
			EpisimExceptionHandler.getInstance().displayException(e);
		}		
	}

	public void classLoaderHasChanged() {
		instance = null;
   }
	
	protected boolean isStandardKeratinocyteModel(){
	   if(this.actLoadedCellBehavioralFile == null) return false;
	   else if(checkedStandardModelFileName == null){
	   	EpisimCellType[] cellTypes =this.getAvailableCellTypes();
	   	EpisimDifferentiationLevel[] diffLevels =this.getAvailableDifferentiationLevels();
	   	boolean everyStandardCellTypeFound = true;
	   	for(StandardCellType sCellType: StandardCellType.values()){
	   		boolean found = false;
	   		for(EpisimCellType cellType : cellTypes){
	   			if(cellType.toString().equals(sCellType.toString())) found = true;
	   		}
	   		if(!found) everyStandardCellTypeFound= false;
	   	}
	   	boolean everyStandardDiffLevelFound = false;
	   
	   	boolean stemCellFound = false;
	   	boolean taCellFound = false;
	   	for(EpisimDifferentiationLevel diffLevel : diffLevels){
	   		if(diffLevel.toString().equals(StandardDiffLevel.STEMCELL.toString())) stemCellFound = true;
	   		if(diffLevel.toString().equals(StandardDiffLevel.TACELL.toString())) taCellFound = true;
	   	}
	   	if(stemCellFound && taCellFound) everyStandardDiffLevelFound= true;
	   	
	   	isStandardKeratinocyteModel = (everyStandardCellTypeFound && everyStandardDiffLevelFound);
	   }	   	
	   return isStandardKeratinocyteModel;
	}
	
}
