package org.jcvi.jira.plugins.utils;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.FieldAccessor;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.NavigableField;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 */
public class JIRAFieldUtils {
    private static final Logger log = Logger.getLogger(JIRAFieldUtils.class);
    public static Set<NavigableField> getFields() {
            try {
                FieldAccessor fieldAccessor = ComponentAccessor.getFieldAccessor();
                return fieldAccessor.getAllAvailableNavigableFields();
            } catch (FieldException fe) {
                log.error("Failed to get fields: ",fe);
                return new HashSet<NavigableField>();
            }
        }
}
