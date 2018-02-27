package sim.app.episim.model.sbml;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import org.COPASI.CCopasiDataModel;
import org.COPASI.CCopasiMessage;
import org.COPASI.CCopasiMethod;
import org.COPASI.CCopasiObject;
import org.COPASI.CCopasiObjectName;
import org.COPASI.CCopasiParameter;
import org.COPASI.CCopasiParameterGroup;
import org.COPASI.CCopasiRootContainer;
import org.COPASI.CCopasiTask;
import org.COPASI.CFunctionParameter;
import org.COPASI.CFunctionParameters;
import org.COPASI.CMetab;
import org.COPASI.CModel;
import org.COPASI.CModelParameter;
import org.COPASI.CModelValue;
import org.COPASI.CReaction;
import org.COPASI.CTaskEnum;
import org.COPASI.CTrajectoryMethod;
import org.COPASI.CTrajectoryProblem;
import org.COPASI.CTrajectoryTask;
import org.COPASI.CVersion;
import org.COPASI.ModelValueVectorN;
import org.COPASI.ObjectStdVector;
import org.COPASI.ReactionVectorNS;

import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.EpisimLogger;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.GlobalClassLoader;
import episiminterfaces.EpisimSbmlModelConfiguration;
import episiminterfaces.EpisimSbmlModelConfigurationEx;
import episiminterfaces.InterfaceVersion;

public class COPASIConnector implements ClassLoaderChangeListener {

	private static COPASIConnector instance;

	private static final int NUMBER_OF_DATA_MODELS_IN_CACHE = 300;

	private HashMap<String, String> sbmlFileCache;

	private HashMap<String, CCopasiDataModel> copasiDataModels;

	private static Semaphore sem = new Semaphore(1);

	private COPASIConnector(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		sbmlFileCache = new HashMap<String, String>();
		copasiDataModels = new HashMap<String, CCopasiDataModel>();

		CCopasiRootContainer.getRoot();
	}

	protected static COPASIConnector getInstance(){		
		if(instance==null){
			try{
				sem.acquire();
				instance = new COPASIConnector();
				sem.release();
			}
			catch (InterruptedException e){
				EpisimExceptionHandler.getInstance().displayException(e);
			}

		}		
		return instance;		
	}

	public void registerNewCopasiDataModelWithSbmlFile(File modelArchiveFile, String sbmlFile) throws IOException, Exception{
		if(!this.copasiDataModels.containsKey(sbmlFile)){
			CCopasiDataModel dataModel = CCopasiRootContainer.addDatamodel();			
			if (!dataModel.importSBMLFromString(loadSBMLFile(modelArchiveFile, sbmlFile)))
			{
				EpisimExceptionHandler.getInstance().displayException(new Exception(CCopasiMessage.getAllMessageText()));
			}
			dataModel.getModel().applyInitialValues();
			this.copasiDataModels.put(sbmlFile, dataModel);
		}		
	}

	public void simulateSBMLModel(EpisimSbmlModelConfiguration modelConfig, SBMLModelState modelState){
		simulateSBMLModel(modelConfig, modelState, 1);
	}
	
	public void simulateSBMLModel(EpisimSbmlModelConfiguration modelConfig, SBMLModelState modelState, int numberOfSteps){
		CCopasiDataModel dataModel = this.copasiDataModels.get(modelConfig.getModelFilename());
		if(dataModel != null){
			executeTrajectoryTask(dataModel, modelConfig, modelState, numberOfSteps);
			updateStateFromCopasi(dataModel, modelState);
		}
	}

	private void updateStateFromCopasi(CCopasiDataModel dataModel, SBMLModelState modelState){
		updateStateFromCopasi(dataModel.getModel(), modelState);
	}

	private void updateStateFromCopasi(CModel model, SBMLModelState modelState) {
		long numSpecies = model.getNumMetabs();
		long numParameters = model.getNumModelValues();
		long numReactions = model.getNumReactions();
		for(long i = 0; i< numSpecies; i++){
			CMetab metab =model.getMetabolite(i);
			if(metab != null){
				double concentration = metab.getConcentration();
				if(Double.isNaN(concentration)){
					System.err.println("Concentration is NaN");
					concentration = CMetab.convertToConcentration(metab.getValue(), metab.getCompartment(), model);
				}
				modelState.addSpeciesValue(new SBMLModelEntity(metab.getObjectName(), metab.getValue(), concentration));
			}
		}
		for(long i = 0; i< numParameters; i++){
			CModelValue value = model.getModelValue(i);
			if(value != null){
				modelState.addParameterValue(new SBMLModelEntity(value.getObjectName(), value.getValue(), 0));
			}
		}
		for(long i = 0; i< numReactions; i++){
			CReaction reaction = model.getReaction(i);
			if(reaction != null){
				modelState.addReactionValue(new SBMLModelEntity(reaction.getObjectName(), reaction.getFlux(), 0));
			}
		}
	}

	private void writeStateToCopasi(CCopasiDataModel dataModel, SBMLModelState modelState){
		writeStateToCopasi(dataModel.getModel(), modelState);
	}

	private void writeStateToCopasi(CModel model, SBMLModelState modelState) {
		ObjectStdVector changedObjects=new ObjectStdVector();
		for(SBMLModelEntity entity : modelState.getParameterValues()){

			CModelValue modelValue = findParameter(model, entity.name);
			if(modelValue != null){
				modelValue.setInitialValue(entity.value);

				changedObjects.add(modelValue.getInitialValueReference());
			}
			/*  else{
			  CCopasiParameter localParameter = findLocalParameter(dataModel.getModel(), entity.name);
			  if(localParameter != null){
				  localParameter.setDblValue(entity.value);
				  changedObjects.add(localParameter.getObject(new CCopasiObjectName("Reference=Value")));
			  }
		  }*/
		}
		for(SBMLModelEntity entity : modelState.getSpeciesValues()){
			CMetab metab =  model.getMetabolite(entity.name);	     
			if(metab != null){		      
				metab.setInitialConcentration(entity.concentration);
				if (Double.isNaN(entity.concentration))
				{
					System.err.println("Encouontered NaN");
				}
				changedObjects.add(metab.getInitialConcentrationReference());
			}
		}
		model.updateInitialValues(changedObjects);
		//model.updateInitialValues(CModelParameter.Concentration);
	}

	private CModelValue findParameter(CModel model, String name){
		ModelValueVectorN valueVect = model.getModelValues();
		if(valueVect != null){
			for(long i = 0; i < valueVect.size(); i++){
				CCopasiObject copObject = valueVect.get(i);
				if(copObject != null){
					String objectName = copObject.getObjectName();
					if(objectName != null && objectName.equals(name)){				 
						return model.getModelValue(name);
					}
				}		  
			}
		}
		return null;
	}

	private CCopasiParameter findLocalParameter(CModel model, String name){
		for(long i = 0; i < model.getNumReactions(); i++){			  
			CReaction cReact = model.getReaction(i);

			if(cReact != null){

				CCopasiParameterGroup parameterGroup = cReact.getParameters();

				if(parameterGroup != null){
					for(long n = 0; n < parameterGroup.size(); n++){
						CCopasiParameter param = parameterGroup.getParameter(n);
						if(param != null && param.getObjectName() != null && param.getObjectName().equals(name)){
							return param;
						}
					}
				}					  
			}		  
		}
		return null;
	}

	private void executeTrajectoryTask(CCopasiDataModel dataModel, EpisimSbmlModelConfiguration modelConfig, SBMLModelState modelState, int numberOfSteps){		
		CTrajectoryTask trajectoryTask = (CTrajectoryTask)dataModel.getTask("Time-Course");

		if (trajectoryTask == null)
		{          
			trajectoryTask = new CTrajectoryTask(dataModel);
			trajectoryTask.setMethodType(CTaskEnum.deterministic);
			trajectoryTask.getProblem().setModel(dataModel.getModel());			
			dataModel.getTaskList().addAndOwn(trajectoryTask);
		}

		trajectoryTask.setScheduled(true);
		//trajectoryTask.setUpdateModel(true);

		// get the problem for the task to set some parameters
		CTrajectoryProblem problem = (CTrajectoryProblem)trajectoryTask.getProblem();       
		//dataModel.getModel().setInitialTime(counter++);

		writeStateToCopasi(dataModel, modelState);


		problem.setStepNumber(modelConfig.getNoOfStepsPerCBMSimstep()*numberOfSteps);
		problem.setDuration((modelConfig.getNoOfTimeUnitsPerCBMSimstep()*((double) numberOfSteps)));

		// problem.setTimeSeriesRequested(true);

		CTrajectoryMethod method = (CTrajectoryMethod)trajectoryTask.getMethod();
		if(modelConfig instanceof EpisimSbmlModelConfigurationEx){

			CCopasiParameter absoluteTolerance = method.getParameter("Absolute Tolerance");
			//System.out.println("Die Absolute Fehlertoleranz ist: " + ((EpisimSbmlModelConfigurationEx)modelConfig).getAbsoluteErrorTolerance());
			CCopasiParameter relativeTolerance = method.getParameter("Relative Tolerance");
			//System.out.println("Die Relative Fehlertoleranz ist: " + ((EpisimSbmlModelConfigurationEx)modelConfig).getRelativeErrorTolerance());
			absoluteTolerance.setDblValue(((EpisimSbmlModelConfigurationEx)modelConfig).getAbsoluteErrorTolerance());
			relativeTolerance.setDblValue(((EpisimSbmlModelConfigurationEx)modelConfig).getRelativeErrorTolerance());
		}
		else{
			CCopasiParameter absoluteTolerance = method.getParameter("Absolute Tolerance");
			absoluteTolerance.setDblValue(modelConfig.getErrorTolerance());
		} 

		try{
			trajectoryTask.initializeRaw((int)CCopasiTask.OUTPUT_UI);
			if (!trajectoryTask.processRaw(true))
			{
				StringBuilder sb = new StringBuilder();
				sb.append("Errors: \n");
				sb.append(trajectoryTask.getProcessError() + "\n\n");
				sb.append("Warnings: \n");
				sb.append(trajectoryTask.getProcessWarning() + "\n\n");
				sb.append("Other: \n");
				sb.append(CCopasiMessage.getAllMessageText() + "\n\n");			
				EpisimExceptionHandler.getInstance().displayException(new Exception(sb.toString()));
			}
			else
			{
				trajectoryTask.restore();
				trajectoryTask.getMathContainer().updateTransientDataValues();
				trajectoryTask.getMathContainer().pushAllTransientValues();
			}
		}
		catch (Exception e){
			EpisimExceptionHandler.getInstance().displayException(e);
		}      

	}


	private String loadSBMLFile(File file, String sbmlFile) throws IOException{

		if(sbmlFileCache.containsKey(sbmlFile)) return sbmlFileCache.get(sbmlFile);
		else{
			URL u = new URL("jar", "", file.toURI().toURL() + "!/" + sbmlFile);

			JarURLConnection uc = (JarURLConnection)u.openConnection();
			uc.setDefaultUseCaches(false); 

			BufferedReader bufferedIn = new BufferedReader(new InputStreamReader(uc.getInputStream()));	
			String input = null;
			input = bufferedIn.readLine();
			StringBuffer stringBuffer = new StringBuffer();

			while(input != null){
				stringBuffer.append(input);
				input = bufferedIn.readLine();
			}

			sbmlFileCache.put(sbmlFile, stringBuffer.toString());

			return stringBuffer.toString();
		}

	}

	public void classLoaderHasChanged() {
		instance = null;
	}

}
