package com.atlassian.crowd.manager.application;

import com.atlassian.crowd.dao.alias.AliasDAO;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import org.apache.commons.lang.Validate;

import java.util.List;

public class AliasManagerImpl implements AliasManager
{
    private final AliasDAO aliasDAO;
    private ApplicationService applicationService;

    public AliasManagerImpl(final AliasDAO aliasDAO, final ApplicationService applicationService)
    {
        this.aliasDAO = aliasDAO;
        this.applicationService = applicationService;
    }

    public String findUsernameByAlias(final Application application, final String authenticatingUsername)
    {
        Validate.notNull(application);
        Validate.notNull(authenticatingUsername);
        
        if (application.isAliasingEnabled())
        {
            final String realusername = aliasDAO.findUsernameByAlias(application, authenticatingUsername);
            if (realusername != null)
            {
                return realusername;
            }
        }

        return authenticatingUsername;
    }

    public String findAliasByUsername(final Application application, final String username)
    {
        Validate.notNull(application);
        Validate.notNull(username);

        if (application.isAliasingEnabled())
        {
            final String alias = aliasDAO.findAliasByUsername(application, username);
            if (alias != null)
            {
                return alias;
            }
        }

        return username;
    }

    public void storeAlias(final Application application, final String username, final String alias) throws AliasAlreadyInUseException
    {
        Validate.notNull(username);
        Validate.notNull(alias);

        String userWithAlias = aliasDAO.findUsernameByAlias(application, alias);
        if (userWithAlias != null)
        {
            if (userWithAlias.equals(username))
            {
                // the op is ok because it's a nil op (ie. do nothing)
                return;
            }
            else
            {
                // alias is used already by another user
                throw new AliasAlreadyInUseException(application.getName(), alias, userWithAlias);
            }
        }

        try
        {
            User user = applicationService.findUserByName(application, alias);

            // user with the alias exists so we need to make sure that this schmoe has an alias too to avoid conflict
            String aliasForUserWithSameNameAsDesiredAlias = aliasDAO.findAliasByUsername(application, user.getName());

            if (aliasForUserWithSameNameAsDesiredAlias == null)
            {
                // we are trying to alias to a username that already exists but isn't aliased to something else
                throw new AliasAlreadyInUseException(application.getName(), alias, user.getName());
            }
        }
        catch (UserNotFoundException e)
        {
            // user doesn't exist, so alias is safe
        }

        aliasDAO.storeAlias(application, username, alias);
    }

    public void removeAlias(final Application application, final String username) throws AliasAlreadyInUseException
    {
        // does anyone have "username" as their alias?
        String userWithUsernameAsAlias = aliasDAO.findUsernameByAlias(application, username);
        if (userWithUsernameAsAlias == null)
        {        
            aliasDAO.removeAlias(application, username);
        }
        else
        {
            throw new AliasAlreadyInUseException(application.getName(), username, userWithUsernameAsAlias);
        }
    }

    public List<String> search(final EntityQuery entityQuery)
    {
        return aliasDAO.search(entityQuery);
    }

}
