package com.atlassian.upm.rest.resources.install;

import java.io.File;
import java.net.URI;

import com.atlassian.upm.PluginDownloadService;
import com.atlassian.upm.PluginDownloadService.Progress;
import com.atlassian.upm.PluginDownloadService.ProgressTracker;
import com.atlassian.upm.PluginInstaller;
import com.atlassian.upm.SelfUpdateController;
import com.atlassian.upm.log.AuditLogService;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.rest.async.AsynchronousTask;
import com.atlassian.upm.rest.representations.PluginRepresentation;
import com.atlassian.upm.rest.resources.install.InstallStatus.DownloadStatus;
import com.atlassian.upm.test.TestRepresentationBuilder.PluginRepresentationBuilder;

import com.google.common.collect.ImmutableMap;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static com.atlassian.integrationtesting.ApplicationPropertiesImpl.getStandardApplicationProperties;
import static com.atlassian.upm.api.util.Option.none;
import static com.atlassian.upm.rest.resources.install.InstallStatus.State.COMPLETE;
import static com.atlassian.upm.rest.resources.install.InstallStatus.State.DOWNLOADING;
import static com.atlassian.upm.rest.resources.install.InstallStatus.State.ERR;
import static com.atlassian.upm.rest.resources.install.InstallStatus.State.INSTALLING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InstallFromUriTaskTest
{

    private static final String URI_VALUE = "http://example.com/some/file";
    private static final URI uri = URI.create(URI_VALUE);
    private static final File FILE = new File("/some/file");

    @Mock PluginDownloadService downloader;
    @Mock PluginInstaller installer;
    @Mock SelfUpdateController selfUpdateController;
    @Mock AuditLogService auditLogger;
    private InstallStatus status;
    UpmUriBuilder uriBuilder = new UpmUriBuilder(getStandardApplicationProperties());

    AsynchronousTask<InstallStatus> task;

    @Before
    public void setUp()
    {
        task = new InstallFromUriTask(uri, downloader, auditLogger, "dummy", installer, selfUpdateController);
        when(selfUpdateController.isUpmPlugin(FILE)).thenReturn(false);
    }

    @Test
    public void assertThatStatusAfterCallingAcceptIsDownloading()
    {
        task.accept();
        assertThat(task.getRepresentation(uriBuilder).getContentType(), is(equalTo(DOWNLOADING.getContentType())));
    }

    @Test
    public void assertThatStatusIsUpdatedWithProgressInformationWhileDownloading() throws Exception
    {
        useMockDownloaderThatSetsDownloadingStatus();

        task.accept();
        task.call();
        assertThat(status, isDownloadingWithProgress(uri, 50L, 100L));
    }

    @Test
    public void assertThatStatusAfterDownloadingCompletesIsInstalling() throws Exception
    {
        when(downloader.downloadPlugin(eq(uri), anyString(), anyString(), any(ProgressTracker.class))).thenReturn(FILE);
        useMockDownloaderThatSetsDownloadingStatus();

        when(installer.install(FILE, URI_VALUE)).thenAnswer(new Answer<PluginRepresentation>()
        {
            public PluginRepresentation answer(InvocationOnMock invocation) throws Throwable
            {
                status = task.getRepresentation(uriBuilder).getStatus();
                return newPluginRepresentation(URI.create("/some/plugin/uri"));
            }
        });

        task.accept();
        task.call();
        assertThat(status.getContentType(), is(equalTo(INSTALLING.getContentType())));
    }

    @Test
    public void assertThatStatusAfterInstallCompletesIsComplete() throws Exception
    {
        when(downloader.downloadPlugin(eq(uri), anyString(), anyString(), any(ProgressTracker.class))).thenReturn(FILE);
        when(installer.install(FILE, URI_VALUE)).thenReturn(newPluginRepresentation(URI.create("/some/plugin/uri")));

        task.accept();
        task.call();
        assertThat(task.getRepresentation(uriBuilder).getContentType(), is(equalTo(COMPLETE.getContentType())));
    }

    @Test
    public void assertThatTaskReturnsNewPluginUri() throws Exception
    {
        when(downloader.downloadPlugin(eq(uri), anyString(), anyString(), any(ProgressTracker.class))).thenReturn(FILE);
        when(installer.install(FILE, URI_VALUE)).thenReturn(newPluginRepresentation(URI.create("/some/plugin/uri")));

        task.accept();
        assertThat(task.call(), is(equalTo(URI.create("/some/plugin/uri"))));
    }

    @Test
    public void assertThatStatusAfterExceptionThrownIsErr() throws Exception
    {
        when(installer.install(FILE, URI_VALUE)).thenThrow(new RuntimeException());
        task.accept();
        task.call();
        assertThat(task.getRepresentation(uriBuilder).getContentType(), is(equalTo(ERR.getContentType())));
    }

    private PluginRepresentation newPluginRepresentation(URI selfLink)
    {
        return new PluginRepresentationBuilder().links(ImmutableMap.of("self", selfLink)).build();
    }

    private void useMockDownloaderThatSetsDownloadingStatus() throws Exception
    {
        when(downloader.downloadPlugin(eq(uri), anyString(), anyString(), any(ProgressTracker.class))).thenAnswer(new Answer<File>()
        {
            public File answer(InvocationOnMock invocation) throws Throwable
            {
                ProgressTracker tracker = (ProgressTracker) invocation.getArguments()[3];
                tracker.notify(new Progress(50L, 100L, none(String.class)));

                status = task.getRepresentation(uriBuilder).getStatus();
                return FILE;
            }
        });
    }
    
    private static Matcher<? super InstallStatus> isDownloadingWithProgress(final URI uri, final long amount, final long total)
    {
        return new TypeSafeDiagnosingMatcher<InstallStatus>()
        {
            @Override
            protected boolean matchesSafely(InstallStatus status, Description mismatchDescription)
            {
                if (!(status instanceof DownloadStatus))
                {
                    mismatchDescription.appendText("status is of type ").appendValue(status.getClass().getSimpleName());
                    return false;
                }
                DownloadStatus d = (DownloadStatus) status;
                if (!d.getSource().equals(uri.toASCIIString()) || !d.getSource().endsWith(d.getFilename()) || d.getAmountDownloaded() != amount || d.getTotalSize() != total)
                {
                    mismatchDescription.appendText("progress for ").appendValue(uri).appendText(" is ").appendValue(d.getAmountDownloaded()).appendText(" / ").appendValue(d.getTotalSize());
                    return false;
                }
                return true;
            }

            public void describeTo(Description description)
            {
                description.appendText("progress of ").appendValue(uri).appendText(" to be ").appendValue(amount).appendText(" / ").appendValue(total);
            }
        };
    }
}
