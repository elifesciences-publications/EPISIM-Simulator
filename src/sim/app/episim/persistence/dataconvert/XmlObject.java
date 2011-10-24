package sim.app.episim.persistence.dataconvert;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.persistence.SimulationStateFile;
import sim.app.episim.persistence.XmlFile;
import sim.util.Double2D;

import episiminterfaces.NoExport;

public class XmlObject {
	private HashMap<String, Object> parameters = new HashMap<String, Object>();
	private Object obj;
	private Class clazz;
	private Node objectNode;

	public XmlObject(Object obj) {
		this.obj = obj;
		this.clazz = obj.getClass();
		exportParametersFromObject(obj);
	}
	
	public XmlObject(Node objectNode){
		this.objectNode = objectNode;
	}
	
	protected Object parse(String objectString, Class objClass){
		Object o = null;
		objectString = objectString.trim();
		if(objClass.equals(String.class)){
			o = objectString;
		} else if(Integer.TYPE.isAssignableFrom(objClass)){
			o = Integer.parseInt(objectString);
		} else if(Double.TYPE.isAssignableFrom(objClass)){
			o = Double.parseDouble(objectString);
		} else if(Float.TYPE.isAssignableFrom(objClass)){
			o = Float.parseFloat(objectString);
		} else if(Boolean.TYPE.isAssignableFrom(objClass)){
			o = Boolean.parseBoolean(objectString);
		} else if(Short.TYPE.isAssignableFrom(objClass)){
			o = Short.parseShort(objectString);
		} else if(Long.TYPE.isAssignableFrom(objClass)){
			o = Long.parseLong(objectString);
		}		
		return o;
	}

	protected ArrayList<Method> getGetters() {
		ArrayList<Method> methods = new ArrayList<Method>();
		for (Method m : clazz.getMethods()) {
			if ((m.getName().startsWith("get") || m.getName().startsWith("is")) && m.getAnnotation(NoExport.class) == null && !m.getName().equals("getClass")) {

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
			if (m.getName().startsWith("set") && m.getAnnotation(NoExport.class) == null) {

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

	// private HashMap<String, Object> getParameterObjectsFromObject(Object
	// object) {
	//
	// HashMap<String, Object> objects = new HashMap<String, Object>();
	// for (Method m : object.getClass().getMethods()) {
	// if ((m.getName().startsWith("get") || m.getName().startsWith("is")) &&
	// m.getAnnotation(NoExport.class) == null &&
	// !m.getName().equals("getClass")) {
	//
	// try {
	// if (m.getParameterTypes().length == 0) {
	//
	// objects.put(methodToName(m.getName()), m.invoke(object, new Object[]
	// {}));
	// System.out.println(object.getClass().getName() + " : " + m.getName() +
	// " -> " + objects.get(methodToName(m.getName())));
	// }
	// } catch (IllegalAccessException e) {
	// ExceptionDisplayer.getInstance().displayException(e);
	// } catch (IllegalArgumentException e) {
	// ExceptionDisplayer.getInstance().displayException(e);
	// } catch (InvocationTargetException e) {
	// ExceptionDisplayer.getInstance().displayException(e.getCause());
	// }
	// }
	// }
	// return objects;
	// }

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

	protected boolean invokeSetMethod(Object object, Method actMethod, Object value) {
		if (actMethod.getParameterTypes().length == 1) {
			try {
				actMethod.invoke(object, value);
			} catch (Exception e) {
				return false;
			}
			return true;
		} else
			return false;
	}

	String get(String parameterName) {
		// TODO Auto-generated method stub
		return null;
	}

	boolean set(String parameterName, Object value) {
		// TODO Auto-generated method stub
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
		node.setAttribute("class", clazz.getCanonicalName());
		for (String s : getParameters().keySet()) {
			Element parameterNode = xmlFile.createElement(s);
			if (getParameters().get(s) != null)
				parameterNode.setTextContent(getParameters().get(s).toString());
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
	
	public void importParametersFromXml(Object obj){
		this.clazz = obj.getClass();
		NodeList nl = objectNode.getChildNodes();
		for(Method m : getGetters()){
			for(int i = 0 ; i<nl.getLength(); i++){
				if(nl.item(i).getNodeName().equalsIgnoreCase(methodToName(m.getName()))){
					parameters.put(nl.item(i).getNodeName(),parse(nl.item(i).getTextContent(),m.getReturnType()));
				}
			}
		}
	}
}
