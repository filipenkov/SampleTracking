package com.atlassian.jira.bc.admin;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.properties.ApplicationPropertiesStore;
import com.atlassian.jira.event.config.ApplicationPropertyChangeEvent;
import com.atlassian.validation.Success;
import org.easymock.Capture;
import org.easymock.classextension.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.easymock.classextension.EasyMock.createMock;

/**
 * Unit test for {@link ApplicationPropertiesServiceImpl}.
 *
 * @since v4.4
 */
public class TestApplicationPropertiesServiceImpl
{
    private static final String NEW_VALUE = "newval";
    private static final String ORIGINAL_VALUE = "original value";
    private static final String KEY = "foo";

    @Test
    public void testSetApplicationProperty() {
        ApplicationPropertyMetadata fooMeta = createMock(ApplicationPropertyMetadata.class);
        EasyMock.expect(fooMeta.validate(NEW_VALUE)).andReturn(new Success(NEW_VALUE));
        ApplicationPropertiesStore store = EasyMock.createMock(ApplicationPropertiesStore.class);

        ApplicationProperty fooValue = new ApplicationProperty(fooMeta, ORIGINAL_VALUE);
        EasyMock.expect(store.getApplicationPropertyFromKey(KEY)).andReturn(fooValue);
        ApplicationProperty newFooValue = new ApplicationProperty(fooMeta, NEW_VALUE);
        EasyMock.expect(store.setApplicationProperty(KEY, NEW_VALUE)).andReturn(newFooValue);

        EventPublisher publisher = EasyMock.createMock(EventPublisher.class);
        Capture<Object> eventCapture = new Capture<Object>();
        publisher.publish(EasyMock.<Object>capture(eventCapture));
        EasyMock.expectLastCall();

        EasyMock.replay(fooMeta, store, publisher);

        ApplicationPropertiesServiceImpl service = new ApplicationPropertiesServiceImpl(store, publisher);

        // production call
        service.setApplicationProperty(KEY, NEW_VALUE);

        EasyMock.verify(fooMeta, store);
        Map<String,Object> params = ((ApplicationPropertyChangeEvent) eventCapture.getValue()).getParams();
        Assert.assertEquals(fooMeta, params.get(ApplicationPropertyChangeEvent.KEY_METADATA));
        Assert.assertEquals(ORIGINAL_VALUE, params.get(ApplicationPropertyChangeEvent.KEY_OLD_VALUE));
        Assert.assertEquals(NEW_VALUE, params.get(ApplicationPropertyChangeEvent.KEY_NEW_VALUE));
    }
}
