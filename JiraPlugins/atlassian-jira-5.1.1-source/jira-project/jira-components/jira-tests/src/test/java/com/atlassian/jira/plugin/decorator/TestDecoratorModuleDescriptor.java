package com.atlassian.jira.plugin.decorator;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;
import org.dom4j.DocumentHelper;
import org.dom4j.Document;
import com.atlassian.plugin.impl.StaticPlugin;
import com.atlassian.plugin.PluginParseException;

public class TestDecoratorModuleDescriptor extends ListeningTestCase
{
    private DecoratorModuleDescriptor desc;

    @Before
    public void setUp() throws Exception
    {
        desc = new DecoratorModuleDescriptor();
    }

    @Test
    public void testParseDecorator() throws Exception
    {
        parse("<decorator key=\"key\" page=\"/templates/page.vmd\"><pattern>/path/*</pattern></decorator>");
        assertEquals("/templates/page.vmd", desc.getPage());
        assertTrue(desc.getPattern().matcher("/path/servlet").matches());
        assertFalse(desc.getPattern().matcher("/other/servlet").matches());
    }

    @Test
    public void testParseDecoratorNoPage() throws Exception
    {
        try
        {
            parse("<decorator key=\"key\"><pattern>/path/*</pattern></decorator>");
            fail("Exception not thrown when no page attribute set");
        }
        catch (PluginParseException ppe)
        {
            // Pass
        }
    }

    @Test
    public void testParseDecoratorNoPattern() throws Exception
    {
        // You may just want a named decorator that can be used when invoking sitemesh directly
        parse("<decorator key=\"key\" page=\"/templates/page.vmd\"></decorator>");
        assertEquals("/templates/page.vmd", desc.getPage());
        assertNull(desc.getPattern());        
    }

    @Test
    public void testParseDecoratorTwoPatterns() throws Exception
    {
        try
        {
            parse("<decorator key=\"key\" page=\"/templates/page.vmd\"><pattern>/path/*</pattern>" +
                "<pattern>/other/*</pattern></decorator>");
            fail("Exception not thrown when two pattern elements exist");
        }
        catch (PluginParseException ppe)
        {
            // Pass
        }
    }

    @Test
    public void testParseDecoratorBadPattern() throws Exception
    {
        try
        {
            parse("<decorator key=\"key\" page=\"/templates/page.vmd\"><pattern>(</pattern></decorator>");
            fail("Exception not thrown when the pattern can't be parsed");
        }
        catch (PluginParseException ppe)
        {
            // Pass
        }
    }

    private void parse(String xml) throws Exception
    {
        Document document = DocumentHelper.parseText(xml);
        desc.init(new StaticPlugin(), document.getRootElement());
    }

}
