package com.atlassian.gadgets.opensocial.spi;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.gadgets.opensocial.OpenSocialRequestContext;
import com.atlassian.gadgets.opensocial.model.Activity;
import com.atlassian.gadgets.opensocial.model.AppId;
import com.atlassian.gadgets.opensocial.model.PersonId;

/**
 * Interface for handling {@code Activity} requests. The appId parameter in the methods below is a unique identifier for
 * a particular gadget (e.g. Chart Gadget). This parameter should be used to enforce that only activities created by a
 * particular gadget should be available to that gadget.
 *
 * <p>
 * This is an optional SPI that host applications may implement if they wish to provide the ability to read and post
 * activity data using the OpenSocial APIs. If the {@link ActivityService} is implemented, an implementation of [@link PersonService}
 * must also be provided. If an implementation of {@link ActivityService} is not supplied, the OpenSocial activity API will not be supported,
 * but other OpenSocial and Gadget functionality will work normally.
 *
 *
 * @since 2.0
 */
public interface ActivityService
{
  /**
   * Returns a list of activities that correspond to the passed in users
   *
   * @param peopleIds  The people whose data is being requested
   * @param appId   The app id for the gadget making this request
   * @param requestContext The request context
   * @return the list of activities.
   * @throws ActivityServiceException if there is a problem while performing this operation
   */
  List<Activity> getActivities(Set<PersonId> peopleIds, AppId appId, OpenSocialRequestContext requestContext) throws ActivityServiceException;

  /**
   * Returns a list of activities for the passed in person that corresponds to a list of activityIds.
   *
   * @param personId The person to fetch activities for
   * @param appId       The app id for the gadget making this request
   * @param activityIds The set of activity ids to fetch.
   * @param requestContext The request context
   * @return the list of activities
   * @throws ActivityServiceException if there is a problem while performing this operation
   */
  List<Activity> getActivities(PersonId personId, AppId appId, Set<String> activityIds, OpenSocialRequestContext requestContext) throws ActivityServiceException;

  /**
   * Returns an activity for the passed in person that corresponds to an activityId
   *
   * @param personId The person to fetch an activity for.
   * @param appId      The app id for the gadget making this request.
   * @param activityId The activity id to fetch.
   * @param requestContext The request context
   * @return the requested activity, or null if the request doesn't match any activity (e.g. if the {@code activityId} doesn't exist
   * @throws ActivityServiceException if there is a problem while performing this operation
   */
  @Nullable Activity getActivity(PersonId personId, AppId appId, String activityId, OpenSocialRequestContext requestContext) throws ActivityServiceException;

  /**
   * Deletes the activity for the passed in person that corresponds to the activityIds.
   *
   * @param personId The person whose activity is being deleted.
   * @param appId       The app id for the gadget making this request.
   * @param activityIds A list of activity ids to delete.
   * @param token       A valid SecurityToken.
   * @throws ActivityServiceException if there is a problem while performing this operation
   */
  void deleteActivities(PersonId personId, AppId appId, Set<String> activityIds, OpenSocialRequestContext token) throws ActivityServiceException;

  /**
   * Creates the passed in activity for the passed in user and group. Once {@code createActivity} is called,
   * {@code getActivities} will be able to return the Activity.
   *
   * @param personId  The id of the person to create an activity for.
   * @param appId    The app id for the gadget making this request.
   * @param activity The activity to create.
   * @param requestContext The request context
   * @return the activity created.
   * @throws ActivityServiceException if there is a problem while performing this operation
   */
  Activity createActivity(PersonId personId, AppId appId, Activity activity, OpenSocialRequestContext requestContext) throws ActivityServiceException;
}
