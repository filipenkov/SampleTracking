/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.search.MockJqlSearchRequest;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operator.Operator;
import org.easymock.classextension.EasyMock;
import org.ofbiz.core.entity.GenericValue;

public class TestComponentStatisticsMapper extends MockControllerTestCase
{
    @Test
    public void testEquals()
    {
        ComponentStatisticsMapper mapper = new ComponentStatisticsMapper();
        assertTrue(mapper.equals(mapper));
        assertEquals(mapper.hashCode(), mapper.hashCode());

        ComponentStatisticsMapper mapper2 = new ComponentStatisticsMapper();
        assertTrue(mapper.equals(mapper2));
        assertEquals(mapper.hashCode(), mapper2.hashCode());

        assertTrue(mapper.equals(new ComponentStatisticsMapper()));
        assertEquals(mapper.hashCode(), new ComponentStatisticsMapper().hashCode());

        assertFalse(mapper.equals(null));
        assertFalse(mapper.equals(new Object()));
        assertFalse(mapper.equals(new IssueTypeStatisticsMapper(null)));
    }


    private static final long PROJECT_ID = 13L;
    private static final long COMPONENT_ID = 555L;
    private static final String CLAUSE_NAME = "component";

    @Test
    public void testGetUrlSuffixForSomeComponent() throws Exception
    {
        final Project project = new MockProject(PROJECT_ID, "PR");
        final GenericValue componentGV = new MockGenericValue("componentGV", MapBuilder.newBuilder().add("id", COMPONENT_ID).add("name", "New Component 555").add("project", project.getId()).toHashMap());
        final ProjectComponent component = new MockComponent(COMPONENT_ID, "New Component 555", project.getId());

        final ProjectComponentManager projectComponentManager;
        projectComponentManager = EasyMock.createMock(ProjectComponentManager.class);
        EasyMock.expect(projectComponentManager.find(COMPONENT_ID)).andReturn(component);

        final ProjectManager projectManager = EasyMock.createMock(ProjectManager.class);
        EasyMock.expect(projectManager.getProjectObj(PROJECT_ID)).andReturn(project);

        ComponentStatisticsMapper mapper = new ComponentStatisticsMapper()
        {
            protected ProjectManager getProjectManager()
            {
                return projectManager;
            }

            @Override
            protected ProjectComponentManager getProjectComponentManager()
            {
                return projectComponentManager;
            }
        };

        final TerminalClauseImpl projectClause = new TerminalClauseImpl(IssueFieldConstants.PROJECT, Operator.EQUALS, PROJECT_ID);
        final TerminalClauseImpl issueTypeClause = new TerminalClauseImpl(IssueFieldConstants.ISSUE_TYPE, Operator.EQUALS, "Bug");
        final AndClause totalExistingClauses = new AndClause(projectClause, issueTypeClause);

        final TerminalClauseImpl myComponentClause = new TerminalClauseImpl(CLAUSE_NAME, Operator.EQUALS, "New Component 555");
        final TerminalClauseImpl myComponentProjectClause = new TerminalClauseImpl(IssueFieldConstants.PROJECT, Operator.EQUALS, PROJECT_ID);

        Query query = new QueryImpl(totalExistingClauses);
        SearchRequest sr = new MockJqlSearchRequest(10000L, query);

        EasyMock.replay(projectManager, projectComponentManager);
        final SearchRequest urlSuffix = mapper.getSearchUrlSuffix(componentGV, sr);
        final String modifiedClauses = urlSuffix.getQuery().getWhereClause().toString();

        assertTrue(modifiedClauses.contains(projectClause.toString()));
        assertTrue(modifiedClauses.contains(issueTypeClause.toString()));
        assertTrue(modifiedClauses.contains(myComponentClause.toString()));
        assertTrue(modifiedClauses.contains(myComponentProjectClause.toString()));
        EasyMock.verify(projectManager, projectComponentManager);
    }

    @Test
    public void testGetUrlSuffixForNullComponent() throws Exception
    {
        ComponentStatisticsMapper mapper = new ComponentStatisticsMapper();

        final TerminalClauseImpl projectClause = new TerminalClauseImpl(IssueFieldConstants.PROJECT, Operator.EQUALS, PROJECT_ID);
        final TerminalClauseImpl issueTypeClause = new TerminalClauseImpl(IssueFieldConstants.ISSUE_TYPE, Operator.EQUALS, "Bug");
        final AndClause totalExistingClauses = new AndClause(projectClause, issueTypeClause);

        final TerminalClauseImpl myComponentClause = new TerminalClauseImpl(CLAUSE_NAME, Operator.IS, EmptyOperand.EMPTY);

        Query query = new QueryImpl(totalExistingClauses);
        SearchRequest sr = new MockJqlSearchRequest(10000L, query);

        final SearchRequest urlSuffix = mapper.getSearchUrlSuffix(null, sr);
        final String modifiedClauses = urlSuffix.getQuery().getWhereClause().toString();

        assertTrue(modifiedClauses.contains(projectClause.toString()));
        assertTrue(modifiedClauses.contains(issueTypeClause.toString()));
        assertTrue(modifiedClauses.contains(myComponentClause.toString()));
    }

    @Test
    public void testGetUrlSuffixForNullSearchRequest() throws Exception
    {
        ComponentStatisticsMapper mapper = new ComponentStatisticsMapper();
        assertNull(mapper.getSearchUrlSuffix(null, null));
    }

    static class MockComponent implements ProjectComponent
    {
        private final Long id;
        private final String name;
        private final Long projectId;

        MockComponent(Long id, String name, Long projectId)
        {
            this.id = id;
            this.name = name;
            this.projectId = projectId;
        }

        public String getName()
        {
            return name;
        }

        public Long getId()
        {
            return id;
        }

        public Long getProjectId()
        {
            return projectId;
        }

        public String getDescription()
        {
            return null;
        }

        public String getLead()
        {
            return null;
        }

        public long getAssigneeType()
        {
            return 0;
        }

        public GenericValue getGenericValue()
        {
            return null;
        }
    }
}
