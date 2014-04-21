package com.atlassian.gadgets.opensocial.internal;

import java.util.List;
import java.util.Set;

import com.atlassian.gadgets.opensocial.OpenSocialRequestContext;
import com.atlassian.gadgets.opensocial.model.Activity;
import com.atlassian.gadgets.opensocial.model.ActivityId;
import com.atlassian.gadgets.opensocial.model.AppId;
import com.atlassian.gadgets.opensocial.spi.ActivityService;
import com.atlassian.gadgets.opensocial.spi.PersonService;
import com.atlassian.gadgets.test.PassThroughTransactionTemplate;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.transaction.TransactionTemplate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.gadgets.opensocial.internal.ShindigOpenSocialTypeAdapter.convertActivityToShindigActivity;
import static com.atlassian.gadgets.opensocial.internal.ShindigOpenSocialTypeAdapter.convertShindigActivityToActivity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ShindigActivityServiceImplTest
{

    @Mock PersonService personService;
    @Mock ActivityService activityService;
    TransactionTemplate txTemplate = new PassThroughTransactionTemplate();
    @Mock ApplicationProperties applicationProperties;

    ShindigActivityServiceImpl shindigActivityServiceImpl;

    String APP_ID_STRING = "appId";

    @Before
    public void setUp()
    {
        when(applicationProperties.getBaseUrl()).thenReturn("");
        shindigActivityServiceImpl = new ShindigActivityServiceImpl(personService, activityService, txTemplate, applicationProperties);
    }

    @Test
    public void testGetActivitiesReturnsConvertedActivitiesReturnedFromActivityService() throws Exception
    {
        String title = "title";
        ActivityId id = ActivityId.valueOf("41");
        AppId appId = AppId.valueOf("coolApp");
        Activity activity = new Activity.Builder(title).id(id).appId(appId).build();
        List<Activity> activities = ImmutableList.of(activity);
        when(activityService.getActivities(isA(Set.class), isA(AppId.class), isA(OpenSocialRequestContext.class))).thenReturn(activities);

        Set<String> fields = null;
        List<org.apache.shindig.social.opensocial.model.Activity> returnedShindigActivities = (shindigActivityServiceImpl.getActivities(ImmutableSet.<UserId>of(), GroupId.fromJson("all"), APP_ID_STRING, fields, new MockSecurityToken())).get().getEntry();
        assertThat(returnedShindigActivities.size(), is(equalTo(1)));

        // shindig activities do not implement equals, and this exercises the conversion methods
        assertThat(convertShindigActivityToActivity(returnedShindigActivities.get(0)), is(equalTo(convertShindigActivityToActivity(convertActivityToShindigActivity(activity, fields)))));
    }

    @Test
    public void testGetActivitiesWithSpecificFieldsReturnsConvertedActivitiesWithSpecificFieldsReturnedFromActivityService() throws Exception
    {
        String title = "title";
        ActivityId id = ActivityId.valueOf("41");
        AppId appId = AppId.valueOf("coolApp");
        Activity activity = new Activity.Builder(title).id(id).appId(appId).build();
        List<Activity> activities = ImmutableList.of(activity);
        when(activityService.getActivities(isA(Set.class), isA(AppId.class), isA(OpenSocialRequestContext.class))).thenReturn(activities);

        Set<String> fields = ImmutableSet.of(Activity.Field.TITLE.toString(), Activity.Field.APP_ID.toString());
        List<org.apache.shindig.social.opensocial.model.Activity> returnedShindigActivities = (shindigActivityServiceImpl.getActivities(ImmutableSet.<UserId>of(), GroupId.fromJson("all"), APP_ID_STRING, fields, new MockSecurityToken())).get().getEntry();
        assertThat(returnedShindigActivities.size(), is(equalTo(1)));
        
        // shindig activities do not implement equals, and this exercises the conversion methods
        assertThat(convertShindigActivityToActivity(returnedShindigActivities.get(0)), is(equalTo(convertShindigActivityToActivity(convertActivityToShindigActivity(activity, fields)))));
    }

    private class MockSecurityToken implements SecurityToken
    {

        public String getOwnerId()
        {
            return "ownerId";
        }

        public String getViewerId()
        {
            return "viewerId";
        }

        public String getAppId()
        {
            return "appId";
        }

        public String getDomain()
        {
            return "domain";
        }

        public String getContainer()
        {
            return "container";
        }

        public String getAppUrl()
        {
            return "appUrl";
        }

        public String getModuleId()
        {
            return "moduleId";
        }

        public String getUpdatedToken()
        {
            return "updatedToken";
        }

        public String getTrustedJson()
        {
            return "trustedJson";
        }

        public boolean isAnonymous()
        {
            return false;
        }

        public String getActiveUrl()
        {
            return "activeUrl";
        }
    }
}
