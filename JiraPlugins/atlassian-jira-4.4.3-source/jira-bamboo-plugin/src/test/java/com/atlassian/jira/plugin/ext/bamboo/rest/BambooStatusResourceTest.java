package com.atlassian.jira.plugin.ext.bamboo.rest;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.jira.plugin.ext.bamboo.PluginConstants;
import com.atlassian.jira.plugin.ext.bamboo.applinks.BambooApplicationLinkManager;
import com.atlassian.jira.plugin.ext.bamboo.model.BuildState;
import com.atlassian.jira.plugin.ext.bamboo.model.LifeCycleState;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanKeys;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanResultKey;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanStatus;
import com.atlassian.jira.plugin.ext.bamboo.model.RestResult;
import com.atlassian.jira.plugin.ext.bamboo.service.BambooReleaseService;
import com.atlassian.jira.plugin.ext.bamboo.service.BambooRestService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import junit.framework.TestCase;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

public class BambooStatusResourceTest extends TestCase
{
    public void testForceRelease() throws Exception
    {
        assertForceRefreshFlag(true, BuildState.SUCCESS.name(), BuildState.SUCCESS, LifeCycleState.FINISHED,  false);
        assertForceRefreshFlag(false, BuildState.FAILED.name(), BuildState.FAILED, LifeCycleState.FINISHED,  false);
        assertForceRefreshFlag(false, null, BuildState.UNKNOWN, LifeCycleState.IN_PROGRESS,  false);
        assertForceRefreshFlag(false, null, BuildState.FAILED, LifeCycleState.FINISHED,  true);
        assertForceRefreshFlag(false, null, BuildState.SUCCESS, LifeCycleState.FINISHED,  true);
        assertForceRefreshFlag(false, BuildState.SUCCESS.name(), BuildState.SUCCESS, LifeCycleState.FINISHED,  false);
    }

    public void assertForceRefreshFlag(final boolean versionReleased,
                                       final String storedState,
                                       final BuildState statusState,
                                       final LifeCycleState lifeCycleState,
                                       final boolean shouldRelease) throws Exception
    {
        // setup
        Map<String, String> buildData = new HashMap<String, String>();
        buildData.put(PluginConstants.PS_BUILD_COMPLETED_STATE, storedState);

        PlanResultKey planResultKey = PlanKeys.getPlanResultKey("BAM-MAIN-23");
        long versionId = 1234;
        final PlanStatus planStatus = new PlanStatus(planResultKey, statusState, lifeCycleState, true);
        String TESTPROJECT = "TESTPROJECT";

        ApplicationLinkRequestFactory factory = mock(ApplicationLinkRequestFactory.class);
        ApplicationLink link = mock(ApplicationLink.class);
        when(link.createAuthenticatedRequestFactory()).thenReturn(factory);
        BambooApplicationLinkManager applicationLinkManager = mock(BambooApplicationLinkManager.class);
        when(applicationLinkManager.getApplicationLink(anyString())).thenReturn(link);

        Project project = mock(Project.class);
        when(project.getKey()).thenReturn(TESTPROJECT);

        Version version = mock(Version.class);
        when(version.getProjectObject()).thenReturn(project);
        when(version.isReleased()).thenReturn(versionReleased);

        VersionManager versionManager = mock(VersionManager.class);
        when(versionManager.getVersion(versionId)).thenReturn(version);

        BambooRestService restService = mock(BambooRestService.class);
        when(restService.getPlanResultJson(factory, planResultKey)).thenReturn(new RestResult<JSONObject>(new JSONObject(), Collections.<String>emptyList()));

        BambooReleaseService releaseService = mock(BambooReleaseService.class);
        when(releaseService.getBuildData(anyString(), anyLong())).thenReturn(buildData);


        BambooStatusResource statusResource = new BambooStatusResource(versionManager, restService, releaseService, applicationLinkManager)
        {
            @Override
            protected PlanStatus getPlanStatusFromJSON(PlanResultKey planResultKey, JSONObject bambooJsonObject) throws JSONException
            {
                return planStatus;
            }
        };

        // perform
        statusResource.getStatus(planResultKey.getKey(), versionId);

        // verify
        if (shouldRelease)
        {
            verify(releaseService, times(1)).releaseIfRequired(planStatus, version);
        }
        else
        {
            verify(releaseService, never()).releaseIfRequired(planStatus, version);
        }
    }
}
