package com.atlassian.jira.plugin.webfragment;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.ContextProvider;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.conditions.ConditionLoadingException;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;

import java.util.List;
import java.util.Map;

public class JiraWebFragmentHelper implements WebFragmentHelper
{
    private static final Logger log = Logger.getLogger(JiraWebFragmentHelper.class);

    private final VelocityManager velocityManager;
    private final JiraAuthenticationContext authenticationContext;
    private final VelocityRequestContextFactory velocityRequestContextFactory;

    public JiraWebFragmentHelper(VelocityManager velocityManager, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext)
    {
        this.velocityManager = velocityManager;
        this.authenticationContext = authenticationContext;
        this.velocityRequestContextFactory = new DefaultVelocityRequestContextFactory(applicationProperties);
    }

    public Condition loadCondition(String className, Plugin plugin) throws ConditionLoadingException
    {
        try
        {
            Class<Condition> conditionClass = plugin.loadClass(className, getClass());
            if (plugin instanceof AutowireCapablePlugin)
            {
                AutowireCapablePlugin autowirePlugin = (AutowireCapablePlugin) plugin;
                return autowirePlugin.autowire(conditionClass);
            }
            else
            {
                return JiraUtils.loadComponent(conditionClass);
            }
        }
        catch (Exception e)
        {
            throw new ConditionLoadingException(e);
        }
    }

    public ContextProvider loadContextProvider(String className, Plugin plugin) throws ConditionLoadingException
    {
        try
        {
            Class<ContextProvider> providerClass = plugin.loadClass(className, getClass());
            final ContextProvider contextProvider;
            if (plugin instanceof ContainerManagedPlugin)
            {
                ContainerManagedPlugin containerManagedPlugin = (ContainerManagedPlugin) plugin;
                contextProvider = containerManagedPlugin.getContainerAccessor().createBean(providerClass);
            }
            else
            {
                contextProvider = JiraUtils.loadComponent(providerClass);
            }

            // If we are cacheable, wrap with the caching decorator
            if(contextProvider instanceof CacheableContextProvider)
            {
                return new CacheableContextProviderDecorator((CacheableContextProvider) contextProvider);
            }
            else
            {
                return contextProvider;
            }
        }
        catch (Exception e)
        {
            throw new ConditionLoadingException(e);
        }
    }

    public String getI18nValue(String key, List arguments, Map context)
    {
        Object i18nObject = context.get(JiraWebInterfaceManager.CONTEXT_KEY_I18N);
        if (i18nObject != null)
        {
            try
            {
                return ((I18nHelper) i18nObject).getText(key, arguments);
            }
            catch (ClassCastException e)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Expected an instance of I18nHelper in the context under key: '"
                              + JiraWebInterfaceManager.CONTEXT_KEY_I18N + "' but it was " + i18nObject.getClass(), e);
                }
            }
        }
        return authenticationContext.getI18nHelper().getText(key, arguments);
    }

    public String renderVelocityFragment(String fragment, Map context)
    {
        if (!needToRender(fragment))
        {
            return fragment;
        }

        return getHtml(fragment, getDefaultParams(context));
    }

    protected String getHtml(String fragment, Map startingParams)
    {
        try
        {
            if (TextUtils.stringSet(fragment))
            {
                return velocityManager.getEncodedBodyForContent(fragment, getBaseUrl(), startingParams);
            }
        }
        catch (VelocityException e)
        {
            log.error("Error while rendering velocity fragment: '" + fragment + "'.", e);
        }

        return "";
    }

    private Map getDefaultParams(Map startingParams)
    {
        return JiraVelocityUtils.getDefaultVelocityParams(startingParams, authenticationContext);
    }

    private boolean needToRender(String velocity)
    {
        return (TextUtils.stringSet(velocity) && (velocity.indexOf("$") >= 0 || velocity.indexOf("#") >= 0));
    }

    /**
     * Returns the base URL from VelocityRequestContext
     *
     * @return the base URL
     * @see com.atlassian.jira.util.velocity.VelocityRequestContext
     */
    private String getBaseUrl()
    {
        return velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl();
    }

}
