package com.atlassian.gadgets.opensocial.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import com.atlassian.gadgets.opensocial.OpenSocialRequestContext;
import com.atlassian.gadgets.opensocial.model.AppId;
import com.atlassian.gadgets.opensocial.model.Person;
import com.atlassian.gadgets.opensocial.model.PersonId;
import com.atlassian.gadgets.opensocial.spi.AppDataService;
import com.atlassian.gadgets.opensocial.spi.AppDataServiceException;
import com.atlassian.gadgets.opensocial.spi.PersonService;
import com.atlassian.gadgets.util.Uri;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;

import com.google.inject.Inject;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.social.ResponseError;
import org.apache.shindig.social.opensocial.spi.DataCollection;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.SocialSpiException;
import org.apache.shindig.social.opensocial.spi.UserId;

import static com.atlassian.gadgets.opensocial.internal.ShindigOpenSocialTypeAdapter.convertShindigSecurityTokenToRequestContext;
import static com.atlassian.gadgets.opensocial.internal.ShindigOpenSocialTypeAdapter.getPeopleIdsFromUserIds;

/**
 * A service adapter that handles {@code org.apache.shindig.social.opensocial.spi.AppDataService} and maps them to {@code com.atlassian.gadgets.opensocial.spi.AppDataService} requests 
 */
public class ShindigAppDataServiceImpl implements org.apache.shindig.social.opensocial.spi.AppDataService
{
    private final AppDataService appDataService;
    private final PersonService personService;
    private final TransactionTemplate txTemplate;
    private final ApplicationProperties applicationProperties;
    private static final String ALL_FIELDS_KEY = "*";

    @Inject
    public ShindigAppDataServiceImpl(AppDataService appDataService, PersonService personService, TransactionTemplate txTemplate, ApplicationProperties applicationProperties)
    {
        this.appDataService = appDataService;
        this.personService = personService;
        this.txTemplate = txTemplate;
        this.applicationProperties = applicationProperties;
    }

    @SuppressWarnings("unchecked")
    public Future<DataCollection> getPersonData(final Set<UserId> userIds, final GroupId groupId, final String appId, final Set<String> fields, final SecurityToken token) throws SocialSpiException
    {
        try
        {
            final AppId relativeAppId = AppId.valueOf(Uri.relativizeUriAgainstBase(applicationProperties.getBaseUrl(), appId).toString());
            Map<PersonId, Map<String, String>> peopleData = (Map<PersonId, Map<String, String>>) txTemplate.execute(new TransactionCallback()
            {
                public Object doInTransaction()
                {
                    Set<PersonId> people = getPeopleIdsFromUserIds(personService, userIds, groupId, token);
                    OpenSocialRequestContext openSocialRequestContext = convertShindigSecurityTokenToRequestContext(token);
                    if (fields.contains(ALL_FIELDS_KEY) || fields.isEmpty())
                    {
                        return appDataService.getPeopleData(people, relativeAppId, openSocialRequestContext);
                    }
                    else
                    {
                        return appDataService.getPeopleData(people, relativeAppId, fields, openSocialRequestContext);
                    }
                }
            });
            Map<String, Map<String, String>> resolvedPeopleData = new HashMap<String, Map<String, String>>();
            for (Map.Entry<PersonId, Map<String, String>> personData : peopleData.entrySet())
            {
                if (!personData.getValue().isEmpty())
                {
                    resolvedPeopleData.put(personData.getKey().toString(), personData.getValue());
                }
            }
            return ImmediateFuture.newInstance(new DataCollection(resolvedPeopleData));
        }
        catch (AppDataServiceException e)
        {
            throw new SocialSpiException(ResponseError.INTERNAL_ERROR, e.getMessage(), e);
        }
    }

    public Future<Void> deletePersonData(final UserId userId, final GroupId groupId, final String appId, final Set<String> fields, final SecurityToken token) throws SocialSpiException
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
                    if (person != null)
                    {
                        if (fields.contains(ALL_FIELDS_KEY) || fields.isEmpty())
                        {
                            appDataService.deletePersonData(person.getPersonId(), relativeAppId, openSocialRequestContext);
                        }
                        else
                        {
                            appDataService.deletePersonData(person.getPersonId(), relativeAppId, fields, openSocialRequestContext);
                        }
                    }
                    return null;
                }
            });
            return ImmediateFuture.newInstance(null);
        }
        catch (AppDataServiceException e)
        {
            throw new SocialSpiException(ResponseError.INTERNAL_ERROR, e.getMessage(), e);
        }
    }

    public Future<Void> updatePersonData(final UserId userId, final GroupId groupId, final String appId, final Set<String> fields, final Map<String, String> values, final SecurityToken token) throws SocialSpiException
    {
        try
        {
            final AppId relativeAppId = AppId.valueOf(Uri.relativizeUriAgainstBase(applicationProperties.getBaseUrl(), appId).toString());
            txTemplate.execute(new TransactionCallback()
            {
                public Object doInTransaction()
                {
                    OpenSocialRequestContext openSocialRequestContext = convertShindigSecurityTokenToRequestContext(token);
                    Person person = personService.getPerson(userId.getUserId(token), openSocialRequestContext);
                    if (person != null)
                    {
                        appDataService.updatePersonData(person.getPersonId(), relativeAppId, values, openSocialRequestContext);
                    }
                    return null;
                }
            });
            return ImmediateFuture.newInstance(null);
        }
        catch (AppDataServiceException e)
        {
            throw new SocialSpiException(ResponseError.INTERNAL_ERROR, e.getMessage(), e);
        }
    }
}
