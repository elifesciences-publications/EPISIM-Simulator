package sim.app.episim.model.biomechanics.vertexbased;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import sim.app.episim.propfilegenerator.PropertyDescriptor;
import sim.app.episim.propfilegenerator.PropertyFileGenerator;


public class QuickParameterFileGenerator {

	
	public static void main(String[] args) {
		List<PropertyDescriptor> propertyDescriptors = new ArrayList<PropertyDescriptor>();
		
		propertyDescriptors.add(new PropertyDescriptor("kappa",Double.TYPE, 750, 50, 100));
		PropertyFileGenerator generator = new PropertyFileGenerator();
		generator.generatePropertyFiles(new File("d:/vmwareshare/propertyfiles/siminput.properties"), propertyDescriptors);

	}

}
