/*
 * #%L
 * Service Activity Monitoring :: Server
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
package org.talend.esb.sam.server.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.esb.sam.common.event.Event;
import org.talend.esb.sam.common.service.MonitoringService;
import org.talend.esb.sam.common.util.EventCreator;
import org.talend.esb.sam.server.persistence.EventRowMapper;

/**
 * Tests the sevice implementation together with the database
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/monitoringServiceTest.xml"})
public class MonitoringServiceImplTest extends AbstractTransactionalJUnit4SpringContextTests {
    @Resource
    MonitoringService monitoringSerivce;

    @Before
    public void setUp() throws Exception {
        executeSqlScript("create.sql", true);
    }
    
    @Test
    public void testWritingSeveralEvents() {
        List<Event> events = new ArrayList<Event>();
        events.add(new EventCreator().generateEvent());
        events.add(new EventCreator().generateEvent());
        events.add(new EventCreator().generateEvent());

        monitoringSerivce.putEvents(events);

        for (Event event : events) {
            EventRowMapper rowMapper = new EventRowMapper();
            Event loaded = simpleJdbcTemplate.queryForObject("select * from EVENTS where ID=?", rowMapper, event.getPersistedId());
            Assert.assertNotNull(loaded);
            Assert.assertEquals(event.getPersistedId(), loaded.getPersistedId());
        }

    }
    
    @After
    public void tearDown() {
        executeSqlScript("drop.sql", true);
    }

    
    //FIXME test filter
    
    //FIXME test handler
}
