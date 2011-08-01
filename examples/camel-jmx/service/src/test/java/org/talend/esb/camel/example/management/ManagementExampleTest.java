/*
 * #%L
 * Camel :: Example :: Management :: TESB container
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
 * 
 * This project is based on  the standard Apache Camel camel-example-management
 * #L%
 */

package org.talend.esb.camel.example.management;

import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.camel.test.junit4.CamelSpringTestSupport;
import org.junit.Test;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @version 
 */
public class ManagementExampleTest extends CamelSpringTestSupport {

    protected AbstractXmlApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("META-INF/spring/camel-context.xml");
    }

    @Test
    public void testManagementExample() throws Exception {
        // Give it a bit of time to run
        Thread.sleep(2000);

        MBeanServer mbeanServer = context.getManagementStrategy().getManagementAgent().getMBeanServer();

        // Find the endpoints
        Set<ObjectName> set = mbeanServer.queryNames(new ObjectName("*:type=endpoints,*"), null);
        // now there is no managed endpoint for the dead queue
        assertEquals(5, set.size()); 
        
        // Find the routes
        set = mbeanServer.queryNames(new ObjectName("*:type=routes,*"), null);
        assertEquals(3, set.size());
        
        // Stop routes
        for (ObjectName on : set) {
            mbeanServer.invoke(on, "stop", null, null);
        }
    }

}
