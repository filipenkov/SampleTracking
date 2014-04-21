/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.plugins.importer.XmlUtil;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.admin.issuetypes.IssueTypeManageableOption;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Map;

public class PivotalSchemeManagerTest {

	@Mock
	private IssueTypeSchemeManager issueTypeSchemeManager;
	@Mock
	private ConstantsManager constantsManager;
	@Mock
	private SubTaskManager subTaskManager;
	@Mock
	private FieldConfigSchemeManager configSchemeManager;
	@Mock
	private IssueTypeManageableOption manageableOptionType;
	@Mock
	private FieldManager fieldManager;
	@Mock
	private JiraContextTreeManager treeManager;
	@Mock
	private WorkflowManager workflowManager;
	@Mock
	private WorkflowSchemeManager workflowSchemeManager;
	@Mock
	private JiraAuthenticationContext authenticationContext;
	private PivotalSchemeManager pivotalSchemeManager;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		Mockito.when(constantsManager.getStatusByName(Mockito.anyString())).thenAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				final Status status = Mockito.mock(Status.class);
				Mockito.when(status.getId()).thenReturn("ID for " + invocation.getArguments()[0]);
				return status;
			}
		});

		pivotalSchemeManager = new PivotalSchemeManager(issueTypeSchemeManager,
				subTaskManager, constantsManager, configSchemeManager, fieldManager, workflowManager,
				workflowSchemeManager, authenticationContext, manageableOptionType, treeManager);
	}

	@Test
	public void testBasicMappingOfStatus() throws Exception {

		// test data reflecting what PivotalSchemeManager would create in the wild
		final Map<String, String> mapping = pivotalSchemeManager.getPTStatusNameToIdMapping();

		final Document expectedDocument = XmlUtil.getSAXBuilder().build(getClass().getResourceAsStream("/pivotal/workflow/pt_workflow_substituted.xml"));
		final String expected = new XMLOutputter(Format.getRawFormat()).outputString(expectedDocument);

		final String out = pivotalSchemeManager.substituteStatusIds(getClass().getResourceAsStream("/pivotal/workflow/pt_workflow_in.xml"), mapping);

		Assert.assertEquals(expected, out);
	}
}
