package com.atlassian.applinks.ui.confluence;

import com.atlassian.applinks.ui.velocity.ListEntityLinksContext;
import com.atlassian.applinks.ui.velocity.VelocityContextFactory;
import com.atlassian.confluence.spaces.actions.AbstractSpaceAction;
import com.atlassian.confluence.spaces.actions.SpaceAdminAction;
import com.opensymphony.webwork.ServletActionContext;

import javax.servlet.http.HttpServletRequest;

/**
 * @since 3.0
 */
public class ListEntityLinksAction extends SpaceAdminAction
{
    private final VelocityContextFactory velocityContextFactory;

    private ListEntityLinksContext context;

    public ListEntityLinksAction(final VelocityContextFactory velocityContextFactory)
    {
        this.velocityContextFactory = velocityContextFactory;
    }

    public ListEntityLinksContext getApplinksContext()
    {
        if (context == null)
        {
            final HttpServletRequest request = ServletActionContext.getRequest();
            final String typeId = request.getParameter("typeId");
            final String key = request.getParameter("key");
            context = velocityContextFactory.buildListEntityLinksContext(request, typeId, key);
        }
        return context;
    }
}
