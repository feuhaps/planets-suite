package eu.planets_project.ifr.core.simple.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.ws.BindingType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xml.ws.developer.StreamingAttachment;

import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.characterise.Characterise;
import eu.planets_project.services.characterise.CharacteriseResult;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.Property;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.datatypes.ServiceReport.Status;
import eu.planets_project.services.datatypes.ServiceReport.Type;
import eu.planets_project.services.utils.DigitalObjectUtils;

/**
 * A simple characterisation service.
 * 
 * This just measures the size of the byte array associated with this Digital Object, in bytes.
 *
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
@Stateless
@WebService(name = SimpleCharacterisationService.NAME, 
        serviceName = Characterise.NAME, 
        targetNamespace = PlanetsServices.NS,
        endpointInterface = "eu.planets_project.services.characterise.Characterise" )
@StreamingAttachment(parseEagerly = true)
@BindingType(value = "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")
public class SimpleCharacterisationService implements Characterise {
    private final Log log = LogFactory.getLog(getClass().getName());
    
    /** A unique name for this service. */
    static final String NAME = "SimpleCharacterisationService";
    
    /** The Planets Property ID for the size of the object. */
    public static final String MIME_PROP_URI = "planets:pc/basic/bytestream/size";

    /**
     * @param name The desired property name
     * @return A property URI representation for the given name
     */
    public static URI makePropertyURI( String name) {
        try {
            URI propUri = new URI( name);
            return propUri;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * @see eu.planets_project.services.PlanetsService#describe()
     */
    public ServiceDescription describe() {
        ServiceDescription.Builder sd = new ServiceDescription.Builder( NAME, Characterise.class.getCanonicalName() );
        sd.description("A simple example characterization service, which just measures the size of single-binary digital objects.");
        return sd.build();
    }

    /**
     * {@inheritDoc}
     * @see eu.planets_project.services.characterise.Characterise#listProperties(java.net.URI)
     */
    public List<Property> listProperties(URI format) {
        List<Property> props = new ArrayList<Property>();
        props.add( new Property( makePropertyURI(MIME_PROP_URI), MIME_PROP_URI, null) );
        return props;
    }

    /**
     * {@inheritDoc}
     * @see eu.planets_project.services.characterise.Characterise#characterise(eu.planets_project.services.datatypes.DigitalObject, java.util.List)
     */
    public CharacteriseResult characterise(DigitalObject digitalObject, List<Parameter> parameters) {
        log.info("Start...");
        // Set up property list:
        List<Property> measured = new ArrayList<Property>();
        ServiceReport sr = new ServiceReport(Type.INFO, Status.SUCCESS, "Nothing checked");
        
        // Attempt to measure size:
        measured.add( new Property( makePropertyURI(MIME_PROP_URI), MIME_PROP_URI, ""+DigitalObjectUtils.getContentSize( digitalObject ) ));

        return new CharacteriseResult(measured, sr);
    }

}