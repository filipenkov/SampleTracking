package com.atlassian.upm.rest.resources.updateall;

import java.io.File;
import java.net.URI;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.WebApplicationException;

import com.atlassian.plugin.PluginException;
import com.atlassian.plugins.domain.model.plugin.Plugin;
import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.executor.ThreadLocalDelegateExecutorFactory;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.PluginDownloadService;
import com.atlassian.upm.PluginDownloadService.ProgressTracker;
import com.atlassian.upm.PluginInstaller;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.license.internal.PluginLicenseRepository;
import com.atlassian.upm.log.AuditLogService;
import com.atlassian.upm.pac.PacClient;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.rest.representations.RepresentationFactory;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;
import com.atlassian.upm.token.TokenManager;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static com.atlassian.integrationtesting.ApplicationPropertiesImpl.getStandardApplicationProperties;
import static com.atlassian.upm.test.UpmMatchers.downloadFailedFor;
import static com.atlassian.upm.test.UpmMatchers.updateFailureOf;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateAllResourceTest
{
    private static final String GOOD_TOKEN = "yay";
    private static final String BAD_TOKEN = "boo";
    
    @Mock PluginAccessorAndController pluginAccessorAndController;
    SingleThreadedAsynchronousTaskManager taskManager;
    @Mock PacClient pacClient;
    @Mock PluginDownloadService pluginDownloadService;
    @Mock PluginInstaller pluginInstaller;
    @Mock RepresentationFactory representationFactory;
    @Mock PermissionEnforcer permissionEnforcer;
    @Mock AuditLogService auditLogger;
    @Mock UserManager userManager;
    @Mock TokenManager tokenManager;
    @Mock ThreadLocalDelegateExecutorFactory factory;
    @Mock PluginLicenseRepository licenseRepository;
    ApplicationProperties applicationProperties;
    UpmUriBuilder uriBuilder;

    UpdateAllResource resource;

    private static final String USERNAME = "admin";

    @Before
    public void createUpdateAllResource()
    {
        when(userManager.getRemoteUsername()).thenReturn(USERNAME);
        when(factory.createExecutorService(Matchers.<ExecutorService>anyObject())).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                return invocationOnMock.getArguments()[0];
            }
        });
        applicationProperties = getStandardApplicationProperties();
        uriBuilder = new UpmUriBuilder(applicationProperties);
        taskManager = new SingleThreadedAsynchronousTaskManager(factory, uriBuilder, userManager);
        resource = new UpdateAllResource(pluginAccessorAndController, taskManager, pacClient, pluginDownloadService,
            pluginInstaller, representationFactory, permissionEnforcer, auditLogger, uriBuilder, userManager, tokenManager, licenseRepository);
        
        when(pluginAccessorAndController.getUpmPluginKey()).thenReturn("arbitrary key that won't be matched");
        when(licenseRepository.getPluginLicense(anyString())).thenReturn(Option.none(PluginLicense.class));
        when(tokenManager.attemptToMatchAndInvalidateToken(USERNAME, GOOD_TOKEN)).thenReturn(true);
        when(tokenManager.attemptToMatchAndInvalidateToken(USERNAME, BAD_TOKEN)).thenReturn(false);
    }

    @Test(expected=WebApplicationException.class)
    public void assertThatMissingTokenCausesError()
    {
        resource.updateAll(null);
    }

    @Test(expected=WebApplicationException.class)
    public void assertThatBadTokenCausesError()
    {
        resource.updateAll(BAD_TOKEN);
    }

    @Test
    public void assertThatConflictResponseIsReturnedIfInSafeMode()
    {
        when(pluginAccessorAndController.isSafeMode()).thenReturn(true);

        assertThat(resource.updateAll(GOOD_TOKEN).getStatus(), is(equalTo(CONFLICT.getStatusCode())));
    }

    @Test
    public void verifyThatTaskIsSubmittedWhenNotInSafeMode()
    {
        resource.updateAll(GOOD_TOKEN);

        assertTrue(taskManager.wasTaskSubmitted());
    }

    @Test
    public void assertThatWhileFindingPluginUpdatesTaskStatusIsFindingUpdates() throws Exception
    {
        final CyclicBarrier barrier = new CyclicBarrier(2);
        whenGettingUpdates(waitOn(barrier, thenReturn(ImmutableList.<PluginVersion>of())));

        resource.updateAll(GOOD_TOKEN);
        barrier.await(1, TimeUnit.SECONDS);      // wait for first barrier to get hit which means that the process should be in the "finding updates" state

        assertThat(
            taskManager.getCurrentlyRunningTask().getRepresentation(uriBuilder).getContentType(),
            is(equalTo(UpdateStatus.State.FINDING_UPDATES.getContentType()))
        );
        barrier.await(1, TimeUnit.SECONDS);      // releases the second barrier so that the update thread can complete
    }

    @Test
    public void assertThatWhileDownloadingPluginUpdatesTaskStatusIsDownloadingPlugin() throws Exception
    {
        final CyclicBarrier barrier = new CyclicBarrier(2);
        whenGettingUpdates(returnUpdates());
        whenDownloading(waitOn(barrier, thenReturn(new File("/some/file"))));

        resource.updateAll(GOOD_TOKEN);
        barrier.await(1, TimeUnit.SECONDS);      // wait for first barrier to get hit which means that the process should be in the "downloading plugin" state

        assertThat(
            taskManager.getCurrentlyRunningTask().getRepresentation(uriBuilder).getContentType(),
            is(equalTo(UpdateStatus.State.DOWNLOADING.getContentType()))
        );
        barrier.await(1, TimeUnit.SECONDS);      // releases the second barrier so that the update thread can complete
    }

    @Test
    public void assertThatWhileUpdatingTaskStatusIsUpdatingPlugin() throws Exception
    {
        final CyclicBarrier barrier = new CyclicBarrier(2);
        whenGettingUpdates(returnUpdates());
        whenDownloading(returnFile());
        whenUpdating(waitOn(barrier));

        resource.updateAll(GOOD_TOKEN);
        barrier.await(1, TimeUnit.SECONDS);      // wait for first barrier to get hit which means that the process should be in the "updating plugin" state

        assertThat(
            taskManager.getCurrentlyRunningTask().getRepresentation(uriBuilder).getContentType(),
            is(equalTo(UpdateStatus.State.UPDATING.getContentType()))
        );
        barrier.await(1, TimeUnit.SECONDS);      // releases the second barrier so that the update thread can complete
    }

    @Test
    public void assertThatDownloadFailuresAreReported() throws Exception
    {
        whenGettingUpdates(returnUpdates());

        final CyclicBarrier barrier = new CyclicBarrier(2);
        whenDownloading(waitOn(barrier, throwResponseException())); // we need to pause the exectution so we can grab the task

        resource.updateAll(GOOD_TOKEN);
        barrier.await(1, TimeUnit.SECONDS);    // wait for first barrier to get hit which means that the process should be in the "downloading plugin" state

        UpdateAllTask task = (UpdateAllTask) taskManager.getCurrentlyRunningTask();
        barrier.await(1, TimeUnit.SECONDS);    // release the second barrier so that the update thread can complete

        waitForTaskCompletion(task);

        assertThat(task.getResults().getFailures(), contains(downloadFailedFor("Test plugin", "test.plugin", "1.1")));
    }

    @Test
    public void assertThatUpdateFailuresAreReported() throws Exception
    {
        whenGettingUpdates(returnUpdates());

        final CyclicBarrier barrier = new CyclicBarrier(2);
        whenDownloading(returnFile());
        whenUpdating(waitOn(barrier, throwPluginInstallException()));

        resource.updateAll(GOOD_TOKEN);
        barrier.await(1, TimeUnit.SECONDS);    // wait for first barrier to get hit which means that the process should be in the "updating" state

        UpdateAllTask task = (UpdateAllTask) taskManager.getCurrentlyRunningTask();
        barrier.await(1, TimeUnit.SECONDS);    // release the second barrier so that the update thread can complete

        waitForTaskCompletion(task);

        assertThat(task.getResults().getFailures(), contains(updateFailureOf("Test plugin", "test.plugin", "1.1")));
    }

    private Answer<Void> waitOn(final CyclicBarrier barrier)
    {
        return waitOn(barrier, this.<Void>thenReturn(null));
    }

    private <T> Answer<T> waitOn(final CyclicBarrier barrier, final Answer<T> answer)
    {
        return new Answer<T>()
        {
            public T answer(InvocationOnMock invocation) throws Throwable
            {
                barrier.await(1, TimeUnit.SECONDS);    // barrier to release the junit thread so it can do the assertion
                // now that it is sure we're in the correct state

                barrier.await(1, TimeUnit.SECONDS);    // wait in the current state until the assertion has been made 
                return answer.answer(invocation);
            }
        };
    }

    private <T> Answer<T> thenReturn(final T returnValue)
    {
        return new Answer<T>()
        {
            public T answer(InvocationOnMock invocation) throws Throwable
            {
                return returnValue;
            }
        };
    }

    private void waitForTaskCompletion(UpdateAllTask task) throws InterruptedException
    {
        while (!task.getRepresentation(uriBuilder).getStatus().isDone())
        {
            Thread.sleep(100);
        }
    }

    private void whenGettingUpdates(Answer<ImmutableList<PluginVersion>> answer)
    {
        when(pacClient.getUpdates()).thenAnswer(answer);
    }

    private Answer<ImmutableList<PluginVersion>> returnUpdates()
    {
        return new Answer<ImmutableList<PluginVersion>>()
        {
            public ImmutableList<PluginVersion> answer(InvocationOnMock invocation) throws Throwable
            {
                Plugin plugin = new Plugin();
                plugin.setName("Test plugin");
                plugin.setPluginKey("test.plugin");

                PluginVersion pv = new PluginVersion();
                pv.setPlugin(plugin);
                pv.setVersion("1.1");
                pv.setBinaryUrl("http://binary/url");

                return ImmutableList.of(pv);
            }
        };
    }

    private void whenDownloading(Answer<File> answer) throws ResponseException
    {
        when(pluginDownloadService.downloadPlugin(isA(URI.class), anyString(), anyString(), isA(ProgressTracker.class))).thenAnswer(answer);
    }

    private Answer<File> throwResponseException()
    {
        return new Answer<File>()
        {
            public File answer(InvocationOnMock invocation) throws Throwable
            {
                throw new ResponseException("Fake exception");
            }
        };
    }

    private Answer<File> returnFile()
    {
        return new Answer<File>()
        {
            public File answer(InvocationOnMock invocation) throws Throwable
            {
                return new File("/some/file");
            }
        };
    }

    private void whenUpdating(Answer<Void> answer)
    {
        when(pluginInstaller.update(isA(File.class), isA(String.class))).thenAnswer(answer);
    }


    private Answer<Void> throwPluginInstallException()
    {
        return new Answer<Void>()
        {
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                throw new PluginException("Fake exception");
            }
        };
    }
}
