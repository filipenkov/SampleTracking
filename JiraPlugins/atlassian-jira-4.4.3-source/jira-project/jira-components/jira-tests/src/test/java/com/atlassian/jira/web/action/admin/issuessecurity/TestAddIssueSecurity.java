package com.atlassian.jira.web.action.admin.issuessecurity;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.local.AbstractWebworkTestCase;
import com.atlassian.jira.scheme.SchemeType;
import com.atlassian.jira.scheme.SchemeTypeManager;
import com.atlassian.jira.security.SecurityTypeManager;
import com.atlassian.jira.web.action.admin.issuesecurity.AddIssueSecurity;
import com.mockobjects.servlet.MockHttpServletResponse;
import mock.servlet.MockHttpServletRequest;
import org.easymock.EasyMock;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;
import webwork.action.ServletActionContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/**
 * Test case for {@link com.atlassian.jira.web.action.admin.issuesecurity.AddIssueSecurity}.
 *
 * @since v4.2
 */
public class TestAddIssueSecurity extends AbstractWebworkTestCase
{
    private static final Long TEST_SCHEME_ID = 1L;
    private static final Long TEST_SECURITY_LEVEL_ID = 1L;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private IssueSecuritySchemeManager mockSchemeManager;
    private SecurityTypeManager mockSecurityTypeManager;
//    private SchemeType mockSchemeType;
    private GenericValue scheme;


    public TestAddIssueSecurity(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        initRequestAndResponse();
        initManagers();
    }

    @Override
    protected void tearDown() throws Exception
    {
        ManagerFactory.removeService(IssueSecuritySchemeManager.class);
        ManagerFactory.removeService(SchemeTypeManager.class);
        JiraTestUtil.resetRequestAndResponse();
        ActionContext.setContext(null);
        super.tearDown();
    }

    private void initRequestAndResponse()
    {
        response = new MockHttpServletResponse();
        request = new MockHttpServletRequest();
        ServletActionContext.setRequest(request);
        ServletActionContext.setResponse(response);
    }

    private void initManagers() throws Exception
    {
        scheme = UtilsForTests.getTestEntity("IssueSecurityScheme", EasyMap.build("id", new Long(1), "name", "name"));
        mockSecurityTypeManager = createMockSchemeTypeManager();
        mockSchemeManager = createMockSchemeManager();

        ManagerFactory.addService(IssueSecuritySchemeManager.class, mockSchemeManager);
        ManagerFactory.addService(SecurityTypeManager.class, mockSecurityTypeManager);
    }

    private SecurityTypeManager createMockSchemeTypeManager() throws Exception
    {
        SecurityTypeManager mock = createNiceMock(SecurityTypeManager.class);
        String p1 = anyObject();
        expect(mock.getSchemeType(p1)).andReturn(createMockSchemeType());
        return mock;
    }

    private IssueSecuritySchemeManager createMockSchemeManager() throws Exception
    {
        IssueSecuritySchemeManager mock = EasyMock.createNiceMock(IssueSecuritySchemeManager.class);
        expect(mock.getScheme(TEST_SCHEME_ID)).andReturn(scheme);
        return mock;                                                                                                                                        
    }


    private SchemeType createMockSchemeType() throws Exception
    {
        SchemeType mock = createNiceMock(SchemeType.class);
//        String p1 = anyObject();
//        Map p2 = anyObject();
//        JiraServiceContext p3 = anyObject();
//        mock.doValidation(p1,p2,p3);
//        expectLastCall();
        replay(mock);
        return mock;
    }

    private void setUpExistingSecurities(GenericValue... securities) throws Exception
    {
        List<GenericValue> secList = Arrays.asList(securities);
        expect(mockSchemeManager.getEntities(scheme, TEST_SECURITY_LEVEL_ID)).andReturn(secList);
        replay(mockSchemeManager);
        replay(mockSecurityTypeManager);
    }

    private GenericValue newSecurity(String type)
    {
        return UtilsForTests.getTestEntity("SchemeIssueSecurities", EasyMap.build("type", type));
    }

    private GenericValue newSecurity(String type, String param)
    {
        return UtilsForTests.getTestEntity("SchemeIssueSecurities", EasyMap.build("type", type, "parameter", param));
    }


    private void setUpExpectedRedirect(String url) throws Exception
    {
        response.setExpectedRedirect(url);
    }

    private void verifyExpectedRedirect()
    {
        response.verify();
    }

    private AddIssueSecurity createTested(String securityType)
    {
        AddIssueSecurity tested = new AddIssueSecurity();
        tested.setSchemeId(TEST_SCHEME_ID);
        tested.setSecurity(TEST_SECURITY_LEVEL_ID);
        tested.setType(securityType);
        return tested;
    }

    private void addParameterToContext(String type, String parameter)
    {
        Map<Object,Object> current = currentSingleParamMap();
        current.put(type, parameter);
        ActionContext.setSingleValueParameters(current);
    }

    @SuppressWarnings("unchecked")
    private Map<Object,Object> currentSingleParamMap() 
    {
        return new HashMap<Object,Object>(ActionContext.getSingleValueParameters());
    }


    public void testAddFirstExistingIssueSecurityWithNoParameters() throws Exception
    {
        AddIssueSecurity tested = createTested("reporter");
        setUpExistingSecurities(newSecurity("reporter"), newSecurity("assignee"));
        tested.execute();
        assertTrue(tested.hasAnyErrors());
        assertTrue(tested.getErrorMessages().contains(tested.getText("admin.errors.this.issue.security.already.exists")));
    }

    public void testAddNonFirstExistingIssueSecurityWithNoParameters() throws Exception
    {
        AddIssueSecurity tested = createTested("reporter");
        setUpExistingSecurities(newSecurity("assignee"), newSecurity("reporter"));
        tested.execute();
        assertTrue(tested.hasAnyErrors());
        assertTrue(tested.getErrorMessages().contains(tested.getText("admin.errors.this.issue.security.already.exists")));
    }

    public void testAddNonExistingIssueSecurityWithNoParameters() throws Exception
    {
        AddIssueSecurity tested = createTested("reporter");
        setUpExistingSecurities(newSecurity("other"), newSecurity("assignee"), newSecurity("user", "someuser"));
        setUpExpectedRedirect("EditIssueSecurities!default.jspa?schemeId=1");
        tested.execute();
        assertFalse(tested.hasAnyErrors());
        verifyExpectedRedirect();
    }

    public void testAddFirstExistingIssueSecurityWithParameters() throws Exception
    {
        AddIssueSecurity tested = createTested("group");
        addParameterToContext("group", "jira-dev");
        setUpExistingSecurities(newSecurity("group", "jira-dev"), newSecurity("assignee"));
        tested.execute();
        assertTrue(tested.hasAnyErrors());
        assertTrue(tested.getErrorMessages().contains(tested.getText("admin.errors.this.issue.security.already.exists")));
    }

    public void testAddNonFirstExistingIssueSecurityWithParameters() throws Exception
    {
        AddIssueSecurity tested = createTested("group");
        addParameterToContext("group", "jira-dev");
        setUpExistingSecurities(newSecurity("group", "confluence-dev"), newSecurity("group", "jira-dev"),
                newSecurity("no-param"));
        tested.execute();
        assertTrue(tested.hasAnyErrors());
        assertTrue(tested.getErrorMessages().contains(tested.getText("admin.errors.this.issue.security.already.exists")));
    }

    public void testAddNonExistingIssueSecurityWithParameters() throws Exception
    {
        AddIssueSecurity tested = createTested("user");
        addParameterToContext("user", "someuser");
        setUpExistingSecurities(newSecurity("no-param"), newSecurity("user", "some-other-user"),
                newSecurity("user", "yet-another-user"));
        setUpExpectedRedirect("EditIssueSecurities!default.jspa?schemeId=1");
        tested.execute();
        assertFalse(tested.hasAnyErrors());
        verifyExpectedRedirect();
    }

    public void testAddIssueSecurityWithNoParameterGivenExistingIssueSecurityWithParameter() throws Exception
    {
        AddIssueSecurity tested = createTested("user");
        setUpExistingSecurities(newSecurity("no-param"), newSecurity("user", "some-other-user"),
                newSecurity("user", "yet-another-user"));
        setUpExpectedRedirect("EditIssueSecurities!default.jspa?schemeId=1");
        tested.execute();
        assertFalse(tested.hasAnyErrors());
        verifyExpectedRedirect();
    }

    public void testAddIssueSecurityWithParameterGivenExistingIssueSecurityWithNoParameter() throws Exception
    {
        AddIssueSecurity tested = createTested("user");
        addParameterToContext("user", "someuser");
        setUpExistingSecurities(newSecurity("user"), newSecurity("user", "some-other-user"),
                newSecurity("user", "yet-another-user"));
        setUpExpectedRedirect("EditIssueSecurities!default.jspa?schemeId=1");
        tested.execute();
        assertFalse(tested.hasAnyErrors());
        verifyExpectedRedirect();
    }

}