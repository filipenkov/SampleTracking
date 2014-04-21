package it.com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.plugins.importer.external.ExternalUserUtils;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.external.UserProvider;
import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.impl.DefaultJiraDataImporter;
import com.atlassian.jira.plugins.importer.imports.importer.impl.ImportObjectIdMappings;
import com.atlassian.jira.plugins.importer.imports.pivotal.CachingPivotalClient;
import com.atlassian.jira.plugins.importer.imports.pivotal.PivotalClient;
import com.atlassian.jira.plugins.importer.imports.pivotal.PivotalConfigBean;
import com.atlassian.jira.plugins.importer.imports.pivotal.PivotalDataBean;
import com.atlassian.jira.plugins.importer.imports.pivotal.PivotalRemoteException;
import com.atlassian.jira.plugins.importer.imports.pivotal.PivotalSchemeManager;
import com.atlassian.jira.plugins.importer.managers.CreateConstantsManager;
import com.atlassian.jira.plugins.importer.managers.CreateProjectManager;
import com.atlassian.jira.plugins.importer.web.SiteConfiguration;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.user.util.UserUtil;
import com.google.common.collect.Maps;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.stubbing.defaultanswers.ReturnsMocks;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericValue;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;

public class PivotalImportTest {
	private final Configuration configuration = ITUtils.getProperties();

	private final String username = configuration.getString("pivotal.username");
	private final String password = configuration.getString("pivotal.password");
	private final String sampleProject = configuration.getString("pivotal.sampleProject");

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Mock(answer = Answers.RETURNS_MOCKS)
	private ExternalUtils externalUtils;
	@Mock(answer = Answers.RETURNS_MOCKS)
	private ExternalUserUtils externalUserUtils;
	@Mock
	private WorklogManager worklogManager;
	@Mock
	private FieldManager fieldManager;
	@Mock
	private WatcherManager watcherManager;
	@Mock
	private VoteManager voteManager;
	@Mock
	private IssueIndexManager indexManager;
	@Mock
	private CreateConstantsManager createConstantsManager;
	@Mock
	private IssueFactory issueFactory;
	@Mock
	private SubTaskManager subTaskManager;
	@Mock
	private JiraContextTreeManager jiraContextTreeManager;
	@Mock
	private VersionManager versionManager;
	@Mock
	private PivotalSchemeManager pivotalSchemeManager;
	@Mock(answer = Answers.RETURNS_MOCKS)
	private CreateProjectManager createProjectManager;
	@Mock
	private CrowdService crowdService;
	@Mock
	private OptionsManager optionsManager;
	@Mock(answer = Answers.RETURNS_MOCKS)
	private SearchProviderFactory searchProviderFactory;
	@Mock
	UserUtil userUtil;
	@Mock(answer = Answers.RETURNS_MOCKS)
	JiraLicenseService jiraLicenseService;

	@Before
	public void initMocks() throws Exception {
		MockitoAnnotations.initMocks(this);
		Mockito.when(externalUtils.getIssueFactory()).thenReturn(issueFactory);
	}

	@Test
	public void testRetrievingAttachments() throws Exception {
		final Map<Issue, ExternalIssue> issueMapping = Maps.newHashMap();
		final Map<ExternalIssue, ExternalAttachment> attachmentMapping = Maps.newHashMap();
		final Map<GenericValue, Issue> genericValueMapping = Maps.newHashMap();

		Mockito.when(crowdService.getUser(anyString())).thenAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				User user = Mockito.mock(User.class);
				when(user.getName()).thenReturn(invocationOnMock.getArguments()[0].toString());
				return user;
			}
		});

		Mockito.when(externalUtils.areAttachmentsEnabled()).thenReturn(true);
		Mockito.when(externalUtils.getProject(Mockito.<ExternalProject>any())).thenReturn(null);
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				final ExternalAttachment attachment = (ExternalAttachment) invocation.getArguments()[1];
				final Issue issue = (Issue) invocation.getArguments()[2];
				final ExternalIssue externalIssue = issueMapping.get(issue);

				attachmentMapping.put(externalIssue, attachment);

				return null;
			}
		}).when(externalUtils).attachFile(Mockito.<UserProvider>any(),
				Mockito.<ExternalAttachment>any(), Mockito.<MutableIssue>any(),
				Mockito.any(ImportLogger.class));
		Mockito.when(externalUtils.convertExternalIssueToIssue(Mockito.<UserProvider>any(), Mockito.<ExternalIssue>any(),
				Mockito.<ExternalProject>any(),
				Mockito.<ImportObjectIdMappings>any(), Mockito.any(ImportLogger.class))).thenAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				final Issue answer = Mockito.mock(MutableIssue.class);
				issueMapping.put(answer, (ExternalIssue) invocation.getArguments()[1]);
				return answer;
			}
		});
		Mockito.when(externalUtils.createIssue(Mockito.<Issue>any(), Mockito.anyString(), Mockito.anyString(),
				Mockito.any(ImportLogger.class)))
				.thenAnswer(new ReturnsMocks() {
					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						final GenericValue genericValue = (GenericValue) super.answer(invocation);
						genericValueMapping.put(genericValue, (Issue) invocation.getArguments()[0]);
						return genericValue;
					}
				});

		Mockito.when(issueFactory.getIssue(Mockito.<GenericValue>any())).thenAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				final GenericValue genericValue = (GenericValue) invocation.getArguments()[0];
				return genericValueMapping.get(genericValue);
			}
		});

		final PivotalConfigBean config = new PivotalConfigBean(
				new SiteConfiguration("http://www.pivotaltracker.com", true, username, password), externalUtils) {
			@Override
			public PivotalClient getPivotalClient() throws PivotalRemoteException {
				PivotalClient fixture = new CachingPivotalClient() {
					@Override
					protected File getTempDir() {
						return temporaryFolder.getRoot();
					}
				};
				fixture.login(username, password);
				return fixture;
			}
		};

		final PivotalDataBean bean = new PivotalDataBean(config, pivotalSchemeManager, true);
		config.populateProjectKeyMappings(Collections.singletonMap(sampleProject, new ExternalProject(sampleProject, "MYT")));

		final DefaultJiraDataImporter importer = new DefaultJiraDataImporter(externalUtils, worklogManager, fieldManager,
				watcherManager, voteManager, indexManager, createConstantsManager, subTaskManager, versionManager,
				externalUserUtils, jiraContextTreeManager, createProjectManager, crowdService, optionsManager,
				searchProviderFactory, userUtil, jiraLicenseService);
		importer.initializeLog();
		importer.setDataBean(bean);
		importer.doImport();

		assertEquals(2, attachmentMapping.size());
		for (Map.Entry<ExternalIssue, ExternalAttachment> entry : attachmentMapping.entrySet()) {
			final ExternalIssue issue = entry.getKey();
			final ExternalAttachment attachment = entry.getValue();

			if (attachment.getFileName().equals("1.png")) {
				assertEquals("wseliga", attachment.getAttacher());
				assertEquals(51636, attachment.getAttachedFile().length());
				assertEquals("As an admin I want to import my attachments", issue.getSummary());
			} else if (attachment.getFileName().equals("putty.zip")) {
				assertEquals("Test Member", attachment.getAttacher());
				assertEquals(1518921, attachment.getAttachedFile().length());
				assertEquals("hello my story", issue.getSummary());
			} else {
				fail("Unknown attachment " + attachment.getFileName());
			}
		}
	}
}
