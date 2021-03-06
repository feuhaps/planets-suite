<%@ page import= "java.io.*, java.net.URLDecoder, java.net.URI, eu.planets_project.tb.impl.data.util.DataHandlerImpl, eu.planets_project.tb.api.data.util.DigitalObjectRefBean, eu.planets_project.tb.impl.data.util.ImageThumbnail" %><% 

// Pick up the parameters:
String fid = request.getParameter("fid");

// Decode the file name (might contain spaces and on) and prepare proper escaped form.
fid = URLDecoder.decode( fid, "UTF-8" );
URI duri = new URI( fid );

// Start up the testbed data handler:
DigitalObjectRefBean dh = new DataHandlerImpl().get(duri.toASCIIString());

// Get the full filename, and the real filename:
String filename = dh.getName(); 

// Guess the mime type:
String mimetype = dh.getMimeType();

// Set the headers appropriately:
response.setContentType( (mimetype != null) ? mimetype : "application/octet-stream" );
/* Size of thumbnail image is not known...
if( dh.getSize() >= 0 ) {
    response.setContentLength( ((Long)dh.getSize()).intValue() );
}
*/

// This should allow the content to be rendered by the browser, but the filename is ignored.
response.setHeader( "Content-Disposition", "inline; filename=\"" + filename + "\"" );
// The following alternative forces a download:
//response.setHeader( "Content-Disposition", "attachment; filename=\"" + filename + "\"" );

// Now stream out the data:
ServletOutputStream op = response.getOutputStream();
try {
    ImageThumbnail.createThumb(dh.getDigitalObject(), op);
} catch( Exception e ) {
    System.out.println( "Exception in thumbnailer: " + e );
    e.printStackTrace();
} finally {
    // Flush and close the output stream:
    op.flush();
    op.close();
}

%>