package sim.app.episim.datamonitoring;

import java.io.Reader;
import java.io.StringReader;

import sim.app.episim.datamonitoring.parser.DataMonitoringExpressionChecker;
import sim.app.episim.datamonitoring.parser.ParseException;
import sim.app.episim.datamonitoring.parser.TokenMgrError;
import sim.app.episim.util.TissueCellDataFieldsInspector;


public class ExpressionCheckerController {
	
	private static ExpressionCheckerController instance;
	
	private ExpressionCheckerController(){}
	
	public static synchronized ExpressionCheckerController getInstance(){
		if(instance == null) instance = new ExpressionCheckerController();
		return instance;
	}
	
	public String checkChartExpression(String expression, TissueCellDataFieldsInspector tissueDataFieldsInspector) throws ParseException,TokenMgrError{
		
		String result = "";
		
	   if(expression != null && tissueDataFieldsInspector != null){
		 StringReader sr = new java.io.StringReader(expression);
	    Reader r = new java.io.BufferedReader( sr );
	    DataMonitoringExpressionChecker parser = new DataMonitoringExpressionChecker(r);
	    result = parser.check(tissueDataFieldsInspector);
	    
	   }
		return result;
	}

}
