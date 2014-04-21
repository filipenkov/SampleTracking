package org.jcvi.jira.plugins.statisticsmapper.shared;

import com.atlassian.jira.issue.fields.CustomField;

import java.util.Comparator;

/**
 * A very simple implementation of CFStatisticsMapper for fields with String
 * values. This should cover most fields.
 */
public class StringCFStatisticsMapper extends CFStatisticsMapper<String> {
    public StringCFStatisticsMapper(CustomField field, boolean exactMatch) {
        super(field, String.class,exactMatch);
    }

    @Override
    public String getValueFromLuceneField(String documentValue) {
        return documentValue;
    }

    @Override
    public Comparator<String> getComparator() {
        return new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (o1 == null && o2 == null) {
                    return 0;
                }
                if (o1 == null) {
                    return -1;
                }
                if (o2 == null) {
                    return 1;
                }
                return o1.compareTo(o2);
            }
        };
    }
}
