package com.atlassian.jira.upgrade.tasks;

import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.jira.entity.Delete;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.EntityListConsumer;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.entity.SelectQuery;
import com.atlassian.jira.entity.Update;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;

import java.util.ArrayList;
import java.util.List;

/**
 * This upgrade task will ensure that the username field is lowercase in the UserHistoryItem and FilterSubscription
 * tables.
 *
 * @since v5.1.1
 */
public class UpgradeTask_Build771 extends AbstractUpgradeTask
{
    private final EntityEngine entityEngine;
    private final UserManager userManager;

    public UpgradeTask_Build771(EntityEngine entityEngine, UserManager userManager)
    {
        super(false);
        this.entityEngine = entityEngine;
        this.userManager = userManager;
    }

    @Override
    public String getBuildNumber()
    {
        return "771";
    }

    @Override
    public String getShortDescription()
    {
        return "Convert username to lowercase in UserHistoryItem and FilterSubscription";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        updateUserHistoryItem();
        updateFilterSubscription();
    }

    private void updateFilterSubscription()
    {
        // Find distinct field values that need converting
        final SelectQuery<String> selectQuery = Select.distinctString("username").from("FilterSubscription");
        final List<String> usernamesToConvert = entityEngine.run(selectQuery).consumeWith(new FindMixedCaseUsernames());

        // Update all mixed case usernames to the lower case "user key"
        for (String username : usernamesToConvert)
        {
            entityEngine.execute(Update
                    .into("FilterSubscription")
                    .set("username", IdentifierUtils.toLowerCase(username))
                    .whereEqual("username", username));
        }
    }

    private void updateUserHistoryItem()
    {
        // Find distinct mixed case field values
        final SelectQuery<String> selectQuery = Select.distinctString("username").from("UserHistoryItem");
        final List<String> usernamesToConvert = entityEngine.run(selectQuery).consumeWith(new FindMixedCaseUsernames());

        for (String username : usernamesToConvert)
        {
            // Find which is the "correct" current username
            ApplicationUser user = userManager.getUserByName(username);
            if (user != null && user.getName().equals(username))
            {
                // This is the current username.
                // First delete existing history under the lowercase name (else we can hit unique constraint)
                entityEngine.delete(Delete.from("UserHistoryItem").whereEqual("username", IdentifierUtils.toLowerCase(username)));

                // Now update current username to lower-case user key
                entityEngine.execute(
                        Update.into("UserHistoryItem")
                                .set("username", IdentifierUtils.toLowerCase(username))
                                .whereEqual("username", username));
            }
            else
            {
                // This is not the current username - case is wrong
                // Just delete the useless history
                entityEngine.delete(Delete.from("UserHistoryItem").whereEqual("username", username));
            }
        }
    }

    private static final class FindMixedCaseUsernames implements EntityListConsumer<String, List<String>>
    {
        private final List<String> usernames = new ArrayList<String>();

        @Override
        public void consume(String username)
        {
            if (username != null && !username.equals(IdentifierUtils.toLowerCase(username)))
            {
                usernames.add(username);
            }
        }

        @Override
        public List<String> result()
        {
            return usernames;
        }
    }
}
