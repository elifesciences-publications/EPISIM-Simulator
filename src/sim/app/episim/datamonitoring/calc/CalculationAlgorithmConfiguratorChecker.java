package sim.app.episim.datamonitoring.calc;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.datamonitoring.ExpressionCheckerController;
import sim.app.episim.util.TissueCellDataFieldsInspector;
import episiminterfaces.calc.CalculationAlgorithmConfigurator;


class CalculationAlgorithmConfiguratorChecker {
	
	
	public static boolean isValidCalculationAlgorithmConfiguration(CalculationAlgorithmConfigurator config, boolean validateExpression, TissueCellDataFieldsInspector cellDataFieldsInspector){
		
		if(config == null
				 || config.getArithmeticExpression().length < 2
				 || config.getArithmeticExpression()[0] == null
				 || config.getArithmeticExpression()[1] == null
				 
				 || config.getArithmeticExpression()[0].trim().equals("")
				 || config.getArithmeticExpression()[1].trim().equals("")
				
				 ||(CalculationAlgorithmServer.getInstance().getCalculationAlgorithmDescriptor(config.getCalculationAlgorithmID()).hasCondition()
				 &&(config.getBooleanExpression().length < 2
				 || config.getBooleanExpression()[0] == null
				 || config.getBooleanExpression()[1] == null
				 || config.getBooleanExpression()[0].trim().equals("")
				 || config.getBooleanExpression()[1].trim().equals("")))) return false;
		else if(validateExpression){
			try{
				int  actSessionId= ExpressionCheckerController.getInstance().getCheckSessionId();
				ExpressionCheckerController.getInstance().checkArithmeticDataMonitoringExpression(actSessionId, config.getArithmeticExpression()[0], cellDataFieldsInspector);
				if(CalculationAlgorithmServer.getInstance().getCalculationAlgorithmDescriptor(config.getCalculationAlgorithmID()).hasCondition()) ExpressionCheckerController.getInstance().checkBooleanDataMonitoringExpression(actSessionId, config.getBooleanExpression()[0], cellDataFieldsInspector);
				if(ExpressionCheckerController.getInstance().hasVarNameConflict(actSessionId, cellDataFieldsInspector)){
					return false;
				}
			
			}
			catch (Exception e1){
				ExceptionDisplayer.getInstance().displayException(e1);
				return false;
			}
		}
		return true;
	}

}
