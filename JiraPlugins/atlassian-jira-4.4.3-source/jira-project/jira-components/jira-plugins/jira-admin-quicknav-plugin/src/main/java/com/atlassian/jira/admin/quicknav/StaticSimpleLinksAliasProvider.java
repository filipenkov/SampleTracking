package com.atlassian.jira.admin.quicknav;

import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * <p/>
 * Default implementation of {@link SimpleLinkAliasProvider}. Retrieves keywords from
 * JIRA's i18n resources and caches them for better performance.
 *
 * <p/>
 * Only works for simple links based on static web items, that contain a parameter {@link #KEYWORD_PARAM_NAME}.
 *
 * @since v4.4
 */
public class StaticSimpleLinksAliasProvider implements SimpleLinkAliasProvider
{
    public static final String KEYWORD_PARAM_NAME = "webfragments.keywords";
    private static final String KEYWORDS_SEPARATOR = ",";

    private static final Set<String> EMPTY_KEYWORDS = Collections.emptySet();

    @Override
    public Set<String> aliasesFor(SimpleLinkSection section, SimpleLink link, JiraAuthenticationContext ctx)
    {
        return getFromBundle(link, ctx);
    }

    private Set<String> getFromBundle(SimpleLink link, final JiraAuthenticationContext ctx)
    {
        final Map<String,String> params = link.getParams();
        if (params == null)
        {
            return EMPTY_KEYWORDS;
        }
        final I18nHelper i18n = ctx.getI18nHelper();
        final String key = keywordResourceKeyFrom(params);
        if (key == null)
        {
            return EMPTY_KEYWORDS;
        }
        String keywords = i18n.getText(key);
        if (StringUtils.isEmpty(keywords) || keywords.equals(key))
        {
            return EMPTY_KEYWORDS;
        }
        return splitKeywords(keywords);
    }


    private String keywordResourceKeyFrom(Map<String, String> params)
    {
        return params != null ? params.get(KEYWORD_PARAM_NAME) : null;
    }

    private Set<String> splitKeywords(final String keywords)
    {
        String[] split = keywords.split(KEYWORDS_SEPARATOR);
        return CollectionBuilder.newBuilder(split).asSortedSet();
    }

}
