package com.atlassian.crowd.plugin.rest.entity;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.plugin.rest.util.EntityExpansionUtil;
import com.atlassian.crowd.plugin.rest.util.GroupEntityUtil;
import com.atlassian.plugins.rest.common.expand.EntityCrawler;
import com.atlassian.plugins.rest.common.expand.EntityExpander;
import com.atlassian.plugins.rest.common.expand.ExpandContext;
import com.atlassian.plugins.rest.common.expand.resolver.EntityExpanderResolver;

/**
 * Expands a GroupEntity from its minimal form to the expanded version. As
 * well as expanding the GroupEntity, this EntityExpander will expand the group
 * attributes if required.
 *
 * This is required because {@link com.atlassian.crowd.manager.application.ApplicationService}
 * does not support retrieving just the group's attributes.
 *
 * @since v2.1
 */
public class GroupEntityExpander implements EntityExpander<GroupEntity>
{
    public final static String ATTRIBUTES_FIELD_NAME = "attributes";

    private final ApplicationService applicationService;
    private final ApplicationManager applicationManager;

    public GroupEntityExpander(final ApplicationService applicationService, final ApplicationManager applicationManager)
    {
        this.applicationService = applicationService;
        this.applicationManager = applicationManager;
    }

    /**
     * {@inheritDoc}
     */
    public GroupEntity expand(final ExpandContext<GroupEntity> context, final EntityExpanderResolver expanderResolver, final EntityCrawler entityCrawler)
    {
        GroupEntity groupEntity = context.getEntity();

        if (groupEntity.isExpanded())
        {
           return groupEntity;
        }

        Application application;
        try
        {
            application = applicationManager.findByName(groupEntity.getApplicationName());
        }
        catch (ApplicationNotFoundException e)
        {
            throw new RuntimeException(e);
        }

        boolean expandAttributes = EntityExpansionUtil.shouldExpandField(GroupEntity.class, ATTRIBUTES_FIELD_NAME, context.getEntityExpandParameter().getExpandParameter(context.getExpandable()));

        GroupEntity expandedGroupEntity;
        try
        {
            expandedGroupEntity = GroupEntityUtil.expandGroup(applicationService, application, groupEntity, expandAttributes);
        }
        catch (GroupNotFoundException e)
        {
            throw new RuntimeException(e);
        }

        if (!context.getEntityExpandParameter().isEmpty())
        {
            entityCrawler.crawl(expandedGroupEntity, context.getEntityExpandParameter().getExpandParameter(context.getExpandable()), expanderResolver);
        }
        return expandedGroupEntity;
    }
}
