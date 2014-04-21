package com.atlassian.jira.bc.whitelist;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.gadgets.event.AddGadgetEvent;
import com.atlassian.gadgets.event.AddGadgetFeedEvent;
import com.atlassian.gadgets.event.ClearHttpCacheEvent;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.util.concurrent.ResettableLazyReference;
import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation that persists the whitelist in applicationproperties
 *
 * @since v4.3
 */
@EventComponent
public class DefaultWhitelistManager implements WhitelistManager
{
    public static final String NO_WILDCARDS_PREFIX = "=";
    public static final String REGEX_PREFIX = "/";
    private static final String[] RULE_ESCAPE_CHARACTERS = { ".", "?", "+", "|" };
    private static final String WILDCARD_CHARACTER_PATTERN = ".*";


    private final ApplicationProperties applicationProperties;
    private final EventPublisher eventPublisher;

    private ResettableLazyReference<List<String>> rules = new ResettableLazyReference<List<String>>()
    {
        @Override
        protected List<String> create() throws Exception
        {
            final String rulesString = applicationProperties.getText(APKeys.JIRA_WHITELIST_RULES);
            final String[] split = StringUtils.split(rulesString, null);
            final List<String> ret = new ArrayList<String>();
            if (split != null)
            {
                ret.addAll(Arrays.asList(split));
            }
            return Collections.unmodifiableList(ret);
        }
    };

    public DefaultWhitelistManager(final ApplicationProperties applicationProperties, final EventPublisher eventPublisher)
    {
        this.applicationProperties = applicationProperties;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<String> getRules()
    {
        //when the whitelist is disabled, there shouldn't be any rules!
        if (isDisabled())
        {
            return Collections.emptyList();
        }

        return rules.get();
    }

    public boolean isDisabled()
    {
        return applicationProperties.getOption(APKeys.JIRA_WHITELIST_DISABLED);
    }

    @Override
    public List<String> updateRules(final List<String> newRules, final boolean disabled)
    {
        notNull("newRules", newRules);

        final StringBuilder builder = new StringBuilder();
        for (String rule : newRules)
        {
            builder.append(rule).append("\n");
        }
        applicationProperties.setText(APKeys.JIRA_WHITELIST_RULES, builder.toString());
        applicationProperties.setOption(APKeys.JIRA_WHITELIST_DISABLED, disabled);

        rules.reset();
        eventPublisher.publish(ClearHttpCacheEvent.INSTANCE);
        return getRules();
    }

    @Override
    public boolean isAllowed(final URI uri)
    {
        if (isDisabled())
        {
            return true;
        }
        else
        {
            for (String rule : getRules())
            {
                if (urlMatches(uri, rule))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean urlMatches(final URI uri, final String rule)
    {
        boolean matches = false;

        final String uriString = uri.normalize().toASCIIString();
        if (rule.startsWith(NO_WILDCARDS_PREFIX))
        {
            matches = uriString.equalsIgnoreCase(rule.substring(NO_WILDCARDS_PREFIX.length()));
        }
        else
        {
            final Pattern pattern;
            try {
                if (rule.startsWith(REGEX_PREFIX))
                {
                    pattern = Pattern.compile(rule.substring(REGEX_PREFIX.length()));
                }
                else
                {
                    pattern = Pattern.compile(createRegex(rule), Pattern.CASE_INSENSITIVE);
                }
                matches = pattern.matcher(uriString).matches();
            }
            catch (PatternSyntaxException e)
            {
                // should never get here, but just in case...
            }
        }

        return matches;
    }

    protected String createRegex(final String rule)
    {
        String regex = rule;
        for (String escapeChar : RULE_ESCAPE_CHARACTERS)
        {
            regex = regex.replaceAll("\\" + escapeChar, "\\\\" + escapeChar);
        }

        regex = regex.replaceAll("\\*", WILDCARD_CHARACTER_PATTERN);

        return regex;
    }

    @EventListener
    public void onAddGadget(final AddGadgetEvent addGadgetEvent)
    {
        addWhitelistEntry(addGadgetEvent.getGadgetUri());
    }

    @EventListener
    public void onAddGadgetFeed(final AddGadgetFeedEvent addGadgetFeedEvent)
    {
        addWhitelistEntry(addGadgetFeedEvent.getFeedUri());
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        rules.reset();
    }

    private void addWhitelistEntry(final URI uri)
    {
        String newRule = uri.getScheme() + "://" + uri.getAuthority() + "/*";
        final List<String> rules = getRules();
        //if we don't have this rule yet and the whitelist is not disabled add a new rule!
        if(!rules.contains(newRule) && !isDisabled())
        {
            final List<String> newRules = new ArrayList<String>(rules);
            newRules.add(newRule);
            updateRules(newRules, isDisabled());
        }
    }
}
