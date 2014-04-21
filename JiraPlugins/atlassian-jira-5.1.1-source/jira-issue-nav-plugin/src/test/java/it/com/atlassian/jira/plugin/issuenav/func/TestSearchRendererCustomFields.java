package it.com.atlassian.jira.plugin.issuenav.func;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.plugin.issuenav.client.SearchRendererClient;
import com.atlassian.jira.plugin.issuenav.service.SearchRendererValue;
import com.atlassian.jira.plugin.issuenav.service.SearchRendererValueResults;
import com.atlassian.jira.plugin.issuenav.util.EditHtmlUtils;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.RestFuncTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Response;


/**
 * Test security and response for the search renderer resource
 *
 * @since v5.1
 */
@WebTest ({ Category.FUNC_TEST })
public class TestSearchRendererCustomFields extends RestFuncTest
{
    private static final String SEARCHER_NAME = "<iframe src=\\\"http://www.atlassian.com\\\"></iframe>";

    private SearchRendererClient client;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        client = new SearchRendererClient(getEnvironmentData());
        administration.restoreData("TestClauseLozenges.xml");
    }

    /**
     * Tests value response form custom fields
     */
    public void testValueCustomFields()
    {
        SearchRendererValueResults values = client.getValue("customfield_10110", "i'm a barbie girl, in a barbie world.");

        assertEquals(1, values.size());

        SearchRendererValue customField = values.get("customfield_10110");
        assertEquals("\"Champ de texte\" ~ \"i'm a barbie girl, in a barbie world.\"", customField.jql);
        assertNotNull(customField);
        assertTrue(customField.validSearcher);
        EditHtmlUtils.assertTextFieldValues(customField.editHtml, "customfield_10110", "i'm a barbie girl, in a barbie world.");
    }

    /**
     * Tests value response form custom fields with a context
     */
    public void testValueCustomFieldsWithContext()
    {
        SearchRendererValueResults values = client.getValue("pid", "10010", "customfield_10071", "i'm a barbie girl, in a barbie world.");

        assertEquals(2, values.size());

        SearchRendererValue customField = values.get("customfield_10071");
        assertNotNull(customField);
        assertTrue(customField.validSearcher);
        assertEquals("\"" + SEARCHER_NAME + "\" ~ \"i'm a barbie girl, in a barbie world.\"", customField.jql);
        assertNotNull(customField.editHtml);
        EditHtmlUtils.assertTextFieldValues(customField.editHtml, "customfield_10071", "i'm a barbie girl, in a barbie world.");
    }

    public void testValueCustomFieldsFromJqlWhenInvalid()
    {
        SearchRendererValueResults values = client.getValue("__jql_customfield_10071", "\"" + SEARCHER_NAME + "\" ~ \"i'm a barbie girl, in a barbie world.\"");

        assertEquals(1, values.size());

        SearchRendererValue customField = values.get("customfield_10071");
        assertNotNull(customField);
        assertFalse(customField.validSearcher);
        assertEquals("\"" + SEARCHER_NAME + "\" ~ \"i'm a barbie girl, in a barbie world.\"", customField.jql);
        assertNull(customField.editHtml);
    }

    public void testValueCustomFieldsFromJqlWhenValid()
    {
        SearchRendererValueResults values = client.getValue("pid", "10010", "__jql_customfield_10071", "\"" + SEARCHER_NAME + "\" ~ \"i'm a barbie girl, in a barbie world.\"");

        assertEquals(2, values.size());

        SearchRendererValue customField = values.get("customfield_10071");
        assertNotNull(customField);
        assertTrue(customField.validSearcher);
        assertEquals("\"" + SEARCHER_NAME + "\" ~ \"i'm a barbie girl, in a barbie world.\"", customField.jql);
        EditHtmlUtils.assertTextFieldValues(customField.editHtml, "customfield_10071", "i'm a barbie girl, in a barbie world.");
    }

    public void testValueCustomFieldsFromInvalidJql()
    {
        Response response = client.getValueResponse("__jql_customfield_10071", "but this is not jql!");

        assertEquals(400, response.statusCode);
        assertEquals(1, response.entity.errorMessages.size());
    }
}
