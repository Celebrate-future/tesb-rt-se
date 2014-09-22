/*
 * #%L
 * Service Locator Monitor
 * %%
 * Copyright (C) 2011 - 2014 Talend Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.talend.esb.locator.commands;

import javax.xml.namespace.QName;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.talend.esb.locator.tracker.ServiceLocatorTracker;
import org.talend.esb.servicelocator.client.ServiceLocator;
import org.talend.esb.servicelocator.client.ServiceLocatorException;

@Command(scope = "tlocator", name = "unregister", description = "Notifies Service Locator that a service endpoint is offline\n"
                                                              + "\n\u001b[1;37mEXAMPLE\u001b[0m"
                                                              + "\n\ttlocator:unregister \"{http://my.company.com/my-service-namespace}MyServiceName\" http://my.server.com:8040/services/MyServiceName"
                                                              + "\n\ttlocator:unregister MyServiceName http://another.server.com:8040/services/MyServiceName")
public class UnregisterOperation extends OsgiCommandSupport {

    private ServiceLocator sl;


    @Argument(index = 0, name = "serviceName", description = "Service name for endpoint to be updated.\nIf Service name is unique at Service Locator it is sufficient to only type local part of service name.", required = true, multiValued = false)
    String service;
    
    @Argument(index = 1, name = "URL", description = "Endpoint address to be unregistered from Service Locator. This endpoint will be marked as offline.", required = true, multiValued = false)
    String endpoint;
    
    /**
     * Needed to resolve local part of service name to fully qualified service name.
     */
    private ServiceLocatorTracker slt;


    public void setServiceLocator(ServiceLocator sl) {
        this.sl = sl;
        slt = ServiceLocatorTracker.getInstance(sl);
    }

    @Override
    protected Object doExecute() throws Exception {

        System.out.println();
        try {
            QName serviceName = slt.getServiceName(service);
            sl.unregister(serviceName, endpoint);
            System.out.println("Endpoint has been unregistered at Service Locator");
        } catch (ServiceLocatorException e) {
            System.err.println(e.getMessage());
        }
        System.out.println();

        return null;
    }

}
