package sim.app.episim.datamonitoring;

import java.io.Reader;
import java.io.StringReader;

import sim.app.episim.datamonitoring.parser.DataMonitoringExpressionChecker;
import sim.app.episim.datamonitoring.parser.ParseException;
import sim.app.episim.datamonitoring.parser.TokenMgrError;
import sim.app.episim.util.TissueCellDataFieldsInspector;


public class ExpressionCheckerController {
	
	private static ExpressionCheckerController instance;
	
	private static final int ARITHMETICTYPE = 1;
	private static final int BOOLEANTYPE = 2;
	
	private ExpressionCheckerController(){}
	
	public static synchronized ExpressionCheckerController getInstance(){
		if(instance == null) instance = new ExpressionCheckerController();
		return instance;
	}
		
	public String checkBooleanDataMonitoringExpression(String expression, TissueCellDataFieldsInspector tissueDataFieldsInspector) throws ParseException, NumberFormatException{
				
		return checkExpression(expression, tissueDataFieldsInspector, BOOLEANTYPE);
	}
	
	public String checkArithmeticDataMonitoringExpression(String expression, TissueCellDataFieldsInspector tissueDataFieldsInspector) throws ParseException, NumberFormatException{
		
		return checkExpression(expression, tissueDataFieldsInspector, ARITHMETICTYPE);
	}
	
	private String checkExpression(String expression, TissueCellDataFieldsInspector tissueDataFieldsInspector, int expressionType) throws NumberFormatException, ParseException{
		String result = "";
		if(expression != null && tissueDataFieldsInspector != null){
			 StringReader sr = new java.io.StringReader(expression);
		    Reader r = new java.io.BufferedReader(sr);
		    DataMonitoringExpressionChecker parser = new DataMonitoringExpressionChecker(r);
		    if(expressionType == ARITHMETICTYPE) return parser.checkExpression(tissueDataFieldsInspector);
		    else if(expressionType == BOOLEANTYPE) return parser.checkBooleanExpression(tissueDataFieldsInspector);
		}
		return result;
	}

}
