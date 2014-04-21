package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.projectconfig.util.TabUrlFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.DateFieldFormat;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.projectconfig.contextproviders.VersionsSummaryPanelContextProvider.SimpleVersion;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static org.easymock.classextension.EasyMock.expect;
import static org.junit.Assert.assertEquals;

/**
 * @since v4.4
 */
public class TestVersionsSummaryPanelContextProvider
{
    private static final String CONFIG_URL = "?panel=versions&projectKey=";

    @Mock
    private ContextProviderUtils utils;

    @Mock
    private VersionService versionService;

    @Mock
    private TabUrlFactory tabFactory;

    @Mock
    DateFieldFormat dateFieldFormat;

    private MockUser user;
    private JiraAuthenticationContext context;

    @Before
    public void setUp() throws Exception
    {
        EasyMockAnnotations.initMocks(this);

        user = new MockUser("bbain");
        context = new MockSimpleAuthenticationContext(user, Locale.ENGLISH, new NoopI18nHelper());
    }
    
    @After
    public void tearDown()
    {
        utils = null;
        versionService = null;
        user = null;
        context = null;
    }

    @Test
    public void testNone() throws Exception
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();
        Project project = new MockProject(48484848L, "KEY");

        Map<String, Object> arguments = MapBuilder.<String, Object>build("argument", true);

        expect(utils.getProject()).andReturn(project);
        expect(versionService.getVersionsByProject(user, project))
                .andReturn(new VersionService.VersionsResult(errorCollection, Collections.<Version>emptyList()));
        expect(utils.flattenErrors(errorCollection)).andReturn(Collections.<String>emptySet());
        expect(tabFactory.forVersions()).andReturn(CONFIG_URL);

        replayMocks();

        VersionsSummaryPanelContextProvider testing = new VersionsSummaryPanelContextProvider(utils, versionService, context, tabFactory, dateFieldFormat);
        Map<String, Object> actualContext = testing.getContextMap(arguments);

        MapBuilder<String, Object> expectedContext = MapBuilder.newBuilder(arguments)
                .add("versions", Collections.<Version>emptyList())
                .add("errors", Collections.<String>emptySet())
                .add("totalSize", 0)
                .add("actualSize", 0)
                .add("manageVersionLink", CONFIG_URL);

        assertEquals(expectedContext.toMap(), actualContext);
    }

    @Test
    public void testAll() throws Exception
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();
        Project project = new MockProject(48484848L, "KEY2");

        Map<String, Object> arguments = MapBuilder.<String, Object>build("argument", true);

        Version version1 = new MockVersion(162727, "Jack1");
        Version version2 = new MockVersion(162728, "Jack2");
        version2.setReleaseDate(new Date());
        Version version3 = new MockVersion(162799, "Jack3");
        version3.setReleased(true);
        version3.setReleaseDate(new Date());
        Version version4 = new MockVersion(162800, "Jack4");
        version4.setArchived(true);
        version4.setReleaseDate(new Date());

        List<Version> versions = Arrays.asList(version1, version2, version3, version4);

        expect(utils.getProject()).andReturn(project);
        expect(versionService.getVersionsByProject(user, project))
                .andReturn(new VersionService.VersionsResult(errorCollection, versions));
        expect(utils.flattenErrors(errorCollection)).andReturn(Collections.<String>emptySet());
        expect(tabFactory.forVersions()).andReturn(CONFIG_URL);

        List<SimpleVersion> simpleVersions = new ArrayList<SimpleVersion>(versions.size());
        int count = 0;
        for (Version version : reverse(versions))
        {
            if (!version.isArchived())
            {
                boolean overdue = version.getId() % 2 == 0;
                String releaseDate = null;
                expect(versionService.isOverdue(version)).andReturn(overdue);
                if (version.getReleaseDate() != null)
                {
                    releaseDate = valueOf(count);
                    expect(dateFieldFormat.format(version.getReleaseDate())).andReturn(releaseDate);
                }
                simpleVersions.add(new SimpleVersion(version.getName(), version.isReleased(), version.isArchived(),
                        overdue, releaseDate));
                count++;
            }
        }

        replayMocks();

        VersionsSummaryPanelContextProvider testing = new VersionsSummaryPanelContextProvider(utils, versionService, context, tabFactory, dateFieldFormat);
        Map<String, Object> actualContext = testing.getContextMap(arguments);

        MapBuilder<String, Object> expectedContext = MapBuilder.newBuilder(arguments)
                .add("versions", simpleVersions)
                .add("errors", Collections.<String>emptySet())
                .add("totalSize", simpleVersions.size())
                .add("actualSize", simpleVersions.size())
                .add("manageVersionLink", CONFIG_URL);

        assertEquals(expectedContext.toMap(), actualContext);
    }

    @Test
    public void testSome() throws Exception
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();

        Project project = new MockProject(48484848L, "TEST4");

        Map<String, Object> arguments = MapBuilder.<String, Object>build("argument", true);

        Version version1 = new MockVersion(162727, "Jack1");
        version1.setArchived(true);
        Version version2 = new MockVersion(162728, "Jack2");
        version2.setReleaseDate(new Date());
        version2.setReleased(true);

        List<Version> versions = Lists.newArrayList(version1, version2);

        for (int i = 0; i < 20; i++)
        {
            versions.add(new MockVersion(i, format("Version-%d", i)));
        }


        expect(utils.getProject()).andReturn(project);
        expect(versionService.getVersionsByProject(user, project))
                .andReturn(new VersionService.VersionsResult(errorCollection, versions));
        expect(utils.flattenErrors(errorCollection)).andReturn(Collections.<String>emptySet());
        expect(tabFactory.forVersions()).andReturn(CONFIG_URL);

        List<SimpleVersion> simpleVersions = new ArrayList<SimpleVersion>(versions.size());
        int count = 0;
        for (Version version : reverse(versions))
        {
            if (!version.isArchived())
            {
                if (simpleVersions.size() < 5)
                {
                    boolean overdue = version.getId() % 2 == 0;
                    String releaseDate = null;
                    expect(versionService.isOverdue(version)).andReturn(overdue);
                    if (version.getReleaseDate() != null)
                    {
                        releaseDate = valueOf(count);
                        expect(dateFieldFormat.format(version.getReleaseDate())).andReturn(releaseDate);
                    }
                    simpleVersions.add(new SimpleVersion(version.getName(), version.isReleased(), version.isArchived(), overdue, releaseDate));
                }
                count++;
            }
        }

        replayMocks();

        VersionsSummaryPanelContextProvider testing = new VersionsSummaryPanelContextProvider(utils, versionService, context, tabFactory, dateFieldFormat);
        Map<String, Object> actualContext = testing.getContextMap(arguments);

        MapBuilder<String, Object> expectedContext = MapBuilder.newBuilder(arguments)
                .add("versions", simpleVersions)
                .add("errors", Collections.<String>emptySet())
                .add("totalSize", count)
                .add("actualSize", simpleVersions.size())
                .add("manageVersionLink", CONFIG_URL);

        assertEquals(expectedContext.toMap(), actualContext);
    }

    @Test
    public void testErrors() throws Exception
    {
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage("brenden");
        final Set<String> expectedErrors = new HashSet<String>(Arrays.asList("error1", "error2"));

        Project project = new MockProject(48484848L, "TEST6");

        Map<String, Object> arguments = MapBuilder.<String, Object>build("argument", true);


        expect(utils.getProject()).andReturn(project);
        expect(versionService.getVersionsByProject(user, project))
                .andReturn(new VersionService.VersionsResult(errorCollection));
        expect(utils.flattenErrors(errorCollection)).andReturn(expectedErrors);
        expect(tabFactory.forVersions()).andReturn(CONFIG_URL);

        replayMocks();

        VersionsSummaryPanelContextProvider testing = new VersionsSummaryPanelContextProvider(utils, versionService, context, tabFactory, dateFieldFormat);
        Map<String, Object> actualContext = testing.getContextMap(arguments);

        MapBuilder<String, Object> expectedContext = MapBuilder.newBuilder(arguments)
                .add("versions", Collections.<Version>emptyList())
                .add("errors", expectedErrors)
                .add("totalSize", 0)
                .add("actualSize", 0)
                .add("manageVersionLink", CONFIG_URL);

        assertEquals(expectedContext.toMap(), actualContext);
    }

    private <T> List<T> reverse(Collection<? extends T> ver)
    {
        List<T> reverseList = new ArrayList<T>(ver);
        Collections.reverse(reverseList);
        return reverseList;
    }

    protected void replayMocks()
    {
        EasyMockAnnotations.replayMocks(this);
    }
}
