/**
 * 
 */
package eu.planets_project.tb.gui.backing.exp;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import eu.planets_project.ifr.core.common.logging.PlanetsLogger;
import eu.planets_project.tb.gui.backing.ServiceBrowser;
import eu.planets_project.tb.gui.backing.service.FormatBean;
import eu.planets_project.tb.gui.util.JSFUtil;

/**
 * 
 * @author <a href="mailto:Andrew.Jackson@bl.uk">Andy Jackson</a>
 *
 */
public class SelectSBInputFormatActionListener implements ActionListener {
    private PlanetsLogger log = PlanetsLogger.getLogger(SelectSBInputFormatActionListener.class, "testbed-log4j.xml");

    public void processAction(ActionEvent anEvent) throws AbortProcessingException {
        log.info("Processing event. SelectBatch.");
        
      UIComponent tmpComponent = anEvent.getComponent();

      while (null != tmpComponent && !(tmpComponent instanceof UIData)) {
        tmpComponent = tmpComponent.getParent();
      }

      if (tmpComponent != null && (tmpComponent instanceof UIData)) {
        Object tmpRowData = ((UIData) tmpComponent).getRowData();
        if (tmpRowData instanceof FormatBean ) {
            ServiceBrowser sb = (ServiceBrowser)JSFUtil.getManagedObject("ServiceBrowser");
            FormatBean fb = (FormatBean) tmpRowData;
            if( fb.equals( sb.getSelectedInputFormat() )) {
                sb.setSelectedInputFormat(null);
            } else {
                sb.setSelectedInputFormat( fb );
            }
        }
      }
    }
    
}