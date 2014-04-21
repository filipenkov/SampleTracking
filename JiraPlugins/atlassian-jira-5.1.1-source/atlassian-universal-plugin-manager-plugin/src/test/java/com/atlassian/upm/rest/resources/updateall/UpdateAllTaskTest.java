package com.atlassian.upm.rest.resources.updateall;

import java.io.File;
import java.net.URI;

import com.atlassian.plugins.domain.model.plugin.Plugin;
import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.PluginDownloadService;
import com.atlassian.upm.PluginDownloadService.Progress;
import com.atlassian.upm.PluginDownloadService.ProgressTracker;
import com.atlassian.upm.PluginInstaller;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.license.internal.PluginLicenseRepository;
import com.atlassian.upm.log.AuditLogService;
import com.atlassian.upm.pac.PacClient;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.rest.async.AsynchronousTask;
import com.atlassian.upm.rest.representations.PluginRepresentation;
import com.atlassian.upm.test.TestRepresentationBuilder.PluginRepresentationBuilder;

import com.google.common.collect.ImmutableList;
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
import static com.atlassian.upm.rest.resources.updateall.UpdateFailed.Type.INSTALL;
import static com.atlassian.upm.rest.resources.updateall.UpdateStatus.State.COMPLETE;
import static com.atlassian.upm.rest.resources.updateall.UpdateStatus.State.ERR;
import static com.atlassian.upm.rest.resources.updateall.UpdateStatus.State.FINDING_UPDATES;
import static com.atlassian.upm.rest.resources.updateall.UpdateStatus.State.UPDATING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateAllTaskTest
{
    private static final URI uri = URI.create("http://example.com/some/file");
    private static final URI upmUri = URI.create("http://example.com/the/upm");
    private static final File FILE = new File("/some/file");
    private static final File UPM_FILE = new File("/upm/file");
    private static final String PLUGIN_NAME = "Test plugin";
    private static final String PLUGIN_KEY = "test-plugin-key";
    private static final String UPM_PLUGIN_NAME = "UPM";
    private static final String UPM_PLUGIN_KEY = "the-upm-plugin-key";
    private static final PluginVersion PLUGIN_VERSION = newPluginVersion(PLUGIN_NAME, PLUGIN_KEY, "1.2", uri);
    private static final PluginVersion NON_DEPLOYABLE_PLUGIN_VERSION = newNonDeployablePluginVersion(PLUGIN_NAME, PLUGIN_KEY, "1.2", uri);
    private static final PluginVersion UPM_PLUGIN_VERSION = newPluginVersion(UPM_PLUGIN_NAME, UPM_PLUGIN_KEY, "1.7", upmUri);

    @Mock PacClient pacClient;
    @Mock PluginDownloadService downloader;
    @Mock PluginAccessorAndController accessor;
    @Mock PluginInstaller installer;
    @Mock AuditLogService auditLogger;
    @Mock PluginLicenseRepository licenseRepository;
    UpmUriBuilder uriBuilder = new UpmUriBuilder(getStandardApplicationProperties());

    AsynchronousTask<UpdateStatus> task;

    @Before
    public void createUpdateAllTask()
    {
        task = new UpdateAllTask(pacClient, downloader, accessor, installer, auditLogger, uriBuilder, licenseRepository, "user");
        
        when(accessor.getUpmPluginKey()).thenReturn(UPM_PLUGIN_KEY);
        when(licenseRepository.getPluginLicense(anyString())).thenReturn(Option.none(PluginLicense.class));
    }

    @Test
    public void assertThatStatusAfterCallingAcceptIsFindingUpdates()
    {
        task.accept();
        assertThat(task.getRepresentation(uriBuilder).getContentType(), is(equalTo(FINDING_UPDATES.getContentType())));
    }

    @Test
    public void assertThatAfterUpdatesAreFoundStatusIsDownloading() throws Exception
    {
        when(pacClient.getUpdates()).thenReturn(ImmutableList.of(PLUGIN_VERSION));

        final UpdateStatus[] status = new UpdateStatus[1];

        task.accept();
        // this is nasty but it is the best way to get the status after the tracker has been notified of progress
        when(downloader.downloadPlugin(eq(uri), anyString(), anyString(), any(ProgressTracker.class))).thenAnswer(new Answer<File>()
        {
            public File answer(InvocationOnMock invocation) throws Throwable
            {
                ProgressTracker tracker = (ProgressTracker) invocation.getArguments()[3];
                tracker.notify(new Progress(50L, 100L, none(String.class)));

                status[0] = task.getRepresentation(uriBuilder).getStatus();
                return FILE;
            }
        });

        task.call();
        assertThat(status[0], isDownloadingWithProgress(uri, 50L, 100L));
    }

    @Test
    public void assertThatStatusAfterDownloadingCompletesIsUpdating() throws Exception
    {
        when(pacClient.getUpdates()).thenReturn(ImmutableList.of(PLUGIN_VERSION));

        final UpdateStatus[] status = new UpdateStatus[1];

        when(downloader.downloadPlugin(eq(uri), anyString(), anyString(), any(ProgressTracker.class))).thenReturn(FILE);
        when(installer.update(FILE, PLUGIN_NAME)).thenAnswer(new Answer<PluginRepresentation>()
        {
            public PluginRepresentation answer(InvocationOnMock invocation) throws Throwable
            {
                status[0] = task.getRepresentation(uriBuilder).getStatus();
                return newPluginRepresentation(URI.create("/some/plugin/uri"));
            }
        });

        task.accept();
        task.call();
        assertThat(status[0].getContentType(), is(equalTo(UPDATING.getContentType())));
    }

    @Test
    public void assertThatStatusAfterInstallCompletesIsComplete() throws Exception
    {
        when(pacClient.getUpdates()).thenReturn(ImmutableList.of(PLUGIN_VERSION));
        when(downloader.downloadPlugin(eq(uri), anyString(), anyString(), any(ProgressTracker.class))).thenReturn(FILE);
        when(installer.update(FILE, PLUGIN_NAME)).thenReturn(newPluginRepresentation(URI.create("/some/plugin/uri")));

        task.accept();
        task.call();
        assertThat(task.getRepresentation(uriBuilder).getContentType(), is(equalTo(COMPLETE.getContentType())));
    }

    @Test
    public void assertThatStatusAfterExceptionThrownIsErr() throws Exception
    {
        task.accept();
        task.call();
        assertThat(task.getRepresentation(uriBuilder).getContentType(), is(equalTo(ERR.getContentType())));
    }

    @Test
    public void assertThatNonDeployablePluginIsNotUpdated() throws Exception
    {
        when(pacClient.getUpdates()).thenReturn(ImmutableList.of(NON_DEPLOYABLE_PLUGIN_VERSION));

        task.accept();
        task.call();
        UpdateAllResults results = (UpdateAllResults) task.getRepresentation(uriBuilder).getStatus();
        assertThat(results.getFailures(), contains(udateInstallFailure(NON_DEPLOYABLE_PLUGIN_VERSION, "not.deployable")));
    }

    @Test
    public void assertThatUpmPluginIsSkipped() throws Exception
    {
        when(pacClient.getUpdates()).thenReturn(ImmutableList.of(UPM_PLUGIN_VERSION, PLUGIN_VERSION));
        when(installer.update(FILE, PLUGIN_NAME)).thenReturn(newPluginRepresentation(URI.create("/some/plugin/uri")));

        final boolean[] downloadedUpm = new boolean[] { false };
        
        when(downloader.downloadPlugin(eq(uri), anyString(), anyString(), any(ProgressTracker.class))).thenReturn(FILE);
        when(installer.update(FILE, PLUGIN_NAME)).thenReturn(newPluginRepresentation(URI.create("/some/plugin/uri")));
        
        when(downloader.downloadPlugin(eq(upmUri), anyString(), anyString(), any(ProgressTracker.class))).thenAnswer(new Answer<File>()
        {
            public File answer(InvocationOnMock invocation) throws Throwable
            {
                downloadedUpm[0] = true;
                return UPM_FILE;
            }
        });
        when(installer.update(UPM_FILE, UPM_PLUGIN_NAME)).thenThrow(new IllegalStateException());
        
        task.accept();
        task.call();
        UpdateAllResults results = (UpdateAllResults) task.getRepresentation(uriBuilder).getStatus();
        assertThat(results.getSuccesses(), contains(updateInstallSuccess(PLUGIN_VERSION)));
        assertFalse(downloadedUpm[0]);
    }

    private Matcher<UpdateSucceeded> updateInstallSuccess(final PluginVersion plugin)
    {
        return new TypeSafeDiagnosingMatcher<UpdateSucceeded>()
        {
            @Override
            protected boolean matchesSafely(UpdateSucceeded item, Description mismatchDescription)
            {
                if (!item.getName().equals(plugin.getPlugin().getName())
                    || !item.getVersion().equals(plugin.getVersion()))
                {
                    mismatchDescription.appendText("update install success for ")
                        .appendValue(item.getName())
                        .appendText(" version ")
                        .appendValue(item.getVersion());
                    return false;
                }
                return true;
            }

            public void describeTo(Description description)
            {
                description.appendText("update install success for ")
                    .appendValue(plugin.getPlugin().getName())
                    .appendText(" version ")
                    .appendValue(plugin.getVersion());
            }
        };
    }

    private Matcher<UpdateFailed> udateInstallFailure(final PluginVersion plugin, final String failure)
    {
        return new TypeSafeDiagnosingMatcher<UpdateFailed>()
        {
            @Override
            protected boolean matchesSafely(UpdateFailed item, Description mismatchDescription)
            {
                if (!item.getName().equals(plugin.getPlugin().getName())
                    || !item.getVersion().equals(plugin.getVersion())
                    || !item.getType().equals(INSTALL)
                    || !item.getSubCode().equals(failure))
                {
                    mismatchDescription.appendText("update install failure for ")
                        .appendValue(item.getName())
                        .appendText(" version ")
                        .appendValue(item.getVersion())
                        .appendText(" saying ")
                        .appendValue(failure);
                    return false;
                }
                return true;
            }

            public void describeTo(Description description)
            {
                description.appendText("update install failure for ")
                    .appendValue(plugin.getPlugin().getName())
                    .appendText(" version ")
                    .appendValue(plugin.getVersion())
                    .appendText(" saying ")
                    .appendValue(failure);
            }
        };
    }

    private static Matcher<? super UpdateStatus> isDownloadingWithProgress(final URI uri, final long l, final long m)
    {
        return new TypeSafeDiagnosingMatcher<UpdateStatus>()
        {
            @Override
            protected boolean matchesSafely(UpdateStatus status, Description mismatchDescription)
            {
                if (!(status instanceof DownloadingPluginStatus))
                {
                    mismatchDescription.appendText("status is of type ").appendValue(status.getClass().getSimpleName());
                    return false;
                }
                DownloadingPluginStatus d = (DownloadingPluginStatus) status;
                if (!d.getUri().equals(uri) || d.getAmountDownloaded() != l || d.getTotalSize() != m)
                {
                    mismatchDescription.appendText("progress for ").appendValue(uri).appendText(" is ").appendValue(d.getAmountDownloaded()).appendText(" / ").appendValue(d.getTotalSize());
                    return false;
                }
                return true;
            }

            public void describeTo(Description description)
            {
                description.appendText("progress of ").appendValue(uri).appendText(" to be ").appendValue(l).appendText(" / ").appendValue(m);
            }
        };
    }

    private static final PluginVersion newPluginVersion(String name, String key, String version, URI uri)
    {
        Plugin p = new Plugin();
        p.setName(name);
        p.setPluginKey(key);

        PluginVersion pv = new PluginVersion();
        pv.setPlugin(p);
        pv.setVersion(version);
        pv.setBinaryUrl(uri.toASCIIString());
        return pv;
    }

    private static final PluginVersion newNonDeployablePluginVersion(String name, String key, String version, URI uri)
    {
        PluginVersion pv = newPluginVersion(name, key, version, uri);
        pv.getPlugin().setDeployable(false);
        return pv;
    }

    private PluginRepresentation newPluginRepresentation(URI selfLink)
    {
        return new PluginRepresentationBuilder().links(ImmutableMap.of("self", selfLink)).build();
    }
}
