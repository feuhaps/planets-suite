package eu.planets_project.tb.impl.services;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import eu.planets_project.tb.api.services.TestbedService;
import eu.planets_project.tb.api.services.ServiceRegistry;
import eu.planets_project.tb.impl.services.TestbedServiceImpl;

/**
 * @author alindley
 *
 */

public class ServiceRegistryImpl implements ServiceRegistry{
	
	//Map<ServiceUUID, Service>
	private Map<String,TestbedService> hm_AllServices;
	//Map containing the free annotation Tags for tagging a service for all UUIDS
	//Structure is: Map<UUID,List<Map<key,value>>>
	private Map<String,Map<String, String>> mapTags;
	
	private static ServiceRegistryImpl instance; 
	
	
	/**
	 * This Class implements the Java singleton pattern
	 */
	private ServiceRegistryImpl(){
		//HashMap<ServiceUUID, TestbedService>
		this.hm_AllServices = new HashMap<String,TestbedService>();
		this.mapTags = new HashMap<String,Map<String,String>>();
	}
	
	
	/**
	 * This class is implemented following the Java Singleton Pattern.
	 * Use this method to retrieve the instance of this class.
	 * @return
	 */
	public static synchronized ServiceRegistryImpl getInstance(){
		if (instance == null){
			instance = new ServiceRegistryImpl();
		}
		return instance;
	}

	
	/* (non-Javadoc)
	 * @see eu.planets.test.backend.api.model.mockup.ServiceRegistry#getAllServiceNames()
	 */
	public List<String> getAllServiceNames(){
		List<String> sRet = new Vector<String>();
		Iterator<TestbedService> it =this.getAllServices().iterator();
		while(it.hasNext()){
			//get the service's name and add it
			sRet.add(it.next().getName());
		}
		return sRet;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.planets.test.backend.api.model.mockup.ServiceRegistry#getAllServicNamesAndServiceEntrySets()
	 */
	public Set<Entry<String,TestbedService>> getAllServicNamesAndServiceEntrySets(){
		//Set contains Entry<UUID,TBservice>
		Set<Entry<String,TestbedService>> setUUIDAndService = this.hm_AllServices.entrySet();
		//Return Set contains: Entry<Name, TBService>
		Set<Entry<String,TestbedService>> setRet = new HashSet<Entry<String,TestbedService>>();
		
		Iterator<Entry<String,TestbedService>> it = setUUIDAndService.iterator();
		while(it.hasNext()){
			Entry<String,TestbedService> entry = it.next();
			TestbedService service = entry.getValue();
			Map<String,TestbedService> mapHelper = new HashMap<String,TestbedService>();
			//now put ServiceName and TestbedService as return value
			mapHelper.put(service.getName(), service);
			Iterator<Entry<String,TestbedService>> entryret = mapHelper.entrySet().iterator();
			setRet.add(entryret.next());
		}
		
		return setRet;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.planets.test.backend.api.model.mockup.ServiceRegistry#getAllServices()
	 */
	public Collection<TestbedService> getAllServices(){
		return this.hm_AllServices.values();
	}
	
	
	/* (non-Javadoc)
	 * @see eu.planets.test.backend.api.model.mockup.ServiceRegistry#getAllServiceUUIDs()
	 */
	public Collection<String> getAllServiceUUIDs(){
		return this.hm_AllServices.keySet();
	}
	

	/* (non-Javadoc)
	 * @see eu.planets.test.backend.api.model.mockup.ServiceRegistry#getAllEndpoints()
	 */
	public List<String> getAllEndpoints() {
		List<String> sRet = new Vector<String>();
		Iterator<TestbedService> it = this.getAllServices().iterator();
		while(it.hasNext()){
			sRet.add(it.next().getEntpoint());
		}
		return sRet;
	}
	

	/* (non-Javadoc)
	 * @see eu.planets.test.backend.api.model.mockup.ServiceRegistry#getServiceByID(java.lang.String)
	 */
	public TestbedService getServiceByID(String sServiceUUID) {
		if(sServiceUUID!=null){
			if(this.getAllServiceUUIDs().contains(sServiceUUID)){
				//now return the object
				return this.hm_AllServices.get(sServiceUUID);
			}
		}
		return null;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.planets.test.backend.api.model.mockup.ServiceRegistry#getServiceByWSDLContent(java.lang.String)
	 */
	public TestbedService getServiceByWSDLContent(String sWSDLContent){
		if(sWSDLContent!=null){
			Iterator<TestbedService> it = this.getAllServices().iterator();
			while(it.hasNext()){
				TestbedService tbService = it.next();
				String content = tbService.getWSDLContent();
				if((content!=null)&&(sWSDLContent.equals(content))){
					return tbService;
				}
			}
		}
		return null;
	}

	
	/* (non-Javadoc)
	 * @see eu.planets.test.backend.api.model.mockup.ServiceRegistry#registerService(eu.planets.test.backend.api.model.mockup.TestbedService)
	 */
	public void registerService(TestbedService service) throws Exception{
		if((service!=null)&&(service.getUUID()!="")){
			this.hm_AllServices.put(service.getUUID(), service);
		}
	}


	/* (non-Javadoc)
	 * @see eu.planets.test.backend.api.model.mockup.ServiceRegistry#isServiceRegistered(java.lang.String)
	 */
	public boolean isServiceRegistered(String WSDLContent) {
		Iterator<TestbedService> it = this.getAllServices().iterator();
		while(it.hasNext()){
			//this can never be null;
			String wsdlcontent = it.next().getWSDLContent();
			if(wsdlcontent.equals(WSDLContent))
				return true;
		}
		return false;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.planets.test.backend.api.model.mockup.ServiceRegistry#isServiceEndpointRegistered(java.lang.String)
	 */
	public boolean isServiceEndpointRegistered(String sEndpointRef){
		Iterator<TestbedService> it = this.getAllServices().iterator();
		while(it.hasNext()){
			//this can never be null;
			String endpoint = it.next().getEntpoint();
			if(endpoint.equals(sEndpointRef))
				return true;
		}
		return false;
	}


	/* (non-Javadoc)
	 * @see eu.planets.test.backend.api.model.mockup.ServiceRegistry#addTag(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void addTag(String serviceUUID, String sTagName, String sTagValue) {
		if((serviceUUID!=null)&&(sTagName!=null)&&(sTagValue!=null)){
			Map<String,String> tags;
			if(this.mapTags.containsKey(serviceUUID)){
				//1) get List with tags and add the new items
				tags = this.mapTags.get(serviceUUID);
			}
			else{
				//first tags for this object
				//1)initialize a new Map and add it to the outer map
				tags = new HashMap<String,String>();
				this.mapTags.put(serviceUUID, tags);
			}
			//2) add the new items
			tags.put(sTagName, sTagValue);
		}
	}


	/* (non-Javadoc)
	 * @see eu.planets.test.backend.api.model.mockup.ServiceRegistry#removeTag(java.lang.String, java.lang.String)
	 */
	public void removeTag(String serviceUUID, String sTagName) {
		if((serviceUUID!=null)&&(sTagName!=null)){
			if(this.mapTags.containsKey(serviceUUID)){
				Map<String,String> tags = this.mapTags.get(serviceUUID);
				if(tags.containsKey(sTagName))
					tags.remove(sTagName);
			}
			//else: do nothing, this Service didn't have any tags
		}
	}


	/* (non-Javadoc)
	 * @see eu.planets.test.backend.api.model.mockup.ServiceRegistry#getTag(java.lang.String, java.lang.String)
	 */
	public String getTag(String serviceUUID, String sTagName) {
		if((serviceUUID!=null)&&(sTagName!=null)){
			if(this.mapTags.containsKey(serviceUUID)){
				Map<String,String> tags = this.mapTags.get(serviceUUID);
				if(tags.containsKey(sTagName))
					return tags.get(sTagName);
			}
			//else: do nothing, this Service didn't have any tags
		}
		return "";
	}


	/* (non-Javadoc)
	 * @see eu.planets.test.backend.api.model.mockup.ServiceRegistry#getTags(java.lang.String)
	 */
	public Map<String, String> getTags(String serviceUUID) {
		Map<String, String> mapRet = new HashMap<String,String>();
		if(serviceUUID!=null){
			if(this.mapTags.containsKey(serviceUUID)){
				mapRet = this.mapTags.get(serviceUUID);
			}
		}
		return mapRet;
		
	}


	/* (non-Javadoc)
	 * @see eu.planets.test.backend.api.model.mockup.ServiceRegistry#removeTags(java.lang.String)
	 */
	public void removeTags(String serviceUUID) {
		if(serviceUUID!=null){
			if(this.mapTags.containsKey(serviceUUID)){
				Map<String, String> mapRet = new HashMap<String, String>();
				this.mapTags.put(serviceUUID, mapRet);
			}
		}
	}
	
}
