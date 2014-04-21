package com.atlassian.jira.issue.changehistory;


import com.atlassian.annotations.PublicApi;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.annotation.concurrent.Immutable;
import java.sql.Timestamp;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A simple holder for change items
 *
 * In 4.4 the behaviour of this domain object was changed to add support for changes that can can have multi value states
 * for instance an affectedVersion could change from the state "4.4 4.4.1" -> "4.4 4.4.1 4.4.2"
 * To this end end extra methods are added getFroms(), getTos() that return a map of all the changes in a single change item
 * getFrom and getTo will return the first change in this list, this will not break behaviour for any of the supported
 * single value fields.  For Versions and other multi value fields the behaviour of getFrom(), getTo(), getFromValue()
 * and get ToValue() will return only the first change in the map.  It is preferred to use getFroms and getTos in this case.
 * getFrom(), getTo(), getFromValue() and getToValue() are therefore all amrked as deprectaed and may be removed
 * in future versions.
 *
 * @since v4.3
 */
@Immutable
@PublicApi
public class ChangeHistoryItem implements Comparable<ChangeHistoryItem>
{
    private final Long id;
    private final Long changeGroupId;
    private final String user;
    private final String field;
    private final Long projectId;
    private final Long issueId;
    private final String issueKey;
    private final Timestamp created;
    private final Timestamp nextChangeCreated;
    private final Map<String, String> fromValues;
    private final Map<String, String> toValues;
    private static final Timestamp TS_MAX = new Timestamp(Long.MAX_VALUE);

    public ChangeHistoryItem(Long id, Long changeGroupId, Long projectId, Long issueId, String issueKey, String field, Timestamp created, String from, String to, String fromValue, String toValue, String user)
    {
        this(id, changeGroupId, projectId, issueId, issueKey, field, created, new Timestamp(Long.MAX_VALUE), from, to, fromValue, toValue, user);
    }

    public ChangeHistoryItem(Long id, Long changeGroupId, Long projectId, Long issueId, String issueKey, String field, Timestamp created,
            Timestamp nextChange, String from, String to, String fromValue, String toValue, String user)
    {
        this.fromValues = Maps.newHashMap();
        this.toValues = Maps.newHashMap();
        this.field=field;
        this.id = id;
        this.changeGroupId = changeGroupId;
        this.user = user;
        this.projectId = projectId;
        this.issueId = issueId;
        this.issueKey = issueKey;
        this.created = created;
        this.nextChangeCreated = nextChange;
        if (fromValue != null)
        {
            this.fromValues.put(fromValue, from == null ? "" : from);
        }
        if (toValue != null)
        {
            this.toValues.put(toValue, to == null ? "" : to);
        }
    }

    private ChangeHistoryItem(Long id, Long changeGroupId, Long projectId, Long issueId, String issueKey, String field, Timestamp created,
            Timestamp nextChange, Map<String,String> fromValues, Map<String, String> toValues, String user)
    {
        this.fromValues = fromValues;
        this.toValues = toValues;
        this.id = id;
        this.changeGroupId = changeGroupId;
        this.user = user;
        this.projectId = projectId;
        this.issueId = issueId;
        this.issueKey = issueKey;
        this.created = created;
        this.nextChangeCreated = nextChange;
        this.field=field;
    }

    public Long getId()
    {
        return id;
    }

    public Long getChangeGroupId()
    {
        return changeGroupId;
    }

    public String getUser()
    {
        return user;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public Long getIssueId()
    {
        return issueId;
    }

    public String getIssueKey()
    {
        return issueKey;
    }

    public Timestamp getCreated()
    {
        return created;
    }

    /**
     * @deprecated As of 4.4.2, prefer to use {@link #getFroms()}
     */
    @Deprecated
    public String getFrom()
    {
        String from = null;
        if (fromValues.size() > 0)
        {
            from = fromValues.values().iterator().next();
        }
        return from;
    }

    /**
     * @deprecated As of 4.4.2, prefer to use {@link #getTos()}}
     */
    @Deprecated
    public String getTo()
    {
        String to = null;
        if (toValues.size() > 0)
        {
            to = toValues.values().iterator().next();
        }
        return to;
    }

    /**
     * @deprecated As of 4.4.2, prefer to use {@link #getFroms()}
     */
    @Deprecated
    public String getFromValue()
    {
        String fromValue = null;
        if (fromValues.size() > 0)
        {
            fromValue = fromValues.keySet().iterator().next();
        }
        return fromValue;
    }

    /**
     * @deprecated As of 4.4.2, prefer to use {@link #getTos()}
     */
    @Deprecated
    public String getToValue()
    {
        String toValue=null;
        if (toValues.size() > 0)
        {
            toValue = toValues.keySet().iterator().next();
        }
        return toValue;
    }

    public Map<String, String> getFroms()
    {
        return ImmutableMap.copyOf(fromValues);
    }

    public Map<String, String> getTos()
    {
        return ImmutableMap.copyOf(toValues);
    }

    public String getField()
    {
        return field;
    }

    public Timestamp getNextChangeCreated()
    {
        return nextChangeCreated;
    }

    public Long getDuration()
    {
        if (nextChangeCreated.equals(TS_MAX))
        {
             return -1L;
        }
        else
        {
            return nextChangeCreated.getTime() - created.getTime();
        }

    }

    public boolean containsFromValue(String fromValue)
    {
        return this.fromValues.keySet().contains(fromValue);
    }

    public boolean containsToValue(String toValue)
    {
        return this.toValues.keySet().contains(toValue);
    }

    @Override
    public boolean equals(Object o)
    {
        if(o == null || !(o instanceof  ChangeHistoryItem))
        {
            return false;
        }
        ChangeHistoryItem rhs = (ChangeHistoryItem)o;
        return new EqualsBuilder()
                .append(getId(), rhs.getId())
                .append(getChangeGroupId(), rhs.getChangeGroupId())
                .append(getField(), rhs.getField())
                .append(getUser(), rhs.getUser())
                .append(getProjectId(), rhs.getProjectId())
                .append(getIssueId(), rhs.getIssueId())
                .append(getIssueKey(), rhs.getIssueKey())
                .append(getCreated(), rhs.getCreated())
                .append(getNextChangeCreated(), rhs.getNextChangeCreated())
                .append(getFroms(), rhs.getFroms())
                .append(getTos(), rhs.getTos())
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
                .append(getId())
                .append(getChangeGroupId())
                .append(getField())
                .append(getUser())
                .append(getProjectId())
                .append(getIssueId())
                .append(getIssueKey())
                .append(getCreated())
                .append(getNextChangeCreated())
                .append(getFroms())
                .append(getTos())
                .toHashCode();
    }


    @Override
    public int compareTo(ChangeHistoryItem other)
    {
        if (changeGroupId.compareTo(other.getChangeGroupId()) == 0 )
        {
            return id.compareTo(other.getId());
        }
        return created.compareTo(other.getCreated());
    }

    public static class Builder
    {
        private Long id;
        private Long changeGroupId;
        private Long projectId;
        private Long issueId;
        private String issueKey;
        private String field;
        private Timestamp created;
        private Map<String, String> fromValues = Maps.newHashMap();
        private Map<String, String> toValues = Maps.newHashMap();
        private String userName;
        private Timestamp nextChangeCreated = new Timestamp(Long.MAX_VALUE);

        public Builder fromChangeItem(ChangeHistoryItem changeItem)
        {
            this.fromChangeItemWithoutPreservingChanges(changeItem);
            this.fromValues = Maps.newHashMap(changeItem.getFroms());
            this.toValues = Maps.newHashMap(changeItem.getTos());
            return this;
        }

        public Builder fromChangeItemWithoutPreservingChanges(ChangeHistoryItem changeItem)
        {
            this.id = changeItem.getId();
            this.projectId = changeItem.getProjectId();
            this.changeGroupId = changeItem.getChangeGroupId();
            this.issueId = changeItem.getIssueId();
            this.issueKey = changeItem.getIssueKey();
            this.field = changeItem.getField();
            this.created = changeItem.getCreated();
            this.userName = changeItem.getUser();
            this.nextChangeCreated = changeItem.getNextChangeCreated();
            return this;
        }

        public Builder fromChangeItemPreservingFromValues(ChangeHistoryItem changeItem)
        {
            this.fromChangeItemWithoutPreservingChanges(changeItem);
            this.fromValues = Maps.newHashMap(changeItem.getFroms());
            return this;
        }

        public Builder fromChangeItemPreservingToValues(ChangeHistoryItem changeItem)
        {
            this.fromChangeItemWithoutPreservingChanges(changeItem);
            this.toValues = Maps.newHashMap(changeItem.getTos());
            return this;
        }

        public Builder withId(final Long id)
        {
            this.id = id;
            return this;
        }

        /**
         * @deprecated since 5.1  - Use {@link #withId(Long id)}
         */
        public Builder withId(final long id)
        {
            return withId(Long.valueOf(id));
        }

        public Builder inChangeGroup(Long id)
        {
            checkNotNull(id);
            this.changeGroupId = id;
            return this;
        }

        /**
         * @deprecated since 5.1  - Use {@link #inChangeGroup(Long id)}
         */
        public Builder inChangeGroup(long id)
        {
            return inChangeGroup(Long.valueOf(id));
        }

        public Builder inProject(final Long projectId)
        {
            checkNotNull(projectId);
            this.projectId = projectId;
            return this;
        }

        /**
         * @deprecated since 5.1  - Use {@link #inProject(Long projectId)}
         */
        public Builder inProject(final long projectId)
        {
            return inProject(Long.valueOf(projectId));
        }

        public Builder forIssue(final Long issueId, final String issueKey)
        {
            checkNotNull(issueId);
            this.issueId = issueId;
            this.issueKey = issueKey == null ? "" : issueKey;
            return this;
        }

        /**
         * @deprecated since 5.1  - Use {@link #forIssue(Long issueId, String issueKey)}
         */
        public Builder forIssue(final long issueId, final String issueKey)
        {
            return forIssue(Long.valueOf(issueId), issueKey);
        }

        public Builder field(final String field)
        {
            checkNotNull(field);
            this.field = field;
            return this;
        }

        public Builder changedFrom(final String from, final String fromValue)
        {
            if (fromValue != null)
            {
                this.fromValues.put(fromValue, from == null ? "" : from);
            }
            return this;
        }

        public Builder to(final String to, final String toValue)
        {
            if (toValue != null)
            {
                this.toValues.put(toValue, to == null ? "" : to);
            }
            return this;
        }

        public Builder byUser(final String userName)
        {
            this.userName = userName;
            return this;
        }

        public Builder on(final Timestamp created)
        {
            checkNotNull(created);
            this.created = created;
            return this;
        }

        public Builder nextChangeOn(final Timestamp nextChangeCreated)
        {
            this.nextChangeCreated = nextChangeCreated;
            return this;
        }

        public Builder withTos(Map <String, String> tos)
        {
            this.toValues = Maps.newHashMap(tos);
            return this;
        }

        public Builder withFroms(Map<String, String> froms)
         {
             this.fromValues = Maps.newHashMap(froms);
             return this;
         }


        public ChangeHistoryItem build()
        {
            return new ChangeHistoryItem(id, changeGroupId, projectId, issueId, issueKey, field, created, nextChangeCreated,
                    fromValues, toValues, userName);
        }
    }
}
