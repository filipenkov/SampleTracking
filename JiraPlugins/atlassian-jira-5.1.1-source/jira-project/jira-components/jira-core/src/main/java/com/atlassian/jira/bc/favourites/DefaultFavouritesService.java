package com.atlassian.jira.bc.favourites;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.favourites.FavouritesManager;
import com.atlassian.jira.sharing.SharedEntity;

public class DefaultFavouritesService implements FavouritesService
{
    private final FavouritesManager favouritesManager;

    public DefaultFavouritesService(FavouritesManager favouritesManager)
    {
        this.favouritesManager = favouritesManager;
    }

    public void addFavourite(final JiraServiceContext ctx, final SharedEntity entity)
    {
        try
        {
            favouritesManager.addFavourite(ctx.getLoggedInUser(), entity);
        }
        catch (PermissionException e)
        {
            ctx.getErrorCollection().addErrorMessage(ctx.getI18nBean().getText("common.favourites.no.permission"));
        }
    }

    public void addFavouriteInPosition(final JiraServiceContext ctx, final SharedEntity entity, final long position)
    {
        try
        {
            favouritesManager.addFavouriteInPosition(ctx.getLoggedInUser(), entity, position);
        }
        catch (PermissionException e)
        {
            ctx.getErrorCollection().addErrorMessage(ctx.getI18nBean().getText("common.favourites.not.added"));
        }
    }

    public void removeFavourite(final JiraServiceContext ctx, final SharedEntity entity)
    {
        favouritesManager.removeFavourite(ctx.getLoggedInUser(), entity);
    }

    @Override
    public boolean isFavourite(User user, final SharedEntity entity)
    {
        try
        {
            return favouritesManager.isFavourite(user, entity);
        }
        catch (PermissionException e)
        {
            throw new RuntimeException(e);
        }
    }
}
