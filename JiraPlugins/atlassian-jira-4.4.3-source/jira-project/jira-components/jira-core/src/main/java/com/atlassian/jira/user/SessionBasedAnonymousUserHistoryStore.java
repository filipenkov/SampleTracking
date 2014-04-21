package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.util.concurrent.Function;
import com.atlassian.util.concurrent.ManagedLock;
import com.atlassian.util.concurrent.ManagedLocks;
import com.atlassian.util.concurrent.Supplier;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Session based caching implementation of {@link UserHistoryStore}.  Allows anonymous users to have history.
 *
 * @since v4.0
 */
public class SessionBasedAnonymousUserHistoryStore implements UserHistoryStore
{
    private static final int DEFAULT_MAX_ITEMS = 20;
    private static final Logger log = Logger.getLogger(SessionBasedAnonymousUserHistoryStore.class);

    /**
     * Lock on the sessionID.
     */
    private final Function<VelocityRequestSession, ManagedLock> lockManager = ManagedLocks.weakManagedLockFactory(new Function<VelocityRequestSession, String>()
    {
        public String get(final VelocityRequestSession input)
        {
            return input.getId();
        }
    });

    private final UserHistoryStore delegatingStore;
    private final ApplicationProperties applicationProperties;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    static final String SESSION_PREFIX = "history-";

    public SessionBasedAnonymousUserHistoryStore(final UserHistoryStore delegatingStore, final ApplicationProperties applicationProperties, final VelocityRequestContextFactory velocityRequestContextFactory)
    {
        this.delegatingStore = delegatingStore;
        this.applicationProperties = applicationProperties;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
    }

    public VelocityRequestSession getSession()
    {
        final VelocityRequestSession session = velocityRequestContextFactory.getJiraVelocityRequestContext().getSession();
        if(session == null || session.getId() == null)
        {
            return null;
        }
        return session;
    }

    public void addHistoryItem(@NotNull final User user, @NotNull final UserHistoryItem historyItem)
    {
        notNull("historyItem", historyItem);

        final VelocityRequestSession session = getSession();

        if (session == null)
        {
            if (user == null)
            {
                // can't do anything for this poor fella
                return;
            }
            delegatingStore.addHistoryItem(user, historyItem);
            return;
        }

        lockManager.get(session).withLock(new Runnable()
        {
            public void run()
            {
                final String SESSION_KEY = SESSION_PREFIX + historyItem.getType().getName();
                @SuppressWarnings("unchecked")
                final List<UserHistoryItem> sessionHistory = (List<UserHistoryItem>) session.getAttribute(SESSION_KEY);

                if ((sessionHistory != null) && !sessionHistory.isEmpty() && (user != null))
                {
                    // user has history in session but is not anonymous.  Must have just logged in.
                    copySessionToStore(user, sessionHistory);
                    session.removeAttribute(SESSION_KEY);
                    delegatingStore.addHistoryItem(user, historyItem);
                }
                else if (user != null)
                {
                    // No history in session, delegate to store
                    delegatingStore.addHistoryItem(user, historyItem);
                }
                else if (sessionHistory == null)
                {
                    // No history in session and no user.  Add new history to session
                    final ArrayList<UserHistoryItem> newHistory = new ArrayList<UserHistoryItem>();
                    newHistory.add(historyItem);
                    session.setAttribute(SESSION_KEY, newHistory);
                }
                else
                {
                    // Anonymous user has history in session.  Add to session
                    final int index = getIndexOfHistoryItem(sessionHistory, historyItem);

                    if (index == -1)
                    {
                        sessionHistory.add(0, historyItem);
                        final int maxItems = getMaxItems(historyItem.getType());
                        if (sessionHistory.size() > maxItems)
                        {
                            while (sessionHistory.size() > maxItems)
                            {
                                sessionHistory.remove(sessionHistory.size() - 1);
                            }
                        }

                    }
                    else
                    {
                        sessionHistory.remove(index);
                        sessionHistory.add(0, historyItem);
                    }
                }
            }
        });
    }

    private void copySessionToStore(final User user, final List<UserHistoryItem> sessionItems)
    {
        for (int i = sessionItems.size(); i > 0; i--)
        {
            final UserHistoryItem userHistoryItem = sessionItems.get(i - 1);
            delegatingStore.addHistoryItem(user, userHistoryItem);
        }
    }

    private int getIndexOfHistoryItem(final List<UserHistoryItem> history, final UserHistoryItem historyItem)
    {
        for (int i = 0; i < history.size(); i++)
        {
            final UserHistoryItem currentHistoryItem = history.get(i);
            if (currentHistoryItem.getEntityId().equals(historyItem.getEntityId()) && currentHistoryItem.getType().equals(historyItem.getType()))
            {
                return i;
            }
        }
        return -1;
    }

    @NotNull
    public List<UserHistoryItem> getHistory(final UserHistoryItem.Type type, final User user)
    {
        notNull("type", type);

        final VelocityRequestSession session = getSession();

        if (session == null)
        {
            if (user == null)
            {
                // can't do anything for this poor fella
                return Collections.emptyList();
            }
            return delegatingStore.getHistory(type, user);
        }
        try
        {
            return lockManager.get(session).withLock(new Supplier<List<UserHistoryItem>>()
            {
                public List<UserHistoryItem> get()
                {
                    final String SESSION_KEY = SESSION_PREFIX + type.getName();
                    @SuppressWarnings("unchecked")
                    final List<UserHistoryItem> sessionHistory = (List<UserHistoryItem>) session.getAttribute(SESSION_KEY);

                    if ((sessionHistory != null) && !sessionHistory.isEmpty() && (user != null))
                    {
                        // User has something in the session.  Must have just logged in.
                        copySessionToStore(user, sessionHistory);
                        session.removeAttribute(SESSION_KEY);
                        return delegatingStore.getHistory(type, user);
                    }
                    else if (user != null)
                    {
                        // No history in session, delegate to store
                        return delegatingStore.getHistory(type, user);
                    }
                    else if (sessionHistory == null)
                    {
                        final ArrayList<UserHistoryItem> newHistory = new ArrayList<UserHistoryItem>();
                        session.setAttribute(SESSION_KEY, newHistory);
                        return newHistory;
                    }
                    return sessionHistory;
                }
            });
        }
        catch (final RuntimeException e)
        {
            log.error("Exception thrown while retrieving UserhistoryItems.", e);
        }
        return Collections.emptyList();
    }

    public Set<UserHistoryItem.Type> removeHistoryForUser(@NotNull final User user)
    {
        // The session is most probably not related to the User being passed in, so we will not remove it.  We let the
        // session die a natural death.

        if (user == null)
        {
            // can't do anything for this poor fella
            return Collections.emptySet();
        }
        return delegatingStore.removeHistoryForUser(user);
    }

    private int getMaxItems(final UserHistoryItem.Type type)
    {
        final String maxItemsForTypeStr = applicationProperties.getDefaultBackedString("jira.max." + type.getName() + ".history.items");
        final int maxItems = DEFAULT_MAX_ITEMS;
        try
        {
            if (StringUtils.isNotBlank(maxItemsForTypeStr))
            {
                return Integer.parseInt(maxItemsForTypeStr);
            }
        }
        catch (final NumberFormatException e)
        {
            log.warn("Incorrect format of property 'jira.max.history.items'.  Should be a number.");
        }

        final String maxItemsStr = applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_HISTORY_ITEMS);
        try
        {
            if (StringUtils.isNotBlank(maxItemsStr))
            {
                return Integer.parseInt(maxItemsStr);
            }
        }
        catch (final NumberFormatException e)
        {
            log.warn("Incorrect format of property 'jira.max.history.items'.  Should be a number.");
        }
        return maxItems;
    }
}