/**
 * 
 */
package eu.planets_project.tb.gui.backing;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.richfaces.component.UITree;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.event.NodeSelectedEvent;
import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;

import eu.planets_project.tb.impl.services.Service;
import eu.planets_project.tb.impl.services.ServiceRegistryManager;
import eu.planets_project.tb.impl.services.util.DiscoveryUtils;

/**
 * TODO Merge in the ServiceTemplates and the IF Service Registry - all endpoints only once, of course.
 * TODO Add in storage of service records.
 * TODO Add in service active/inactive status, and service history information.
 * 
 * @author <a href="mailto:Andrew.Jackson@bl.uk">Andy Jackson</a>
 *
 */
public class ServiceBrowser {
    /** */
    private static final Log log = LogFactory.getLog(ServiceBrowser.class);
    /** */
    public static final String CATEGORY = "category";
    public static final String SERVICE = "service";
    public static final String UNKNOWN_SERVICE = "Unrecognised Services";
    
    private List<Service> services;
    
    private TreeNode<ServiceTreeItem> rootNode = null;
    private Service selectedService = null;    
    
    private String nodeTitle;
    
    public class ServiceTreeItem {
        String type;
        String category;
        Service service;

        /**
         * @param s
         */
        public ServiceTreeItem(Service s) {
            this.type = SERVICE;
            this.service  = s;
        }

        /**
         * @param type
         */
        public ServiceTreeItem(String category) {
            this.type = CATEGORY;
            this.category = category;
        }

        /**
         * @return the type
         */
        public String getType() {
            return type;
        }

        /**
         * @return the category
         */
        public String getCategory() {
            return category;
        }

        /**
         * @return the service
         */
        public Service getService() {
            return service;
        }
        
    }
    
    
    /**
     * 
     * @return
     */
    public List<Service> getAvailableServices() {
        if( services == null )
            services = ServiceRegistryManager.listAvailableServices();
        return services;
    }
    
    /**
     * 
     */
    private void loadTree() {
        rootNode = new TreeNodeImpl<ServiceTreeItem>();
        // For PA services
        TreeNode<ServiceTreeItem> paCat = addCategoryNode("Preservation Action", rootNode );
        addServiceTypeTree("Migrate", "eu.planets_project.services.migrate", paCat );
        // For PC services
        TreeNode<ServiceTreeItem> pcCat = addCategoryNode("Preservation Characterisation", rootNode );
        addServiceTypeTree("Identify", "eu.planets_project.services.identify", pcCat );
        addServiceTypeTree("Validate", "eu.planets_project.services.validate", pcCat );
        addServiceTypeTree("DetermineProperties", "eu.planets_project.services.characterise", pcCat );
        // For unknown/unrecognised services:
        addServiceTypeTree(UNKNOWN_SERVICE, "unknown", rootNode );
    }

    /**
     * 
     * @param category
     * @param parentNode
     * @return
     */
    private TreeNode<ServiceTreeItem> addCategoryNode( String category, TreeNode<ServiceTreeItem> parentNode ) {
        TreeNodeImpl<ServiceTreeItem> categoryNode = new TreeNodeImpl<ServiceTreeItem>();
        categoryNode.setData( new ServiceTreeItem(category) );
        parentNode.addChild(category, categoryNode);
        return categoryNode;
    }
    
    /**
     * 
     * @param type
     * @param typeClass
     * @param node
     */
    private void addServiceTypeTree( String type, String typeClass, TreeNode<ServiceTreeItem> node ) {
        TreeNodeImpl<ServiceTreeItem> categoryNode = new TreeNodeImpl<ServiceTreeItem>();
        categoryNode.setData( new ServiceTreeItem(type) );
        node.addChild(type, categoryNode);
        addNodes(typeClass, categoryNode, this.getAvailableServices());
    }
    
    /**
     * 
     * @param type
     * @param node
     * @param slist
     */
    private void addNodes(String type, TreeNode<ServiceTreeItem> node, List<Service> slist ) {
        for( int i = 0; i < slist.size(); i++ ) {
            Service s = slist.get(i);
            if( s.getType().startsWith(type) ) {
                TreeNodeImpl<ServiceTreeItem> nodeImpl = new TreeNodeImpl<ServiceTreeItem>();
                nodeImpl.setData( new ServiceTreeItem(s) );
                node.addChild(new Integer(i), nodeImpl);
            }
        }
    }
    
    /**
     * 
     * @param event
     */
    public void processSelection(NodeSelectedEvent event) {
        //log.info("Dealing with event: "+event);
        HtmlTree tree = (HtmlTree) event.getComponent();
        ServiceTreeItem currentNode = (ServiceTreeItem) tree.getRowData();
        if( currentNode.getType().equals(SERVICE) ) {
            nodeTitle = currentNode.getService().getName();
            selectedService = currentNode.getService();
        } else if( currentNode.getType().equals(CATEGORY) ) {
            // TODO Summarise the category?
        }
    }
    
    /**
     * This locks the tree into a particular state, and is not in use at present.
     * 
     * @param uitree
     * @return
     */
    public boolean adviseNodeExpanded( UITree uitree ) 
    {
        ServiceTreeItem sti = (ServiceTreeItem) uitree.getTreeNode().getData();
        log.info("Current ServiceTreeItem of type: " + sti.getType() + " : " + sti.getCategory() );
        if( CATEGORY.equals(sti.getType()) ) {
            if( UNKNOWN_SERVICE.equals(sti.getCategory())) {
                return Boolean.FALSE;
            } else {
                return Boolean.TRUE;
            }
        } else {
            return Boolean.FALSE;
        }
    }
    
    public TreeNode<ServiceTreeItem> getTreeNode() {
        if (rootNode == null) {
            loadTree();
        }
        
        return rootNode;
    }

    public String getNodeTitle() {
        return nodeTitle;
    }

    public void setNodeTitle(String nodeTitle) {
        this.nodeTitle = nodeTitle;
    }


    /**
     * @return the selectedService
     */
    public Service getSelectedService() {
        return selectedService;
    }
    
    /**
     * FIXME This should to real service look-ups...
     * @return
     */
    public List<SelectItem> getIdentifyServicesSelectList() {
        List<SelectItem> slist = new ArrayList<SelectItem>();
        
        slist.add(new SelectItem("http://localhost:8080/pserv-pc-droid/Droid?wsdl", "DROID @ localhost"));
        
        return slist;
    }
    
}