
package uk.gov.nationalarchives.pronom;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.3-b02-
 * Generated source version: 2.1
 * 
 */
@WebServiceClient(name = "PronomService", targetNamespace = "http://pronom.nationalarchives.gov.uk", wsdlLocation = "http://www.nationalarchives.gov.uk/pronom/service.asmx?WSDL")
public class PronomService_Service
    extends Service
{

    private final static URL PRONOMSERVICE_WSDL_LOCATION;
    private final static Logger logger = Logger.getLogger(uk.gov.nationalarchives.pronom.PronomService_Service.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = uk.gov.nationalarchives.pronom.PronomService_Service.class.getResource(".");
            url = new URL(baseUrl, "http://www.nationalarchives.gov.uk/pronom/service.asmx?WSDL");
        } catch (MalformedURLException e) {
            logger.warning("Failed to create URL for the wsdl Location: 'http://www.nationalarchives.gov.uk/pronom/service.asmx?WSDL', retrying as a local file");
            logger.warning(e.getMessage());
        }
        PRONOMSERVICE_WSDL_LOCATION = url;
    }

    public PronomService_Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public PronomService_Service() {
        super(PRONOMSERVICE_WSDL_LOCATION, new QName("http://pronom.nationalarchives.gov.uk", "PronomService"));
    }

    /**
     * 
     * @return
     *     returns PronomService
     */
    @WebEndpoint(name = "PronomServiceSoap")
    public PronomService getPronomServiceSoap() {
        return super.getPort(new QName("http://pronom.nationalarchives.gov.uk", "PronomServiceSoap"), PronomService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns PronomService
     */
    @WebEndpoint(name = "PronomServiceSoap")
    public PronomService getPronomServiceSoap(WebServiceFeature... features) {
        return super.getPort(new QName("http://pronom.nationalarchives.gov.uk", "PronomServiceSoap"), PronomService.class, features);
    }

}
