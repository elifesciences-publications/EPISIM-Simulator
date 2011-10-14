package sim.app.episim.persistence;

import java.awt.geom.Rectangle2D.Double;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.engine.SimStateHack.TimeSteps;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.NoExport;

public class ConvertObjectXML {

	private SimulationStateFile simstate = null;

	public ConvertObjectXML(SimulationStateFile simstateFile) {
		this.simstate = simstateFile;
	}

	public Element objectToXML(Object obj, String nodeName) {
		System.out.println(obj + " - "+nodeName);
		Element node = null;
		if (obj == null) {
			return simstate.createElement(nodeName);
		} else if (obj instanceof List) {
			node = listToXML((List) obj, nodeName);
		} else if (hasGoodToString(obj)) {
			node = simstate.createElement(nodeName);
			node.setTextContent(obj.toString());
		} else {
			node = simstate.createElement(nodeName);
			HashMap<String, Object> parameters = getParameterObjectsFromObject(obj);
			for (String name : parameters.keySet()) {

				Object parameter = parameters.get(name);
				if (parameter != null) {
					node.appendChild(objectToXML(parameter, methodToName(name)));
				}
			}
		}
		return node;
	}

	private boolean hasGoodToString(Object obj) {
		if (obj instanceof Integer || obj instanceof Float || obj instanceof Double || obj instanceof String || obj instanceof Boolean)
			return true;
		else
			return false;
	}

	private boolean isAnnotated(Object obj) {
		if (false) // TODO nur sone idee
			return true;
		else
			return false;
	}
	
	private boolean notAnnotated(Object obj) {
		if (false) // TODO nur sone idee
			return true;
		else
			return false;
	}

	private Element listToXML(List list, String nodeName) {
		ArrayList<Element> nodeList = new ArrayList<Element>();
		if (list.size() > 0)
			for (Object obj : list) {
				nodeList.add(objectToXML(obj, nodeName));
			}
		else {
			return null;
		}
		Element node = simstate.createElement(list.getClass().getSimpleName() + "_" + nodeName);
		for (Node n : nodeList) {
			node.appendChild(n);
		}
		return node;
	}

	public <T> T xMLToObject(Node node, Class<T> objClass) {
		xMLToObject(node);
		String convertedElement = "test";
		return (T) convertedElement;
	}

	public Object xMLToObject(Node node) {
		String convertedElement = "test";
		return convertedElement;
	}

	private String methodToName(String methodName) {
		if (methodName == null || methodName.length() == 0)
			return "ERROR!";
		String parameterName = methodName;
		if (methodName.startsWith("get"))
			parameterName = parameterName.substring(3);
		if (methodName.startsWith("set"))
			parameterName = parameterName.substring(3);
		else if (methodName.startsWith("is"))
			parameterName = parameterName.substring(2);

		if (parameterName == null || parameterName.length() == 0)
			return "ERROR!";

		StringBuilder sb = new StringBuilder();
		sb.append(Character.toLowerCase(parameterName.charAt(0)));
		sb.append(parameterName.substring(1));
		parameterName = sb.toString();
		return parameterName;
	}

	private HashMap<String, Object> getParameterObjectsFromObject(Object object) {

		HashMap<String, Object> objects = new HashMap<String, Object>();
		for (Method m : object.getClass().getMethods()) {
			if ((m.getName().startsWith("get") || m.getName().startsWith("is")) && m.getAnnotation(NoExport.class) == null && !m.getName().equals("getClass")) {

				try {
					if (m.getParameterTypes().length == 0) {

						objects.put(methodToName(m.getName()), m.invoke(object, new Object[] {}));
						// System.out.println(object.getClass().getName() +
						// " : "
						// + m.getName() + " -> " +
						// objects.get(methodToName(m.getName())));
					}
				} catch (IllegalAccessException e) {
					ExceptionDisplayer.getInstance().displayException(e);
				} catch (IllegalArgumentException e) {
					ExceptionDisplayer.getInstance().displayException(e);
				} catch (InvocationTargetException e) {
					ExceptionDisplayer.getInstance().displayException(e.getCause());
				}
			}
		}
		return objects;
	}

	private Object invokeGetMethod(Object object, Method actMethod) {
		Object obj = null;
		if (actMethod.getParameterTypes().length == 0) {
			try {
				obj = actMethod.invoke(object, new Object[0]);
			} catch (Exception e) {
				return null;
			}
		}
		return obj;
	}
	
	private enum GoodToStrings{
		INTEGER(Integer.class),
		FLOAT(Float.class),
		DOUBLE(Double.class),
		STRING(String.class),
		BOOLEAN(Boolean.class);
		
		private Class clazz;
		private GoodToStrings(Class clazz) {
			this.clazz = clazz;
		}
}
	
	private enum AnnotatedClasses{

		UNIVERSALCELL(UniversalCell.class),
		EPISIMBIOMECHANICALMODELGLOBALPARAMETERS(EpisimBiomechanicalModelGlobalParameters.class),
		EPISIMCELLBEHAVIORALMODELGLOBALPARAMETERS(EpisimCellBehavioralModelGlobalParameters.class),
		MISCALLENEOUSGLOBALPARAMETERS(MiscalleneousGlobalParameters.class);
		
		private Class clazz;
		private AnnotatedClasses(Class clazz) {
			this.clazz = clazz;
		}
	}
	
	private enum NotAnnotatedClasses{
		//TODO
	}

}
