package com.atlassian.plugins.rest.common.expand.parameter;

import com.atlassian.plugins.rest.common.expand.Expandable;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses and allow easy retrieval of information of expansion parameter.
 */
public final class DefaultExpandParameter implements ExpandParameter
{
    private static final String DOT = ".";
    private static final String COMMA = ",";
    private static final String WILDCARD = "*";

    private static final ExpandParameter EMPTY_EXPAND_PARAMETER = new DefaultExpandParameter((String) null);

    private final Map<String, ExpandInformation> parameters;

    private DefaultExpandParameter(String expand)
    {
        this(StringUtils.isNotBlank(expand) ? Collections.singleton(expand) : Collections.<String>emptyList());
    }

    public DefaultExpandParameter(Collection<String> expands)
    {
        parameters = parse(expands != null ? expands : Collections.<String>emptyList());
    }

    public boolean shouldExpand(Expandable expandable)
    {
        return parameters.containsKey(WILDCARD) || parameters.containsKey(Preconditions.checkNotNull(expandable).value());
    }

    public Indexes getIndexes(Expandable expandable)
    {
        final ExpandInformation expandInformation = parameters.get(Preconditions.checkNotNull(expandable).value());
        return expandInformation != null ? expandInformation.getIndexes() : IndexParser.EMPTY;
    }

    public ExpandParameter getExpandParameter(Expandable expandable)
    {
        final ExpandInformation wildcardExpandInformation = parameters.get(WILDCARD);
        final ExpandInformation valueExpandInformation = parameters.get(Preconditions.checkNotNull(expandable).value());

        return new ChainingExpandParameter(
                wildcardExpandInformation != null ? wildcardExpandInformation.getExpandParameter() : EMPTY_EXPAND_PARAMETER,
                valueExpandInformation != null ? valueExpandInformation.getExpandParameter() : EMPTY_EXPAND_PARAMETER);
    }

    public boolean isEmpty()
    {
        return parameters.isEmpty();
    }

    private static Map<String, ExpandInformation> parse(Collection<String> expands)
    {
        final Map<String, ExpandInformation> parameters = Maps.newHashMap();
        for (String expand : preProcess(expands))
        {
            if (StringUtils.isNotEmpty(expand))
            {
                final ExpandKey key = ExpandKey.from(StringUtils.substringBefore(expand, DOT));

                parameters.put(key.getName(), new ExpandInformation(key.getIndexes(), new DefaultExpandParameter(StringUtils.substringAfter(expand, DOT))));
            }
        }
        return parameters;
    }

    private static Collection<String> preProcess(Collection<String> expands)
    {
        final Collection<String> preProcessed = new HashSet<String>();
        for (String expand : expands)
        {
            preProcessed.addAll(Sets.newHashSet(expand.split(COMMA)));
        }
        return preProcessed;
    }

    private static class ExpandKey
    {
        private static final Pattern KEY_PATTERN = Pattern.compile("(\\w+|\\*)(?:\\[([\\d:\\-\\|]+)\\])?");

        private final String name;
        private final Indexes indexes;

        ExpandKey(String name, Indexes indexes)
        {
            this.name = name;
            this.indexes = indexes;
        }

        public String getName()
        {
            return name;
        }

        public Indexes getIndexes()
        {
            return indexes;
        }

        private static ExpandKey from(String key)
        {
            final Matcher keyMatcher = KEY_PATTERN.matcher(key);
            if (!keyMatcher.matches())
            {
                throw new RuntimeException("key <" + key + "> doesn't match pattern");
            }

            final String name = keyMatcher.group(1);
            final String indexesString = keyMatcher.group(2);
            return new ExpandKey(name, IndexParser.parse(indexesString));
        }
    }

    private static class ExpandInformation
    {
        private final Indexes indexes;
        private final ExpandParameter expandParameter;

        public ExpandInformation(Indexes indexes, ExpandParameter expandParameter)
        {
            this.indexes = Preconditions.checkNotNull(indexes);
            this.expandParameter = Preconditions.checkNotNull(expandParameter);
        }

        public Indexes getIndexes()
        {
            return indexes;
        }

        public ExpandParameter getExpandParameter()
        {
            return expandParameter;
        }
    }
}
