package com.atlassian.administration.quicksearch.impl.spi.alias;

import com.atlassian.administration.quicksearch.impl.spi.AbstractDefaultAdminWebItem;
import com.atlassian.administration.quicksearch.spi.AdminLink;
import com.atlassian.administration.quicksearch.spi.AdminLinkAliasProvider;
import com.atlassian.administration.quicksearch.spi.AdminLinkSection;
import com.atlassian.administration.quicksearch.spi.UserContext;
import com.atlassian.plugin.event.PluginEventManager;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Looks for a link param, whose value is assumed to be i18n key of a list of aliases to return.
 *
 * @since 1.0
 */
public class StaticParamAliasProvider implements AdminLinkAliasProvider
{
    public static final String DEFAULT_PARAM_NAME = "atlassian.link.aliases";

    private final String paramName;
    private final StaticAliasProviderHelper helper;

    public StaticParamAliasProvider(String paramName, PluginEventManager eventManager)
    {
        this.paramName = checkNotNull(paramName, "paramName");
        this.helper = new StaticAliasProviderHelper();
        eventManager.register(helper);
    }

    public StaticParamAliasProvider(PluginEventManager eventManager)
    {
        this(DEFAULT_PARAM_NAME, eventManager);
    }

    public StaticParamAliasProvider(String paramName, StaticAliasProviderHelper aliasHelper, PluginEventManager eventManager)
    {
        this.paramName = checkNotNull(paramName, "paramName");
        this.helper = aliasHelper;
        eventManager.register(helper);
    }

    public StaticParamAliasProvider(StaticAliasProviderHelper aliasHelper, PluginEventManager eventManager)
    {
        this(DEFAULT_PARAM_NAME, aliasHelper, eventManager);
    }

    @Override
    public Set<String> getAliases(AdminLink link, Iterable<AdminLinkSection> parentSections, UserContext userContext)
    {
        String aliasesKey = link.getParameters().get(paramName);
        // TODO possibly move this to another provider
        if (StringUtils.isEmpty(aliasesKey))
        {
            aliasesKey = getPluginKey(link);
        }
        if (StringUtils.isEmpty(aliasesKey))
        {
            return StaticAliasProviderHelper.EMPTY_KEYWORDS;
        }
        return helper.aliasesFor(aliasesKey, userContext);
    }

    private String getPluginKey(AdminLink link)
    {
        if (link instanceof AbstractDefaultAdminWebItem)
        {
            final String key = AbstractDefaultAdminWebItem.class.cast(link).getCompleteKey();
            if (!StringUtils.isEmpty(key))
            {
                return paramName + "." + key.replace(":", ".").replace("-", ".");
            }
        }
        return null;
    }
}
