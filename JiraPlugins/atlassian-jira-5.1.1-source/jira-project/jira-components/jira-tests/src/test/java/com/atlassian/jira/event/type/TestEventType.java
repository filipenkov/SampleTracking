package com.atlassian.jira.event.type;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestEventType extends ListeningTestCase
{
    @Test
    public void testGetNameKey() throws Exception
    {
        assertEquals("event.type.dude.name", new EventType("Dude", "test", 2L).getNameKey());
        assertEquals("event.type.myname.name", new EventType("My Name", "test", 2L).getNameKey());
        assertEquals("event.type.mynameagain.name", new EventType("My Name Again", "test", 2L).getNameKey());
        assertEquals("event.type.thisismyname.name", new EventType("  this is  MY   NAME   ", "test", 2L).getNameKey());
    }

    @Test
    public void testGetDescKey() throws Exception
    {
        assertEquals("event.type.foo.desc", new EventType("Foo", "test", 2L).getDescKey());
        assertEquals("event.type.foobar.desc", new EventType("Foo Bar", "test", 2L).getDescKey());
        assertEquals("event.type.foobarbaz.desc", new EventType("Foo Bar Baz", "test", 2L).getDescKey());
        assertEquals("event.type.foobarbaz.desc", new EventType(" Foo Bar    Baz  ", "test", 2L).getDescKey());
    }
}
