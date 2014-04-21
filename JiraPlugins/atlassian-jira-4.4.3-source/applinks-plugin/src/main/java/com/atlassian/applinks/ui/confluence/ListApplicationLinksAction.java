package com.atlassian.applinks.ui.confluence;

import com.atlassian.applinks.ui.velocity.ListApplicationLinksContext;
import com.atlassian.applinks.ui.velocity.VelocityContextFactory;
import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.opensymphony.webwork.ServletActionContext;

/**
 * @since 3.0
 */
public class ListApplicationLinksAction extends ConfluenceActionSupport
{
    private final VelocityContextFactory velocityContextFactory;

    private ListApplicationLinksContext context;

    public ListApplicationLinksAction(final VelocityContextFactory velocityContextFactory)
    {
        this.velocityContextFactory = velocityContextFactory;
    }

    @Override
    public String execute() throws Exception
    {
        return SUCCESS;
    }

    public ListApplicationLinksContext getApplinksContext()
    {
        if (context == null)
        {
            context = velocityContextFactory.buildListApplicationLinksContext(ServletActionContext.getRequest());
        }
        return context;
    }

}
