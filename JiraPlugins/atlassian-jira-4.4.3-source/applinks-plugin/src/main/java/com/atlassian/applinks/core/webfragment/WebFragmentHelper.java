package com.atlassian.applinks.core.webfragment;

import com.atlassian.applinks.core.rest.model.WebItemEntity;
import com.atlassian.applinks.core.rest.model.WebItemEntityList;
import com.atlassian.applinks.core.rest.model.WebPanelEntity;
import com.atlassian.applinks.core.rest.model.WebPanelEntityList;

/**
 * Provides rendered web-items and web-panels for display by the applinks plugin
 */
public interface WebFragmentHelper
{
    /**
     * This web-item location adds operations to each Application Link in the Application Link list admin screen
     */
    String APPLICATION_LINK_LIST_OPERATION = "applinks.application.link.list.operation";

    /**
     * This web-item location adds operations to each Entity Link in the Entity Link list admin screen
     */
    String ENTITY_LINK_LIST_OPERATION = "applinks.entity.link.list.operation";

    /**
     * @param location the web-item location to retrieve web-items for
     * @param context the {@link WebFragmentContext} to supply to the web-item for rendering
     * @return a {@link WebItemEntityList} containing rendered {@link WebItemEntity}s for the specified location and
     * context
     */
    WebItemEntityList getWebItemsForLocation(String location, WebFragmentContext context);

    /**
     * @param location the web-panel location to retrieve web-panels for
     * @param context the {@link WebFragmentContext} to supply to the web-panel for rendering
     * @return a {@link WebPanelEntityList} containing rendered {@link WebPanelEntity}s for the specified location and
     * context
     */
    WebPanelEntityList getWebPanelsForLocation(String location, WebFragmentContext context);
}
