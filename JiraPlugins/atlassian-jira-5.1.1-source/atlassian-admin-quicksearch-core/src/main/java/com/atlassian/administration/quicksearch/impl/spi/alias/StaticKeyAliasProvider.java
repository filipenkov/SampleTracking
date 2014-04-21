package com.atlassian.administration.quicksearch.impl.spi.alias;

import com.atlassian.administration.quicksearch.spi.AdminLink;
import com.atlassian.administration.quicksearch.spi.AdminLinkAliasProvider;
import com.atlassian.administration.quicksearch.spi.AdminLinkSection;
import com.atlassian.administration.quicksearch.spi.UserContext;
import com.atlassian.plugin.event.PluginEventManager;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

/**
 * <p/>
 * Provides the same (i18ned) set of aliases for each link.
 *
 * <p/>
 * Pass an empty key to return empty set of aliases.
 *
 * @since 1.0
 */
public abstract class StaticKeyAliasProvider implements AdminLinkAliasProvider
{
    private final StaticAliasProviderHelper helper;

    public StaticKeyAliasProvider(PluginEventManager eventManager)
    {
        this.helper = new StaticAliasProviderHelper();
        eventManager.register(helper);
    }

    public StaticKeyAliasProvider(StaticAliasProviderHelper aliasHelper, PluginEventManager eventManager)
    {
        this.helper = aliasHelper;
        eventManager.register(helper);
    }

    protected abstract String aliasKey();

    @Override
    public Set<String> getAliases(AdminLink link, Iterable<AdminLinkSection> parentSections, UserContext userContext)
    {
        final String key = aliasKey();
        if (StringUtils.isNotEmpty(key))
        {
            return helper.aliasesFor(key, userContext);
        }
        else
        {
            return StaticAliasProviderHelper.EMPTY_KEYWORDS;
        }
    }

}
