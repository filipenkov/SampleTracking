package com.atlassian.jira.web.action.admin.subtasks;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.core.util.map.EasyMap;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.io.IOException;
import java.util.Map;

public class TestEditSubTaskIssueTypes extends LegacyJiraMockTestCase
{
    EditSubTaskIssueTypes estit;
    private Mock mockConstantMaanger;
    private Mock mockSubTaskManager;
    private String id;
    private String iconurl;
    private String name;
    private Long sequence;
    private String description;

    public TestEditSubTaskIssueTypes(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        mockConstantMaanger = new Mock(ConstantsManager.class);
        mockConstantMaanger.setStrict(true);

        mockSubTaskManager = new Mock(SubTaskManager.class);
        mockSubTaskManager.setStrict(true);

        estit = new EditSubTaskIssueTypes((SubTaskManager) mockSubTaskManager.proxy(), (ConstantsManager) mockConstantMaanger.proxy());

        id = "1";
        name = "test name";
        sequence = new Long(0);
        description = "test description";
        iconurl = "test iconurl";
    }

    public void testGettersSetters()
    {
        estit.setId(id);
        estit.setName(name);
        estit.setSequence(sequence);
        estit.setDescription(description);
        estit.setIconurl(iconurl);

        assertEquals(id, estit.getId());
        assertEquals(name, estit.getName());
        assertEquals(sequence, estit.getSequence());
        assertEquals(description, estit.getDescription());
        assertEquals(iconurl, estit.getIconurl());
    }


    public void testDoDefaultSubTasksDisabled() throws Exception
    {
        mockSubTaskManager.expectAndReturn("isSubTasksEnabled", Boolean.FALSE);

        final String result = estit.doDefault();
        assertEquals(Action.ERROR, result);
        checkSingleElementCollection(estit.getErrorMessages(), "Sub-Tasks are disabled.");

        mockSubTaskManager.verify();
    }

    public void testDoDefaultNoId() throws Exception
    {
        mockSubTaskManager.expectAndReturn("isSubTasksEnabled", Boolean.TRUE);

        final String result = estit.doDefault();
        assertEquals(Action.ERROR, result);
        checkSingleElementCollection(estit.getErrorMessages(), "No id set.");

        mockSubTaskManager.verify();
    }

    public void testDoDefault() throws Exception
    {
        final GenericValue issueTypeGV = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", id, "name", name, "sequence", sequence, "description", description, "iconurl", iconurl));

        mockSubTaskManager.expectAndReturn("isSubTasksEnabled", Boolean.TRUE);
        mockSubTaskManager.expectAndReturn("getSubTaskIssueTypeById", P.args(new IsEqual(id)), issueTypeGV);

        estit.setId(id);
        final String result = estit.doDefault();
        assertEquals(Action.INPUT, result);

        assertEquals(id, estit.getId());
        assertEquals(name, estit.getName());
        assertEquals(sequence, estit.getSequence());
        assertEquals(description, estit.getDescription());
        assertEquals(iconurl, estit.getIconurl());

        mockSubTaskManager.verify();
    }

    public void testDoValidationSubTasksDisabled() throws Exception
    {
        mockSubTaskManager.expectAndReturn("isSubTasksEnabled", Boolean.FALSE);

        final String result = estit.execute();
        assertEquals(Action.INPUT, result);
        checkSingleElementCollection(estit.getErrorMessages(), "Sub-Tasks are disabled.");

        mockSubTaskManager.verify();
    }

    public void testDoValidationNoId() throws Exception
    {
        mockSubTaskManager.expectAndReturn("isSubTasksEnabled", Boolean.TRUE);

        final String result = estit.execute();
        assertEquals(Action.INPUT, result);
        checkSingleElementCollection(estit.getErrorMessages(), "No id set.");

        mockSubTaskManager.verify();
    }

    public void testDoValidationNoNameNoIconUrl() throws Exception
    {
        mockSubTaskManager.expectAndReturn("isSubTasksEnabled", Boolean.TRUE);

        estit.setId(id);
        final String result = estit.execute();
        assertEquals(Action.INPUT, result);

        final Map errorMessages = estit.getErrors();
        assertNotNull(errorMessages);
        assertEquals(2, errorMessages.size());
        assertEquals("You must specify a name for this new sub-task issue type.", errorMessages.get("name"));
        assertEquals("You must specify a URL for the icon of this new issue type.", errorMessages.get("iconurl"));

        mockSubTaskManager.verify();
    }

    public void testDoValidationDuplicateName() throws Exception
    {
        final String anotherId = "2";
        final String anotherName = "another name";
        final IssueType issueType = new MockIssueType(anotherId, anotherName);

        mockSubTaskManager.expectAndReturn("isSubTasksEnabled", Boolean.TRUE);
        mockConstantMaanger.expectAndReturn("getIssueConstantByName", P.args(new IsEqual("IssueType"), new IsEqual(anotherName)), issueType);

        estit.setId(id);
        estit.setName(anotherName);
        estit.setIconurl(iconurl);

        final String result = estit.execute();
        assertEquals(Action.INPUT, result);

        final Map errorMessages = estit.getErrors();
        assertNotNull(errorMessages);
        assertEquals(1, errorMessages.size());
        assertEquals("An issue type with this name already exists.", errorMessages.get("name"));

        mockSubTaskManager.verify();
        mockConstantMaanger.verify();
    }

    public void testDoValidationSameName() throws Exception
    {
        final MockHttpServletResponse mockHttpServletResponse = setupRedirectResponse();

        final IssueType issueType = new MockIssueType(id, name);

        mockSubTaskManager.expectAndReturn("isSubTasksEnabled", Boolean.TRUE);
        mockSubTaskManager.expectVoid("updateSubTaskIssueType", new Constraint[]{new IsEqual(id), new IsEqual(name), new IsEqual(sequence), new IsEqual(description), new IsEqual(iconurl)});
        mockConstantMaanger.expectAndReturn("getIssueConstantByName", P.args(new IsEqual("IssueType"), new IsEqual(name)), issueType);

        estit.setId(id);
        estit.setName(name);
        estit.setSequence(sequence);
        estit.setDescription(description);
        estit.setIconurl(iconurl);

        final String result = estit.execute();
        assertEquals(Action.NONE, result);

        mockHttpServletResponse.verify();
        mockSubTaskManager.verify();
        mockConstantMaanger.verify();
    }

    private MockHttpServletResponse setupRedirectResponse() throws IOException
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ManageSubTasks.jspa");
        return response;
    }
}
