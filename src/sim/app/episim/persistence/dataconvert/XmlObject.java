package sim.app.episim.persistence.dataconvert;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.persistence.ExportException;
import sim.app.episim.persistence.XmlFile;
import sim.util.Double2D;
import sim.util.Double3D;
import sim.util.Int2D;
import sim.util.Int3D;

import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.NoExport;

public class XmlObject<T> {
	private HashMap<String, Object> parameters = new HashMap<String, Object>();
	private HashMap<String, XmlObject<?>> subXmlObjects = new HashMap<String, XmlObject<?>>();
	private HashMap<String, Object> parameterMinima = new HashMap<String, Object>();
	private HashMap<String, Object> parameterMaxima = new HashMap<String, Object>();
	private Node objectNode;
	private T object = null;
	private static final String CLASS = "class";
	private static final String VALUE = "value";
	private static final String MIN = "min";
	private static final String MAX = "max";

	public XmlObject(T obj) throws ExportException {
		this.object = obj;
		exportSubXmlObjectsFromParameters();
	}

	public XmlObject(Node objectNode) {
		this.objectNode = objectNode;
	}

	private void postProcessMinMax(Object object) {
		for (Method m : object.getClass().getMethods()) {
			if (m.getName().startsWith("_getMin") && m.getName().length() > 7) {
				parameterMinima.put(methodToName(m.getName()),
						invokeGetMethod(object, m));
			} else if (m.getName().startsWith("_getMax")
					&& m.getName().length() > 7) {
				parameterMaxima.put(methodToName(m.getName()),
						invokeGetMethod(object, m));
			}
		}

	}

	protected static Object parse(String objectString, Class<?> objClass) {
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

	protected static ArrayList<Method> getGetters(Class<?> clazz) {
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

	protected static ArrayList<Method> getSetters(Class<?> clazz) {
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

	protected static Object invokeGetMethod(Object object, Method actMethod) {
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

	protected Object get(String parameterName) {
		Object ret = parameters.get(parameterName);
		if (ret != null)
			return ret;
		XmlObject<?> xmlObj = subXmlObjects.get(parameterName);
		if (xmlObj != null)
			ret = xmlObj.copyValuesToTarget(null);
		if (ret != null)
			return ret;
		if (objectNode == null)
			return null;
		NodeList nl = objectNode.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node.getNodeName().equalsIgnoreCase(parameterName)) {
				Node attNode = node.getAttributes().getNamedItem(VALUE);
				if (attNode != null)
					return attNode.getNodeValue();
			}

		}
		return null;
	}

	protected boolean set(String parameterName, Object value, Object target) {
		if (value == null)
			return false;
		for (Method m : getSetters(target.getClass())) {
			String mName = m.getName().substring(3);
			if (mName.equalsIgnoreCase(parameterName)) {
				return invokeSetMethod(target, m, value);
			}
		}
		return false;
	}

	protected boolean setMinMax(String parameterName, Object value,
			Object target) {
		if (value == null)
			return false;
		for (Method m : getSetters(target.getClass())) {
			String mName = m.getName().substring(4);
			if (mName.equalsIgnoreCase(parameterName)) {
				return invokeSetMethod(target, m, value);
			}
		}
		return false;
	}

	protected static String methodToName(String methodName) {
		String parameterName = methodName;
		if (methodName.startsWith("get") || methodName.startsWith("set"))
			parameterName = parameterName.substring(3);
		if (methodName.startsWith("_getMax")
				|| methodName.startsWith("_getMin"))
			parameterName = parameterName.substring(7);
		StringBuilder sb = new StringBuilder();
		sb.append(Character.toLowerCase(parameterName.charAt(0)));
		sb.append(parameterName.substring(1));
		parameterName = sb.toString();
		return parameterName;
	}

	public Element toXMLNode(String nodeName, XmlFile xmlFile)
			throws ExportException {
		Element node = xmlFile.createElement(nodeName);
		for (String s : getSubXmlObjects().keySet()) {
			Element subNode = getSubXmlObjects().get(s).toXMLNode(s, xmlFile);
			if (subNode != null) {
				if (parameterMinima.get(s) != null)
					subNode.setAttribute(MIN, parameterMinima.get(s).toString());
				if (parameterMaxima.get(s) != null)
					subNode.setAttribute(MAX, parameterMaxima.get(s).toString());
				node.appendChild(subNode);
			}

		}
		return node;
	}

	public HashMap<String, Object> getParameters() {
		return parameters;
	}

	public HashMap<String, XmlObject<?>> getSubXmlObjects() {
		return subXmlObjects;
	}

	private void addParameter(String parameterName, Object parameterValue) {
		this.parameters.put(parameterName, parameterValue);
	}

	protected void addSubXmlObject(String parameterName,
			XmlObject<?> subXmlObject) {
		this.subXmlObjects.put(parameterName, subXmlObject);
	}

	protected void exportSubXmlObjectsFromParameters() throws ExportException {
		if (object != null) {
			for (Method m : getGetters(object.getClass())) {
				addParameter(methodToName(m.getName()),
						invokeGetMethod(object, m));
			}
			transformParametersToSubXmlObjects();
			postProcessMinMax(object);
		} else
			throw new ExportException(getClass().getSimpleName()
					+ ": Parameter doesn't exist");
	}

	private void transformParametersToSubXmlObjects() throws ExportException {
		for (String parameterName : parameters.keySet()) {
			Object subObj = parameters.get(parameterName);
			if (subObj instanceof Double2D) {
				subXmlObjects.put(parameterName, new XmlDouble2D(
						(Double2D) subObj));
			} else if (subObj instanceof Double3D) {
				subXmlObjects.put(parameterName, new XmlDouble3D(
						(Double3D) subObj));
			} else if (subObj instanceof Int2D) {
				subXmlObjects.put(parameterName, new XmlInt2D((Int2D) subObj));
			} else if (subObj instanceof Int3D) {
				subXmlObjects.put(parameterName, new XmlInt3D((Int3D) subObj));
			} else {
				subXmlObjects.put(parameterName, new XmlPrimitive(subObj));
			}
		}
	}

	protected void importParametersFromXml(Class<?> clazz) {
		NodeList nl = objectNode.getChildNodes();
		for (Method m : getGetters(clazz)) {
			for (int i = 0; i < nl.getLength(); i++) {
				String methName = methodToName(m.getName());
				Node node = nl.item(i);
				if (node.getNodeName().equalsIgnoreCase(methName)) {
					if (m.getReturnType().equals(Double2D.class)) {
						XmlDouble2D xmlObject = new XmlDouble2D(node);
						xmlObject.importParametersFromXml(m.getReturnType());
						subXmlObjects.put(methName, xmlObject);

					} else if (m.getReturnType().equals(Double3D.class)) {
						XmlDouble3D xmlObject = new XmlDouble3D(node);
						xmlObject.importParametersFromXml(m.getReturnType());
						subXmlObjects.put(methName, xmlObject);

					} else if (m.getReturnType().equals(Int2D.class)) {
						XmlInt2D xmlObject = new XmlInt2D(node);
						xmlObject.importParametersFromXml(m.getReturnType());
						subXmlObjects.put(methName, xmlObject);

					} else if (m.getReturnType().equals(Int3D.class)) {
						XmlInt3D xmlObject = new XmlInt3D(node);
						xmlObject.importParametersFromXml(m.getReturnType());
						subXmlObjects.put(methName, xmlObject);

					} else if(subXmlObjects.get(methName) == null) {
						XmlPrimitive xmlObject = new XmlPrimitive(node);
						xmlObject.importParametersFromXml(m.getReturnType());
						subXmlObjects.put(methName, xmlObject);

						Node maxNode = node.getAttributes().getNamedItem(MAX);
						Node minNode = node.getAttributes().getNamedItem(MIN);
						if (maxNode != null)
							parameterMaxima.put(
									methName,
									parse(maxNode.getNodeValue(),
											m.getReturnType()));
						if (minNode != null)
							parameterMinima.put(
									methName,
									parse(maxNode.getNodeValue(),
											m.getReturnType()));

					}
				}
			}
		}
	}

	public T copyValuesToTarget(T target) {
		if (target == null)
			return null;
		this.object = target;
		importParametersFromXml(target.getClass());
		for (String parameterName : subXmlObjects.keySet()) {
			XmlObject<?> xmlObj = subXmlObjects.get(parameterName);
			set(parameterName, xmlObj.copyValuesToTarget(null), target);

			setMinMax(MIN + parameterName, parameterMinima.get(parameterName),
					target);

			setMinMax(MAX + parameterName, parameterMaxima.get(parameterName),
					target);
		}
		for (String parameterName : parameters.keySet()) {
			set(parameterName, parameters.get(parameterName), target);
		}
		return target;
	}

	protected T getObject() {
		return object;
	}

	public void setObject(T object) {
		this.object = object;
	}

	protected Node getObjectNode() {
		return objectNode;
	}

}
