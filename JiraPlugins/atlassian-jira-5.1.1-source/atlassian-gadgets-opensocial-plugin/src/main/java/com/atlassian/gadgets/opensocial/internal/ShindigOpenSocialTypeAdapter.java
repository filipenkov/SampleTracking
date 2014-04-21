package com.atlassian.gadgets.opensocial.internal;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.atlassian.gadgets.opensocial.OpenSocialRequestContext;
import com.atlassian.gadgets.opensocial.model.Activity;
import com.atlassian.gadgets.opensocial.model.ActivityId;
import com.atlassian.gadgets.opensocial.model.AppId;
import com.atlassian.gadgets.opensocial.model.Group;
import com.atlassian.gadgets.opensocial.model.MediaItem;
import com.atlassian.gadgets.opensocial.model.Person;
import com.atlassian.gadgets.opensocial.model.PersonId;
import com.atlassian.gadgets.opensocial.spi.PersonService;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

import org.apache.shindig.social.core.model.MediaItemImpl;
import org.apache.shindig.social.core.model.NameImpl;
import org.apache.shindig.social.core.model.PersonImpl;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;

/**
 * Utility methods for converting between Shindig's OpenSocial classes and Atlassian's OpenSocial SPI classes
 */
class ShindigOpenSocialTypeAdapter
{
    private ShindigOpenSocialTypeAdapter()
    {
        throw new RuntimeException(ShindigOpenSocialTypeAdapter.class.getName() + " cannot be instantiated");
    }

    /**
     * Converts Gadget SPI Person objects to Opensocial Person objects for consumption by Shindig
     * @param person the {@code Person} to convert
     * @return a Shindig {@code org.apache.shindig.social.opensocial.model.Person} or {@code null} if the passed-in person is null
     */
    static org.apache.shindig.social.opensocial.model.Person convertPersonToShindigPerson(Person person)
    {
        // Here, Gadgets SPI Person objects are converted to Opensocial Person objects
        if (person == null)
        {
            return null;
        }
        return new PersonImpl(person.getPersonId().toString(), person.getPersonId().toString(), new NameImpl(person.getPersonId().toString()));
    }

    /**
     * Converts Gadget SPI Activity objects to Shindig Activity objects for consumption by Shindig
     * @param from the {@code Activity} to convert
     * @param fields the fields to include in the converted activity. Empty set means all
     * @return a Shindig {@code org.apache.shindig.social.opensocial.model.Activity} or {@code null} if the passed-in activity is null
     */
    static org.apache.shindig.social.opensocial.model.Activity convertActivityToShindigActivity(Activity from, Set<String> fields)
    {
        if (from == null)
        {
            return null;
        }

        Predicate<String> fieldFilter = (Predicate<String>)(isNullOrEmpty(fields) ? Predicates.<String>alwaysTrue() : Predicates.compose(Predicates.in(fields), Functions.toStringFunction()));
        org.apache.shindig.social.opensocial.model.Activity shindigActivity = new org.apache.shindig.social.core.model.ActivityImpl();

        if (fieldFilter.apply(Activity.Field.APP_ID.toString()))
        {
            shindigActivity.setAppId(from.getAppId().toString());
        }
        if (fieldFilter.apply(Activity.Field.BODY.toString()))
        {
            shindigActivity.setBody(from.getBody());
        }
        if (fieldFilter.apply(Activity.Field.EXTERNAL_ID.toString()))
        {
            shindigActivity.setExternalId(from.getExternalId());
        }
        if (from.getId() != null && fieldFilter.apply(Activity.Field.ID.toString()))
        {
            shindigActivity.setId(from.getId().toString());
        }
        if (fieldFilter.apply(Activity.Field.LAST_UPDATED.toString()))
        {
            shindigActivity.setUpdated(from.getUpdated());
        }
        if (fieldFilter.apply(Activity.Field.MEDIA_ITEMS.toString()))
        {
            List<org.apache.shindig.social.opensocial.model.MediaItem> shindigMediaItems = Lists.transform(from.getMediaItems(), mediaItemToShindigMediaItemFunction());
            shindigActivity.setMediaItems(shindigMediaItems);
        }
        if (fieldFilter.apply(Activity.Field.POSTED_TIME.toString()))
        {
            shindigActivity.setPostedTime(from.getPostedTime());
        }
        if (fieldFilter.apply(Activity.Field.PRIORITY.toString()))
        {
            shindigActivity.setPriority(from.getPriority());
        }
        if (fieldFilter.apply(Activity.Field.STREAM_FAVICON_URL.toString()))
        {
            shindigActivity.setStreamFaviconUrl(from.getStreamFaviconUrl());
        }
        if (fieldFilter.apply(Activity.Field.STREAM_SOURCE_URL.toString()))
        {
            shindigActivity.setStreamSourceUrl(from.getStreamSourceUrl());
        }
        if (fieldFilter.apply(Activity.Field.STREAM_TITLE.toString()))
        {
            shindigActivity.setStreamTitle(from.getStreamTitle());
        }
        if (fieldFilter.apply(Activity.Field.STREAM_URL.toString()))
        {
            shindigActivity.setStreamUrl(from.getStreamUrl());
        }
        if (fieldFilter.apply(Activity.Field.URL.toString()))
        {
            shindigActivity.setUrl(from.getUrl());
        }
        if (fieldFilter.apply(Activity.Field.TITLE.toString()))
        {
            shindigActivity.setTitle(from.getTitle());
        }
        if (from.getUserId() != null && fieldFilter.apply(Activity.Field.USER_ID.toString()))
        {
            shindigActivity.setUserId(from.getUserId().toString());
        }
        return shindigActivity;
    }

    private static boolean isNullOrEmpty(Set<String> fields)
    {
        return fields == null || fields.isEmpty();
    }

    /**
     * Converts Shindig Activity objects to Gadget SPI Activity objects
     * @param from the {@code org.apache.shindig.social.opensocial.model.Activity} to convert
     * @return a Shindig {@code Activity}, or null if the from activity is null
     */
    static Activity convertShindigActivityToActivity(final org.apache.shindig.social.opensocial.model.Activity from)
    {
        if (from == null)
        {
            return null;
        }

        Activity.Builder builder = new Activity.Builder(from.getTitle())
                                   .body(from.getBody())
                                   .externalId(from.getExternalId())
                                   .postedTime(from.getPostedTime())
                                   .priority(from.getPriority())
                                   .streamFaviconUrl(from.getStreamFaviconUrl())
                                   .streamSourceUrl(from.getStreamSourceUrl())
                                   .streamTitle(from.getStreamTitle())
                                   .streamUrl(from.getStreamUrl())
                                   .updated(from.getUpdated())
                                   .url(from.getUrl());

        if (from.getAppId() != null)
        {
            builder.appId(AppId.valueOf(from.getAppId()));
        }
        if (from.getId() != null)
        {
            builder.id(ActivityId.valueOf(from.getId()));
        }
        if (from.getMediaItems() != null)
        {
            List<MediaItem> mediaItems = Lists.transform(from.getMediaItems(), shindigMediaItemToMediaItemFunction());
            builder.mediaItems(mediaItems);
        }
        if (from.getUserId() != null)
        {
            builder.userId(PersonId.valueOf(from.getUserId()));
        }
        return builder.build();
    }

    /**
     * Converts a Shindig {@code GroupID} to {@code Group}
     * @param groupId the group ID to convert
     * @return the Group for the provided group ID
     */
    static Group groupIdToGroup(GroupId groupId)
    {
        switch (groupId.getType())
        {
            case all:
                return Group.ALL;
            case self:
                return Group.SELF;
            case friends:
                return Group.FRIENDS;
            default:
                return Group.of(groupId.getGroupId());
        }
    }

    /**
     * Converts a Shindig {@code SecurityToken}
     * @param token the security token to convert
     * @return the converted token
     */
    static OpenSocialRequestContext convertShindigSecurityTokenToRequestContext(final org.apache.shindig.auth.SecurityToken token)
    {
        return new OpenSocialRequestContext()
        {
            public String getOwnerId()
            {
                return token.getOwnerId();
            }

            public String getViewerId()
            {
                return token.getViewerId();
            }

            public boolean isAnonymous()
            {
                return token.isAnonymous();
            }

            public String getActiveUrl()
            {
                return token.getActiveUrl();
            }
        };
    }

    /**
     * {@code Function} that converts a Person to a Shindig Person
     * @return a function for performing Person to Shindig Person transformations
     */
    static Function<Person, org.apache.shindig.social.opensocial.model.Person> personToShindigPersonFunction()
    {
        return PersonToShindigPerson.FUNCTION;
    }

    private static enum PersonToShindigPerson implements Function<Person, org.apache.shindig.social.opensocial.model.Person>
    {
        FUNCTION;

        public org.apache.shindig.social.opensocial.model.Person apply(Person from)
        {
            return convertPersonToShindigPerson(from);
        }
    }

    static Set<Person> getPeopleFromUserIds(PersonService personService, Set<UserId> userIds, GroupId groupId, final org.apache.shindig.auth.SecurityToken token)
    {
        Group group = groupIdToGroup(groupId);
        Set<String> personIds = new HashSet<String>();
        OpenSocialRequestContext openSocialRequestContext = convertShindigSecurityTokenToRequestContext(token);
        for (UserId userId : userIds)
        {
            personIds.add(userId.getUserId(token));
        }
        return personService.getPeople(personIds, group, openSocialRequestContext);
    }

    static Set<PersonId> getPeopleIdsFromUserIds(PersonService personService, Set<UserId> userIds, GroupId groupId, final org.apache.shindig.auth.SecurityToken token)
    {
        Set<Person> people = getPeopleFromUserIds(personService, userIds, groupId, token);
        Set<PersonId> result = new HashSet<PersonId>();
        for (Person person : people)
        {
            result.add(person.getPersonId());
        }
        return result;
    }

    /**
     * {@code Function} that converts a Activity to a Shindig Activity
     * @param fields the fields to include in the converted activity. Empty set means all
     * @return a function for performing Activity to Shindig Activity transformations
     */
    static Function<Activity, org.apache.shindig.social.opensocial.model.Activity> activityToShindigActivityFunction(final Set<String> fields)
    {
        return new Function<Activity, org.apache.shindig.social.opensocial.model.Activity>()
        {
            public org.apache.shindig.social.opensocial.model.Activity apply(Activity from)
            {
                return convertActivityToShindigActivity(from, fields);
            }
        };
    }

    /**
     * Converts Gadget API MediaItem objects to Shindig MediaItem objects for consumption by Shindig
     * @param from the {@code MediaItem} to convert
     * @return a Shindig {@code org.apache.shindig.social.opensocial.model.MediaItem} or {@code null} if the passed-in item is null
     */
    static org.apache.shindig.social.opensocial.model.MediaItem convertMediaItemToShindigMediaItem(MediaItem from)
    {
        if (from == null)
        {
            return null;
        }

        return new MediaItemImpl(from.getMimeType(),
                (from.getType() != null) ? org.apache.shindig.social.opensocial.model.MediaItem.Type.valueOf(from.getType().toString().toUpperCase()) : null, 
                from.getUrl().toString());
    }

    /**
     * {@code Function} that converts a MediaItem to a Shindig MediaItem
     * @return a function for performing MediaItem to Shindig MediaItem transformations
     */
    static Function<MediaItem, org.apache.shindig.social.opensocial.model.MediaItem> mediaItemToShindigMediaItemFunction()
    {
        return new Function<MediaItem, org.apache.shindig.social.opensocial.model.MediaItem>()
        {
            public org.apache.shindig.social.opensocial.model.MediaItem apply(MediaItem from)
            {
                return convertMediaItemToShindigMediaItem(from);
            }
        };
    }
    
    /**
     * Converts Gadget API MediaItem objects to Shindig MediaItem objects for consumption by Shindig
     * @param from the {@code MediaItem} to convert
     * @return a Shindig {@code org.apache.shindig.social.opensocial.model.MediaItem} or {@code null} if the passed-in item is null
     */
    static MediaItem convertShindigMediaItemToMediaItem(org.apache.shindig.social.opensocial.model.MediaItem from)
    {
        if (from == null)
        {
            return null;
        }

        return new MediaItem.Builder(URI.create(from.getUrl()))
                .mimeType(from.getMimeType())
                .type((from.getType() != null) ? MediaItem.Type.valueOf(from.getType().toString().toUpperCase()) : null)
                .build();
    }

    /**
     * {@code Function} that converts a Shindig MediaItem to a MediaItem
     * @return a function for performing Shindig MediaItem to MediaItem transformations
     */
    static Function<org.apache.shindig.social.opensocial.model.MediaItem, MediaItem> shindigMediaItemToMediaItemFunction()
    {
        return new Function<org.apache.shindig.social.opensocial.model.MediaItem, MediaItem>()
        {
            public MediaItem apply(org.apache.shindig.social.opensocial.model.MediaItem from)
            {
                return convertShindigMediaItemToMediaItem(from);
            }
        };
    }
}
