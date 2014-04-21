package com.atlassian.crowd.plugin.rest.entity;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.plugin.rest.util.EntityExpansionUtil;
import com.atlassian.crowd.plugin.rest.util.UserEntityUtil;
import com.atlassian.plugins.rest.common.expand.EntityCrawler;
import com.atlassian.plugins.rest.common.expand.EntityExpander;
import com.atlassian.plugins.rest.common.expand.ExpandContext;
import com.atlassian.plugins.rest.common.expand.resolver.EntityExpanderResolver;

/**
 * Expands a UserEntity from its minimal form to the expanded version. As well as expanding the UserEntity, this
 * EntityExpander will expand the user attributes if required.
 *
 * This is required because {@link ApplicationService} does not support retrieving just the user's attributes.
 *
 * @since v2.1
 */
public class UserEntityExpander implements EntityExpander<UserEntity>
{
    private final ApplicationService applicationService;
    private final ApplicationManager applicationManager;

    public UserEntityExpander(final ApplicationService applicationService, final ApplicationManager applicationManager)
    {
        this.applicationService = applicationService;
        this.applicationManager = applicationManager;
    }

    /**
     * {@inheritDoc}
     */
    public UserEntity expand(final ExpandContext<UserEntity> context, final EntityExpanderResolver expanderResolver, final EntityCrawler entityCrawler)
    {
        UserEntity userEntity = context.getEntity();

        if (userEntity.isExpanded())
        {
           return userEntity;
        }

        Application application;
        try
        {
            application = applicationManager.findByName(userEntity.getApplicationName());
        }
        catch (ApplicationNotFoundException e)
        {
            throw new RuntimeException(e);
        }

        boolean expandAttributes = EntityExpansionUtil.shouldExpandField(UserEntity.class, UserEntity.ATTRIBUTES_FIELD_NAME, context.getEntityExpandParameter().getExpandParameter(context.getExpandable()));

        UserEntity expandedUserEntity;
        try
        {
            expandedUserEntity = UserEntityUtil.expandUser(applicationService, application, userEntity, expandAttributes);
        }
        catch (UserNotFoundException e)
        {
            throw new RuntimeException(e);
        }

        if (!context.getEntityExpandParameter().isEmpty())
        {
            entityCrawler.crawl(expandedUserEntity, context.getEntityExpandParameter().getExpandParameter(context.getExpandable()), expanderResolver);
        }
        return expandedUserEntity;
    }
}

