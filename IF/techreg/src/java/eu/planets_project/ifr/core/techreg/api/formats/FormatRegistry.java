/**
 * 
 */
package eu.planets_project.ifr.core.techreg.api.formats;

import java.net.URI;
import java.util.Set;
import java.util.List;

/**
 * @author <a href="mailto:Andrew.Jackson@bl.uk">Andy Jackson</a>
 *
 */
public interface FormatRegistry {

    /**
     * 
     * @param puri
     * @return
     */
    public abstract Format getFormatForURI(URI puri);
    
    /**
     * 
     * @param ext
     * @return
     */
    public abstract Set<URI> getURIsForExtension(String ext);

    /**
     * 
     * @param mimetype
     * @return
     */
    public abstract Set<URI> getURIsForMimeType(String mimetype);
    
    /**
     * 
     * @param query
     * @return
     */
    public abstract List<URI> search( String query );

    /**
     * This class looks up the different Format URIs consistent wit the given URI.
     * 
     * @param typeURI
     * @return
     */
    public abstract List<URI> getFormatURIAliases( URI typeURI );

    /**
     * 
     * @param typeURI
     * @return
     */
    public abstract List<Format> getFormatAliases( URI typeURI );
    
}