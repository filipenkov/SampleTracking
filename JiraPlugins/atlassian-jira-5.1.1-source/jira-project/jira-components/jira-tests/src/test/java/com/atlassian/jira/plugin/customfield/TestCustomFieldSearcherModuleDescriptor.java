package com.atlassian.jira.plugin.customfield;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.impl.StaticPlugin;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestCustomFieldSearcherModuleDescriptor extends ListeningTestCase
{
    String singleCustomFieldXML = "<customfield-searcher key=\"daterange\" name=\"Date Range Searcher\"\n" +
            "        class=\"com.atlassian.jira.issue.customfields.searchers.DateRangeSearcher\">\n" +
            "        <description>Allow searching for a date which falls between to dates.</description>\n" +
            "        <resource type=\"velocity\" name=\"search\" location=\"templates/plugins/fields/search-userpicker.vm\" />\n" +
            "        <valid-customfield-type package=\"packagename\" key=\"key\" />\n" +
            "    </customfield-searcher>";

    String singleCustomFieldNoPackageXML = "<customfield-searcher key=\"daterange\" name=\"Date Range Searcher\"\n" +
            "        class=\"com.atlassian.jira.issue.customfields.searchers.DateRangeSearcher\">\n" +
            "        <description>Allow searching for a date which falls between to dates.</description>\n" +
            "        <resource type=\"velocity\" name=\"search\" location=\"templates/plugins/fields/search-userpicker.vm\" />\n" +
            "        <valid-customfield-type key=\"key\" />\n" +
            "    </customfield-searcher>";

    String multipleCustomFieldXML = "<customfield-searcher key=\"daterange\" name=\"Date Range Searcher\"\n" +
            "        class=\"com.atlassian.jira.issue.customfields.searchers.DateRangeSearcher\">\n" +
            "        <description>Allow searching for a date which falls between to dates.</description>\n" +
            "        <resource type=\"velocity\" name=\"search\" location=\"templates/plugins/fields/search-userpicker.vm\" />\n" +
            "        <valid-customfield-type package=\"packagename\" key=\"key\" />\n" +
            "        <valid-customfield-type package=\"packagename2\" key=\"key2\" />\n" +
            "    </customfield-searcher>";


    private Document document;
    private CustomFieldSearcherModuleDescriptor smd = new CustomFieldSearcherModuleDescriptorImpl(null, null, null);
    private Plugin plugin = new StaticPlugin();


    @Test
    public void testGetSingleCustomFieldTypes() throws PluginParseException, DocumentException
    {
        document = DocumentHelper.parseText(singleCustomFieldXML);
        smd.init(plugin, document.getRootElement());
        assertEquals("packagename:key", smd.getValidCustomFieldKeys().iterator().next());
    }

    @Test
    public void testGetSingleCustomFieldTypesUsesDefaultPackage() throws PluginParseException, DocumentException
    {
        document = DocumentHelper.parseText(singleCustomFieldNoPackageXML);
        plugin.setKey("defaultpackage");
        smd.init(plugin, document.getRootElement());
        assertEquals("defaultpackage:key", smd.getValidCustomFieldKeys().iterator().next());
    }

    @Test
    public void testGetMultipleCustomFieldTypes() throws PluginParseException, DocumentException
    {
        document = DocumentHelper.parseText(multipleCustomFieldXML);
        smd.init(plugin, document.getRootElement());
        assertEquals(2, smd.getValidCustomFieldKeys().size());
        assertTrue(smd.getValidCustomFieldKeys().contains("packagename:key"));
        assertTrue(smd.getValidCustomFieldKeys().contains("packagename2:key2"));
    }
}
