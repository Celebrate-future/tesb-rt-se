/*
 * #%L
 * Talend :: ESB :: Job :: Controller
 * %%
 * Copyright (C) 2011 Talend Inc.
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
package org.talend.esb.job.controller.internal;

import org.osgi.framework.*;
import org.talend.esb.job.controller.Controller;
import org.talend.esb.job.controller.JobLauncher;

import routines.system.api.TalendJob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of Talend job controller.
 */
public class ControllerImpl implements Controller, ServiceListener {

    private BundleContext bundleContext;
    private JobLauncher jobLauncher;

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        this.bundleContext.addServiceListener(this);
    }

    public void setJobLauncher(JobLauncher jobLauncher) {
        this.jobLauncher = jobLauncher;
    }

    public Map<String, List<String>> list() throws Exception {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        map.put("jobs", this.listJobs());
        map.put("routes", this.listRoutes());
        return map;
    }

    public List<String> listJobs() throws Exception {
        ArrayList<String> list = new ArrayList<String>();
        ServiceReference[] references = bundleContext.getServiceReferences(TalendJob.class.getName(), "(!(type=route))");
        if (references != null) {
            for (ServiceReference reference:references) {
                if (reference != null) {
                    String name = (String) reference.getProperty("name");
                    if (name != null) {
                        list.add(name);
                    }
                }
            }
        }
        return list;
    }

    public List<String> listRoutes() throws Exception {
        ArrayList<String> list = new ArrayList<String>();
        ServiceReference[] references = bundleContext.getServiceReferences(TalendJob.class.getName(), "(type=route)");
        if (references != null) {
            for (ServiceReference reference:references) {
                if (reference != null) {
                    String name = (String) reference.getProperty("name");
                    if (name != null) {
                        list.add(name);
                    }
                }
            }
        }
        return list;
    }

    public Bundle getBundle(String name) throws Exception {
        ServiceReference[] references = bundleContext.getServiceReferences(TalendJob.class.getName(), "(name=" + name + ")");
        if (references == null) {
            throw new IllegalArgumentException("Talend job " + name + " not found");
        }
        return references[0].getBundle();
    }

    public void run(String name) throws Exception {
        this.run(name, new String[0]);
    }

    public void run(String name, final String[] args) throws Exception {
        ServiceReference[] references = bundleContext.getServiceReferences(TalendJob.class.getName(), "(name=" + name + ")");
        if (references == null) {
            throw new IllegalArgumentException("Talend job " + name + " not found");
        }
        final TalendJob job = (TalendJob) bundleContext.getService(references[0]);
        if (job != null) {
            jobLauncher.startJob(job, args);
        }
    }

    public void serviceChanged(ServiceEvent event) {
        if (event.getType() == ServiceEvent.UNREGISTERING) {
            Object service = bundleContext.getService(event.getServiceReference());
            if (service instanceof TalendJob) {
                jobLauncher.stopJob((TalendJob)service);
            }
        }
    }

}
