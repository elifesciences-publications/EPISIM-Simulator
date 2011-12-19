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
import sim.field.grid.DoubleGrid2D;
import sim.util.Double2D;

public class XmlDoubleGrid2D extends XmlObject<DoubleGrid2D> {

	private static final String COLUMNS = "columns";
	private static final String ROWS = "rows";
	private static final String ROW = "row";
	private static final String DATA = "data";
	private static final String INDEX = "i";

	public static void main(String[] args) {
		try {
			XmlFile xml = new XmlFile("test");
			DoubleGrid2D doubleArray = new DoubleGrid2D(4,6);
			for(int x = 0;x<doubleArray.getHeight();x++){
				for(int y = 0;y<doubleArray.getWidth();y++){
					doubleArray.set(y, x, 4*x+y);
					System.out.print(doubleArray.get(y, x)+"\t");
				}
				System.out.println();
			}
			
			XmlDoubleGrid2D fds = new XmlDoubleGrid2D(doubleArray);
			xml.getRoot().appendChild(fds.toXMLNode("testDouble", xml));
			xml.save(new File("test"));

			XmlFile xmlLoad = new XmlFile(new File("test"));
			NodeList nl = xmlLoad.getRoot().getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node testNode = nl.item(i);
				if (testNode.getNodeName().equals("testDouble")) {
					XmlDoubleGrid2D test = new XmlDoubleGrid2D(testNode);
					DoubleGrid2D testdouble = test.copyValuesToTarget(null);
					for(int x = 0;x<testdouble.getHeight();x++){
						for(int y = 0;y<testdouble.getWidth();y++){
							System.out.print(testdouble.get(y, x)+"\t");
						}
						System.out.println();
					}

				}
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public XmlDoubleGrid2D(DoubleGrid2D obj) {
		super(obj);
	}

	public XmlDoubleGrid2D(Node objectNode) {
		super(objectNode);
	}

	@Override
	protected void exportSubXmlObjectsFromParameters() {
	}

	@Override
	public Element toXMLNode(String nodeName, XmlFile xmlFile) {

		int rows = 0;
		if (getObject() != null)
			rows = getObject().getHeight();
		int columns = 0;
		if (rows > 0)
			columns = getObject().getWidth();
		
		Element arrayNode = xmlFile.createElement(nodeName);
		arrayNode.setAttribute(ROWS, rows + "");
		arrayNode.setAttribute(COLUMNS, columns + "");

		for (int i = 0; i < rows; i++) {
			Element rowElement = xmlFile.createElement(ROW);
			rowElement.setAttribute(INDEX, i + "");
			StringBuffer rowString = new StringBuffer();
			for (int j = 0; j < columns; j++) {
				rowString.append(getObject().get(j,i));
				rowString.append("\t");
			}
			rowElement.setTextContent(rowString.toString());
			arrayNode.appendChild(rowElement);
		}

		return arrayNode;
	}

	@Override
	protected void importParametersFromXml(Class<?> clazz) {
		int rows = 0, columns = 0;
		DoubleGrid2D array2d = new DoubleGrid2D(columns, rows);
		if (getObjectNode() != null) {

			rows = Integer.parseInt(getObjectNode().getAttributes()
					.getNamedItem(ROWS).getNodeValue());
			columns = Integer.parseInt(getObjectNode().getAttributes()
					.getNamedItem(COLUMNS).getNodeValue());
			array2d = new DoubleGrid2D(columns, rows);

			NodeList rowsNL = getObjectNode().getChildNodes();
			for (int rowNodeIndex = 0; rowNodeIndex < rowsNL.getLength(); rowNodeIndex++) {
				Node rowNode = rowsNL.item(rowNodeIndex);
				if (rowNode.getNodeName().equalsIgnoreCase(ROW)) {
					int row = 0;
					row = Integer.parseInt(rowNode.getAttributes()
							.getNamedItem(INDEX).getNodeValue());
					String[] doubleStringList = rowNode.getTextContent().trim()
							.split("\t");
					if (doubleStringList.length >= columns) {
						for (int col = 0; col < columns; col++) {
							double val = 0;
							val = Double.parseDouble(doubleStringList[col]);
							array2d.set(col,row,val);
						}
					}
				}
			}
		}
		setObject(array2d);
	}

	@Override
	public DoubleGrid2D copyValuesToTarget(DoubleGrid2D target) {
		importParametersFromXml(null);
		return target.setTo(getObject());
	}

}
