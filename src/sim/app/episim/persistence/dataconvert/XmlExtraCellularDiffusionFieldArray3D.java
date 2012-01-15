package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.Node;

import sim.app.episim.model.diffusion.ExtraCellularDiffusionField3D;
import sim.app.episim.persistence.ExportException;

public class XmlExtraCellularDiffusionFieldArray3D extends XmlObject<ExtraCellularDiffusionField3D[]> {

	private static final String GRID3D = "grid3d";
	private static final String NAME = "name";

	public XmlExtraCellularDiffusionFieldArray3D(ExtraCellularDiffusionField3D[] obj) throws ExportException {
		super(obj);
	}

	public XmlExtraCellularDiffusionFieldArray3D(Node objectNode) {
		super(objectNode);
	}
	
	
}
