package com.atlassian.jira.rest.v2.issue.version;

import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.rest.v2.common.SimpleLinkBean;
import com.atlassian.jira.rest.v2.issue.VersionResource;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.DateFieldFormat;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.Lists;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.atlassian.jira.matchers.ReflectionEqualTo.reflectionEqualTo;
import static org.easymock.classextension.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @since v4.4
 */
public class TestVersionBeanFactoryImpl
{
    @Mock
    VersionService versionService;

    @Mock
    JiraAuthenticationContext authContext;

    @Mock
    SimpleLinkManager simpleLinkManager;

    @Mock
    UriInfo info;

    @Mock
    UriBuilder builder;

    @Mock
    DateFieldFormat dateFieldFormat;

    @Test
    public void testCreateVersionBeanNoReleaseDate() throws Exception
    {
        URI uri = new URI("http://localhost:8090/jira");
        String pKey = "KEY";

        MockProject project = new MockProject(67, pKey);
        MockVersion version = new MockVersion();

        version.setId(54748L);
        version.setProjectObject(project);

        expect(info.getBaseUriBuilder()).andReturn(builder);
        expect(builder.path(VersionResource.class)).andReturn(builder);
        expect(builder.path(version.getId().toString())).andReturn(builder);
        expect(builder.build()).andReturn(uri);

        replayMocks();

        VersionBeanFactoryImpl factory = new VersionBeanFactoryImpl(versionService, info, dateFieldFormat, authContext, simpleLinkManager);
        VersionBean bean = factory.createVersionBean(version);

        assertThat(bean, reflectionEqualTo(new VersionBean(version, null, null, uri)));
    }

    @Test
    public void testCreateVersionBeansReleasedAndReleaseDate() throws Exception
    {
        URI uri = new URI("http://localhost:8090/jira");
        String datePretty = "Great Date";


        MockVersion version = new MockVersion();
        version.setId(54748L);
        version.setReleaseDate(new Date());
        version.setReleased(true);
        version.setDescription("    ");


        expect(info.getBaseUriBuilder()).andReturn(builder);
        expect(builder.path(VersionResource.class)).andReturn(builder);
        expect(builder.path(version.getId().toString())).andReturn(builder);
        expect(builder.build()).andReturn(uri);

        expect(dateFieldFormat.format(version.getReleaseDate())).andReturn(datePretty);

        replayMocks();

        VersionBeanFactoryImpl factory = new VersionBeanFactoryImpl(versionService, info, dateFieldFormat, authContext, simpleLinkManager);
        VersionBean bean = factory.createVersionBean(version);

        assertThat(bean, reflectionEqualTo(new VersionBean(version, null, datePretty, uri)));
    }

    @Test
    public void testCreateVersionBeansWithReleaseDate() throws Exception
    {
        URI uri = new URI("http://localhost:8090/jira");
        String datePretty = "Great Date";

        MockVersion version = new MockVersion();
        version.setId(54748L);
        version.setReleaseDate(new Date());
        version.setReleased(false);

        expect(info.getBaseUriBuilder()).andReturn(builder);
        expect(builder.path(VersionResource.class)).andReturn(builder);
        expect(builder.path(version.getId().toString())).andReturn(builder);
        expect(builder.build()).andReturn(uri);

        expect(versionService.isOverdue(version)).andReturn(true);

        expect(dateFieldFormat.format(version.getReleaseDate())).andReturn(datePretty);

        replayMocks();

        VersionBeanFactoryImpl factory = new VersionBeanFactoryImpl(versionService, info, dateFieldFormat, authContext, simpleLinkManager);
        VersionBean bean = factory.createVersionBean(version);

        assertThat(bean, reflectionEqualTo(new VersionBean(version, true, datePretty, uri)));
    }

    @Test
    public void testCreateVersionBeansWithEmptyOperations() throws Exception
    {
        URI uri = new URI("http://localhost:8090/jira");
        String datePretty = "Great Date";

        MockVersion version = new MockVersion();
        version.setId(54748L);
        version.setReleaseDate(new Date());
        version.setReleased(false);

        MockProject project = new MockProject(101010, "HSP", "homospaian");
        version.setProjectObject(project);
        MockUser fred = new MockUser("fred");

        JiraHelper helper = new JiraHelper(null, project, MapBuilder.build("version", version, "user", fred, "project", project));


        expect(info.getBaseUriBuilder()).andReturn(builder);
        expect(builder.path(VersionResource.class)).andReturn(builder);
        expect(builder.path(version.getId().toString())).andReturn(builder);
        expect(builder.build()).andReturn(uri);

        expect(versionService.isOverdue(version)).andReturn(true);

        expect(dateFieldFormat.format(version.getReleaseDate())).andReturn(datePretty);

        expect(authContext.getLoggedInUser()).andReturn(fred);
        expect(simpleLinkManager.getLinksForSection(VersionBeanFactory.VERSION_OPERATIONS_WEB_LOCATION, fred, helper)).andReturn(new ArrayList<SimpleLink>());

        replayMocks();

        VersionBeanFactoryImpl factory = new VersionBeanFactoryImpl(versionService, info, dateFieldFormat, authContext, simpleLinkManager);
        VersionBean bean = factory.createVersionBean(version, true);

        VersionBean expected = new VersionBean(version, true, datePretty, uri, new ArrayList<SimpleLinkBean>());
        expected.setExpand("operations");
        assertThat(bean, reflectionEqualTo(expected));
    }

    @Test
    public void testCreateVersionBeansWithSomeOperations() throws Exception
    {
        URI uri = new URI("http://localhost:8090/jira");
        String datePretty = "Great Date";

        MockVersion version = new MockVersion();
        version.setId(54748L);
        version.setReleaseDate(new Date());
        version.setReleased(false);

        MockProject project = new MockProject(101010, "HSP", "homospaian");
        version.setProjectObject(project);
        MockUser fred = new MockUser("fred");

        JiraHelper helper = new JiraHelper(null, project, MapBuilder.build("version", version, "user", fred, "project", project));


        expect(info.getBaseUriBuilder()).andReturn(builder);
        expect(builder.path(VersionResource.class)).andReturn(builder);
        expect(builder.path(version.getId().toString())).andReturn(builder);
        expect(builder.build()).andReturn(uri);

        expect(versionService.isOverdue(version)).andReturn(true);

        expect(dateFieldFormat.format(version.getReleaseDate())).andReturn(datePretty);

        expect(authContext.getLoggedInUser()).andReturn(fred);
        ArrayList<SimpleLink> operations = new ArrayList<SimpleLink>();
        SimpleLinkImpl link1 = new SimpleLinkImpl("link1", "Link One", "Link One Title", null, "style 1", "", null);
        SimpleLinkImpl link2 = new SimpleLinkImpl("link2", "Link Two", "Link Two Title", null, "style 2", "", null);
        operations.add(link1);
        operations.add(link2);
        expect(simpleLinkManager.getLinksForSection(VersionBeanFactory.VERSION_OPERATIONS_WEB_LOCATION, fred, helper)).andReturn(operations);

        replayMocks();

        VersionBeanFactoryImpl factory = new VersionBeanFactoryImpl(versionService, info, dateFieldFormat, authContext, simpleLinkManager);
        VersionBean bean = factory.createVersionBean(version, true);

        ArrayList<SimpleLinkBean> expectedOperations = new ArrayList<SimpleLinkBean>();
        expectedOperations.add(new SimpleLinkBean(link1));
        expectedOperations.add(new SimpleLinkBean(link2));
        VersionBean expected = new VersionBean(version, true, datePretty, uri, expectedOperations);
        expected.setExpand("operations");
        assertThat(bean, reflectionEqualTo(expected));
    }

    @Test
    public void testCreateVersionBeanMultiple() throws Exception
    {
        URI uri = new URI("http://localhost:8090/jira");
        String datePretty = "Great Date";

        MockVersion version1 = new MockVersion();
        version1.setId(67473784534L);

        MockVersion version2 = new MockVersion();
        version2.setId(54748L);
        version2.setReleaseDate(new Date());


        expect(info.getBaseUriBuilder()).andReturn(builder);
        expect(builder.path(VersionResource.class)).andReturn(builder);
        expect(builder.path(version1.getId().toString())).andReturn(builder);
        expect(builder.build()).andReturn(uri);

        expect(info.getBaseUriBuilder()).andReturn(builder);
        expect(builder.path(VersionResource.class)).andReturn(builder);
        expect(builder.path(version2.getId().toString())).andReturn(builder);
        expect(builder.build()).andReturn(uri);

        expect(versionService.isOverdue(version2)).andReturn(false);
        expect(dateFieldFormat.format(version2.getReleaseDate())).andReturn(datePretty);

        replayMocks();

        VersionBeanFactoryImpl factory = new VersionBeanFactoryImpl(versionService, info, dateFieldFormat, authContext, simpleLinkManager);
        List<VersionBean> beans = factory.createVersionBeans(Lists.newArrayList(version1, version2));

        assertThat(beans, Matchers.<VersionBean>hasItems(
                reflectionEqualTo(new VersionBean(version1, null, null, uri)),
                reflectionEqualTo(new VersionBean(version2, false, datePretty, uri))
        ));
        assertThat(beans.size(), equalTo(2));
    }

    @Before
    public void setUp() throws Exception
    {
        EasyMockAnnotations.initMocks(this);
    }

    protected void replayMocks()
    {
        EasyMockAnnotations.replayMocks(this);
    }
}
