package com.atlassian.jira.issue.index;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistoryFieldConstants;
import com.atlassian.jira.issue.changehistory.ChangeHistoryGroup;
import com.atlassian.jira.issue.changehistory.ChangeHistoryItem;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.changehistory.JqlChangeItemMapping;
import com.atlassian.jira.issue.index.DefaultIssueIndexer.ChangeHistoryRetriever;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.util.NotNull;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultChangeHistoryRetriever implements ChangeHistoryRetriever
{
    private final ChangeHistoryManager changeManager;
    private final IndexedChangeHistoryFieldManager indexedChangeHistoryFieldManager;
    private final ChangeHistoryFieldConstants changeHistoryConstants;
    private static final Map<String, String> mappings = ImmutableMap.of( SystemSearchConstants.FIX_FOR_VERSION_CHANGEITEM,
                                                                          SystemSearchConstants.FIX_FOR_VERSION);
    private final JqlChangeItemMapping jqlChangeItemMapping;

    public DefaultChangeHistoryRetriever(@NotNull final ChangeHistoryManager changeManager,
                                         @NotNull final IndexedChangeHistoryFieldManager indexedChangeHistoryFieldManager,
                                         @NotNull ChangeHistoryFieldConstants changeHistoryConstants,
                                         @NotNull JqlChangeItemMapping jqlChangeItemMapping)
    {
        this.changeManager = notNull("changeManager", changeManager);
        this.indexedChangeHistoryFieldManager = notNull("indexedChangeHistoryFieldManager", indexedChangeHistoryFieldManager);
        this.changeHistoryConstants = notNull("changeHistoryConstants",changeHistoryConstants);
        this.jqlChangeItemMapping = jqlChangeItemMapping;
    }

    @SuppressWarnings ("unchecked")
    public List<ChangeHistoryGroup> get(final Issue issue)
    {
        final List<ChangeHistoryItem> changeHistoryItems = changeManager.getAllChangeItems(issue);
        return createChangeGroupsFromChangeItems(changeHistoryItems, issue);

    }

    private List<ChangeHistoryGroup> createChangeGroupsFromChangeItems(final List<ChangeHistoryItem> changeHistoryItems, final Issue issue)
    {
        // need to maintain sorting order here, for this to work
        List<ChangeHistoryItem> filteredChangeItems =  Lists.newArrayList(filterChangeItems(issue, changeHistoryItems));
        Collections.sort(filteredChangeItems);
        long currentChangeGroup = -1;
        ChangeHistoryGroup.Builder builder = new ChangeHistoryGroup.Builder();
        List<ChangeHistoryGroup.Builder> builders = Lists.newArrayList(builder);
        for (ChangeHistoryItem item : filteredChangeItems)
        {
            if (item.getChangeGroupId() != currentChangeGroup)
            {
                currentChangeGroup = item.getChangeGroupId();
                builder = new ChangeHistoryGroup.Builder();
                builders.add(builder);
            }
            builder.addChangeItem(item);
        }
        return Lists.transform(builders, new Function<ChangeHistoryGroup.Builder, ChangeHistoryGroup>()
        {
            @Override
            public ChangeHistoryGroup apply(@Nullable ChangeHistoryGroup.Builder builder)
            {
                return builder.build();
            }
        });

    }

    private List<ChangeHistoryItem> filterChangeItems(Issue issue, Collection<ChangeHistoryItem> changeItems)
    {
        List<ChangeHistoryItem> changes = new ArrayList<ChangeHistoryItem>();

        for (final IndexedChangeHistoryField field : indexedChangeHistoryFieldManager.getIndexedChangeHistoryFields())
        {
            final List<ChangeHistoryItem> supportedChangeItems = ImmutableList.copyOf(Iterables.filter(changeItems, new Predicate<ChangeHistoryItem>()
            {
                public boolean apply(final ChangeHistoryItem input)
                {
                    //add this field and id to the constants manager
                    String fieldName = jqlChangeItemMapping.mapJqlClauseToFieldName(field.getFieldName());
                    if (input != null && fieldName.equals(input.getField()))
                    {
                        if (input.getFroms().size() > 0)
                        {
                            changeHistoryConstants.addChangeHistoryFieldConstant(input.getField(),
                                                                                 input.getFroms().values().iterator().next(),
                                                                                 input.getFroms().keySet().iterator().next());
                        }
                        if (input.getTos().size() > 0)
                        {
                            changeHistoryConstants.addChangeHistoryFieldConstant(input.getField(),
                                                                                 input.getTos().values().iterator().next(),
                                                                                 input.getTos().keySet().iterator().next());
                        }
                        return true;
                    }
                    return false;
                }
            }));
            // make sure there is always at least one change item, and the date ranges are correct
            if (supportedChangeItems != null) {
                changes.addAll(field.getDateRangeBuilder().buildDateRanges(issue, supportedChangeItems));
            }

        }
        return changes;
    }
}
