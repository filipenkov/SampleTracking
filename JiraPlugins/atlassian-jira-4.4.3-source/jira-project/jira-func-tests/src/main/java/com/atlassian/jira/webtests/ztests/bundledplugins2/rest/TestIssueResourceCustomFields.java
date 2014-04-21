package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Issue;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueClient;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Func tests for custom fields.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceCustomFields extends RestFuncTest
{
    private IssueClient issueClient;

    public void testTextField() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");

        Issue issue = issueClient.get("HSP-1");
        Issue.IssueField<String> textField = issue.fields.get("customfield_10021");

        assertNotNull(textField);
        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:textfield", textField.type);
        assertEquals("this is text", textField.value);
    }

    public void testTextArea() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");

        Issue issue = issueClient.get("HSP-1");
        Issue.IssueField<String> textArea = issue.fields.get("customfield_10013");

        assertNotNull(textArea);
        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:textarea", textArea.type);
        assertEquals("lots of text here, brother!", textArea.value);
    }

    public void testDatePicker() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");

        Issue issue = issueClient.get("HSP-1");
        Issue.IssueField<String> datePickerCF = issue.fields.get("customfield_10012");

        assertNotNull(datePickerCF);
        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:datepicker", datePickerCF.type);
        assertEquals("2010-06-23", datePickerCF.value);
    }

    public void testDateTime() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue.IssueField<String> dateTimeCF = issueClient.get("HSP-1").fields.get("customfield_10001");

        assertNotNull(dateTimeCF);
        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:datetime", dateTimeCF.type);
        assertEqualDateStrings("2010-06-16T15:26:00.000+1000", dateTimeCF.value);
    }

    public void testFloat() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");

        Issue issue = issueClient.get("HSP-1");
        Issue.IssueField<Double> floatCF = issue.fields.get("customfield_10018");

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:float", floatCF.type);
        assertEquals(42.0, floatCF.value);
    }

    public void testImportId() throws Exception
    {
        administration.restoreData("TestIssueLinkCheck.xml");

        Issue issue = issueClient.get("ANOT-1");
        Issue.IssueField<Double> bugzillaId = issue.fields.get("customfield_10000");

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:importid", bugzillaId.type);
        assertEquals(2.0, bugzillaId.value);
    }

    public void testSelect() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue.IssueField<Map<String, String>> selectList = issueClient.get("HSP-1").fields.get("customfield_10020");

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:select", selectList.type);
        Map<String, String> options = selectList.value;
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/customFieldOption/10011", options.get("self"));
        assertEquals("Select!", options.get("value"));
    }

    public void testRadioButtons() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue.IssueField<Map<String, String>> radioButtons = issueClient.get("HSP-1").fields.get("customfield_10019");

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:radiobuttons", radioButtons.type);
        Map<String, String> options = radioButtons.value;
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/customFieldOption/10010", options.get("self"));
        assertEquals("Radio Ga Ga", options.get("value"));
    }

    public void testProject() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue.IssueField<Map<String, Object>> projectPicker = issueClient.get("HSP-1").fields.get("customfield_10007");

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:project", projectPicker.type);
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/project/MKY", projectPicker.value.get("self"));
        assertEquals("MKY", projectPicker.value.get("key"));
    }

    public void testMultiVersion() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue.IssueField<List<Map<String, String>>> multiVersion = issueClient.get("HSP-1").fields.get("customfield_10011");
        List<Map<String, String>> versions = multiVersion.value;

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:multiversion", multiVersion.type);
        assertEquals(2, versions.size());
        Map<String, String> v1 = versions.get(0);
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/version/10000", v1.get("self"));
        assertEquals("New Version 1", v1.get("name"));
        Map<String, String> v5 = versions.get(1);
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/version/10002", v5.get("self"));
        assertEquals("New Version 5", v5.get("name"));
    }

    public void testVersion() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue.IssueField<Map<String, String>> version = issueClient.get("HSP-1").fields.get("customfield_10009");
        Map<String, String> v4 = version.value;

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:version", version.type);
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/version/10001", v4.get("self"));
        assertEquals("New Version 4", v4.get("name"));
    }

    public void testUserPicker() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue.IssueField<Map<String, String>> userPicker = issueClient.get("HSP-1").fields.get("customfield_10022");

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:userpicker", userPicker.type);
        Map<String, String> fred = userPicker.value;
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/user?username=fred", fred.get("self"));
        assertEquals(FRED_USERNAME, fred.get("name"));
        assertEquals(FRED_FULLNAME, fred.get("displayName"));
    }

    public void testUrl() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue.IssueField<String> url = issueClient.get("HSP-1").fields.get("customfield_10010");

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:url", url.type);
        assertEquals("http://www.atlassian.com", url.value);
    }

    public void testMultiSelect() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue.IssueField<List<Map<String, String>>> multiSelect = issueClient.get("HSP-1").fields.get("customfield_10017");

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:multiselect", multiSelect.type);
        assertEquals(2, multiSelect.value.size());

        boolean option2Present = false;
        boolean option3Present = false;
        List<String> expectedValues = EasyList.build("Option 2", "Option 3");
        for (Map<String, String> option : multiSelect.value)
        {
            assertTrue(expectedValues.contains(option.get("value")));
            if (option.get("value").equals("Option 2"))
            {
                option2Present = true;
                assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/customFieldOption/10007", option.get("self"));
            }
            if (option.get("value").equals("Option 3"))
            {
                option3Present = true;
                assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/customFieldOption/10008", option.get("self"));
            }
        }
        assertTrue((option2Present && option3Present));
    }

    public void testMultiCheckboxes() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue.IssueField<List<Map<String, String>>> multiCheckbox = issueClient.get("HSP-1").fields.get("customfield_10016");

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:multicheckboxes", multiCheckbox.type);
        assertEquals(1, multiCheckbox.value.size());
        Map<String, String> option1 = multiCheckbox.value.get(0);
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/customFieldOption/10014", option1.get("self"));
        assertEquals("check out my stats", option1.get("value"));
    }

    public void testMultiUserPicker() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue.IssueField<List<Map<String, String>>> multiUser = issueClient.get("HSP-1").fields.get("customfield_10006");
        List<Map<String, String>> users = multiUser.value;

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:multiuserpicker", multiUser.type);
        assertEquals(2, users.size());

        Map<String, String> admin = users.get(0);
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/user?username=admin", admin.get("self"));
        assertEquals(ADMIN_USERNAME, admin.get("name"));
        assertEquals(ADMIN_FULLNAME, admin.get("displayName"));

        Map<String, String> fred = users.get(1);
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/user?username=fred", fred.get("self"));
        assertEquals(FRED_USERNAME, fred.get("name"));
        assertEquals(FRED_FULLNAME, fred.get("displayName"));
    }

    public void testMultiGroupPicker() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue.IssueField<List<String>> multiGroup = issueClient.get("HSP-1").fields.get("customfield_10005");

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:multigrouppicker", multiGroup.type);
        assertEquals("jira-developers", multiGroup.value.get(0));
        assertEquals("jira-users", multiGroup.value.get(1));
    }

    public void testGroupPicker() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue.IssueField<String> group = issueClient.get("HSP-1").fields.get("customfield_10002");
        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:grouppicker", group.type);
        assertEquals("jira-developers", group.value);
    }

    public void testCascadingSelect() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue.IssueField<List<String>> cascadingSelect = issueClient.get("HSP-1").fields.get("customfield_10000");

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect", cascadingSelect.type);
        assertEquals(Arrays.asList("Option 2", "Sub-option I"), cascadingSelect.value);
    }

    public void testLabels() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue.IssueField<List<String>> labels = issueClient.get("HSP-1").fields.get("customfield_10004");

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:labels", labels.type);
        assertEquals(Arrays.asList("wack", "whoa"), labels.value);
    }

    public void testEmptyFieldsReturned() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        navigation.issue().createIssue("homosapien", "Bug", "some issue");

        Issue issue = issueClient.get("HSP-2");
        Issue.Fields fields = issue.fields;
        assertTrue(fields.has("customfield_10000"));
        assertNull(fields.get("customfield_10000").value);
        assertTrue(fields.has("customfield_10012"));
        assertNull(fields.get("customfield_10012").value);
        assertTrue(fields.has("customfield_10001"));
        assertNull(fields.get("customfield_10001").value);
        assertTrue(fields.has("customfield_10013"));
        assertNull(fields.get("customfield_10013").value);
        assertTrue(fields.has("customfield_10002"));
        assertNull(fields.get("customfield_10002").value);
        assertTrue(fields.has("customfield_10003"));
        assertNull(fields.get("customfield_10003").value);
        assertTrue(fields.has("customfield_10016"));
        assertNull(fields.get("customfield_10016").value);
        assertTrue(fields.has("customfield_10017"));
        assertNull(fields.get("customfield_10017").value);
        assertTrue(fields.has("customfield_10005"));
        assertNull(fields.get("customfield_10005").value);
        assertTrue(fields.has("customfield_10006"));
        assertNull(fields.get("customfield_10006").value);
        assertTrue(fields.has("customfield_10018"));
        assertNull(fields.get("customfield_10018").value);
        assertTrue(fields.has("customfield_10007"));
        assertNull(fields.get("customfield_10007").value);
        assertTrue(fields.has("customfield_10019"));
        assertNull(fields.get("customfield_10019").value);
        assertTrue(fields.has("customfield_10008"));
        assertNull(fields.get("customfield_10008").value);
        assertTrue(fields.has("customfield_10020"));
        assertNull(fields.get("customfield_10020").value);
        assertTrue(fields.has("customfield_10009"));
        assertNull(fields.get("customfield_10009").value);
        assertTrue(fields.has("customfield_10021"));
        assertNull(fields.get("customfield_10021").value);
        assertTrue(fields.has("customfield_10010"));
        assertNull(fields.get("customfield_10010").value);
        assertTrue(fields.has("customfield_10022"));
        assertNull(fields.get("customfield_10022").value);
        assertTrue(fields.has("customfield_10011"));
        assertNull(fields.get("customfield_10011").value);
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
    }
}
