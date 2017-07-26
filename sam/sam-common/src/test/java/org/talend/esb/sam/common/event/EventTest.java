package org.talend.esb.sam.common.event;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class EventTest {

    @Test
    public void testEvent() {
        Event event = new Event();
        Long id = new Long(5);

        event.setPersistedId(id);
        Assert.assertEquals(id, event.getPersistedId());

        Date date = new Date();
        event.setTimestamp(date);
        Assert.assertEquals(date, event.getTimestamp());

        event.setContentCut(true);
        Assert.assertTrue(event.isContentCut());

        Originator originator = new Originator("process_id", "127.0.0.0.1", "localhost", "custom_id", "principal");
        event.setOriginator(originator);
        Assert.assertEquals(originator, event.getOriginator());

        Assert.assertTrue(event.toString().endsWith("process_id,127.0.0.0.1,localhost,custom_id,principal,<null>,true,<null>,{}"));
    }
}
