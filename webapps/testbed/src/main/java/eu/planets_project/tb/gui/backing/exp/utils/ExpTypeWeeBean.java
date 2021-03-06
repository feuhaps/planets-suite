/*******************************************************************************
 * Copyright (c) 2007, 2010 The Planets Project Partners.
 *
 * All rights reserved. This program and the accompanying 
 * materials are made available under the terms of the 
 * Apache License, Version 2.0 which accompanies 
 * this distribution, and is available at 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package eu.planets_project.tb.gui.backing.exp.utils;

import eu.planets_project.ifr.core.wee.api.workflow.generated.WorkflowConf;

public interface ExpTypeWeeBean {
    
    /**
     * The object containing the workflow's configuration - that's the one being submitted to the WEE
     * @param wfConfig
     */
    //public void setWeeWorkflowConf(WorkflowConf wfConfig);
    
    public WorkflowConf getWeeWorkflowConf();
    
    public boolean isValidCurrentConfiguration();
    
    /**
     * Builds the currentXMLConfig from the given service/param configuration
     * and writes it to a temporary file that's accessible via an external url ref.
     * This can be used within the browser to download the currentXMLConfig
     * @return
     */
    public String getTempFileDownloadLinkForCurrentXMLConfig();
    
    public boolean isTemplateAvailableInWftRegistry();
    
    /**
	 *  Takes the current bean's configuration (e.g. service, input, output format) and
	 *  creates a WorkflowConf object.
	 */
	public WorkflowConf buildWorkflowConfFromCurrentConfiguration()  throws Exception;
    
}
