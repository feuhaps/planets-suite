/**
 * 
 */
package eu.planets_project.tb.gui.backing.admin;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIPanel;
import javax.faces.component.UISelectItems;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.component.html.HtmlGraphicImage;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import eu.planets_project.tb.gui.backing.admin.wsclient.faces.WSClientBean;

/**
 * 
 * This bean implements the following logic:
 * 
 *  - the service's operation deployer for the application
 *  - templates and xpath statements for service requests
 *  - writes TBServices to the backend and registers them within the service registry
 *  
 * @author Andrew Lindley, ARC
 *
 */
public class RegisterTBServices {
	
	//FacesContext
	FacesContext facesContext = FacesContext.getCurrentInstance();
	// that holds the service's endpoint URI
	private String sEndpointURI = "";
	
	//JSF UI containing the xmlRequestTemplate UI elements
	private UIComponent panelStep2 = new UIPanel();
	//counts how many inputText and outputText are needed to generate the required panelStep1 UI
	private int iCountNumbOfXMLRequestInputTokens = 0;
	private int iCountNumberOfXMLRequestOutputTokens = 0;
	
	//JSF UI Step2: containing sample invocation data
	private UIComponent panelStep3 = new UIPanel();
	private UIComponent panelStep3Add = new UIPanel();
	
	
	//a list of human readable TagNames. e.g. "File" or "FileArray"
	private List<SelectItem> lTagNames = new Vector<SelectItem>();
	
	//the selected TagName
	private String sTagIDFile = "File";
	private String sTagIDFileArray = "FileArray";
	private SelectItem selectedTagNameItem = new SelectItem(sTagIDFile);
	
	//The tokens that are used
	private final String TAG_FILE = "@tbFile@";
	private final String TAG_FILEARRAYLINE_START = "@tbFileArrayLineStart@";
	private final String TAG_FILEARRAYLINE_END = "@tbFileArrayLineEnd@";
	
	//Contains the registered operations (names) and their xmltemplates
	//holding the data for the registeredOperations in Form of a List<String>: e.g. [0]xmlrequesttemplate [1]xmlrespondstemplate
	private Map<String, List<String>> registeredOperations = new HashMap<String,List<String>>();

	//holds the inputField's current state
	private String sCurrentFileRef = new String("C:/DATA/Test1.doc");
	//Please Note: addedFileRefs and fileOutput are connected as follows
	//i.e. fileRef1 and its fileOutput1 both have key "1"
	//holds a list of all added file references. When array:[1..n] when input type file: [1]
	private Map<String,String> addedFileRefs = new HashMap<String,String>();
	private Map<String,String> fileOutput = new HashMap<String,String>();
	//a helper counter that counts one up everytime a new file gets added (but not down when one is deleted)
	private int iFileRefCounter = 0;
	
	//TODO: add. restrictions on the size of the file array
	private int iFileArrayLineMinRequired = 1;
	private int iFileArrayLineMaxAllowed  = 100;
	//output type of the Service e.g. PA or PC 
	private SelectItem serviceTypeItem = new SelectItem();
	
	//the selected outputType where the XPath points to
	private SelectItem outputTypesItem = new SelectItem();
	private List<SelectItem> lOutputTypes = new Vector<SelectItem>();
	private String sOutputTypeFile = "java.io.File";
	private String sOutputTypeString = "java.lang.String";
	
	//JSF UI Step3: XPath queries for the XML Responds
	private UIComponent panelStep4 = new UIPanel();
	private UIComponent panelStep4Add = new UIPanel();
	//The query entered by the user to point to the output-elements in the XML Responds
	private String sXpathQueryToOutput = new String("/*//return/item");
	
	
//GUI Rendering settings
	//is set to true when the first operation is added. 
	private boolean bServiceSelectionDisabled = false;
	//contains information about which stage of the wizzard is completed
	private int iStageCompleted = 0;
	//contains information about which stage of the wizzard is rendered
	private int iStageRendered = 0;
	
//Service Registry Metadata
	private String sServiceDescription ="";
	private Map<String,String> mapTagNamesValues = new HashMap<String,String>();
	private String newTagName = "";
	private String newTagValue = "";
	
	
	public RegisterTBServices(){
		//set the Tag value and labels
		lTagNames.add(new SelectItem(sTagIDFile));
		lTagNames.add(new SelectItem(sTagIDFileArray,"FileArray:File"));
		
		//set the Tag value and labels 
		lOutputTypes.add(new SelectItem(sOutputTypeFile,   "File"));
		lOutputTypes.add(new SelectItem(sOutputTypeString, "String"));
	}
	
	
	public void setEndpointURI(String uri){
		this.sEndpointURI = uri;
	}
	
	
	public String getEndpointURI(){
		return this.sEndpointURI;
	}
	
	
	/**
	 * Takes the provided WSDLURI and extracts ServiceInformation as service and operation names, etc.
	 * @return page-navigation-rule "error-analyze" or "success-analyze"
	 */
	public String command_analyzeWSDL(){
		boolean success = initWSClientBean();
		System.out.println("Erfolg: "+success);
		if(success){
			WSClientBean wsclient = getCurrentWSClientBean();
			//set the URI and call analyze
			wsclient.setWsdlURI(this.sEndpointURI);
			
			//returns either: "error-analyze" or "success-analyze"
			return wsclient.analyzeWsdl();
		}
		else{
			return "error-analyze";
		}
	}
	
	
	/**
	 * selects the operation's name and displays the form to enter its template data
	 * removes the operation name from the one
	 */
	public String command_useOperation(){
		this.setStageCompleted(1);
		String pageNavigationRule = createXMLRequestTokensGUI();
		return pageNavigationRule;
	}
	
	
	/**
	 * Addds one selected file reference from the input field to the list of added file refs for Step2.
	 * This information is rendered within the GUI by a HtmlOutputText + CommandLink&Icon (remove symbol) which are connected via the ID.
	 * The specified file ref(s) are used as input data to invoke a given service operation. 
	 * @return
	 */
	public String command_addFileRefToStep3(){
		
		try
        {
			facesContext = FacesContext.getCurrentInstance();
			
			//1) Get the InputFileRef data from the HtmlInputText Element
			String sFileRef = ((String)((HtmlInputText)this.getComponent("formXmlRequestSampleInvocation:input_selectFile")).getValue());
			
			if((sFileRef!=null)&&(!addedFileRefs.containsValue(sFileRef))){
				//2) Add the Data to the bean's variable
				this.addedFileRefs.put(iFileRefCounter+"", sFileRef);
			
				//3) For the current fileRef create an GUI outputfield + a RemoveIcon+Link
				int id = this.iFileRefCounter;
					//OutputText
				HtmlOutputText outputText = (HtmlOutputText) facesContext.getApplication().createComponent(HtmlOutputText.COMPONENT_TYPE);
				outputText.setValue(sFileRef);
				outputText.setId("OutputTextStep2Refs"+id);
	        		//CommandLink+Icon allowing to delete this entry
				HtmlCommandLink link_remove = (HtmlCommandLink) facesContext.getApplication().createComponent(HtmlCommandLink.COMPONENT_TYPE);
				//set the ActionMethod to the method: "command_removeFileRefFromStep2(ActionEvent e)"
				Class[] parms = new Class[]{ActionEvent.class};
				MethodBinding mb = FacesContext.getCurrentInstance().getApplication().createMethodBinding("#{RegisterTBServiceBean.command_removeFileRefFromStep3}", parms);
				link_remove.setActionListener(mb);
				link_remove.setId("UICommandStep2Refs"+id);
				//send along an helper attribute to identify which component triggered the event
				link_remove.getAttributes().put("IDint", id);
				HtmlGraphicImage image =  (HtmlGraphicImage)facesContext.getApplication().createComponent(HtmlGraphicImage.COMPONENT_TYPE);
				image.setUrl("../graphics/button_delete.gif");
				image.setAlt("delete-image");
				image.setId("HtmlGraphicImageStep2Refs"+id);
				link_remove.getChildren().add(image);
            
				//add both components
				this.getComponentPanelStep3Add().getChildren().add(link_remove);
				this.getComponentPanelStep3Add().getChildren().add(outputText);
	        
				//4) reset the fileInputText
				this.setCurrentFileInput("Enter File URI");
				
				this.iFileRefCounter++;
        	}
        }
        catch (java.lang.Throwable t)
        {
            System.out.println("java.lang.Throwable exception encountered...t.getMessage()=" + t.getMessage());
            t.printStackTrace();
        }
		return "reload-page";
	}
	
	
	/**
	 * Removes one selected file reference from the list of added file refs for Step3.
	 * The specified file ref(s) are used as input data to invoke a given service operation. 
	 * @return
	 */
	public String command_removeFileRefFromStep3(ActionEvent event){
		
		try {
			facesContext = FacesContext.getCurrentInstance();
			
			//1)get the passed Attribute "IDint", which is the counting number of the component
			int IDnr = ((Integer)event.getComponent().getAttributes().get("IDint")).intValue();
			
			//2) Remove the data from the bean's variable
			this.addedFileRefs.remove(IDnr+"");
			
			//3) Remove the GUI elements from the panel
			UIComponent comp_link = this.getComponent("formXmlRequestSampleInvocation:UICommandStep2Refs"+IDnr);
			UIComponent comp_text = this.getComponent("formXmlRequestSampleInvocation:OutputTextStep2Refs"+IDnr);
			this.getComponentPanelStep3Add().getChildren().remove(comp_link);
			this.getComponentPanelStep3Add().getChildren().remove(comp_text);

			
		} catch (Exception e) {
			// TODO: handle exception
		}
		return "reload-page";
	}
	
	public String command_continueToAddServiceRegistryMetadata(){
		this.setStageCompleted(4);
		return "reload-page";
	}
	
	
	/**
	 * Returns if the "add file button" is rendered or not
	 *  - it is rendered for file arrays (until the max input is reached)
	 *  - it is rendered for a single file and then removed when it was added.
	 * @return
	 */
	public boolean isAddFileButtonRendered(){
		
		if (this.getTagSelectItemValue()!=null){
			//When InputType Array: it is always rendered
			if(this.getTagSelectItemValue().equals(this.sTagIDFileArray))
				return true;
			
			//When InputType File: only rendered if no file was added
			if(this.getTagSelectItemValue().equals(this.sTagIDFile)){
				if(this.getAllFileInputs().size()==0){
					return true;
				}
			}
			
		}
		return false;
	}
	
	
	/**
	 * Used to render a collection of all added FileInputs which are used for sample invocation
	 * @return
	 */
	public Collection<String> getAllFileInputs(){
		return this.addedFileRefs.values();
	}
	
	/**
	 * Takes the XMLRequest template containing the "?" and replaces it with
	 * TB specific tokens. These GUI elements are created and placed within this method
	 * @return "error-analyze" or "error_analyze"
	 */
	private String createXMLRequestTokensGUI(){
		WSClientBean wsclient = getCurrentWSClientBean();
		
		//RequestTemplate is the one with "?" and without TB specific tokens
		String sRequestTemplate = wsclient.getOperationRequestTemplate();
		
		//Split the RequestTemplate message to fill the gui elements
		StringTokenizer templateTokenizer = new StringTokenizer(sRequestTemplate,"?",true);
		if(templateTokenizer.countTokens()>1){
			//UIPanel panel = (UIPanel)getComponent("formXmlRequestTemplate:panelStep2");
			UIComponent panel = this.getComponentPanelStep2();
			while(templateTokenizer.hasMoreTokens()){
				//the next token to operate on
				String templateToken = templateTokenizer.nextToken();
				try
		        {
					if(!templateToken.equals("?")){
						facesContext = FacesContext.getCurrentInstance();
						//dynamically add an OutputTextField component for this to the GUI
						HtmlOutputText outputText = (HtmlOutputText) facesContext.getApplication().createComponent(HtmlOutputText.COMPONENT_TYPE);
						//
						outputText.setValue(templateToken);
						outputText.setId("xmlRequestOutputText"+this.iCountNumberOfXMLRequestOutputTokens);
						
						this.iCountNumberOfXMLRequestOutputTokens++;
						
						//add the UIcomponent outputText to the UI parent component ("UIPanel")
						panel.getChildren().add(outputText);
					}
					else{
						//found the placeholder "?" and add the possible tokens to choose from
						facesContext = FacesContext.getCurrentInstance();
						
						//dynamically add a InputTextField (which is only enabled for FileArray)
						HtmlInputText arrayInputStart = (HtmlInputText) facesContext.getApplication().createComponent(HtmlInputText.COMPONENT_TYPE);
						arrayInputStart.setId("xmlRequestArrayStart"+this.iCountNumbOfXMLRequestInputTokens);
						arrayInputStart.setValue("<surrounding xml>");
						arrayInputStart.setRendered(false);
						
						//dynamically add SelectOneMenu with the possible XMLRequestInputTypes (lTagNames)
						HtmlSelectOneMenu selectText = (HtmlSelectOneMenu) facesContext.getApplication().createComponent(HtmlSelectOneMenu.COMPONENT_TYPE);
						selectText.setValue(((String)lTagNames.get(0).getValue()));
						selectText.setId("xmlRequestInputSelect"+this.iCountNumbOfXMLRequestInputTokens);
						//set the ValueChangeListener to the method: "processTokenValueChange(vce)"
						Class[] parms = new Class[]{ValueChangeEvent.class};
						MethodBinding mb = FacesContext.getCurrentInstance().getApplication().createMethodBinding("#{RegisterTBServiceBean.processTokenValueChange}", parms);
						selectText.setValueChangeListener(mb);
						//add a onChangeMethod
						selectText.setOnchange("submit()");
						UISelectItems items = new UISelectItems();
						items.setValue(this.lTagNames);
						
						//dynamically add a InputTextField (which is only enabled for FileArray)
						HtmlInputText arrayInputEnd = (HtmlInputText) facesContext.getApplication().createComponent(HtmlInputText.COMPONENT_TYPE);
						arrayInputEnd.setId("xmlRequestArrayEnd"+this.iCountNumbOfXMLRequestInputTokens);
						arrayInputEnd.setValue("</surrounding xml>");
						arrayInputEnd.setRendered(false);
						
						this.iCountNumbOfXMLRequestInputTokens++;
						
						//add the UIcomponents to their UI parent components (mainly: "UIPanel")
						panel.getChildren().add(arrayInputStart);
						selectText.getChildren().add(items);
						panel.getChildren().add(selectText);
						panel.getChildren().add(arrayInputEnd);
					}
		            
		        }
		        catch (java.lang.Throwable t)
		        {
		        	//TODO: add log statement
		            System.out.println("java.lang.Throwable exception encountered...t.getMessage()=" + t.getMessage());
		            t.printStackTrace();
		            return "error-analyze";
		        }
			}
		}
		else{
			//TODO: add log statement: MEssage wasn't analyzed correctly
			return "error-analyze";
		}
		
		return "reload-page";
	}
	
	
	/**
	 * This command is triggered after the RequestTemplate data is filled in the command
	 * process from Step2 to Step3 is triggered.
	 * @return
	 */
	public String command_addRequestTemplate(){

		WSClientBean wsclient = getCurrentWSClientBean();
		//add the operation we've just registered
		String sOperationName = wsclient.getOperationSelectItemValue();
		String sXMLRequestTemplate = this.buildXMLRequestTemplate(true);
		//store the XMLRequestTemplate within this bean
		this.setXMLRequestTemplate(sOperationName, sXMLRequestTemplate);
		
		//reset the required helpers:
		this.iCountNumberOfXMLRequestOutputTokens = 0;
		this.iCountNumbOfXMLRequestInputTokens = 0;
		
		//set the rendered wizzard stage to the next screen:
		this.setStageCompleted(2);
		
		return "reload-page";
	}
	
	
	/* 
	 * This method is triggered by ValueChangeEvents e.g. SelectMenuOne
	 * and is currently restricted to process operations with only one file or fileArray
	 * 
	 * (non-Javadoc)
	 * @see javax.faces.event.ValueChangeListener#processValueChange(javax.faces.event.ValueChangeEvent)
	 */
	public void processTokenValueChange(ValueChangeEvent vce){
		
		//selectMenuOne changed from File to FileArray or vice-versa
		this.setTagSelectedItemValue((String)vce.getNewValue());
		
		//get the InputPanel and set the InputTextFields when File:disbled / FileArray:enabled
		boolean bInputIsFile = true;
		//determine which input we're dealing with:
			if(this.getTagSelectItemValue().equals("File"))
				bInputIsFile = true;
			else
				//value="FileArray"
				bInputIsFile = false;
		if(bInputIsFile){
			//get the SurroundingXMLRequestInputElements and set disabled
			((HtmlInputText)this.getComponent("xmlRequestArrayStart"+(this.iCountNumbOfXMLRequestInputTokens-1))).setRendered(false);
			((HtmlInputText)this.getComponent("xmlRequestArrayEnd"+(this.iCountNumbOfXMLRequestInputTokens-1))).setRendered(false);
		}
		else{
			//get the SurroundingXMLRequestInputElements and set enabled
			((HtmlInputText)this.getComponent("xmlRequestArrayStart"+(this.iCountNumbOfXMLRequestInputTokens-1))).setRendered(true);
			((HtmlInputText)this.getComponent("xmlRequestArrayEnd"+(this.iCountNumbOfXMLRequestInputTokens-1))).setRendered(true);
		}	
	}
	
	
	/**
	 * This method either returns the XMLRequestTemplate with "?" or with the specified
	 * Testbed placeholders e.g. "@tbFile@"
	 * @param withTestbedPlaceholders true: b) false: a)
	 * @return
	 */
	private String buildXMLRequestTemplate(boolean withTestbedPlaceholders){
		String sRet = "";
		if(withTestbedPlaceholders){
			//return the XMLRequestTemplate the user has modified e.g. with "@tbFile@"
			if(this.iCountNumbOfXMLRequestInputTokens>0){
				sRet = this.getXMLRequestTemplateInputHelper();
			}
			else{
				//operation was not yes performed
				sRet = "";
			}
		}
		else{
			//return the XMLRequestTemplate with "?"
			WSClientBean wsclient = this.getCurrentWSClientBean();
			sRet= wsclient.getOperationSelectItemValue();
		}
		
		return sRet;
	}
	
	
	/**
	 * A private helper method to extract the Input Values that have been set
	 * via the dynamically created part of the UI for specifying the Testbed Tokens 
	 * within the XML Request template.
	 * @return
	 */
	private String getXMLRequestTemplateInputHelper(){
		
		String sRet ="";
		WSClientBean wsclient = this.getCurrentWSClientBean();
		StringTokenizer tokenizer = new StringTokenizer(wsclient.getOperationRequestTemplate(),"?",true);
		
		if(tokenizer.countTokens()!=this.iCountNumberOfXMLRequestOutputTokens+this.iCountNumbOfXMLRequestInputTokens){
			//TODO: add to log: some kind of failure occured
		}
		else{
			int inputCount = 0;
			int outputCount = 0;
			while(tokenizer.hasMoreTokens()){
				String sToken = tokenizer.nextToken();
				
				if(sToken.equals("?")){
					//get the dynamically added components:
					HtmlSelectOneMenu input = (HtmlSelectOneMenu)getComponent("xmlRequestInputSelect"+inputCount);
					inputCount++;
					String sHelper =(String)input.getValue();
					
					//check if we're adding a File or a FileArray
					if(sHelper.equals("File")){
						sRet+=this.TAG_FILE;
					}
					else{
						//sHelper ist "FileArray"
						sRet+=this.TAG_FILEARRAYLINE_START;
						HtmlInputText startArrayToken = (HtmlInputText)getComponent("xmlRequestArrayStart"+(this.iCountNumbOfXMLRequestInputTokens-1));
						sRet+=startArrayToken.getValue();
						sRet+=this.TAG_FILE;
						HtmlInputText endArrayToken = (HtmlInputText)getComponent("xmlRequestArrayEnd"+(this.iCountNumbOfXMLRequestInputTokens-1));
						sRet+=endArrayToken.getValue();
						sRet+=this.TAG_FILEARRAYLINE_END;
					}
				}
				else{
					HtmlOutputText output = (HtmlOutputText)getComponent("xmlRequestOutputText"+outputCount);
					outputCount++;
					sRet+=(String)output.getValue();
				}
			}
		}
		
		return sRet;
	}
	
	
	/**
	 * Stores a given XMLRequestTemplate for a certain OperationName
	 * @param sOperationName
	 * @param sXMLRequestTemplate
	 */
	public void setXMLRequestTemplate(String sOperationName, String sXMLRequestTemplate){
		this.helperSetXMLTemplate(sOperationName, sXMLRequestTemplate, true);
	}
	
	
	/**
	 * A private helper method to set the XMLTemplates (XMLRequest and XMLRespond)
	 * @param sOperationName
	 * @param sXMLTemplate
	 * @param isRequestTemplate: if false: it is a XMLRespond template
	 */
	private void helperSetXMLTemplate(String sOperationName, String sXMLTemplate, boolean isRequestTemplate){
		if((sOperationName!=null)&&(sXMLTemplate!=null)){
			//set the helper
			int i =0;
			if(isRequestTemplate)
				i = 0;
			else
				i=1;
			//fill in the data
			if(this.registeredOperations.containsKey(sOperationName)){
				//this operation was already registered
				List<String> items = this.registeredOperations.get(sOperationName);
				if(items!=null){
					items.add(i, sXMLTemplate);
				}
				else{
					//this branch should actually never be reached
					//first element that's bein registered --> create new List
					items = new ArrayList<String>();
					items.add(i, sXMLTemplate);
					this.registeredOperations.put(sOperationName, items);
				}
			}
			else{
				//first element that's bein registered --> create new List
				List<String> items = new ArrayList<String>();
				items.add(i, sXMLTemplate);
				this.registeredOperations.put(sOperationName, items);
			}
			
		}
		else{
			//TODO: log: error
		}
	}
	
	
	/**
	 * User specifies XPath statement for querying the Sample Request Responds xml for 
	 * the output elements (e.g. the migrated file location)
	 * @return
	 */
	public String getXPathToOutputQuery(){
		return this.sXpathQueryToOutput;
	}
	
	/**
	 * User specifies XPath statement for querying the Sample Request Responds xml for 
	 * the output elements (e.g. the migrated file location)
	 * @return
	 */
	public void setXPathToOutputQuery(String sQuery){
		this.sXpathQueryToOutput = sQuery;
	}
	
	
	/**
	 * Sets the selected Tag name value (i.e. a String) from the 
 	 * drop-down list of available tags (File, FileArray,..)
	 * @param tag
	 */
	public void setTagSelectedItemValue(String tag){
		this.setTagSelectedItem(new SelectItem(tag));
		
	}
	
	/**
 	 * Gets the selected Tag name value (i.e. a String) from the 
 	 * drop-down list of available tags (File, FileArray,..)
 	 * 
 	 * @return the selected WebService name (i.e. a String)
	 */
	public String getTagSelectItemValue()
	{
		//log.debug("Getting serviceSelectItemValue");
		if (this.getTagSelectedItem() != null) {
			if (this.getTagSelectedItem().getValue() != null)
				return this.getTagSelectedItem().getValue().toString();    		
		}
		return "";
	}
	
	
	/**
	 * Is used by the GUI to set the selected Tag name (e.g. File or FileArray:File)
	 * as SelectItem element within the drop down box.
	 * @param item
	 */
	public void setTagSelectedItem(SelectItem item){
		this.selectedTagNameItem = item;
	}
	
	
	/**
	 * Is used by the GUI to get the selected Tag name (e.g. File or FileArray:File)
	 * as SelectItem element within the drop down box.
	 * @return
	 */
	public SelectItem getTagSelectedItem(){
		return this.selectedTagNameItem;
	}
	
	
	/**
	 * Is triggered when an already completed step of the wizzard navigation menu is
	 * chosen. The step to render is passed as parameter
	 * @return
	 */
	public String command_renderWizzardStep(){
		String requParam = (String)facesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("wizzardStep"); 
		if(requParam.equals("step1")){
			this.setRenderedStage(1);
		}
		if(requParam.equals("step2")){
			this.setRenderedStage(2);
		}
		if(requParam.equals("step3")){
			this.setRenderedStage(3);
		}
		if(requParam.equals("step4")){
			this.setRenderedStage(4);
		}
		if(requParam.equals("step5")){
			this.setRenderedStage(5);
		}
		return "reload-page";
	}
	
	
	/**
	 * This command is triggered after the SampleInvocation data is filled into the UI 
	 * and the process from Step3 to Step4 is triggered. i.e. invoke service with this data 
	 * and display UI to create XMLRespondsTemplate (with TB tokens) 
	 * @return
	 */
	public String command_invokeSampleRequest(){
		
		try{
			//assemble Sample Request out of Tokens and InputFiles
			String sSampleRequest = buildSampleRequest();
			
			//invoke wsclient
			WSClientBean wsclient = this.getCurrentWSClientBean();
			wsclient.setXmlRequest(sSampleRequest);
			//outcome either "success-send" or "error-send";
			String sOutcome = wsclient.sendRequest();
			
			this.setStageCompleted(3);
			
			return sOutcome;
		}catch(Exception e){
			return "error-send";
		}
	}
	
	
	/**
	 * Helper method to parse the Request XML message (including TB tokens) and to
	 * assemble the XML Request including the file references 
	 * This method requires: either one file reference or one file array with a file reference as its input
	 * @return
	 */
	private String buildSampleRequest() throws Exception{
		
		String sRet = "";
		String sMessageStart = "", sFileArrayLine = "", sMessageEnd = "";
		
		//1)parse TB tokens and inject file references instead
		String sTokenTemplate = this.getXMLRequestTemplate(getCurrentOperationName());
		
	//2a) action for token FILE_ARRAY
		boolean isFileArray = false;
		String tokenizer[] = sTokenTemplate.split(this.TAG_FILEARRAYLINE_START);
		if(tokenizer.length!=1){
			isFileArray = true;
			//this parsing procedure assumes that only one file array is being useed
			sMessageStart = tokenizer[0];
			String sTemp = tokenizer[1];
			String array[] = sTemp.split(this.TAG_FILEARRAYLINE_END);
			sFileArrayLine = array[0];
			sMessageEnd = array[1];
			
			//there shouldn't be any more tokens:
			if((tokenizer.length>3)||(array.length>2)){
				//TODO: log ERROR by parsing: violating agains constraints of only one file or file array per message
				throw new Exception("BuildingSampleRequest: Error by parsing: violating against constraints of only one file or file array per message");
			}
		}
		
	//2b) action for Token FILE
		if(!isFileArray){
			String fileTokens[] = sTokenTemplate.split(this.TAG_FILE);
			sMessageStart = fileTokens[0];
			//add file ref inbetween
			sMessageEnd = fileTokens[1];
			if((this.addedFileRefs.values()!=null)&&(this.addedFileRefs.values().size()==1)){
				//get file ref
				String sFileRef = this.addedFileRefs.values().iterator().next();
				
				//build return String
				sRet = sMessageStart + sFileRef + sMessageEnd;
			}
			else{
				//TODO: log ERROR: too many file refs are added.
				throw new Exception("BuildingSampleRequest: Error: too many file references were added");
			}
		}
		else{
			//we're having a file array
			String fileTokens[] = sFileArrayLine.split(this.TAG_FILE);
			String sArrayLineStart = fileTokens[0];
			//add file ref inbetween
			String sArrayLineEnd = fileTokens[1];
			
			if(this.addedFileRefs.values()!=null){
				Iterator it = addedFileRefs.values().iterator();
				
				//build the return String
				sRet = sMessageStart;
				while(it.hasNext()){
					//add file ref to arrayLine
					sRet+=sArrayLineStart + it.next() +sArrayLineEnd;
				}
				sRet += sMessageEnd;
			}
		}

		//return the XML request that can be sent via the webservice stack
		return sRet;
	}
	
	
	/**
	 * Returns the currently in the gui selected operation name that we're working on
	 * @return
	 */
	private String getCurrentOperationName(){
		WSClientBean wsclient = this.getCurrentWSClientBean();
		return wsclient.getOperationSelectItemValue();
	}
	
	
	/**
	 * @param sOperationName
	 * @return the XMLRequestTemplate String (including TB Tokens and no "?") and null if not registered
	 */
	public String getXMLRequestTemplate(String sOperationName){
		String sRet = null;
		if(sOperationName!=null){
			if(this.registeredOperations.containsKey(sOperationName)){
				List<String> items = this.registeredOperations.get(sOperationName);
				if(items!=null){
					//XMLRequestTemplate is stored in List position [0]
					try {
						sRet = items.get(0);
					} catch (Exception e) {}
				}
			}
		}
		return sRet;
	}
	
	
	/**
	 * Puts a "WSClientBean" in the Session and sets its working dir
	 * @return true if registered successfully, false if an error occured
	 */
	private boolean initWSClientBean(){
		//load properties for WSClientBean from BackendResources.properties file
		Properties properties = new Properties();
	    try {
	        java.io.InputStream ResourceFile = getClass().getClassLoader().getResourceAsStream("eu/planets_project/tb/impl/BackendResources.properties");
	        properties.load(ResourceFile); 

	        //Note: sFileDirBase = ifr_server/bin/../server/default/deploy/jbossweb-tomcat55.sar/ROOT.war
	        String sFileDirBase = properties.getProperty("Jboss.FiledirBase");
	        ResourceFile.close();

	        WSClientBean wcb = new WSClientBean();
			wcb.setWorkDir(sFileDirBase+"/planets/wsexecution");

			facesContext = FacesContext.getCurrentInstance();
			facesContext.getExternalContext().getSessionMap().put("WSClientBean", wcb);
			return true;
			
	    } catch (IOException e) {
	    	//TODO add logg statement
	    	System.out.println("readJBossFileDirs from BackendResources failed!"+e.toString());
	    	return false;
	    }
	}
	
	
	/**
	 * Checks the WSDL whether it conforms to the latest WS-I profile or not
	 * 
	 * @return true if compliant
	 */
	public boolean isEndpointWSICompliant(){
		facesContext = FacesContext.getCurrentInstance();
		WSClientBean wsclient = this.getCurrentWSClientBean();
		
		if(wsclient!=null){
			//String saying either "error" or "success"
			//TODO: Add this line of code when WSI check problem is solved
			//return wsclient.checkWsdl().equals("success");
			return true;
		}else{
			return false;
		}
	}
	
	
	public boolean getIsEndpointSelected(){
		if (this.sEndpointURI.equals(""))
			return false;
		return true;
	}
	
	public void setIsEndpointSelected(boolean b){
		
	}
	
	public boolean isServiceSelectionDisabled(){
		return this.bServiceSelectionDisabled;
	}
	
	/**
	 * Helper to fetch the WSClientBean from the session
	 * @return
	 */
	private WSClientBean getCurrentWSClientBean(){
		facesContext = FacesContext.getCurrentInstance();
		return (WSClientBean)facesContext.getExternalContext().getSessionMap().get("WSClientBean"); 
	}
	
	
	/**
	 * Used by UI Element to set the FileRef input field which will get added
	 * through the "add link" for sample Invocation
	 * @param sFileRef
	 */
	public void setCurrentFileInput(String sFileRef){
		this.sCurrentFileRef = sFileRef;
	}
	
	/**
	 * Used by UI Element to get the FileRef input field
	 * @return
	 */
	public String getCurrentFileInput(){
		try{ 
			return this.sCurrentFileRef;
		}
		catch(Exception e){
			return "";
		}
	}
	
	
	/**
	 * Sets iStageCompleted and iStageRendered to the stage number and performs any
	 * operations like get dynamically added components and set them disabled
	 * @param stageNr 1..n
	 */
	private void setStageCompleted(int stageNr){
		//analyze WSDL and select service + operation name
		if(stageNr == 1){
			this.iStageCompleted = stageNr;
			this.iStageRendered = stageNr;
			//nothing else required
		}
		//create XML Request Template
		if(stageNr == 2){
			this.iStageCompleted = stageNr;
			this.iStageRendered = stageNr;
			
			//retrieve dynamically added model elements and set them disabled
			Iterator<UIComponent> itComps = this.getComponentPanelStep2().getChildren().iterator();
			while(itComps.hasNext()){
				UIComponent com = itComps.next();
				try{
					HtmlSelectOneMenu menu = (HtmlSelectOneMenu)com;
					menu.setDisabled(true);
				}catch(Exception e){}
				try{
					HtmlInputText input = (HtmlInputText)com;
					input.setDisabled(true);
				}catch(Exception e){}
			}
		}
		//add sample invocation data and send request
		if(stageNr == 3){
			this.iStageCompleted = stageNr;
			this.iStageRendered = stageNr;
			//nothing else required
		}
		//mapping output with xpath
		if(stageNr == 4){
			this.iStageCompleted = stageNr;
			this.iStageRendered = stageNr;
		}
		//provide metadata 
		if(stageNr == 5){
			this.iStageCompleted = stageNr;
			this.iStageRendered = stageNr;
		}
	}
	
	
	/**
	 * Used to query which UI form of the deployServiceOperation wizzard is completed
	 * and therefore which items to set disabled
	 * Stage1: selecting endpoint and service + operation name
	 * @return
	 */
	public boolean isStage1Completed(){
		//e.g. iStageCompleted = 1... stage1Enabled = false, stage2Enabled = true;
		if(this.iStageCompleted>=1)
			return true;

		return false;
	}
	
	/**
	 * Used to query which UI form of the deployServiceOperation wizzard is completed
	 * and therefore which items to set disabled
	 * Stage2: building and invoking sample request
	 * @return
	 */
	public boolean isStage2Completed(){
		//e.g. iStageCompleted = 2... stage2Enabled = false, stage3Enabled = true;
		if(this.iStageCompleted>=2)
			return true;

		return false;
	}
	
	/**
	 * Used to query which UI form of the deployServiceOperation wizzard is completed
	 * and therefore which items to set disabled
	 * @return
	 */
	public boolean isStage3Completed(){
		if(this.iStageCompleted>=3)
			return true;

		return false;
	}
	
	public boolean isStage4Completed(){
		if(this.iStageCompleted>=4)
			return true;

		return false;
	}
	
	public boolean isStage5Completed(){
		if(this.iStageCompleted>=5)
			return true;

		return false;
	}
	
	/**
	 * Gets the rendered stage from 1..n
	 * @return
	 */
	public int getCompletedStage(){
		return this.iStageCompleted+1;
	}
	
	/**
	 * Set the currently rendered stage 1..n
	 * @param i
	 */
	public void setCompletedStage(int i){
		if(i>=1)
			this.iStageCompleted = i-1;
	}
	
	
	/**
	 * Gets the rendered stage from 1..n
	 * @return
	 */
	public int getRenderedStage(){
		if((this.iStageRendered+1)>=1){
			return iStageRendered+1;
		}
		else{
			System.out.println("returning rendered stage = 1");
			return 1;
		}
	}
	
	/**
	 * Set the currently rendered stage 1..n
	 * @param i
	 */
	public void setRenderedStage(int i){
		if(i>=1)
			this.iStageRendered = i-1;
	}
	
	/**
	 * Returns the UIComponent containing the XmlRequestTemplate Screen
	 * @return
	 */
	public UIComponent getComponentPanelStep2(){ 
		System.out.println(" A:"+panelStep2.getChildCount());
		System.out.println(" B:"+panelStep2.getId());
		return this.panelStep2;
	}
	
	/**
	 * Returns the UIComponent containing the XmlRequestTemplate Screen
	 */
	public void setComponentPanelStep2(UIComponent panel){
		this.panelStep2 = panel;
	}
	

	public UIComponent getComponentPanelStep3(){
		return this.panelStep3;
	}
	
	public void setComponentPanelStep3(UIComponent panel){
		this.panelStep3 = panel;
	}
	
	/**
	 * Returns the UIComponent containing the already added file refs for sample invocation
	 * @return
	 */
	public UIComponent getComponentPanelStep3Add(){
		return this.panelStep3Add;
	}
	
	public UIComponent getComponentPanelStep4(){
		return this.panelStep4;
	}
	
	public void setComponentPanelStep4(UIComponent panel){
		this.panelStep4 = panel;
	}
	
	public UIComponent getComponentPanelStep4Add(){
		return this.panelStep4Add;
	}
	
	public void setComponentPanelStep4Add(UIComponent panel){
		this.panelStep4Add = panel;
	}
	
	/**
	 * sets the UIComponent containing the already added file refs for sample invocation
	 * @return
	 */
	public void setComponentPanelStep3Add(UIComponent panel){
		this.panelStep3Add = panel;
	}
	
	
	/**
	 * Get the component from the JSF view model - it's id is registered withinin the page
	 * @return
	 */
	private UIComponent getComponent(String sID){

			facesContext = FacesContext.getCurrentInstance();
			
			Iterator<UIComponentBase> it = facesContext.getViewRoot().getChildren().iterator();
			UIComponent returnComp = null;
			
			while(it.hasNext()){
				UIComponent guiComponent = it.next().findComponent(sID);
				if(guiComponent!=null){
					returnComp = guiComponent;
				}
			}
			
			//changes on the object are directly reflected within the GUI
			return returnComp;
	  }
	
	
	/**
	 * Applies the provided XPath statement and tries to query the provided output results
	 * @return
	 */
	public String command_sampleRespondsQueryTest(){
		
		//1) apply the XPath statement
		//get the queryinformation entered by the user
		String sQuery = this.getXPathToOutputQuery();
		//get the executed and received XMLResponds data
		String xmlResponds = this.getCurrentWSClientBean().getXmlResponse();

		try {
			//2) query the DOM with the entered XPath statement
			DocumentBuilderFactory factory =   DocumentBuilderFactory.newInstance();  
			factory.setNamespaceAware(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Reader reader = new StringReader(xmlResponds);
			InputSource inputSource = new InputSource(reader);
			Document document = builder.parse(inputSource);
			XPath xpath = XPathFactory.newInstance().newXPath();
			NodeList nodes = (NodeList) xpath.evaluate(sQuery, document, XPathConstants.NODESET);
			
			//3) clear previous entered output information and gui renderers
			UIComponent panel = this.getComponentPanelStep3Add();
			if(panel.getChildren()!=null){
				this.fileOutput = new HashMap<String,String>();
				int j = panel.getChildCount();
				for(int i=0; i<j;i++){
					UIComponent comp = (UIComponent)panel.getChildren().get(0);
					panel.getChildren().remove(comp);
				}
			}
			
			//4) store the output data
			this.storeNodeListOutputs(nodes);
			
			//5) now add the output to step4 panel:
			this.addOutputToStep4Add();
			
		} catch (XPathExpressionException e) {
			//TODO add logg statement
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			//TODO add logg statement
			e.printStackTrace();
		} catch (SAXException e) {
			//TODO add logg statement
			e.printStackTrace();
		} catch (IOException e) {
			// TODO add logg statement
			e.printStackTrace();
		}
		
		return "reload-page";
	}
	
	/**
	 * Takes a given w3c.dom.NodeList, trys to extract its information according
	 * to fit into the expected output schema.
	 * i.e. expects the nodes to be element nodes and try to extract the information
	 * from their text-nodes.
	 * @param node
	 */
	private void storeNodeListOutputs(NodeList nodes){
		if(nodes!=null){
			for(int j=0;j<nodes.getLength();j++){
				Node item = nodes.item(j);
				if(item.getNodeType() == Node.ELEMENT_NODE){
					this.setOutput(j, item.getTextContent());
				}
			}
		}
	}
	
	/**
	 * Fetches for a given InputFile reference it's output data 
	 * @param fileInput a given and added inputFileReference
	 * @return output data if available - null if not
	 */
	public String getOutput(int position){
		if((this.fileOutput!=null)&&(this.fileOutput.containsKey(position+""))){
			return this.fileOutput.get(position+"");
		}
		//no matching pair was found
		return null;
	}
	
	
	/**
	 * Stores a given String value in a provided position as output
	 * @param position
	 * @param value
	 */
	public void setOutput(int position, String value){
		if(value!=null)
			this.fileOutput.put(position+"", value);
	}
	
	
	/**
	 * Fetches a certain fileInput for a given position
	 * @param position
	 * @return
	 */
	public String getFileInput(int position){
		if((this.addedFileRefs!=null)&&(this.addedFileRefs.containsKey(position+""))){
			return this.addedFileRefs.get(position+"");
		}
		//no matching pair was found
		return null;
	}
	
	/**
	 * Tests if the localFileRef can be converted into a File, if it's a file and if it's redable
	 * @param localFileRef
	 * @return
	 */
	private boolean canReadFile(String localFileRef){
		File f = new File(localFileRef);
		if(f.exists()&&f.isFile()&&f.canRead())
			return true;
		return false;
	}
	
	
	/**
	 * Adds InputFileRef and Output (either as Link or String) to the GUI
	 */
	private void addOutputToStep4Add(){
		
		facesContext = FacesContext.getCurrentInstance();
		UIComponent panel = this.getComponentPanelStep4Add();
		
		//loop through all input files and try to assign its output
		int j = this.getAllFileInputs().size();
		for(int i=0;i<j;i++){
			String input = this.getFileInput(i);
			String output = this.getOutput(i);
			
			//Display the item (input+output) only if the input != null
			if(input!=null){
				//Display the INPUT
				int addedrow = 0;
				//if file can be read, then display as Link
				if(canReadFile(input)){
					//OutputLink to view this File
					HtmlOutputLink link_input = (HtmlOutputLink) facesContext.getApplication().createComponent(HtmlOutputLink.COMPONENT_TYPE);
					link_input.setId("Step3OutputLinkInput"+i);
					link_input.setValue("file:///"+input);
					HtmlOutputText link_text = (HtmlOutputText) facesContext.getApplication().createComponent(HtmlOutputText.COMPONENT_TYPE);
					link_text.setId("Step3LinkInputText"+i);
					link_text.setValue(input);
					link_input.getChildren().add(link_text);
					panel.getChildren().add(link_input);
					addedrow++;
				}
				//display as String
				else{
					HtmlOutputText outputText = (HtmlOutputText) facesContext.getApplication().createComponent(HtmlOutputText.COMPONENT_TYPE);
					outputText.setId("Step3OutputTextInput"+i);
					outputText.setValue(input);
					panel.getChildren().add(outputText);
					addedrow++;
				}
				
				if(output!=null){
					//Display the OUTPUT
					if(canReadFile(output)){
						//OutputLink to view this File
						HtmlOutputLink link_output = (HtmlOutputLink) facesContext.getApplication().createComponent(HtmlOutputLink.COMPONENT_TYPE);
						link_output.setId("Step3OutputLinkOutput"+i);
						link_output.setValue("file:///"+output);
						HtmlOutputText link_text = (HtmlOutputText) facesContext.getApplication().createComponent(HtmlOutputText.COMPONENT_TYPE);
		                link_text.setId("Step3LinkOutputText"+i);
		                link_text.setValue(input);
		                link_output.getChildren().add(link_text);
						panel.getChildren().add(link_output);
						addedrow++;
					}
					//display as String
					else{
						HtmlOutputText outputText = (HtmlOutputText) facesContext.getApplication().createComponent(HtmlOutputText.COMPONENT_TYPE);
						outputText.setId("Step3OutputTextOutput"+i);
						outputText.setValue(output);
						panel.getChildren().add(outputText);
						addedrow++;
					}
				}
				else{
					if(addedrow==1){
						//Then add a placeholder element just for formating purposes
						HtmlOutputText outputText = (HtmlOutputText) facesContext.getApplication().createComponent(HtmlOutputText.COMPONENT_TYPE);
						outputText.setId("Step3OutputTextOutput"+i);
						outputText.setValue("");
						panel.getChildren().add(outputText);
					}
				}
			}
		}
	}
	
	
	/**
	 * Used to fill the gui selectOneMenu with all possible outputTypes
	 * i.e. File and String
	 * @return
	 */
	public List<SelectItem> getSelectedOutputTypeItems(){
		return this.lOutputTypes;
	}
	
	/**
	 * Metadata for the operation's output type: "String" or "File"
	 * @return
	 */
	public String getSelectedOutputTypeValue(){
		if (this.getSelectedOutputTypeItem() != null) {
			if (this.getSelectedOutputTypeItem().getValue() != null)
				return this.getSelectedOutputTypeItem().getValue().toString();    		
		}
		return "";
	}
	
	public void setSelectedOutputTypeValue(String sOutputType){
		setSelectedOutputTypeItem(new SelectItem(sOutputType));
	}
	
	public SelectItem getSelectedOutputTypeItem(){
		return this.outputTypesItem;
	}
	
	public void setSelectedOutputTypeItem(SelectItem item){
		this.outputTypesItem = item;
	}
	
	
	/**
	 * This method is triggered by the final step of the gui. Tasks are
	 *  - store relevant information in the backend 
	 *  - register service within the service registry (inkluding collected metadata)
	 * @return
	 */
	public String command_completeRegistration(){
		this.setStageCompleted(5);
		/*TestbedServiceImpl tbService = new TestbedServiceImpl();
		TestbedService.ServiceOperation operation = tbService.new ServiceOperationImpl();
		ServiceRegistry registry = ServiceRegistryImpl.getInstance();
		
		//relevant information to build the wsclient bean: wsdlURI, servicename, opname
		//relevant information to build the TB parts: XMLRequest template, XPath query
		
		//endpoint + extract WSDLcontent
		tbService.setEndpoint(this.getEndpointURI(),true);
		//serviceName
		tbService.setName(this.getCurrentServiceName());
		
		//check if this is a new service or just a new operation for it
		boolean bKnown = registry.isServiceRegistered(tbService.getWSDLContent());
		TestbedService s = registry.getServiceByWSDLContent(tbService.getWSDLContent());
		if(s!=null){
			tbService = (TestbedServiceImpl)s;
		}
		//operationName
		operation.setName(this.getCurrentOperationName());
		//xmlRequestTemplate
		operation.setXMLRequestTemplate(this.getXMLRequestTemplate(this.getCurrentOperationName()));
		//xpathQueryToOutput
		operation.setXPathToOutput(this.getXPathToOutputQuery());
		//operation name
		operation.setName(this.getCurrentOperationName());
		
		//add the service operation
		tbService.addServiceOperation(operation);
		
		//register the service within the registry
		try {
			registry.registerService(tbService);
		} catch (Exception e) {
			//render general error page
			return "general-error-page";
		}
*/
		//return "reg-services-overview";
		return "reload-page";
	}
	
	public void setMaxAllowedNrOfInputFiles(int i){
		if(i>=1)
			this.iFileArrayLineMaxAllowed = i;
	}
	
	public int getMaxAllowedNrOfInputFiles(){
		String TagFileOrArray = getTagSelectItemValue();
		if((TagFileOrArray!=null)&&(TagFileOrArray!="")){
			//if FileArray
			if(TagFileOrArray.equals(this.sTagIDFileArray))
				return this.iFileArrayLineMaxAllowed;

			//if File
			if(TagFileOrArray.equals(this.sTagIDFileArray))
				return 1;
		}
		return this.iFileArrayLineMaxAllowed;
	}
	
	public void setMinRequiredNrOfInputFiles(int i){
		if(i>=1)
			this.iFileArrayLineMinRequired = i;
	}
	
	public int getMinRequireddNrOfInputFiles(){
		String TagFileOrArray = getTagSelectItemValue();
		if((TagFileOrArray!=null)&&(TagFileOrArray!="")){
			//if FileArray
			if(TagFileOrArray.equals(this.sTagIDFileArray))
				return this.iFileArrayLineMinRequired;

			//if File
			if(TagFileOrArray.equals(this.sTagIDFileArray))
				return 1;
		}
		return this.iFileArrayLineMinRequired;
	}
	
	
	/**
	 * Returns true if the selected Tag ID in the process of building the sample
	 * xml request template corresponds to "FileArray" and not "File";
	 * @return
	 */
	public boolean isSelectedTagIDArray(){
		String TagFileOrArray = getTagSelectItemValue();
		if((TagFileOrArray!=null)&&(TagFileOrArray!="")){
			if(TagFileOrArray.equals(this.sTagIDFileArray))
				return true;
		}
		return false;
	}
	
	/**
	 * A method for setting the service's metadata which then can be used for
	 * searching and querying
	 * @param sDescr: Human readable description about the service
	 */
	public void setServiceDescription(String sDescr){
		if(sDescr != null)
			this.sServiceDescription = sDescr;
	}
	
	public String getServiceDescription(){
		return this.sServiceDescription;
	}
	
	/**
	 * Returns a list of all added Tag Names
	 * @return
	 */
	public Collection<String> getAllAddedTagNames(){
		return this.mapTagNamesValues.keySet();
	}
	
	/**
	 * For a given TagName (=key) it's according value is returned.
	 * If no value stored, then "" is returned
	 * @param sTagName
	 * @return
	 */
	public String getTagValue(String sTagName){
		if((sTagName!=null)&&(this.mapTagNamesValues.keySet().contains(sTagName))){
			if(this.mapTagNamesValues.get(sTagName)!=null){
				return this.mapTagNamesValues.get(sTagName);
			}
			else{
				return "";
			}
		}
		return "";
	}
	
	/**
	 * Fills in the GUI element for creating new TagNames
	 * @param sTagName
	 */
	public void setTagName(String sTagName){
		if(sTagName!=null){
			this.newTagName = sTagName;
		}
	}
	
	/**
	 * Gets the String to fill in the GUI element for creating new TagNames
	 * @return
	 */
	public String getTagName(){
		return this.newTagName;
	}
	
	/**
	 * Fills in the GUI element for creating new TagValues
	 * @param sTagName
	 */
	public void setTagValue(String sTagValue){
		if(sTagValue!=null){
			this.newTagName = sTagValue;
		}
	}
	
	/**
	 * Gets the String to fill in the GUI element for creating new TagValues
	 * @return
	 */
	public String getTagValue(){
		return this.newTagValue;
	}
	
	/**
	 * Adds new TagNames and TagValues - TagName must not correspond to "" - TagValue any value.
	 * @return
	 */
	public String command_addTagNameAndValue(){
		if((this.newTagName!=null)&&(!this.newTagName.equals(""))&&(this.newTagValue!=null)){
			this.mapTagNamesValues.put(this.newTagName, this.newTagValue);
		}
		return "reload-page";
	}
	
	public List<String> getAllTagNames(){
		List<String> test = new Vector<String>();
		test.add("hallo");
		test.add("welt");
		return test;
	}

}
