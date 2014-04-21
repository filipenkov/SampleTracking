package com.atlassian.crowd.plugin.rest.service.controller;

import com.atlassian.crowd.event.EventTokenExpiredException;
import com.atlassian.crowd.event.Events;
import com.atlassian.crowd.event.IncrementalSynchronisationNotAvailableException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.plugin.rest.entity.EventEntityList;
import com.atlassian.crowd.plugin.rest.util.EntityTranslator;

import java.net.URI;

public class EventsController extends AbstractResourceController
{

    public EventsController(final ApplicationService applicationService, final ApplicationManager applicationManager)
    {
        super(applicationService, applicationManager);
    }

    public EventEntityList getCurrentEventToken()
    {
        return new EventEntityList(applicationService.getCurrentEventToken(), null);
    }

    public EventEntityList getEventsSince(String applicationName, String eventToken, URI baseURI) throws EventTokenExpiredException, OperationFailedException, IncrementalSynchronisationNotAvailableException
    {
        final Application application = getApplication(applicationName);
        final Events events = applicationService.getNewEvents(application, eventToken);
        return EntityTranslator.toEventEntities(events, baseURI);
    }
}
