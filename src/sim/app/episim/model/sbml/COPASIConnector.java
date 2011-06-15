package sim.app.episim.model.sbml;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.HashMap;

import org.COPASI.CCopasiDataModel;
import org.COPASI.CCopasiMethod;
import org.COPASI.CCopasiObjectName;
import org.COPASI.CCopasiParameter;
import org.COPASI.CCopasiRootContainer;
import org.COPASI.CMetab;
import org.COPASI.CModelValue;
import org.COPASI.CTrajectoryMethod;
import org.COPASI.CTrajectoryProblem;
import org.COPASI.CTrajectoryTask;
import org.COPASI.ObjectStdVector;

import episiminterfaces.EpisimSbmlModelConfiguration;

public class COPASIConnector {
	
	private static COPASIConnector instance;
	
	private HashMap<String, String> sbmlFileCache;
	
	private COPASIConnector(){
		sbmlFileCache = new HashMap<String, String>();
		CCopasiRootContainer.getRoot();
	}
	
	protected synchronized static COPASIConnector getInstance(){		
		if(instance == null) instance = new COPASIConnector();		
		return instance;		
	}
	
	public CCopasiDataModel getNewCopasiDataModelForSbmlFile(File modelArchiveFile, String sbmlFile) throws IOException, Exception{
		CCopasiDataModel dataModel = CCopasiRootContainer.addDatamodel();
		
		dataModel.importSBMLFromString(loadSBMLFile(modelArchiveFile, sbmlFile));
		
		return dataModel;
	}
	
	public void setInitialConcentrationOfSpecies(CCopasiDataModel dataModel, String speciesName, double value){
		long index = -1;
		ObjectStdVector changedObjects=new ObjectStdVector();
		
		index = dataModel.getModel().findMetabByName(speciesName);          
      CMetab metab =  dataModel.getModel().getMetabolite(index);
      if(metab != null){
	      metab.setInitialConcentration(value);
	      changedObjects.add(metab.getObject(new CCopasiObjectName("Reference=InitialConcentration")));
	      dataModel.getModel().updateInitialValues(changedObjects);
      }
	}
	
	public void setInitialValueOfParameter(CCopasiDataModel dataModel, String speciesName, double value){
		ObjectStdVector changedObjects=new ObjectStdVector();
	   CModelValue modelValue = dataModel.getModel().getModelValue(speciesName);
	   if(modelValue != null){
		   modelValue.setInitialValue(value);
		   changedObjects.add(modelValue.getObject(new CCopasiObjectName("Reference=InitialValue")));
		   dataModel.getModel().updateInitialValues(changedObjects);
	   }
	}
   
	
	public CTrajectoryTask getNewTrajectoryTask(CCopasiDataModel dataModel, EpisimSbmlModelConfiguration modelConfig){
		 CTrajectoryTask trajectoryTask = (CTrajectoryTask)dataModel.getTask("Time-Course");
		 
		 if (trajectoryTask == null)
       {          
           trajectoryTask = new CTrajectoryTask();
           dataModel.getTaskList().addAndOwn(trajectoryTask);
       }

       trajectoryTask.setMethodType(CCopasiMethod.deterministic);
       trajectoryTask.getProblem().setModel(dataModel.getModel());
       trajectoryTask.setScheduled(true);

   
       // get the problem for the task to set some parameters
       CTrajectoryProblem problem = (CTrajectoryProblem)trajectoryTask.getProblem();
       
       dataModel.getModel().setInitialTime(0.0);
       problem.setStepNumber(modelConfig.getNoOfStepsPerCBMSimstep());
       problem.setDuration(modelConfig.getNoOfTimeUnitsPerCBMSimstep());
       problem.setTimeSeriesRequested(true);
       
       CTrajectoryMethod method = (CTrajectoryMethod)trajectoryTask.getMethod();
       CCopasiParameter parameter = method.getParameter("Absolute Tolerance");
       parameter.setDblValue(modelConfig.getErrorTolerance());
              
       return trajectoryTask;
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

}
