/**
 * 
 */
package eu.planets_project.services.compare;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import eu.planets_project.services.datatypes.Property;
import eu.planets_project.services.datatypes.ServiceReport;

/**
 * Result type for the {@link Compare} interface.
 * @author Fabian Steeg (fabian.steeg@uni-koeln.de)
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.FIELD)
public final class CompareResult {
    private List<Property> properties;
    private ServiceReport report;

    /** For JAXB. */
    @SuppressWarnings("unused")
    private CompareResult() {}

    /**
     * @param properties The result properties
     * @param report The report
     */
    public CompareResult(final List<Property> properties, final ServiceReport report) {
        super();
        this.properties = properties;
        this.report = report;
    }

    /**
     * @return the result properties
     */
    public List<Property> getProperties() {
        return properties == null ? new ArrayList<Property>() : properties;
    }

    /**
     * @return the report
     */
    public ServiceReport getReport() {
        return report;
    }

}
