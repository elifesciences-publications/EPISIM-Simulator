package sim.app.episim.persistence.dataconvert;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.persistence.XmlFile;
import sim.util.Double2D;

import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.NoExport;

public class XmlObject<T> {
	private HashMap<String, Object> parameters = new HashMap<String, Object>();
	private Class<? extends Object> clazz;
	private Node objectNode;
	private static final String CLASS = "class";
	private static final String VALUE = "value";

	public XmlObject(Object obj) {
		this.clazz = obj.getClass();
		exportParametersFromObject(obj);
	}

	public XmlObject(Node objectNode) {
		this.objectNode = objectNode;
	}

	protected Object parse(String objectString, Class<?> objClass) {
		Object o = null;
		objectString = objectString.trim();
		if (objClass.equals(String.class)) {
			o = objectString;
		} else if (Integer.TYPE.isAssignableFrom(objClass)) {
			o = Integer.parseInt(objectString);
		} else if (Double.TYPE.isAssignableFrom(objClass)) {
			o = Double.parseDouble(objectString);
		} else if (Float.TYPE.isAssignableFrom(objClass)) {
			o = Float.parseFloat(objectString);
		} else if (Boolean.TYPE.isAssignableFrom(objClass)) {
			o = Boolean.parseBoolean(objectString);
		} else if (Short.TYPE.isAssignableFrom(objClass)) {
			o = Short.parseShort(objectString);
		} else if (Long.TYPE.isAssignableFrom(objClass)) {
			o = Long.parseLong(objectString);
		} else if (Double2D.class.isAssignableFrom(objClass)) {
			o = parseDouble2D(objectString);
		} else if (EpisimDifferentiationLevel.class.isAssignableFrom(objClass)) {
			o = parseEpisimDifferentiationLevel(objectString);
		} else if (EpisimCellType.class.isAssignableFrom(objClass)) {
			o = parseEpisimCellType(objectString);
		}
		return o;
	}

	private static EpisimDifferentiationLevel parseEpisimDifferentiationLevel(
			String objString) {
		for (EpisimDifferentiationLevel diffLevel : ModelController
				.getInstance().getCellBehavioralModelController()
				.getAvailableDifferentiationLevels()) {
			if (diffLevel.toString().equals(objString))
				return diffLevel;
		}
		return null;
	}

	private static EpisimCellType parseEpisimCellType(String objString) {
		for (EpisimCellType cellType : ModelController.getInstance()
				.getCellBehavioralModelController().getAvailableCellTypes()) {
			if (cellType.toString().equals(objString))
				return cellType;
		}
		return null;
	}

	private static Double2D parseDouble2D(String objString) {
		if (!objString.startsWith("Double2D"))
			return null;
		String sub = objString.substring(9, objString.length() - 1);
		String[] split = sub.split(",");
		if (split.length != 2)
			return null;
		Double2D retDouble2D = new Double2D(Double.parseDouble(split[0]),
				Double.parseDouble(split[1]));
		return retDouble2D;

	}

	protected ArrayList<Method> getGetters() {
		ArrayList<Method> methods = new ArrayList<Method>();
		for (Method m : clazz.getMethods()) {
			if ((m.getName().startsWith("get") || m.getName().startsWith("is"))
					&& m.getAnnotation(NoExport.class) == null
					&& !m.getName().equals("getClass")) {

				try {
					if (m.getParameterTypes().length == 0) {
						methods.add(m);
					}
				} catch (IllegalArgumentException e) {
					ExceptionDisplayer.getInstance().displayException(e);
				}
			}
		}
		return methods;
	}

	protected ArrayList<Method> getSetters() {
		ArrayList<Method> methods = new ArrayList<Method>();
		for (Method m : clazz.getMethods()) {
			if (m.getName().startsWith("set")
					&& m.getAnnotation(NoExport.class) == null) {

				try {
					if (m.getParameterTypes().length == 1) {
						methods.add(m);
					}
				} catch (IllegalArgumentException e) {
					ExceptionDisplayer.getInstance().displayException(e);
				}
			}
		}
		return methods;
	}

	protected Object invokeGetMethod(Object object, Method actMethod) {
		Object obj = null;
		if (actMethod.getParameterTypes().length == 0) {
			try {
				obj = actMethod.invoke(object);
			} catch (Exception e) {
				return null;
			}
		}
		return obj;
	}

	protected boolean invokeSetMethod(Object target, Method method, Object value) {
		if (method.getParameterTypes().length == 1) {
			try {
				method.invoke(target, value);
			} catch (Exception e) {
				return false;
			}
			return true;
		} else
			return false;
	}

	public Object get(String parameterName) {
		return parameters.get(parameterName);
	}

	boolean set(String parameterName, Object value, Object target) {
		for (Method m : getSetters()) {
			String mName = m.getName().substring(3);
			if (mName.equalsIgnoreCase(parameterName)) {
				return invokeSetMethod(target, m, value);
			}
		}
		return false;
	}

	public static String methodToName(String methodName) {
		String parameterName = methodName;
		if (methodName.startsWith("get") || methodName.startsWith("set"))
			parameterName = parameterName.substring(3);
		StringBuilder sb = new StringBuilder();
		sb.append(Character.toLowerCase(parameterName.charAt(0)));
		sb.append(parameterName.substring(1));
		parameterName = sb.toString();
		return parameterName;
	}

	public Node toXMLNode(String nodeName, XmlFile xmlFile) {
		Element node = xmlFile.createElement(nodeName);
		node.setAttribute(CLASS, clazz.getCanonicalName());
		for (String s : getParameters().keySet()) {
			Element parameterNode = xmlFile.createElement(s);
			if (getParameters().get(s) != null)
				parameterNode.setAttribute(VALUE, getParameters().get(s)
						.toString());
//			parameterNode.setTextContent(getParameters().get(s).toString());
			node.appendChild(parameterNode);
		}
		return node;
	}

	public HashMap<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(HashMap<String, Object> parameters) {
		this.parameters = parameters;
	}

	public void addParameter(String parameterName, Object parameterValue) {
		this.parameters.put(parameterName, parameterValue);
	}

	public void exportParametersFromObject(Object obj) {
		for (Method m : getGetters()) {
			addParameter(methodToName(m.getName()), invokeGetMethod(obj, m));
		}
	}

	public void importParametersFromXml(Class<? extends T> clazz) {
		this.clazz = clazz;
		NodeList nl = objectNode.getChildNodes();
		for (Method m : getGetters()) {
			for (int i = 0; i < nl.getLength(); i++) {
				if (nl.item(i).getNodeName()
						.equalsIgnoreCase(methodToName(m.getName()))) {
					parameters.put(
							nl.item(i).getNodeName(),
							parse(nl.item(i).getAttributes()
									.getNamedItem(VALUE).getNodeValue(),
									m.getReturnType()));
				}
			}
		}
	}

	public void copyValuesToTarget(Object target) {
		for (String parameterName : parameters.keySet()) {
			set(parameterName, parameters.get(parameterName), target);
		}
	}
}
