package com.atlassian.upm.rest.resources.install;

import java.io.File;
import java.net.URI;

import com.atlassian.upm.PluginInstaller;
import com.atlassian.upm.SelfUpdateController;
import com.atlassian.upm.log.AuditLogService;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.rest.async.AsynchronousTask;
import com.atlassian.upm.rest.representations.PluginRepresentation;
import com.atlassian.upm.test.TestRepresentationBuilder.PluginRepresentationBuilder;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.integrationtesting.ApplicationPropertiesImpl.getStandardApplicationProperties;
import static com.atlassian.upm.api.util.Option.option;
import static com.atlassian.upm.rest.resources.install.InstallStatus.State.COMPLETE;
import static com.atlassian.upm.rest.resources.install.InstallStatus.State.ERR;
import static com.atlassian.upm.rest.resources.install.InstallStatus.State.INSTALLING;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InstallFromFileTaskTest
{
    private static final String FILENAME = "file";
    private static final File FILE = new File("/path/to/some/" + FILENAME);

    @Mock PluginInstaller installer;
    @Mock SelfUpdateController selfUpdateController;
    @Mock AuditLogService auditLogger;
    UpmUriBuilder uriBuilder = new UpmUriBuilder(getStandardApplicationProperties());

    AsynchronousTask<InstallStatus> task;
    private static final String USER = "dummy";

    @Before
    public void setUp()
    {
        when(selfUpdateController.isUpmPlugin(FILE)).thenReturn(false);
        task = new InstallFromFileTask(option(FILE.getName()), FILE, USER, installer, selfUpdateController);
    }

    @Test
    public void assertThatStatusAfterCallingAcceptIsInstalling()
    {
        task.accept();
        assertThat(task.getRepresentation(uriBuilder).getContentType(), is(equalTo(INSTALLING.getContentType())));
    }

    @Test
    public void assertThatTaskContainsUsernameInRepresentation()
    {
        task.accept();
        assertThat(task.getRepresentation(uriBuilder).getUsername(), is(equalTo(USER)));
    }

    @Test
    public void assertThatTaskContainsNonNullTimestampInRepresentation()
    {
        task.accept();
        assertThat(task.getRepresentation(uriBuilder).getTimestamp(), is(not(equalTo(null))));
    }

    @Test
    public void assertThatStatusAfterInstallCompletesIsComplete() throws Exception
    {
        when(installer.install(FILE, FILENAME)).thenReturn(newPluginRepresentation(URI.create("/some/plugin/uri")));

        task.accept();
        task.call();
        assertThat(task.getRepresentation(uriBuilder).getContentType(), is(equalTo(COMPLETE.getContentType())));
    }

    @Test
    public void assertThatTaskReturnsNewPluginUri() throws Exception
    {
        when(installer.install(FILE, FILENAME)).thenReturn(newPluginRepresentation(URI.create("/some/plugin/uri")));

        task.accept();
        assertThat(task.call(), is(equalTo(URI.create("/some/plugin/uri"))));
    }

    @Test
    public void assertThatStatusAfterExceptionThrownIsErr() throws Exception
    {
        when(installer.install(FILE, FILENAME)).thenThrow(new RuntimeException());
        task.accept();
        task.call();
        assertThat(task.getRepresentation(uriBuilder).getContentType(), is(equalTo(ERR.getContentType())));
    }

    private PluginRepresentation newPluginRepresentation(URI selfLink)
    {
        return new PluginRepresentationBuilder().links(ImmutableMap.of("self", selfLink)).build();
    }
}
