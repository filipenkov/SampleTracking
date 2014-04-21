package com.atlassian.jira.plugin.ext.bamboo.gadgets;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.application.bamboo.BambooApplicationType;
import com.atlassian.applinks.api.event.ApplicationLinkAddedEvent;
import com.atlassian.applinks.api.event.ApplicationLinkDeletedEvent;
import com.atlassian.applinks.api.event.ApplicationLinkDetailsChangedEvent;
import com.atlassian.applinks.api.event.ApplicationLinksIDChangedEvent;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.plugin.ext.bamboo.applinks.BambooApplicationLinkManager;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class GadgetListener implements InitializingBean, DisposableBean
{
    private static final Logger log = Logger.getLogger(GadgetListener.class);

    private static final List<String> GADGET_URLS = Lists.newArrayList(
            "/rest/gadgets/1.0/g/com.atlassian.bamboo.gadgets/charts/bambooCharts.xml",
            "/rest/gadgets/1.0/g/com.atlassian.bamboo.gadgets/status/planStatus.xml",
            "/rest/gadgets/1.0/g/com.atlassian.bamboo.gadgets/charts/planSummaryChart.xml",
            "/rest/gadgets/1.0/g/com.atlassian.bamboo.gadgets/status/cloverCoverage.xml"
    );


    private final BambooApplicationLinkManager applicationLinkManager;
    private final BambooGadgetSpecProvider bambooGadgetSpecProvider;
    private final EventPublisher eventPublisher;

    public GadgetListener(BambooApplicationLinkManager applicationLinkManager,
                          BambooGadgetSpecProvider bambooGadgetSpecProvider,
                          EventPublisher eventPublisher)
    {
        this.applicationLinkManager = applicationLinkManager;
        this.bambooGadgetSpecProvider = bambooGadgetSpecProvider;
        this.eventPublisher = eventPublisher;
    }

    private void createGadgets()
    {
        //applicationLinkManager only returns Bamboo application links
        for (ApplicationLink applicationLink : applicationLinkManager.getApplicationLinks())
        {
            addGadgetsForApplicationLink(applicationLink);
        }
    }

    @EventListener
    public void onEvent(ApplicationLinkAddedEvent event)
    {
        if (event.getApplicationType() instanceof BambooApplicationType)
        {
            log.info("Adding gadgets for application link " + event.getApplicationId());
            addGadgetsForApplicationLink(event.getApplicationLink());
        }
    }

    @EventListener
    public void onEvent(ApplicationLinkDeletedEvent event)
    {
        if (event.getApplicationType() instanceof BambooApplicationType)
        {
            log.info("Removing gadgets for application link " + event.getApplicationId());
            bambooGadgetSpecProvider.removeBambooGadgets(event.getApplicationId());
        }
    }

    /**
     * Handles events where application id changes.
     */
    @EventListener
    public void onEvent(ApplicationLinksIDChangedEvent event)
    {
        if (event.getApplicationType() instanceof BambooApplicationType)
        {
            log.info("Migrating gadgets from application link " + event.getOldApplicationId() + " to " + event.getApplicationId());
            bambooGadgetSpecProvider.migrateBambooGadgets(event.getOldApplicationId(), event.getApplicationId());
        }
    }

    /**
     * Handles events where application link details (including display url) change.
     */
    @EventListener
    public void onEvent(ApplicationLinkDetailsChangedEvent event)
    {
        if (event.getApplicationType() instanceof BambooApplicationType)
        {
            log.info("Updating gadgets for application link " + event.getApplicationId());
            //remove all existing gadgets for the application id and re-register them with the new display url.
            bambooGadgetSpecProvider.removeBambooGadgets(event.getApplicationId());
            addGadgetsForApplicationLink(event.getApplicationLink());
        }
    }

    private void addGadgetsForApplicationLink(ApplicationLink applicationLink)
    {
        String host = applicationLink.getDisplayUrl().toASCIIString();

        for (String gadgetUrl : GADGET_URLS)
        {
            try
            {
                final URI uri = new URI(host + gadgetUrl);
                bambooGadgetSpecProvider.addBambooGadget(applicationLink.getId(), uri);
            }
            catch (URISyntaxException e)
            {
                log.warn("Could not add bamboo gadgets for " + host, e);
            }
        }
    }


    /**
     * Called upon startup.
     */
    public void afterPropertiesSet() throws Exception
    {
        createGadgets();
        eventPublisher.register(this);
    }

    /**
     * Called upon shutdown.
     */
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }
}
