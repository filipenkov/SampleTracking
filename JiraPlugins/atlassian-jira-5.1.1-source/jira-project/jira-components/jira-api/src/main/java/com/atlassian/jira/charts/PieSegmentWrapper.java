package com.atlassian.jira.charts;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.statistics.FilterStatisticsValuesGenerator;
import com.atlassian.jira.issue.statistics.util.DefaultFieldValueToDisplayTransformer;
import com.atlassian.jira.issue.statistics.util.FieldValueToDisplayTransformer;
import com.atlassian.jira.issue.statistics.util.ObjectToFieldValueMapper;
import com.atlassian.jira.util.I18nHelper;

/**
 * A simple wrapper object to implement Comparable for PieDataset as well as retrieve the key's real string name.
 *
 * @since v4.0
 */
public class PieSegmentWrapper implements Comparable
{
    private Object key;
    private String name;
    private boolean generateUrl;

    public PieSegmentWrapper(Object key, final I18nHelper i18nHelper, final String statisticType, final ConstantsManager constantsManager, final CustomFieldManager customFieldManager)
    {
        this.key = key;
        this.generateUrl = true;
        final FieldValueToDisplayTransformer<String> fieldValueToDisplayTransformer =
                new DefaultFieldValueToDisplayTransformer(i18nHelper, constantsManager, customFieldManager);

        if(key == FilterStatisticsValuesGenerator.IRRELEVANT)
        {
            this.key = null;
            this.generateUrl = false;
        }

        name = ObjectToFieldValueMapper.transform(statisticType, key, null, fieldValueToDisplayTransformer);
    }

    public String getName()
    {
        return name;
    }

    public String toString()
    {
        return name;
    }

    public int compareTo(Object o)
    {
        return name.compareTo(((PieSegmentWrapper) o).name);
    }

    public Object getKey()
    {
        return key;
    }

    public boolean isGenerateUrl()
    {
        return generateUrl;
    }

}
