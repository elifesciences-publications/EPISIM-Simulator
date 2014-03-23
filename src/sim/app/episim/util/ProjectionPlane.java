package sim.app.episim.util;


import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
 
@XmlType(name = "projection-plane")
@XmlEnum
public enum ProjectionPlane {
 
    @XmlEnumValue("xy-plane")
    XY_PLANE("xy-plane"),
    
    @XmlEnumValue("xz-plane")
    XZ_PLANE("xz-plane"),
    
    @XmlEnumValue("yz-plane")
    YZ_PLANE("yz-plane");
 
    private final String value;
 
    ProjectionPlane(String v) {
        value = v;
    }
 
    public String toString(){ return value;}
    
    public String value() {
        return value;
    }
 
    public static ProjectionPlane fromValue(String v) {
        for (ProjectionPlane c: ProjectionPlane.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
 
