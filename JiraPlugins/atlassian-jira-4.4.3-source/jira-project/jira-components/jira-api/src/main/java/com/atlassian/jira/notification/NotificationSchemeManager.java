/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.mail.MailException;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface NotificationSchemeManager extends SchemeManager
{
    public Set<NotificationRecipient> getRecipients(IssueEvent event, SchemeEntity notification) throws GenericEntityException;

    public boolean hasEntities(GenericValue scheme, Long event, String type, String parameter, Long templateId) throws GenericEntityException;

    public void removeSchemeEntitiesForField(String fieldId) throws RemoveException;

    public boolean isHasMailServer() throws MailException;

    public GenericValue getNotificationSchemeForProject(GenericValue projectGV);

    public Map<Long, String> getSchemesMapByConditions(Map<String, ?> conditions);

    /**
     * This is a method that is meant to quickly get you all the schemes that contain an entity of the
     * specified type and parameter.
     * @param type is the entity type
     * @param parameter is the scheme entries parameter value
     * @return Collection of GenericValues that represents a scheme
     */
    public Collection<GenericValue> getSchemesContainingEntity(String type, String parameter);

}
