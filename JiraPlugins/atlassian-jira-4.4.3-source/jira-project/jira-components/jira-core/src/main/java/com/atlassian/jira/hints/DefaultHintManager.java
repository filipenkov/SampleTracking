package com.atlassian.jira.hints;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.jira.plugin.webfragment.descriptors.JiraWebItemModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.JiraWebLabel;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.plugin.web.model.WebLabel;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Random;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of the {@link HintManager}.
 *
 * @since v4.2
 */
public class DefaultHintManager implements HintManager
{
    private static final Logger log = Logger.getLogger(DefaultHintManager.class);

    public static final String ALL_HINTS_SECTION = "jira.hints/all";
    public static final String HINTS_PREFIX = "jira.hints/";
    private final Random random = new Random();

    private final JiraWebInterfaceManager webInterfaceManager;

    public DefaultHintManager(final JiraWebInterfaceManager webInterfaceManager)
    {
        this.webInterfaceManager = webInterfaceManager;
    }

    public Hint getRandomHint(final User user, final JiraHelper jiraHelper)
    {
        return getRandomHint(getAllHints(user, jiraHelper));
    }

    public Hint getRandomHint(final com.opensymphony.user.User user, final JiraHelper jiraHelper)
    {
        return getRandomHint((User) user, jiraHelper);
    }

    public List<Hint> getAllHints(final User user, final JiraHelper jiraHelper)
    {
        return getHintsForSection(user, jiraHelper, ALL_HINTS_SECTION);
    }

    public List<Hint> getAllHints(final com.opensymphony.user.User user, final JiraHelper jiraHelper)
    {
        return getAllHints((User) user, jiraHelper);
    }

    public Hint getHintForContext(final User remoteUser, final JiraHelper jiraHelper, final Context context)
    {
        notNull("context", context);
        return getRandomHint(getHintsForSection(remoteUser, jiraHelper, HINTS_PREFIX + context.toString()));
    }

    public Hint getHintForContext(final com.opensymphony.user.User remoteUser, final JiraHelper jiraHelper, final Context context)
    {
        return getHintForContext((User) remoteUser, jiraHelper, context);
    }

    private Hint getRandomHint(final List<Hint> hints)
    {
        if (hints.isEmpty())
        {
            return null;
        }
        int randomPosition = random.nextInt(hints.size());
        return hints.get(randomPosition);
    }

    private List<Hint> getHintsForSection(final User user, final JiraHelper helper, final String section)
    {
        notNull("helper", helper);
        @SuppressWarnings ({ "unchecked" })
        List<JiraWebItemModuleDescriptor> items = webInterfaceManager.getDisplayableItems(section, user, helper);
        return CollectionUtil.transform(items, new Function<JiraWebItemModuleDescriptor, Hint>(){
            public Hint get(final JiraWebItemModuleDescriptor input)
            {
                if(input.getWebLabel() == null)
                {
                    log.warn("Hint web item with key '" + input.getKey() + "' does not define a label");
                }
                return new Hint(getText(input.getLabel(), user, helper), getText(input.getTooltip(), user, helper));
            }
        });
    }

    private String getText(final WebLabel input, User user, JiraHelper helper)
    {
        if(input == null)
        {
            return "";
        }
        return ((JiraWebLabel)input).getDisplayableLabel(user, helper);
    }
}
