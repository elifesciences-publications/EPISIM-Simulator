package episimbiomechanics;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;




public abstract class EpisimModelConnector implements java.io.Serializable{
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Hidden{}	
	
	
	
	
	public abstract String getBiomechanicalModelId();
	public abstract String getBiomechanicalModelName();
}
