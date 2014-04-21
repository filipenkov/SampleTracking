package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.jira.bc.project.projectoperation.ProjectOperationManager;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.plugin.projectoperation.AbstractPluggableProjectOperation;
import com.atlassian.jira.plugin.projectoperation.PluggableProjectOperation;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.projectconfig.order.NativeComparator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.vcs.Repository;
import com.atlassian.jira.vcs.RepositoryBrowser;
import com.atlassian.jira.vcs.RepositoryException;
import com.atlassian.jira.vcs.RepositoryManager;
import mock.user.MockOSUser;
import net.sf.statcvs.model.Commit;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.easymock.classextension.EasyMock.createControl;
import static org.easymock.classextension.EasyMock.expect;
import static org.junit.Assert.assertEquals;

/**
 * @since v4.4
 */
public class TestSettingsSummaryPanelContextProvider
{
    private MockUser user;
    private IMocksControl control;
    private ProjectOperationManager projectOperationManager;
    private ContextProviderUtils contextProviderUtils;
    private JiraAuthenticationContext authenticationContext;
    private RepositoryManager repositoryManager;
    private ApplicationLinkService applicationLinkService;
    private InternalHostApplication  hostApplication;
    private MockOSUser oldUser;

    @Before
    public void setUp() throws Exception
    {
        user = new MockUser("bbain");
        oldUser = new MockOSUser("bbain");
        control = createControl();

        projectOperationManager = control.createMock(ProjectOperationManager.class);
        contextProviderUtils = control.createMock(ContextProviderUtils.class);
        authenticationContext = new MockSimpleAuthenticationContext(user, Locale.ENGLISH, new NoopI18nHelper())
                .setOSUser(oldUser);
        repositoryManager = control.createMock(RepositoryManager.class);
        applicationLinkService = control.createMock(ApplicationLinkService.class);
        hostApplication = control.createMock(InternalHostApplication.class);
    }

    @Test
    public void testHappyPath() throws GenericEntityException
    {
        final MockProject project = new MockProject(167L, "HSP");
        final Map<String, Object> argument = MapBuilder.<String, Object>build("argument", true);

        expect(contextProviderUtils.getProject()).andReturn(project);
        expect(contextProviderUtils.getStringComparator()).andReturn(NativeComparator.<String>getInstance());
        expect(projectOperationManager.getVisibleProjectOperations(project, user)).andReturn(operations("one", "two"));
        expect(repositoryManager.getRepositoriesForProject(project.getGenericValue())).andReturn(repositories("three", "four"));

        expect(applicationLinkService.getApplicationLinks()).andReturn(Collections.<ApplicationLink>emptyList());

        control.replay();

        SettingsSummaryPanelContextProvider provider = new SettingsSummaryPanelContextProvider(projectOperationManager,
                contextProviderUtils, authenticationContext, repositoryManager, applicationLinkService, null);

        MapBuilder<String, Object> expectedContext = MapBuilder.<String, Object>newBuilder(argument)
                .add("pluginsHtml", Arrays.asList("one", "two"))
                .add("showAppLinks", false)
                .add("repos", repositories("four", "three"));

        assertEquals(expectedContext.toMap(), provider.getContextMap(argument));

        control.verify();
    }

    private static List<PluggableProjectOperation> operations(String...values)
    {
        List<PluggableProjectOperation> operations = new ArrayList<PluggableProjectOperation>();
        for (final String value : values)
        {
            operations.add(new AbstractPluggableProjectOperation()
            {
                public String getHtml(Project project, com.opensymphony.user.User user)
                {
                    return value;
                }

                public boolean showOperation(Project project, com.opensymphony.user.User user)
                {
                    return true;
                }
            });
        }
        return operations;
    }

    private static List<Repository> repositories(String...names)
    {
        List<Repository> repositories = new ArrayList<Repository>();
        for (String name : names)
        {
            repositories.add(new MockRepository(name));
        }
        return repositories;
    }

    private static class MockRepository implements Repository
    {
        private final String name;

        public MockRepository(String name)
        {
            this.name = name;
        }

        public List<Commit> getCommitsForIssue(String issueKey) throws RepositoryException
        {
            throw new UnsupportedOperationException();
        }

        public Long getId()
        {
            throw new UnsupportedOperationException();
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            throw new UnsupportedOperationException();
        }

        public String getType()
        {
            throw new UnsupportedOperationException();
        }

        public void setRepositoryBrowser(RepositoryBrowser repositoryBrowser)
        {
            throw new UnsupportedOperationException();
        }

        public RepositoryBrowser getRepositoryBrowser()
        {
            throw new UnsupportedOperationException();
        }

        public void copyContent(Repository repository)
        {
            throw new UnsupportedOperationException();
        }

        public int compareTo(Repository o)
        {
            return getName().compareTo(o.getName());
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            MockRepository that = (MockRepository) o;

            if (name != null ? !name.equals(that.name) : that.name != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            return name != null ? name.hashCode() : 0;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    private static <T extends Comparable<? super T>> Comparator<T> naturalComparator()
    {
        return new Comparator<T>()
        {
            public int compare(T o1, T o2)
            {
                return o1.compareTo(o2);
            }
        };
    }
}
