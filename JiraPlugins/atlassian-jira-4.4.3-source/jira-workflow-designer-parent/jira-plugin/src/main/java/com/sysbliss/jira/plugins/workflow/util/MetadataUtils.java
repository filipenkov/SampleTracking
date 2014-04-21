/**
 *
 */
package com.sysbliss.jira.plugins.workflow.util;

import com.atlassian.jira.workflow.JiraWorkflow;

/**
 * @author jdoklovic
 */
public class MetadataUtils {

    public static boolean isReservedKey(final String key) {
        if (key == null)
            return false;
        for (int i = 0; i < JiraWorkflow.JIRA_META_ATTRIBUTE_ALLOWED_LIST.length; i++) {
            // Check if our meta attribute starts with one of the allowed prefixes, eg. 'jira.permission'
            final String allowedPrefix = JiraWorkflow.JIRA_META_ATTRIBUTE_ALLOWED_LIST[i];
            if (key.equals(allowedPrefix) || key.startsWith(allowedPrefix)) {
                return false;
            }
        }
        return key.startsWith(JiraWorkflow.JIRA_META_ATTRIBUTE_KEY_PREFIX);
    }
}
