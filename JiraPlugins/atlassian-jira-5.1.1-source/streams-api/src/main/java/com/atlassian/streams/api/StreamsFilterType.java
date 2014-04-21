package com.atlassian.streams.api;

import com.google.common.collect.ImmutableList;

import static com.atlassian.streams.api.StreamsFilterType.Operator.AFTER;
import static com.atlassian.streams.api.StreamsFilterType.Operator.BEFORE;
import static com.atlassian.streams.api.StreamsFilterType.Operator.BETWEEN;
import static com.atlassian.streams.api.StreamsFilterType.Operator.CONTAINS;
import static com.atlassian.streams.api.StreamsFilterType.Operator.DOES_NOT_CONTAIN;
import static com.atlassian.streams.api.StreamsFilterType.Operator.IS;
import static com.atlassian.streams.api.StreamsFilterType.Operator.NOT;

/**
 * A Filter option type for the streams
 */
public enum StreamsFilterType
{
    STRING("string", IS, NOT, CONTAINS, DOES_NOT_CONTAIN),
    STRING_LIKE("string", CONTAINS, DOES_NOT_CONTAIN),
    STRING_EXACT("string", IS, NOT),
    LIST("list", IS, NOT),
    DATE("date", BEFORE, AFTER, BETWEEN),
    SELECT("select", IS, NOT),
    USER("user", IS, NOT);

    private final String type;
    private final Iterable<Operator> operators;

    private StreamsFilterType(String type, Operator... operators)
    {
        this.type = type;
        this.operators = ImmutableList.of(operators);
    }

    /**
     * The filter option type string
     *
     * @return The filter option type string
     */
    public String getType()
    {
        return type;
    }

    /**
     * The list of operators allowed for the filter type
     *
     * @return The list of operators allowed for the filter type
     */
    public Iterable<Operator> getOperators()
    {
        return operators;
    }

    /**
     * Operators for the filter type
     */
    public enum Operator
    {
        IS("is", "is", "streams.filter.operator.is"),
        NOT("not", "not", "streams.filter.operator.not"),
        CONTAINS("contains", "contains", "streams.filter.operator.contains"),
        DOES_NOT_CONTAIN("does_not_contain", "does not contain", "streams.filter.operator.does.not.contain"),
        BEFORE("before", "before", "streams.filter.operator.before"),
        AFTER("after", "after", "streams.filter.operator.after"),
        BETWEEN("between", "between", "streams.filter.operator.between");

        private final String displayName;
        private final String i18nKey;
        private final String key;

        private Operator(String key, String displayName, String i18nKey)
        {
            this.key = key;
            this.displayName = displayName;
            this.i18nKey = i18nKey;
        }
        
        public String getKey()
        {
            return key;
        }
        
        public String getDisplayName()
        {
            return displayName;
        }

        public String getI18nKey()
        {
            return i18nKey;
        }
    }
}
