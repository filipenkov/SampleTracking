package com.atlassian.gadgets.opensocial.internal;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import com.atlassian.gadgets.opensocial.OpenSocialRequestContext;
import com.atlassian.gadgets.opensocial.model.Activity;
import com.atlassian.gadgets.opensocial.model.AppId;
import com.atlassian.gadgets.opensocial.model.Person;
import com.atlassian.gadgets.opensocial.model.PersonId;
import com.atlassian.gadgets.opensocial.spi.ActivityService;
import com.atlassian.gadgets.opensocial.spi.ActivityServiceException;
import com.atlassian.gadgets.opensocial.spi.PersonService;
import com.atlassian.gadgets.util.Uri;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.social.ResponseError;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.RestfulCollection;
import org.apache.shindig.social.opensocial.spi.SocialSpiException;
import org.apache.shindig.social.opensocial.spi.UserId;

import static com.atlassian.gadgets.opensocial.internal.ShindigOpenSocialTypeAdapter.activityToShindigActivityFunction;
import static com.atlassian.gadgets.opensocial.internal.ShindigOpenSocialTypeAdapter.convertActivityToShindigActivity;
import static com.atlassian.gadgets.opensocial.internal.ShindigOpenSocialTypeAdapter.convertShindigActivityToActivity;
import static com.atlassian.gadgets.opensocial.internal.ShindigOpenSocialTypeAdapter.convertShindigSecurityTokenToRequestContext;
import static com.atlassian.gadgets.opensocial.internal.ShindigOpenSocialTypeAdapter.getPeopleIdsFromUserIds;


/**
 * A service adapter that handles Shindig {@link org.apache.shindig.social.opensocial.spi.ActivityService} requests and maps them to {@link com.atlassian.gadgets.opensocial.spi.ActivityService} requests.
 */
public class ShindigActivityServiceImpl implements org.apache.shindig.social.opensocial.spi.ActivityService
{
    private final PersonService personService;
    private final ActivityService activityService;
    private final TransactionTemplate txTemplate;
    private final ApplicationProperties applicationProperties;

    @Inject
    public ShindigActivityServiceImpl(PersonService personService, ActivityService activityService, TransactionTemplate txTemplate, ApplicationProperties applicationProperties)
    {
        this.personService = personService;
        this.activityService = activityService;
        this.txTemplate = txTemplate;
        this.applicationProperties = applicationProperties;
    }

    @SuppressWarnings("unchecked")
    public Future<RestfulCollection<org.apache.shindig.social.opensocial.model.Activity>> getActivities(final Set<UserId> userIds, final GroupId groupId, final String appId, final Set<String> fields, final SecurityToken token) throws SocialSpiException
    {
        try
        {
            final AppId relativeAppId = AppId.valueOf(Uri.relativizeUriAgainstBase(applicationProperties.getBaseUrl(), appId).toString());
            List<Activity> activities = (List<Activity>) txTemplate.execute(new TransactionCallback()
            {
                public Object doInTransaction()
                {
                    Set<PersonId> people = getPeopleIdsFromUserIds(personService, userIds, groupId, token);
                    return activityService.getActivities(people, relativeAppId, convertShindigSecurityTokenToRequestContext(token));
                }
            });
            List<org.apache.shindig.social.opensocial.model.Activity> shindigActivities = Lists.transform(activities, activityToShindigActivityFunction(fields));
            return ImmediateFuture.newInstance(new RestfulCollection<org.apache.shindig.social.opensocial.model.Activity>(shindigActivities));
        }
        catch (ActivityServiceException e)
        {
            throw new SocialSpiException(ResponseError.INTERNAL_ERROR, e.getMessage(), e);
        }
    }

    public Future<RestfulCollection<org.apache.shindig.social.opensocial.model.Activity>> getActivities(final UserId userId, final GroupId groupId, final String appId, final Set<String> fields, final Set<String> activityIds, final SecurityToken token) throws SocialSpiException
    {
        try
        {
            final OpenSocialRequestContext openSocialRequestContext = convertShindigSecurityTokenToRequestContext(token);
            final AppId relativeAppId = AppId.valueOf(Uri.relativizeUriAgainstBase(applicationProperties.getBaseUrl(), appId).toString());
            List<Activity> activities = (List<Activity>) txTemplate.execute(new TransactionCallback()
            {
                public Object doInTransaction()
                {
                    Person person = personService.getPerson(userId.getUserId(token), openSocialRequestContext);
                    return activityService.getActivities(person.getPersonId(), relativeAppId, activityIds, openSocialRequestContext);
                }
            });
            List<org.apache.shindig.social.opensocial.model.Activity> shindigActivities = Lists.transform(activities, activityToShindigActivityFunction(fields));
            return ImmediateFuture.newInstance(new RestfulCollection<org.apache.shindig.social.opensocial.model.Activity>(shindigActivities));
        }
        catch (ActivityServiceException e)
        {
            throw new SocialSpiException(ResponseError.INTERNAL_ERROR, e.getMessage(), e);
        }
    }

    public Future<org.apache.shindig.social.opensocial.model.Activity> getActivity(final UserId userId, final GroupId groupId, final String appId, final Set<String> fields, final String activityId, final SecurityToken token) throws SocialSpiException
    {
        try
        {
            final AppId relativeAppId = AppId.valueOf(Uri.relativizeUriAgainstBase(applicationProperties.getBaseUrl(), appId).toString());
            Activity activity = (Activity) txTemplate.execute(new TransactionCallback()
            {
                public Object doInTransaction()
                {
                    final OpenSocialRequestContext openSocialRequestContext = convertShindigSecurityTokenToRequestContext(token);
                    Person person = personService.getPerson(userId.getUserId(token), openSocialRequestContext);
                    return activityService.getActivity(person.getPersonId(), relativeAppId, activityId, openSocialRequestContext);
                }
            });
            return ImmediateFuture.newInstance(convertActivityToShindigActivity(activity, fields));
        }
        catch (ActivityServiceException e)
        {
            throw new SocialSpiException(ResponseError.INTERNAL_ERROR, e.getMessage(), e);
        }
    }

    public Future<Void> deleteActivities(final UserId userId, final GroupId groupId, final String appId, final Set<String> activityIds, final SecurityToken token) throws SocialSpiException
    {
        try
        {
            final OpenSocialRequestContext openSocialRequestContext = convertShindigSecurityTokenToRequestContext(token);
            final AppId relativeAppId = AppId.valueOf(Uri.relativizeUriAgainstBase(applicationProperties.getBaseUrl(), appId).toString());
            txTemplate.execute(new TransactionCallback()
            {
                public Object doInTransaction()
                {
                    Person person = personService.getPerson(userId.getUserId(token), openSocialRequestContext);
                    activityService.deleteActivities(person.getPersonId(), relativeAppId, activityIds, openSocialRequestContext);
                    return null;
                }
            });
            return ImmediateFuture.newInstance(null);
        }
        catch (ActivityServiceException e)
        {
            throw new SocialSpiException(ResponseError.INTERNAL_ERROR, e.getMessage(), e);
        }
    }

    public Future<Void> createActivity(final UserId userId, final GroupId groupId, final String appId, final Set<String> fields, final org.apache.shindig.social.opensocial.model.Activity activity, final SecurityToken token) throws SocialSpiException
    {
        // the fields parameter is currently ignored (as it is in shindig example JsonDbOpensocialService). if people
        // want to create an activity with only certain fields, they shouldn't submit an activity that contains other fields

        try
        {
            final OpenSocialRequestContext openSocialRequestContext = convertShindigSecurityTokenToRequestContext(token);
            final AppId relativeAppId = AppId.valueOf(Uri.relativizeUriAgainstBase(applicationProperties.getBaseUrl(), appId).toString());
            txTemplate.execute(new TransactionCallback()
            {
                public Object doInTransaction()
                {
                    Person person = personService.getPerson(userId.getUserId(token), openSocialRequestContext);
                    activityService.createActivity(person.getPersonId(), relativeAppId, convertShindigActivityToActivity(activity), openSocialRequestContext);
                    return null;
                }
            });
            return ImmediateFuture.newInstance(null);
        }
        catch (ActivityServiceException e)
        {
            throw new SocialSpiException(ResponseError.INTERNAL_ERROR, e.getMessage(), e);
        }
    }
}
