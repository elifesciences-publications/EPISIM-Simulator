package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import sim.app.episim.persistence.ExportException;
import sim.app.episim.persistence.XmlFile;
import sim.field.grid.DoubleGrid2D;

public class XmlDoubleGrid2D extends XmlObject<DoubleGrid2D> {

	private static final String COLUMNS = "columns";
	private static final String ROWS = "rows";
	private static final String ROW = "row";
	private static final String DATA = "data";
	private static final String ROWNO = "y";

	public XmlDoubleGrid2D(DoubleGrid2D obj) throws ExportException {
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
			rowElement.setAttribute(ROWNO, i + "");
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
							.getNamedItem(ROWNO).getNodeValue());
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
