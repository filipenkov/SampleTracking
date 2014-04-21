package com.atlassian.gadgets.opensocial.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import com.atlassian.gadgets.opensocial.OpenSocialRequestContext;
import com.atlassian.gadgets.opensocial.model.Person;
import com.atlassian.gadgets.opensocial.spi.PersonService;
import com.atlassian.gadgets.opensocial.spi.PersonServiceException;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;

import com.google.common.collect.Collections2;
import com.google.inject.Inject;

import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.social.ResponseError;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.RestfulCollection;
import org.apache.shindig.social.opensocial.spi.SocialSpiException;
import org.apache.shindig.social.opensocial.spi.UserId;

import static com.atlassian.gadgets.opensocial.internal.ShindigOpenSocialTypeAdapter.convertPersonToShindigPerson;
import static com.atlassian.gadgets.opensocial.internal.ShindigOpenSocialTypeAdapter.convertShindigSecurityTokenToRequestContext;
import static com.atlassian.gadgets.opensocial.internal.ShindigOpenSocialTypeAdapter.getPeopleFromUserIds;
import static com.atlassian.gadgets.opensocial.internal.ShindigOpenSocialTypeAdapter.personToShindigPersonFunction;

/**
 * A service adapter that handles Shindig {@link org.apache.shindig.social.opensocial.spi.PersonService} requests and maps them to {@link com.atlassian.gadgets.opensocial.spi.PersonService} requests.
 */
public class ShindigPersonServiceImpl implements org.apache.shindig.social.opensocial.spi.PersonService
{
    private final PersonService personService;
    private final TransactionTemplate txTemplate;

    private static final Comparator<org.apache.shindig.social.opensocial.model.Person> NAME_COMPARATOR =
            new Comparator<org.apache.shindig.social.opensocial.model.Person>()
            {
                public int compare(org.apache.shindig.social.opensocial.model.Person person, org.apache.shindig.social.opensocial.model.Person person1)
                {
                    String name = person.getName().getFormatted();
                    String name1 = person1.getName().getFormatted();
                    return name.compareTo(name1);
                }
            };

    @Inject
    public ShindigPersonServiceImpl(PersonService personService, TransactionTemplate txTemplate)
    {
        this.personService = personService;
        this.txTemplate = txTemplate;
    }

    @SuppressWarnings("unchecked")
    public Future<RestfulCollection<org.apache.shindig.social.opensocial.model.Person>> getPeople(final Set<UserId> userIds, final GroupId groupId, CollectionOptions collectionOptions,final Set<String> fields, final org.apache.shindig.auth.SecurityToken token) throws SocialSpiException
    {
        try
        {
            Set<Person> people = (Set<Person>) txTemplate.execute(new TransactionCallback()
            {
                public Object doInTransaction()
                {
                    return getPeopleFromUserIds(personService, userIds, groupId, token);
                }
            });

            List<org.apache.shindig.social.opensocial.model.Person> shindigPeople =
                    new ArrayList<org.apache.shindig.social.opensocial.model.Person>(Collections2.transform(people, personToShindigPersonFunction()));

            if (collectionOptions.getSortBy().equals(org.apache.shindig.social.opensocial.model.Person.Field.NAME.toString()))
            {
                Collections.sort(shindigPeople, NAME_COMPARATOR);
            }
            if (collectionOptions.getSortOrder().equals(SortOrder.descending))
            {
                Collections.reverse(shindigPeople);
            }
            int totalSize = shindigPeople.size();
            int last = collectionOptions.getFirst() + collectionOptions.getMax();
            shindigPeople = shindigPeople.subList(Math.min(collectionOptions.getFirst(), totalSize), Math.min(last, totalSize));
            return ImmediateFuture.newInstance(new RestfulCollection<org.apache.shindig.social.opensocial.model.Person>(shindigPeople, collectionOptions.getFirst(), totalSize));
        }
        catch (PersonServiceException e)
        {
            throw new SocialSpiException(ResponseError.INTERNAL_ERROR, e.getMessage(), e);
        }
    }

    public Future<org.apache.shindig.social.opensocial.model.Person> getPerson(final UserId id, final Set<String> fields, final org.apache.shindig.auth.SecurityToken token) throws SocialSpiException
    {
        try
        {
            final OpenSocialRequestContext openSocialRequestContext = convertShindigSecurityTokenToRequestContext(token);
            Person person = (Person) txTemplate.execute(new TransactionCallback()
            {
                public Object doInTransaction()
                {
                    return personService.getPerson(id.getUserId(token), openSocialRequestContext);
                }
            });
            if (person == null)
            {
                throw new SocialSpiException(ResponseError.BAD_REQUEST, "Person " + id.getUserId(token) + " not found");
            }
            return ImmediateFuture.newInstance(convertPersonToShindigPerson(person));
        }
        catch (PersonServiceException e)
        {
            throw new SocialSpiException(ResponseError.INTERNAL_ERROR, e.getMessage(), e);
        }
    }

}
