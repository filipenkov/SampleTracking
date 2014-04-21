/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.subscription;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.DataAccessException;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import java.util.List;
import java.util.Map;

public interface SubscriptionManager
{
    boolean hasSubscription(com.opensymphony.user.User user, Long filterId) throws GenericEntityException;

    boolean hasSubscription(User user, Long filterId) throws GenericEntityException;

    GenericValue getSubscription(com.opensymphony.user.User user, Long subId) throws GenericEntityException;

    GenericValue getSubscription(User user, Long subId) throws GenericEntityException;

    List<GenericValue> getSubscriptions(com.opensymphony.user.User user, Long filterId) throws GenericEntityException;

    List<GenericValue> getSubscriptions(User user, Long filterId) throws GenericEntityException;

    GenericValue createSubscription(com.opensymphony.user.User user, Long filterId, String groupName, Long period, Boolean emailOnEmpty);

    GenericValue createSubscription(User user, Long filterId, String groupName, Long period, Boolean emailOnEmpty);

    /**
     * Creates a new subscription based on the passed in filter id and fired
     * in accordance with the passed in trigger
     *
     * @param user         the current user performing this operation
     * @param filterId     Id of the filter subscribing to
     * @param groupName    Sent ot group
     * @param trigger      The trigger to store
     * @param emailOnEmpty send email if filter returns no results
     * @return GenericValue representing new subscription
     */
    GenericValue createSubscription(com.opensymphony.user.User user, Long filterId, String groupName, Trigger trigger, Boolean emailOnEmpty);

    /**
     * Creates a new subscription based on the passed in filter id and fired
     * in accordance with the passed in trigger
     *
     * @param user         the current user performing this operation
     * @param filterId     Id of the filter subscribing to
     * @param groupName    Sent ot group
     * @param trigger      The trigger to store
     * @param emailOnEmpty send email if filter returns no results
     * @return GenericValue representing new subscription
     */
    GenericValue createSubscription(User user, Long filterId, String groupName, Trigger trigger, Boolean emailOnEmpty);

    void deleteSubscription(Long subId) throws Exception;

    void runSubscription(GenericValue subId) throws Exception;

    void runSubscription(com.opensymphony.user.User u, Long subId) throws Exception;

    void runSubscription(User u, Long subId) throws Exception;

    GenericValue getSubscriptionFromTriggerName(String triggerName) throws GenericEntityException;

    Trigger getTriggerFromSubscription(GenericValue subscription) throws SchedulerException;

    /**
     * This will update the subscription identified by the subscription id to
     * contain the details specifed in the fields map and it will update the
     * trigger with the provided the Trigger.
     *
     * @param user           the current user performing this operation
     * @param subscriptionId identifies the subscription to update
     * @param fields         the fields to update the subscription with.
     * @param trigger        The trigger to update the subscription with
     * @throws DataAccessException if there is a problem persisting the data.
     *
     * @deprecated Should use the method with an explicit list of fields
     */
    @Deprecated
    void updateSubscription(com.opensymphony.user.User user, Long subscriptionId, Map fields, Trigger trigger) throws DataAccessException;

    /**
     *
     * @param user           the current user performing this operation
     * @param subscriptionId identifies the subscription to update
     * @param groupName      (optional) the name of the group to receive the email
     * @param trigger        The trigger to update the subscription with
     * @throws DataAccessException if there is a problem persisting the data.
     */
    void updateSubscription(com.opensymphony.user.User user, Long subscriptionId, String groupName, Trigger trigger, Boolean emailOnEmpty) throws DataAccessException;

    /**
     *
     * @param user           the current user performing this operation
     * @param subscriptionId identifies the subscription to update
     * @param groupName      (optional) the name of the group to receive the email
     * @param trigger        The trigger to update the subscription with
     * @throws DataAccessException if there is a problem persisting the data.
     */
    void updateSubscription(User user, Long subscriptionId, String groupName, Trigger trigger, Boolean emailOnEmpty) throws DataAccessException;

    List<GenericValue> getAllSubscriptions(Long filterId);

    List<GenericValue> getAllSubscriptions();

    void deleteSubscriptionsForUser(com.opensymphony.user.User user) throws Exception;

    void deleteSubscriptionsForUser(User user) throws Exception;

    void deleteSubscriptionsForGroup(com.opensymphony.user.Group group) throws Exception;

    void deleteSubscriptionsForGroup(Group group) throws Exception;
}