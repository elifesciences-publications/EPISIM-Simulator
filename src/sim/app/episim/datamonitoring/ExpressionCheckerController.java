package sim.app.episim.datamonitoring;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import sim.app.episim.datamonitoring.parser.DataMonitoringExpressionChecker;
import sim.app.episim.datamonitoring.parser.ParseException;
import sim.app.episim.datamonitoring.parser.TokenMgrError;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GlobalClassLoader;
import sim.app.episim.util.TissueCellDataFieldsInspector;


public class ExpressionCheckerController implements ClassLoaderChangeListener{
	
	private static ExpressionCheckerController instance;
	
	private static final int ARITHMETICTYPE = 1;
	private static final int BOOLEANTYPE = 2;
	
	private Map<Integer, Set<String>> varNameRegistry;
	
	private ExpressionCheckerController(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		varNameRegistry = new HashMap<Integer, Set<String>>();
	}
	
	private int nextCheckSessionId = 1;
	
	public static synchronized ExpressionCheckerController getInstance(){
		if(instance == null) instance = new ExpressionCheckerController();
		return instance;
	}
	
	public int getCheckSessionId(){ return nextCheckSessionId++; }
		
	public String checkBooleanDataMonitoringExpression(int sessionId, String expression, TissueCellDataFieldsInspector tissueDataFieldsInspector) throws ParseException, NumberFormatException{
				
		return checkExpression(sessionId, expression, tissueDataFieldsInspector, BOOLEANTYPE);
		
	}
	
	public String checkArithmeticDataMonitoringExpression(int sessionId, String expression, TissueCellDataFieldsInspector tissueDataFieldsInspector) throws ParseException, NumberFormatException{
		
		return checkExpression(sessionId, expression, tissueDataFieldsInspector, ARITHMETICTYPE);
	}
	
	private String checkExpression(int sessionId, String expression, TissueCellDataFieldsInspector tissueDataFieldsInspector, int expressionType) throws NumberFormatException, ParseException{
		String result = "";
		if(expression != null && tissueDataFieldsInspector != null){
			 StringReader sr = new java.io.StringReader(expression);
		    Reader r = new java.io.BufferedReader(sr);
		    DataMonitoringExpressionChecker parser = new DataMonitoringExpressionChecker(r);
		    if(expressionType == ARITHMETICTYPE) result = parser.checkExpression(tissueDataFieldsInspector);
		    else if(expressionType == BOOLEANTYPE) result = parser.checkBooleanExpression(tissueDataFieldsInspector);
		    addRecognizedVarNames(sessionId, parser.getRecognizedVarOrConstantNames());
		}
		return result;
	}
	
	public boolean hasVarNameConflict(int sessionId, TissueCellDataFieldsInspector tissueDataFieldsInspector){
		if(varNameRegistry.containsKey(sessionId)){
			return tissueDataFieldsInspector.hasCellTypeConflict(varNameRegistry.get(sessionId));
		}
		return false;
	}
	
	private void addRecognizedVarNames(int sessionId, Set<String> varNames){
		if(varNames != null && !varNames.isEmpty()){
			if(varNameRegistry.containsKey(sessionId)){
				varNameRegistry.get(sessionId).addAll(varNames);
			}
			else varNameRegistry.put(sessionId, varNames);
		}
	}
	
   public void classLoaderHasChanged() {
	   instance = null;
   }

}
