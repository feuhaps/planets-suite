package eu.planets_project.ifr.core.registry.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.planets_project.ifr.core.registry.api.jaxr.model.BindingList;
import eu.planets_project.ifr.core.registry.api.jaxr.model.PsBinding;
import eu.planets_project.ifr.core.registry.api.jaxr.model.PsService;
import eu.planets_project.ifr.core.registry.api.jaxr.model.ServiceList;
import eu.planets_project.services.datatypes.ServiceDescription;

/**
 * 
 * @author <a href="mailto:andrew.jackson@bl.uk">Andy Jackson</a>
 * @author <a href="mailto:carl.wilson@bl.uk">Carl Wilson</a>
 *
 */
public class ServiceLookup {
    private static final Log log = LogFactory.getLog(ServiceLookup.class);
    private static final Pattern urlpattern = Pattern.compile("http://[^><\\s]*?\\?wsdl");

    /** Properties for the IF Service Registry */
    protected static final String USERNAME = "provider";
    protected static final String PASSWORD = "provider";
    protected static final String WILDCARD = "%";

    public static ServiceList listTypedServices() {
    	ServiceList sl = new ServiceList();
    	for (URI endpoint : listAvailableEndpoints()) {
    		ServiceDescription sd = null;
    		BindingList bindings = new BindingList();
    		List<PsBinding> bl = new ArrayList<PsBinding>();
    		bl.add(new PsBinding("", endpoint.toString(), false, null));
    		bindings.bindings = bl;
    		PsBinding binding = new PsBinding();
    		PsService service = new PsService();
    		
    		//service.
    		try {
				sd = DiscoveryUtils.getServiceDescription(endpoint.toURL());
				if (null == sd) {
					
				} else {
					
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				log.error("URL error when finding typed services");
				e.printStackTrace();
			}
    	}
    	return sl;
    }
    
    /**
     * 
     * @return a list of all available service endpoints as URIs
     */
	public static List<URI> listAvailableEndpoints() {
		log.info("ServiceLookup.listAvailableEndpoints()");
        Set<URI> uniqueSE = new HashSet<URI>();
        
        // Inspect the local JBossWS endpoints:
        try {
        	log.info("Creating authority string");
            String authority = PlanetsServerConfig.getHostname() + ":" + PlanetsServerConfig.getPort();
            log.info("authority string is " + authority);
            log.info("Creating URI for JBOSS services page");
            URI uriPage = new URI("http",authority,"/jbossws/services",null,null);
            log.info("URI set up OK to " + uriPage.toASCIIString());
            //2) extract the page's content: note: not well-formed --> modifications
            log.info("Reading page contents to string");
            String pageContent = readUrlContents(uriPage);
            //3) build a dom tree and extract the text nodes
            //String xPath = new String("/*//fieldset/table/tbody/tr/td/a/@href");
            log.info("Got page content, now extracting URIs of services");
             uniqueSE.addAll( extractEndpointsFromWebPage(pageContent) );
        } catch (URISyntaxException e) {
            log.error("URI Syntax exception : " + e);
        } catch (IOException e) {
        	log.error("IO Exception reading URL contents" + e);
        }
                
        // Now sort the list and return it.
        log.info("Creating sorted list of URIs");
        List<URI> sList = new ArrayList<URI>(uniqueSE);
        log.info("List created and has " + sList.size() + " services");
        java.util.Collections.sort( sList );
        log.info("returning list");
        return sList;
	}

    /**
     * Takes a given http URI and extracts the page's content which is returned as String
     * @param uri
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static String readUrlContents(URI uri)throws FileNotFoundException, IOException{

        InputStream in = null;
        try{
            if(!uri.getScheme().equals("http")){
                throw new FileNotFoundException("URI schema "+uri.getScheme()+" not supported");
            }
            in = uri.toURL().openStream();
            boolean eof = false;
            String content = "";
            StringBuffer sb = new StringBuffer();
            while(!eof){
                int byteValue = in.read();
                if(byteValue != -1){
                    char b = (char)byteValue;
                    sb.append(b);
                }
                else{
                    eof = true;
                }
            }
            content = sb.toString();
            if(content!=null){
                //now return the services WSDL content
                return content;
            }
            else{
                throw new FileNotFoundException("extracted content is null");
            }
        }
        finally{
            in.close();
        }
    }
    
    /**
     * As the wsdl content is not well-formed we're not able to use DOM here.
     * Parse through the xml manually to return a list of given ServiceEndpointAddresses
     * @param xhtml
     * @return
     * @throws Exception
     */
    private static Set<URI> extractEndpointsFromWebPage(String xhtml){
    	log.info("ServiceLookup.extractEndpointsFromWebPage()");
        Set<URI> ret = new HashSet<URI>();

        // Pull out all matching URLs:
        log.info("about to try the regexp magik");
        Matcher matcher = urlpattern.matcher(xhtml);
        while( matcher.find() ) { 
            log.info("Found match: " + matcher.group());
            try {
            	log.info("creating new URL");
                URL wsdlUrl = new URL(matcher.group());
                // Switch the authority for the 'proper' one:
                log.info("Patching in the hacked authority information");
                wsdlUrl = new URL( wsdlUrl.getProtocol(), 
                        PlanetsServerConfig.getHostname(), 
                        PlanetsServerConfig.getPort() , 
                        wsdlUrl.getFile());
                log.info("Got matching URL: "+wsdlUrl);
                try {
                    ret.add(wsdlUrl.toURI());
                } catch (URISyntaxException e) { }
            } catch (MalformedURLException e) {
                log.warn("Could not parse URL from "+matcher.group());
            }
        }
        log.info("returning the URI set");
        return ret;
    }
    
    /**
     * TODO This is a real bodge at the moment and hard codes localhost:8080 change to properties
     * 
     * @author <a href="mailto:carl.wilson@bl.uk">Carl Wilson</a>
     *
     */
    public static class PlanetsServerConfig {

    	/**
    	 * TODO Correct hard coded localhost reference
    	 * @return host name of the if server
    	 */
    	public static String getHostname() {
    		return "localhost";
    	}

    	/**
    	 * TODO correct hard coded bodge of port number
    	 * @return the port number of the if server
    	 */
    	public static int getPort() {
    		return 8080;
    	}
    }
}
