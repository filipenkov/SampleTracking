package com.atlassian.jira.issue.fields;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.user.MockCrowdService;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.issue.MockIssueFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.MockIssueSecurityLevelManager;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.user.User;
import com.atlassian.jira.local.ListeningTestCase;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.Collections;

/**
 * Test SecurityLevelSystemField
 *
 * @since v3.13
 */
public class TestSecurityLevelSystemField extends ListeningTestCase
{
    @Test
    public void testNeedsMoveWithDefaultTargetSecurityLevel() throws GenericEntityException
    {
        // Having a default TargetSecurityLevel should only affect us when we move project.
        MockIssueFactory.setProjectManager(MockProjectManager.createDefaultProjectManager());

        MockIssueSecurityLevelManager mockIssueSecurityLevelManager = new MockIssueSecurityLevelManager();
        mockIssueSecurityLevelManager.setDefaultSecurityLevelForProject(new Long(2), new Long(20000));

        SecurityLevelSystemField securityLevelSystemField = new SecurityLevelSystemField(null, null, null, null,
                mockIssueSecurityLevelManager, null, null);

        MutableIssue mockSourceIssue = MockIssueFactory.createIssue(1, "RANDOM-1", 2);
        MutableIssue mockTargetIssue = MockIssueFactory.createIssue(1, "RANDOM-1", 2);

        Mock mockTargetFieldLayoutItem = new Mock(FieldLayoutItem.class);
        mockTargetFieldLayoutItem.expectAndReturn("isRequired", Boolean.FALSE);

        // Source and target projects are the same - Should not need move.
        MessagedResult messagedResult = securityLevelSystemField.needsMove(EasyList.build(mockSourceIssue), mockTargetIssue, (FieldLayoutItem) mockTargetFieldLayoutItem.proxy());
        assertFalse(messagedResult.getResult());
        assertNull(messagedResult.getMessage());

        mockTargetFieldLayoutItem.expectAndReturn("isRequired", Boolean.FALSE);

        // Make the Source and Target Projects different, so that we take notice of the default.
        mockSourceIssue.setProjectId(new Long(1));
        messagedResult = securityLevelSystemField.needsMove(EasyList.build(mockSourceIssue), mockTargetIssue, (FieldLayoutItem) mockTargetFieldLayoutItem.proxy());
        assertTrue(messagedResult.getResult());
        assertNull(messagedResult.getMessage());

        mockTargetFieldLayoutItem.expectAndReturn("isRequired", Boolean.TRUE);

        messagedResult = securityLevelSystemField.needsMove(EasyList.build(mockSourceIssue), mockTargetIssue, (FieldLayoutItem) mockTargetFieldLayoutItem.proxy());
        assertTrue(messagedResult.getResult());
        assertNull(messagedResult.getMessage());
    }

    @Test
    public void testNeedsMoveWithoutDefaultTargetSecurityLevel()
    {
        Long projectId = new Long(1000);
        MockGenericValue mockProjectGV = new MockGenericValue("Project", EasyMap.build("id", projectId));
        Mock mockIssueSecurityLevelManager = new Mock(IssueSecurityLevelManager.class);
        //now lets not return a default security level
        mockIssueSecurityLevelManager.expectAndReturn("getSchemeDefaultSecurityLevel", new Constraint[] { P.eq(mockProjectGV) }, null);


        SecurityLevelSystemField securityLevelSystemField = new SecurityLevelSystemField(null, null, null, null,
                (IssueSecurityLevelManager) mockIssueSecurityLevelManager.proxy(), null, null);

        MutableIssue mockSourceIssue = MockIssueFactory.createIssue(1);
        MutableIssue mockTargetIssue = MockIssueFactory.createIssue(1);
        mockTargetIssue.setProject(mockProjectGV);

        Mock mockTargetFieldLayoutItem = new Mock(FieldLayoutItem.class);
        mockTargetFieldLayoutItem.expectAndReturn("isRequired", Boolean.FALSE);

        MessagedResult messagedResult = securityLevelSystemField.needsMove(EasyList.build(mockSourceIssue), mockTargetIssue, (FieldLayoutItem) mockTargetFieldLayoutItem.proxy());
        assertFalse(messagedResult.getResult());
        assertNull(messagedResult.getMessage());

        mockTargetFieldLayoutItem.expectAndReturn("isRequired", Boolean.TRUE);

        messagedResult = securityLevelSystemField.needsMove(EasyList.build(mockSourceIssue), mockTargetIssue, (FieldLayoutItem) mockTargetFieldLayoutItem.proxy());
        assertTrue(messagedResult.getResult());
        assertNull(messagedResult.getMessage());
    }

    @Test
    public void testNeedsMoveWithSecurityLevelOnOriginalIssue()
    {
        MockProviderAccessor mpa = new MockProviderAccessor();
        User user = new User("admin", mpa, new MockCrowdService());

        Long projectId = new Long(1000);
        MockGenericValue mockProjectGV = new MockGenericValue("Project", EasyMap.build("id", projectId));
        MutableIssue mockSourceIssue = MockIssueFactory.createIssue(1);
        mockSourceIssue.setSecurityLevel(new MockGenericValue("SecurityLevel", EasyMap.build("id", new Long(30000))));

        MutableIssue mockTargetIssue = MockIssueFactory.createIssue(1);
        mockTargetIssue.setProject(mockProjectGV);

        Mock mockIssueSecurityLevelManager = new Mock(IssueSecurityLevelManager.class);
        //now lets not return a default security level
        mockIssueSecurityLevelManager.expectAndReturn("getSchemeDefaultSecurityLevel", new Constraint[] { P.eq(mockProjectGV) }, new Long(20000));
        mockIssueSecurityLevelManager.expectAndReturn("getUsersSecurityLevels", new Constraint[] { P.eq(mockTargetIssue.getGenericValue()), P.eq(user) }, Collections.EMPTY_LIST);

        MockAuthenticationContext mockAuthenticationContext = new MockAuthenticationContext(user);

        SecurityLevelSystemField securityLevelSystemField = new SecurityLevelSystemField(null, null, null, mockAuthenticationContext,
                (IssueSecurityLevelManager) mockIssueSecurityLevelManager.proxy(), null, null);

        MessagedResult messagedResult = securityLevelSystemField.needsMove(EasyList.build(mockSourceIssue), mockTargetIssue, null);
        assertTrue(messagedResult.getResult());
        assertNull(messagedResult.getMessage());
    }

    @Test
    public void testDoesNotNeedMoveWithSecurityLevelOnOriginalIssue()
    {
        MockProviderAccessor mpa = new MockProviderAccessor();
        User user = new User("admin", mpa, new MockCrowdService());

        Long projectId = new Long(1000);
        MockGenericValue mockProjectGV = new MockGenericValue("Project", EasyMap.build("id", projectId));
        MutableIssue mockSourceIssue = MockIssueFactory.createIssue(1);
        MockGenericValue mockSecurityLevel = new MockGenericValue("SecurityLevel", EasyMap.build("id", new Long(30000)));
        mockSourceIssue.setSecurityLevel(mockSecurityLevel);

        MutableIssue mockTargetIssue = MockIssueFactory.createIssue(1);
        mockTargetIssue.setProject(mockProjectGV);

        Mock mockIssueSecurityLevelManager = new Mock(IssueSecurityLevelManager.class);
        //now lets not return a default security level
        mockIssueSecurityLevelManager.expectAndReturn("getSchemeDefaultSecurityLevel", new Constraint[] { P.eq(mockProjectGV) }, new Long(20000));
        mockIssueSecurityLevelManager.expectAndReturn("getUsersSecurityLevels", new Constraint[] { P.eq(mockTargetIssue.getGenericValue()), P.eq(user) }, EasyList.build(mockSecurityLevel));

        MockAuthenticationContext mockAuthenticationContext = new MockAuthenticationContext(user);

        SecurityLevelSystemField securityLevelSystemField = new SecurityLevelSystemField(null, null, null, mockAuthenticationContext,
                (IssueSecurityLevelManager) mockIssueSecurityLevelManager.proxy(), null, null);

        MessagedResult messagedResult = securityLevelSystemField.needsMove(EasyList.build(mockSourceIssue), mockTargetIssue, null);
        assertFalse(messagedResult.getResult());
        assertNull(messagedResult.getMessage());
    }
}
