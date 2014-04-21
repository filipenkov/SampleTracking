package com.atlassian.jira.security.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.MapBuilder;
import org.apache.lucene.search.BooleanQuery;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestAbstractIssueFieldSecurityType extends ListeningTestCase
{
    //Test for JRA-27590
    @Test
    public void testQueryForProjectLowerCased()
    {
        final TestableAbstractIssueFieldSecurityType security = new TestableAbstractIssueFieldSecurityType();
        final GenericValue mockProject = new MockGenericValue("Project", MapBuilder.build("id", 10000L));
        final User mockUser = new MockUser("britneySpears");

        final BooleanQuery query = security.getQueryForProject(mockProject, mockUser);
        assertEquals("+projid:10000 +assignee:britneyspears", query.toString());
    }

    //Test for JRA-27590
    @Test
    public void testQueryForSecurityLevelLowerCased()
    {
        final TestableAbstractIssueFieldSecurityType security = new TestableAbstractIssueFieldSecurityType();
        final GenericValue mockSecurityLevel = new MockGenericValue("SecurityLevel", MapBuilder.build("id", 10500L));
        final User mockUser = new MockUser("britneySpears");

        final BooleanQuery query = security.getQueryForSecurityLevel(mockSecurityLevel, mockUser);
        assertEquals("+issue_security_level:10500 +assignee:britneyspears", query.toString());
    }


    static class TestableAbstractIssueFieldSecurityType extends AbstractIssueFieldSecurityType
    {

        @Override
        protected String getFieldName()
        {
            return "assignee";
        }

        @Override
        protected boolean hasIssuePermission(User user, boolean issueCreation, GenericValue issueGv, String argument)
        {
            return false;
        }

        @Override
        protected boolean hasProjectPermission(User user, boolean issueCreation, GenericValue project)
        {
            return false;
        }

        @Override
        public String getDisplayName()
        {
            return null;
        }

        @Override
        public String getType()
        {
            return null;
        }

        @Override
        public void doValidation(String key, Map parameters, JiraServiceContext jiraServiceContext)
        {
        }
    }
}
