package com.atlassian.jira.bc.whitelist;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.gadgets.event.AddGadgetEvent;
import com.atlassian.gadgets.event.ClearHttpCacheEvent;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.collect.CollectionBuilder;
import org.junit.Test;

import java.net.URI;
import java.util.List;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestDefaultWhitelistManager
{
    @Test
    public void testGetRules()
    {
        final ApplicationProperties mockApplicationProperties = createMock(ApplicationProperties.class);

        expect(mockApplicationProperties.getOption("jira.whitelist.disabled")).andReturn(false);
        expect(mockApplicationProperties.getText("jira.whitelist.rules")).andReturn("http://www.atlassian.com/*\nhttp://www.google.com\n=http://www.twitter.com");

        replay(mockApplicationProperties);
        DefaultWhitelistManager manager = new DefaultWhitelistManager(mockApplicationProperties, null);

        final List<String> rules = manager.getRules();
        assertEquals(CollectionBuilder.list("http://www.atlassian.com/*", "http://www.google.com", "=http://www.twitter.com"), rules);

        verify(mockApplicationProperties);
    }

    @Test
    public void testGetRulesDisabled()
    {
        final ApplicationProperties mockApplicationProperties = createMock(ApplicationProperties.class);

        expect(mockApplicationProperties.getOption("jira.whitelist.disabled")).andReturn(true);

        replay(mockApplicationProperties);
        DefaultWhitelistManager manager = new DefaultWhitelistManager(mockApplicationProperties, null);

        final List<String> rules = manager.getRules();
        assertTrue(rules.isEmpty());

        verify(mockApplicationProperties);
    }
    
    @Test
    public void testUpdateRules()
    {
        final ApplicationProperties mockApplicationProperties = createMock(ApplicationProperties.class);
        final EventPublisher mockEventPublisher = createMock(EventPublisher.class);

        mockApplicationProperties.setText("jira.whitelist.rules", "http://www.atlassian.com/*\nhttp://www.google.com\n=http://www.twitter.com\n");
        mockApplicationProperties.setOption("jira.whitelist.disabled", false);

        expect(mockApplicationProperties.getOption("jira.whitelist.disabled")).andReturn(false);
        expect(mockApplicationProperties.getText("jira.whitelist.rules")).andReturn("http://www.atlassian.com/*\nhttp://www.google.com\n=http://www.twitter.com\n");
        mockEventPublisher.publish(ClearHttpCacheEvent.INSTANCE);

        replay(mockApplicationProperties, mockEventPublisher);
        DefaultWhitelistManager manager = new DefaultWhitelistManager(mockApplicationProperties, mockEventPublisher);

        final List<String> rules = manager.updateRules(CollectionBuilder.list("http://www.atlassian.com/*", "http://www.google.com", "=http://www.twitter.com"), false);
        assertEquals(CollectionBuilder.list("http://www.atlassian.com/*", "http://www.google.com", "=http://www.twitter.com"), rules);

        verify(mockApplicationProperties, mockEventPublisher);
    }

    @Test
    public void testAddGadgetEvent()
    {
        final ApplicationProperties mockApplicationProperties = createMock(ApplicationProperties.class);
        final EventPublisher mockEventPublisher = createMock(EventPublisher.class);

        expect(mockApplicationProperties.getOption("jira.whitelist.disabled")).andReturn(false).anyTimes();
        expect(mockApplicationProperties.getText("jira.whitelist.rules")).andReturn("http://www.atlassian.com/*\nhttp://www.google.com\n=http://www.twitter.com\n");

        mockApplicationProperties.setText("jira.whitelist.rules", "http://www.atlassian.com/*\nhttp://www.google.com\n=http://www.twitter.com\nhttp://extranet.atlassian.com/*\n");
        mockApplicationProperties.setOption("jira.whitelist.disabled", false);

        mockEventPublisher.publish(ClearHttpCacheEvent.INSTANCE);
        expect(mockApplicationProperties.getText("jira.whitelist.rules")).andReturn("http://www.atlassian.com/*\nhttp://www.google.com\n=http://www.twitter.com\nhttp://extranet.atlassian.com/*\n");

        replay(mockApplicationProperties, mockEventPublisher);
        DefaultWhitelistManager manager = new DefaultWhitelistManager(mockApplicationProperties, mockEventPublisher);

        manager.onAddGadget(new AddGadgetEvent(URI.create("http://extranet.atlassian.com/gadgets/jira/somegadget.xml")));

        verify(mockApplicationProperties, mockEventPublisher);
    }

    @Test
    public void testIsAllowedDisabled()
    {
         final ApplicationProperties mockApplicationProperties = createMock(ApplicationProperties.class);

        expect(mockApplicationProperties.getOption("jira.whitelist.disabled")).andReturn(true);

        replay(mockApplicationProperties);
        DefaultWhitelistManager manager = new DefaultWhitelistManager(mockApplicationProperties, null);

        assertTrue(manager.isAllowed(URI.create("http://www.google.com")));

        verify(mockApplicationProperties);
    }
    
    @Test
    public void testIsAllowed()
    {
         final ApplicationProperties mockApplicationProperties = createMock(ApplicationProperties.class);

        expect(mockApplicationProperties.getOption("jira.whitelist.disabled")).andReturn(false).anyTimes();
        expect(mockApplicationProperties.getText("jira.whitelist.rules")).andReturn(
                "http://www.atlassian.com*\n"
                + "http://www.someurl.com/*\n"
                + "http://www.google.com\n"
                + "=http://www.twitter.com\n"
                + "/.*gogogadget.*");

        replay(mockApplicationProperties);
        DefaultWhitelistManager manager = new DefaultWhitelistManager(mockApplicationProperties, null);

        assertTrue(manager.isAllowed(URI.create("http://www.atlassian.com/")));
        assertTrue(manager.isAllowed(URI.create("http://www.atlassian.com/rest/something")));
        assertTrue(manager.isAllowed(URI.create("http://www.someurl.com/")));
        assertFalse(manager.isAllowed(URI.create("http://www.someurl.com")));
        assertTrue(manager.isAllowed(URI.create("http://www.google.com")));
        assertFalse(manager.isAllowed(URI.create("http://www.google.com/rest/something")));
        assertTrue(manager.isAllowed(URI.create("http://www.twitter.com")));
        assertFalse(manager.isAllowed(URI.create("http://www.twitter.com/not")));
        assertTrue(manager.isAllowed(URI.create("https://ww.gogogadget.com/asdfasdfasf")));

        verify(mockApplicationProperties);
    }
}
