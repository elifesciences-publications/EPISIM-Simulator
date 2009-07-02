package sim.app.episim.datamonitoring.charts;

import java.util.HashMap;
import java.util.Map;

import sim.app.episim.CellType;
import episimexceptions.CellNotValidException;
import episiminterfaces.calc.CalculationHandler;


public class Test {

	
	public void test(){
		CalculationHandler handler = new CalculationHandler(){
			private Map<String, Object> test = new HashMap<String, Object>();
			
			{
				test.put("Test1", true);
				test.put("Test2", 1);
				test.put("Test3", 123.456);
				test.put("Test4", "Zonk");
			}
			
			public double calculate(CellType cell) throws CellNotValidException {

	         // TODO Auto-generated method stub
	         return 0;
         }

			public boolean conditionFulfilled(CellType cell) throws CellNotValidException {

	         // TODO Auto-generated method stub
	         return false;
         }

			public int getCalculationAlgorithmID() {

	         // TODO Auto-generated method stub
	         return 0;
         }

			public long getID() {

	         // TODO Auto-generated method stub
	         return 0;
         }

			public Map<String, Object> getParameters() {

	         // TODO Auto-generated method stub
	         return test;
         }

			public Class<? extends CellType> getRequiredCellType() {

	         // TODO Auto-generated method stub
	         return null;
         }

			public boolean isBaselineValue() {

	         // TODO Auto-generated method stub
	         return false;
         }
			
		};
		appendParameterMapReproduction("test", handler.getParameters());
	}
	
	protected void appendParameterMapReproduction(String parameterMapDataFieldName, Map<String, Object> parameterValueMap){
		StringBuffer generatedSourceCode = new StringBuffer();
		generatedSourceCode.append(parameterMapDataFieldName+ " = new HashMap<String, Object>();\n");
		
		for(String key: parameterValueMap.keySet()){
			Object val = parameterValueMap.get(key);
			if(val instanceof Number || val instanceof Boolean){				
				generatedSourceCode.append(parameterMapDataFieldName+".put(\""+ key+"\", "+val+");\n"); 
			}
		}
		System.out.println(generatedSourceCode.toString());
	}
	
	public static void main(String[] args){
		Test t = new Test();
		t.test();
	}
	
}
