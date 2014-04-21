package com.atlassian.renderer.v2.components;

import junit.framework.TestCase;

public class TestHtmlEscaper extends TestCase
{
    public void testEscapeEverything()
    {
        assertEquals("&amp;&lt;&gt;&quot;&#39;", HtmlEscaper.escapeAll("&<>\"'", false));
        assertEquals("Bonnie &amp; Clyde", HtmlEscaper.escapeAll("Bonnie & Clyde", false));
        assertEquals("My name is &quot;Andrew&quot;", HtmlEscaper.escapeAll("My name is \"Andrew\"", false));
        assertEquals("I don&#39;t want to replace existing &amp; well-known entities like &amp;lt;",
            HtmlEscaper.escapeAll("I don't want to replace existing & well-known entities like &lt;", false));
        assertNull(HtmlEscaper.escapeAll(null, false));
    }

    public void testEscapeExceptQuotes()
    {
        assertEquals("&amp;&lt;&gt;\"'", HtmlEscaper.escapeAllExceptQuotes("&<>\"'", false));
        assertEquals("Bonnie &amp; Clyde", HtmlEscaper.escapeAllExceptQuotes("Bonnie & Clyde", false));
        assertEquals("My name is \"Andrew\"", HtmlEscaper.escapeAllExceptQuotes("My name is \"Andrew\"", false));
        assertEquals("I don't want to replace existing &amp; well-known entities like &amp;lt;",
            HtmlEscaper.escapeAllExceptQuotes("I don't want to replace existing & well-known entities like &lt;", false));
        assertNull(HtmlEscaper.escapeAllExceptQuotes(null, false));
    }

    public void testPreserveExistingEntities()
    {
        assertNull(HtmlEscaper.escapeAll(null, true));
        assertEquals("I don&#39;t want to replace existing &amp; well-known entities like &lt;",
            HtmlEscaper.escapeAll("I don't want to replace existing & well-known entities like &lt;", true));
        assertEquals("Non-escaped entity at the end &lt;",
            HtmlEscaper.escapeAll("Non-escaped entity at the end &lt;", true));
        assertEquals("&#2056; Non-escaped entity at the start",
            HtmlEscaper.escapeAll("&#2056; Non-escaped entity at the start", true));
        assertEquals("Non-escaped entity &quot; in the middle",
            HtmlEscaper.escapeAll("Non-escaped entity &quot; in the middle", true));
        assertEquals("Non-escaped entity &amp;quot missing semi-colon",
            HtmlEscaper.escapeAll("Non-escaped entity &quot missing semi-colon", true));
    }

    public void testPreserveExistingEntitiesMaximumEightCharacters()
    {
        assertEquals("This will not be recognised as an entity: &amp;#12345678;",
            HtmlEscaper.escapeAll("This will not be recognised as an entity: &#12345678;", true));
    }

}
