/*
 * #%L
 * Service Activity Monitoring :: Agent
 * %%
 * Copyright (C) 2011 - 2012 Talend Inc.
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
package org.talend.esb.sam.agent.feature;

import java.util.Iterator;
import java.util.Queue;

import org.apache.cxf.Bus;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.message.Message;
import org.apache.cxf.ws.addressing.MAPAggregator;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.apache.cxf.ws.addressing.soap.MAPCodec;
import org.talend.esb.sam.agent.eventproducer.EventProducerInterceptor;
import org.talend.esb.sam.agent.eventproducer.MessageToEventMapper;
import org.talend.esb.sam.agent.flowidprocessor.FlowIdProducerIn;
import org.talend.esb.sam.agent.flowidprocessor.FlowIdProducerOut;
import org.talend.esb.sam.agent.wiretap.WireTapIn;
import org.talend.esb.sam.agent.wiretap.WireTapOut;
import org.talend.esb.sam.common.event.Event;
import org.talend.esb.sam.common.spi.EventHandler;

/**
 * Feature for adding FlowId Interceptor and EventProducer Interceptor.
 * 
 */
public class EventFeature extends AbstractFeature {

    /*
     * Log the message content to Event as Default
     */
    private boolean logMessageContent = true;
    /*
     * No max message content limitation as Default
     */
    private int maxContentLength = -1;

    /*
     * No WS-Addressing MessageID transfer as Default
     */
    private boolean enforceMessageIDTransfer;

    private EventProducerInterceptor epi;

    /**
     * Instantiates a new event feature.
     */
    public EventFeature() {
        super();
    }

    /* (non-Javadoc)
     * @see org.apache.cxf.feature.AbstractFeature#initializeProvider(org.apache.cxf.interceptor.InterceptorProvider, org.apache.cxf.Bus)
     */
    @Override
    protected void initializeProvider(InterceptorProvider provider, Bus bus) {
        super.initializeProvider(provider, bus);

        //if enforceMessageIDTransfer and WS Addressing feature/interceptors not enabled, 
        //then add its interceptors to InterceptorProvider
        if (enforceMessageIDTransfer && !detectWSAddressingFeature(provider, bus)) {
            addWSAddressingInterceptors(provider);
        }

        FlowIdProducerIn<Message> flowIdProducerIn = new FlowIdProducerIn<Message>();
        provider.getInInterceptors().add(flowIdProducerIn);
        provider.getInFaultInterceptors().add(flowIdProducerIn);

        FlowIdProducerOut<Message> flowIdProducerOut = new FlowIdProducerOut<Message>();
        provider.getOutInterceptors().add(flowIdProducerOut);
        provider.getOutFaultInterceptors().add(flowIdProducerOut);

        WireTapIn wireTapIn = new WireTapIn(logMessageContent);
        provider.getInInterceptors().add(wireTapIn);
        provider.getInInterceptors().add(epi);
        provider.getInFaultInterceptors().add(epi);

        WireTapOut wireTapOut = new WireTapOut(epi, logMessageContent);
        provider.getOutInterceptors().add(wireTapOut);
        provider.getOutFaultInterceptors().add(wireTapOut);
    }

    /**
     * Sets the log message content.
     *
     * @param logMessageContent the new log message content
     */
    public void setLogMessageContent(boolean logMessageContent) {
        this.logMessageContent = logMessageContent;
    }

    /**
     * Sets the max content length for the message.
     *
     * @param maxContentLength the new max content length
     */
    public void setMaxContentLength(int maxContentLength) {
        this.maxContentLength = maxContentLength;
    }

    /**
     * Sets if enforce WS-Addressing MessageID transfer
     *
     * @param enforceMessageIDTransfer the new value of enforce MessageID Transfer
     */
    public void setEnforceMessageIDTransfer(boolean enforceMessageIDTransfer) {
		this.enforceMessageIDTransfer = enforceMessageIDTransfer;
	}

	/**
     * Sets the queue.
     *
     * @param queue the new queue
     */
    public void setQueue(Queue<Event> queue) {
        if (epi == null) {
            MessageToEventMapper mapper = new MessageToEventMapper();
            mapper.setMaxContentLength(maxContentLength);
            
            epi = new EventProducerInterceptor(mapper, queue);
        }
    }

    /**
     * Sets the handler.
     *
     * @param handler the new event handler
     */
    public void setHandler(EventHandler handler) {
        if (this.epi != null) {
            this.epi.setHandler(handler);
        }
    }

    /**
     * detect if WS Addressing feature already enabled.
     *
     * @param provider the interceptor provider
     * @param bus the bus
     * @return true, if successful
     */
    private boolean detectWSAddressingFeature(InterceptorProvider provider, Bus bus) {
        //detect on the bus level
        if (bus.getFeatures() != null) {
            Iterator<AbstractFeature> busFeatures = bus.getFeatures().iterator();
            while (busFeatures.hasNext()) {
                AbstractFeature busFeature = busFeatures.next();
                if (busFeature instanceof WSAddressingFeature) {
                    return true;
                }
            }
        }

        //detect on the endpoint/client level
        Iterator<Interceptor<? extends Message>> interceptors = provider.getInInterceptors().iterator();
        while (interceptors.hasNext()) {
            Interceptor<? extends Message> ic = interceptors.next();
            if (ic instanceof MAPAggregator) {
                return true;
            }
        }

        return false;
    }

    /**
     * Add WSAddressing Interceptors to InterceptorProvider, in order to using
     * AddressingProperties to get MessageID.
     *
     * @param provider the interceptor provider
     */
    private void addWSAddressingInterceptors(InterceptorProvider provider) {
        MAPAggregator mapAggregator = new MAPAggregator();
        MAPCodec mapCodec = new MAPCodec();

        provider.getInInterceptors().add(mapAggregator);
        provider.getInInterceptors().add(mapCodec);

        provider.getOutInterceptors().add(mapAggregator);
        provider.getOutInterceptors().add(mapCodec);

        provider.getInFaultInterceptors().add(mapAggregator);
        provider.getInFaultInterceptors().add(mapCodec);

        provider.getOutFaultInterceptors().add(mapAggregator);
        provider.getOutFaultInterceptors().add(mapCodec);
    }

}
