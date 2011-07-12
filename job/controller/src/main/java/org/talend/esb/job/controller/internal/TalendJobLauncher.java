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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.feature.AbstractFeature;
import org.talend.esb.sam.common.handler.impl.CustomInfoHandler;

import routines.system.api.ESBConsumer;
import routines.system.api.ESBEndpointInfo;
import routines.system.api.ESBEndpointRegistry;
import routines.system.api.ESBProviderCallback;
import routines.system.api.TalendESBJob;
import routines.system.api.TalendJob;

public class TalendJobLauncher implements ESBEndpointRegistry {

    private static final String PUBLISHED_ENDPOINT_URL = "publishedEndpointUrl";
    private static final String DEFAULT_OPERATION_NAME = "defaultOperationName";
    private static final String SERVICE_NAME = "serviceName";
    private static final String PORT_NAME = "portName";
    private static final String COMMUNICATION_STYLE = "COMMUNICATION_STYLE";
    private static final String USE_SERVICE_LOCATOR = "useServiceLocator";
    private static final String USE_SERVICE_ACTIVITY_MONITOR = "useServiceActivityMonitor";

    private static final String VALUE_REQUEST_RESPONSE = "request-response";
    private static final String VALUE_ONE_WAY = "one-way";

    private static final Logger LOG = Logger.getLogger(TalendJobLauncher.class.getName());

    private final Map<ESBProviderKey, Collection<ESBProvider> > endpoints =
        new ConcurrentHashMap<ESBProviderKey, Collection<ESBProvider>>();
    private final Map<TalendJob, Thread > jobs =
        new ConcurrentHashMap<TalendJob, Thread>();

    private Bus bus;
    private AbstractFeature serviceLocator;
    private AbstractFeature serviceActivityMonitoring;
    private CustomInfoHandler customPropertiesHandler;

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public void setServiceLocator(AbstractFeature serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    public void setServiceActivityMonitoring(
            AbstractFeature serviceActivityMonitoring) {
        this.serviceActivityMonitoring = serviceActivityMonitoring;
    }

    public void setCustomPropertiesHandler(CustomInfoHandler customPropertiesHandler) {
        this.customPropertiesHandler = customPropertiesHandler;
    }

    public void runTalendJob(final TalendJob talendJob, final String[] args) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					LazyProviderCallbackDelegate cb = null;
					if (talendJob instanceof TalendESBJob) {
						// We have an ESB Job;
						final TalendESBJob talendESBJob =  (TalendESBJob) talendJob;
						// Create callback delegate
						cb = new LazyProviderCallbackDelegate(new Callable<ESBProviderCallback>() {
							
							@Override
							public ESBProviderCallback call() throws Exception {
								// This will be run after #getRequest will be called from the job
								ESBEndpointInfo endpoint = talendESBJob.getEndpoint();
								if (null != endpoint) {
									return createESBProvider(endpoint.getEndpointProperties());
								}
								throw new NullPointerException("TalendESBJob#getEndpoint() returned null");
							}
						}, new Runnable() {
							
							@Override
							public void run() {
								ESBEndpointInfo endpoint = talendESBJob.getEndpoint();
								if (endpoint != null) {
									destroyESBProvider(endpoint.getEndpointProperties());
								}
							}
						});
						// Inject lazy initialization callback to the job
						talendESBJob.setProviderCallback(cb);
						// get provider end point information
						talendESBJob.setEndpointRegistry(TalendJobLauncher.this);
					}

					LOG.info("Talend Job starting...");
					int ret = talendJob.runJobInTOS(args);
					LOG.info("Talend Job finished with code " + ret);
					if (cb != null) {
						cb.shutdown();
					}
				} finally {
					jobs.remove(talendJob);
				}
			}
		});
		thread.setContextClassLoader(this.getClass().getClassLoader());
		thread.start();
		jobs.put(talendJob, thread);
	}

    public void stopTalendJob(final TalendJob talendJob) {
        jobs.get(talendJob).interrupt();
    }

    private ESBProviderCallback createESBProvider(final Map<String, Object> props) {
        final String publishedEndpointUrl = (String)props.get(PUBLISHED_ENDPOINT_URL);
        final QName serviceName = QName.valueOf((String)props.get(SERVICE_NAME));
        final QName portName = QName.valueOf((String)props.get(PORT_NAME));

        ESBProviderKey key = new ESBProviderKey(serviceName, portName);
        Collection<ESBProvider> esbProviders = endpoints.get(key);
        if(null == esbProviders) {
            esbProviders = new ArrayList<ESBProvider>(1);
            endpoints.put(key, esbProviders);
        }

        // TODO: add publishedEndpointUrl to ESBProviderKey
        ESBProvider esbProvider = null;
        for(ESBProvider provider : esbProviders) {
            if(publishedEndpointUrl.equals(provider.getPublishedEndpointUrl())) {
                esbProvider = provider;
                break;
            }
        }
        if(esbProvider == null) {
            boolean useServiceLocator =
                ((Boolean)props.get(USE_SERVICE_LOCATOR)).booleanValue();
            boolean useServiceActivityMonitor =
                ((Boolean)props.get(USE_SERVICE_ACTIVITY_MONITOR)).booleanValue();

            esbProvider = new ESBProvider(publishedEndpointUrl,
                serviceName,
                portName,
                useServiceLocator ? serviceLocator : null,
                useServiceActivityMonitor ? serviceActivityMonitoring : null,
                customPropertiesHandler);
            esbProvider.run(bus);
            esbProviders.add(esbProvider);
        }

        final String operationName = (String)props.get(DEFAULT_OPERATION_NAME);
        ESBProviderCallback esbProviderCallback =
            esbProvider.createESBProviderCallback(operationName,
                isRequestResponse((String)props.get(COMMUNICATION_STYLE)));

        return esbProviderCallback;
    }

    private void destroyESBProvider(final Map<String, Object> props) {
        final QName serviceName = QName.valueOf((String)props.get(SERVICE_NAME));
        final QName portName = QName.valueOf((String)props.get(PORT_NAME));
        final String publishedEndpointUrl = (String)props.get(PUBLISHED_ENDPOINT_URL);

        final Collection<ESBProvider> esbProviders = endpoints.get(
            new ESBProviderKey(serviceName, portName));
        if (esbProviders != null) {
            for (ESBProvider esbProvider : esbProviders) {
                if(publishedEndpointUrl.equals(esbProvider.getPublishedEndpointUrl())) {
                    final String operationName = (String)props.get(DEFAULT_OPERATION_NAME);
                    if(esbProvider.destroyESBProviderCallback(operationName)) {
                        esbProviders.remove(esbProvider);
                    }
                    break;
                }
            }
        }
    }

	@Override
	public ESBConsumer createConsumer(ESBEndpointInfo endpoint) {
		final Map<String, Object> props = endpoint.getEndpointProperties();

		final QName serviceName = QName.valueOf((String)props.get(SERVICE_NAME));
		final QName portName = QName.valueOf((String)props.get(PORT_NAME));
		final String operationName = (String)props.get(DEFAULT_OPERATION_NAME);

		ESBConsumer esbConsumer = null;
		/*
		 * commenting out this code coz of issue https://jira.sopera.de/browse/TESB-2074
		 * If we get the consumer in the following way, SAM featuer is not set for the consumer
		 * hence the consumer doesnt send out SAM events.
		Collection<ESBProvider> esbProviders = endpoints.get(
				new ESBProviderKey(serviceName, portName));
		if(esbProviders != null) {
			for(ESBProvider provider : esbProviders) {
				esbConsumer = provider.getESBProviderCallback(operationName);
				if(esbConsumer != null) {
					break;
				}
			}
		}

		// create generic consumer
		if(esbConsumer == null) {
		*/
			final String publishedEndpointUrl = (String)props.get(PUBLISHED_ENDPOINT_URL);
			boolean useServiceLocator =
				((Boolean)props.get(USE_SERVICE_LOCATOR)).booleanValue();
			boolean useServiceActivityMonitor =
				((Boolean)props.get(USE_SERVICE_ACTIVITY_MONITOR)).booleanValue();
			esbConsumer = new RuntimeESBConsumer(
					serviceName,
					portName,
					operationName,
					publishedEndpointUrl,
					isRequestResponse((String)props.get(COMMUNICATION_STYLE)),
					useServiceLocator ? serviceLocator : null,
					useServiceActivityMonitor ? serviceActivityMonitoring : null,
					customPropertiesHandler,
					bus);
		//}
		return esbConsumer;
	}

    private static boolean isRequestResponse(String value) {
        if (VALUE_ONE_WAY.equals(value)) {
            return false;
        } else if (VALUE_REQUEST_RESPONSE.equals(value)) {
            return true;
        }
        throw new RuntimeException("Unsupported communication style: " + value);
    }

}
