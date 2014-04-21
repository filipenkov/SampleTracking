package com.opensymphony.user.provider.crowd;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.CrowdException;
import com.atlassian.crowd.model.user.UserTemplate;
import static com.atlassian.crowd.search.EntityDescriptor.user;
import com.atlassian.crowd.search.builder.QueryBuilder;
import static com.atlassian.crowd.search.query.entity.EntityQuery.ALL_RESULTS;
import com.google.common.collect.Lists;
import com.opensymphony.user.provider.CredentialsProvider;

import java.util.Collections;
import java.util.List;

public class EmbeddedCrowdCredentialsProvider extends EmbeddedCrowdAbstractProvider implements CredentialsProvider
{
    private final static String DEFAULT_BLANK = "";

    public boolean authenticate(final String name, final String password)
    {
        try
        {
            User user = getCrowdService().authenticate(name, password);
            return user != null;
        }
        catch (CrowdException e)
        {
            logger.info("User failed to authenticate");
            return false;
        }
    }

    public boolean changePassword(final String name, final String password)
    {
        CrowdService crowdService = getCrowdService();
        try
        {
            User user = crowdService.getUser(name);
            if (user != null)
            {
                crowdService.updateUserCredential(user, password);
                return true;
            }
            else
            {
                logger.error("Could not change password");
                return false;
            }
        }
        catch (CrowdException e)
        {
            logger.error("Could not change password", e);
            return false;
        }
    }

    public boolean create(final String name)
    {
        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Creating user: " + name);
            }

            UserTemplate template = new UserTemplate(name);
            template.setActive(true);
            template.setEmailAddress(DEFAULT_BLANK);
            template.setFirstName(DEFAULT_BLANK);
            template.setLastName(DEFAULT_BLANK);
            template.setDisplayName(DEFAULT_BLANK);

            getCrowdService().addUser(template, DEFAULT_BLANK);
            return true;
        }
        catch (CrowdException e)
        {
            logger.error("Error creating user : " + name + " : " + e, e);
            return false;
        }
    }

    public boolean handles(final String name)
    {
        if (name == null)
        {
            return false;
        }
        try
        {
            User user = getCrowdService().getUser(name);
            return user != null;
        }
        catch (Exception e)
        {
            logger.error("Could not determine if we handle: " + name, e);
            return false;
        }
    }

    public List<String> list()
    {
        Iterable<String> userNames = getCrowdService().search(QueryBuilder.queryFor(String.class, user()).returningAtMost(ALL_RESULTS));
        return Collections.unmodifiableList(Lists.newArrayList(userNames));
    }

    public boolean remove(final String name)
    {
        CrowdService crowdService = getCrowdService();
        try
        {
            User user = crowdService.getUser(name);
            if (user != null)
            {
                crowdService.removeUser(user);
                return true;
            }
            else
            {
                // Logging an error and returning false for compatibility with OFBizCredentialsProvider
                logger.error("Could not remove user: " + name);
                return false;
            }
        }
        catch (CrowdException e)
        {
            logger.error("Could not remove user: " + name, e);
            return false;
        }
    }
}
