package sim.app.episim.propfilegenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import sim.app.episim.EpisimExceptionHandler;

public class PropertyFileGenerator {
	
	private char fileNumberSeparatorChar='_';
	private String fileExtension = ".properties";
	private HashMap<String, Integer> paramNameIndexMap;
	private HashMap<String, PropertyDescriptor> propertyDescriptorsMap;
	
	private String[] stepSizes;
	private String[] currentValues;
	
	
	
	public PropertyFileGenerator(){}
	
	public PropertyFileGenerator(char fileNumberSeparatorChar, String fileExtension){
		this.fileNumberSeparatorChar = fileNumberSeparatorChar;
		if(fileExtension != null)this.fileExtension = fileExtension;
	}
	
	public void generatePropertyFiles(File path, List<PropertyDescriptor> propertyDescriptors){
		int maxNumberOfSteps = calculateMaxNumberOfSteps(propertyDescriptors);
		buildMapsAndArrays(propertyDescriptors);
		
		for(int i = 1; i <= maxNumberOfSteps+1; i++){
			writePropertyFile(path, i);
			calculateCurrentValues();
		
		}
		
	}
	
	private void calculateCurrentValues(){
		for(String paramName : this.paramNameIndexMap.keySet()){
			BigDecimal currentValue = new BigDecimal(this.currentValues[this.paramNameIndexMap.get(paramName)]);
			BigDecimal stepSize = new BigDecimal(this.stepSizes[this.paramNameIndexMap.get(paramName)]);
			
			currentValue = currentValue.add(stepSize);
			
			this.currentValues[this.paramNameIndexMap.get(paramName)]=this.currentValues[this.paramNameIndexMap.get(paramName)] = currentValue.toString();
			if(stepSize.doubleValue() < 0 && currentValue.doubleValue() <=this.propertyDescriptorsMap.get(paramName).getUpperBound()) 
				this.currentValues[this.paramNameIndexMap.get(paramName)]= ""+this.propertyDescriptorsMap.get(paramName).getUpperBound();
			else if(stepSize.doubleValue() > 0 && currentValue.doubleValue() >=this.propertyDescriptorsMap.get(paramName).getUpperBound()) 
				this.currentValues[this.paramNameIndexMap.get(paramName)]= ""+this.propertyDescriptorsMap.get(paramName).getUpperBound();
			
		}
	}
	
	
	private void writePropertyFile(File path, int fileNumber){
		File newPath = getFileName(path, fileNumber);
		
		try{
			Writer fileOut = new OutputStreamWriter(new FileOutputStream(newPath),"UTF-8");
			for(String paramName : this.paramNameIndexMap.keySet()){
				StringBuffer output = new StringBuffer(paramName);
				output.append(":");
				if(isCastToIntNecessary(this.propertyDescriptorsMap.get(paramName).getType())){
					output.append((new BigDecimal(this.currentValues[this.paramNameIndexMap.get(paramName)])).intValue());
				}
				else{
					output.append((new BigDecimal(this.currentValues[this.paramNameIndexMap.get(paramName)])).doubleValue());
				}
				output.append("\n");
				fileOut.write(output.toString());
			}
			fileOut.close();
		}
		catch (IOException e){
			EpisimExceptionHandler.getInstance().displayException(e);
		}
	}
	
	private File getFileName(File path, int fileNumber){
		String newPath = path.getAbsolutePath();
		StringBuffer buffer = new StringBuffer(newPath.substring(0, (newPath.length()-(path.getName().length()))));
	   buffer.append(path.getName().substring(0, path.getName().length()-fileExtension.length()));
	   buffer.append(this.fileNumberSeparatorChar);
	   buffer.append(fileNumber);
	   buffer.append(fileExtension);
		return new File(buffer.toString());
	}
	
	
	private void buildMapsAndArrays(List<PropertyDescriptor> propertyDescriptors){
		this.currentValues = new String[propertyDescriptors.size()];
		this.stepSizes = new String[propertyDescriptors.size()];
		this.paramNameIndexMap = new HashMap<String, Integer>();
		this.propertyDescriptorsMap = new HashMap<String, PropertyDescriptor>();
		
		int index = 0;
		for(PropertyDescriptor descr : propertyDescriptors){			
			this.propertyDescriptorsMap.put(descr.getPropertyName(), descr);
			this.paramNameIndexMap.put(descr.getPropertyName(), index);
			stepSizes[index] = ""+(descr.getLowerBound() > descr.getUpperBound() ? -1*descr.getStepSize():1*descr.getStepSize());
			this.currentValues[index] = ""+descr.getLowerBound();
			index++;
		}
	}
	
	private int calculateMaxNumberOfSteps(List<PropertyDescriptor> propertyDescriptors){
		
		int maxNumberOfSteps = 0;
		
		for(PropertyDescriptor descr : propertyDescriptors){
			int numberOfSteps = (int)(Math.abs(descr.getUpperBound() - descr.getLowerBound()) /descr.getStepSize());
			if(numberOfSteps > maxNumberOfSteps) maxNumberOfSteps = numberOfSteps;
		}
		
		return maxNumberOfSteps;
	}
	
	private boolean isCastToIntNecessary(Class<?> type){
		
		if(Integer.TYPE.isAssignableFrom(type)
				|| Short.TYPE.isAssignableFrom(type)
				|| Byte.TYPE.isAssignableFrom(type)){			
			return true;
		}
		return false;
	}


}
