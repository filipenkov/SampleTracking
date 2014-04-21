package org.jcvi.jira.plugins.statisticsmapper.shared;

import com.atlassian.jira.issue.customfields.converters.DoubleConverter;
import com.atlassian.jira.issue.fields.CustomField;

import java.util.Comparator;

/**
 * A second version of CFStatisticsMapper that handle numbers. The only
 * difference is in the use of 'DoubleConverter' with the
 * getValueFromLuceneField method.
 */
public class NumberCFStatisticsMapper extends CFStatisticsMapper<Double> {
    private final DoubleConverter doubleConverter;

    public NumberCFStatisticsMapper(CustomField field,
                                    DoubleConverter converter) {
        super(field,
              Double.class, //lucene_typeClass - the type of object stored in the index
              true);        //exactMatch
        doubleConverter = converter;
    }

    @Override
    public Double getValueFromLuceneField(String documentValue) {
        return doubleConverter.getDouble(documentValue);
    }

    @Override
    public Comparator<Double> getComparator() {
        return new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
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
