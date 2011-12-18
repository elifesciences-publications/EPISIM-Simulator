package sim.app.episim.persistence.dataconvert;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.netlib.util.doubleW;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sim.app.episim.persistence.XmlFile;
import sim.util.Double2D;

public class XmlDoubleArray2D extends XmlObject<double[][]> {

	private static final String ARRAY2D = "array2d";
	private static final String COLUMNS = "columns";
	private static final String ROWS = "rows";
	private static final String ROW = "row";
	private static final String DATA = "data";
	private static final String INDEX = "i";
	
//	public static void main(String[] args) {
//		try {
//			XmlFile xml = new XmlFile("test");
//			XmlDoubleArray2D fds = new XmlDoubleArray2D(new double[][]{{0d,1d,2d,3.3d,3.3d,3.3d,3.3d},{0d,1d,2d,3.3d,3.3d,3.3d,3.3d},{0d,1d,2d,3.3d,3.1d,3.3d,3.3d},{0d,1d,2.02d,3.3d,3.3d,3.3d,3.3d},{0d,1d,2d,3.3d,3.2d,3.3d,3.3d},{0d,1d,2d,3.3d,3.3d,3.3d,3.3d} });
//			xml.getRoot().appendChild(fds.toXMLNode("testDouble", xml));
//			xml.save(new File("test"));
//			
//			XmlFile xmlLoad = new XmlFile(new File("test"));
//			NodeList nl = xmlLoad.getRoot().getChildNodes();
//			for (int i = 0; i < nl.getLength(); i++) {
//				Node testNode = nl.item(i);
//				if(testNode.getNodeName().equals(ARRAY2D)){
//					XmlDoubleArray2D test = new XmlDoubleArray2D(testNode);
//					double [][] testdouble = test.copyValuesToTarget(null);
//					for(double[] row : testdouble){
//						for(double d : row){
//							System.out.print(d+" : ");
//						}
//						System.out.println();
//					}
//					
//				}
//			}
//		} catch (ParserConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (SAXException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}

	public XmlDoubleArray2D(double[][] obj) {
		super(obj);
	}

	public XmlDoubleArray2D(Node objectNode) {
		super(objectNode);
	}

	@Override
	protected void exportSubXmlObjectsFromParameters() {
	}

	@Override
	public Element toXMLNode(String nodeName, XmlFile xmlFile) {

		int rows = 0;
		if (getObject() != null)
			rows = getObject().length;
		int columns = 0;
		if (rows > 0)
			columns = getObject()[0].length;
		Element arrayNode = xmlFile.createElement(ARRAY2D);
		arrayNode.setAttribute(ROWS, rows + "");
		arrayNode.setAttribute(COLUMNS, columns + "");

		for (int i = 0; i < rows; i++) {
			Element rowElement = xmlFile.createElement(ROW);
			rowElement.setAttribute(INDEX, i + "");
			for (int j = 0; j < columns; j++) {
				Element dataElement = xmlFile.createElement(DATA);
				dataElement.setAttribute(INDEX, j + "");
				dataElement.setAttribute(DATA, getObject()[i][j] + "");
				rowElement.appendChild(dataElement);
			}
			arrayNode.appendChild(rowElement);
		}

		return arrayNode;
	}

	@Override
	protected void importParametersFromXml(Class<?> clazz) {
		int rows = 0, columns = 0;
		double[][] array2d = new double[rows][columns];
		if (getObjectNode() != null) {

			rows = Integer.parseInt(getObjectNode().getAttributes()
					.getNamedItem(ROWS).getNodeValue());
			columns = Integer.parseInt(getObjectNode().getAttributes()
					.getNamedItem(COLUMNS).getNodeValue());
			array2d = new double[rows][columns];

			NodeList rowsNL = getObjectNode().getChildNodes();
			for (int rowNodeIndex = 0; rowNodeIndex < rowsNL.getLength(); rowNodeIndex++) {
				Node rowNode = rowsNL.item(rowNodeIndex);
				if (rowNode.getNodeName().equalsIgnoreCase(ROW)) {
					int row = 0;
					row = Integer.parseInt(rowNode.getAttributes()
							.getNamedItem(INDEX).getNodeValue());
					NodeList dataNL = rowNode.getChildNodes();
					for (int dataNodeIndex = 0; dataNodeIndex < dataNL.getLength(); dataNodeIndex++) {
						Node dataNode = dataNL.item(dataNodeIndex);
						if (dataNode.getNodeName().equalsIgnoreCase(DATA)) {
							int col = 0;
							Node indexNode = dataNode.getAttributes()
									.getNamedItem(INDEX);
							if (indexNode != null)
								col = Integer
										.parseInt(indexNode.getNodeValue());
							double dataValue = 0;
							Node dataValueNode = dataNode.getAttributes()
									.getNamedItem(DATA);
							if (dataValueNode != null)
								dataValue = Double.parseDouble(dataValueNode
										.getNodeValue());

							if (row >= 0 && row < rows && col >= 0
									&& col < columns)
								array2d[row][col] = dataValue;
						}
					}

				}
			}
		}
		setObject(array2d);
	}

	@Override
	public double[][] copyValuesToTarget(double[][] target) {
		importParametersFromXml(null);
		return getObject();
	}

}
