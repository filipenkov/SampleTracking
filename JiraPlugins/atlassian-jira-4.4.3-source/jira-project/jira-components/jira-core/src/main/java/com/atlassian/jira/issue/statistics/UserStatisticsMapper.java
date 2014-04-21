package com.atlassian.jira.issue.statistics;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.comparator.UserBestNameComparator;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.constants.UserFieldSearchConstantsWithEmpty;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.base.Function;

import java.util.Comparator;
import java.util.Locale;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class UserStatisticsMapper implements StatisticsMapper<User>
{
    private final String clauseName;
    private final String indexedField;
    private final UserResolver userResolver;
    protected final JiraAuthenticationContext jiraAuthenticationContext;

    public UserStatisticsMapper(final UserFieldSearchConstantsWithEmpty searchConstants, final UserManager userManager, final JiraAuthenticationContext jiraAuthenticationContext)
    {
        this(searchConstants.getJqlClauseNames().getPrimaryName(), searchConstants.getEmptyIndexValue(), searchConstants.getIndexField(),
            userManager, jiraAuthenticationContext);
    }

    public UserStatisticsMapper(final String clauseName, final String emptyIndexValue, final String indexedField, final UserManager userManager, final JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.clauseName = notBlank("clauseName", clauseName);
        this.indexedField = notBlank("indexedField", indexedField);
        userResolver = new OSUserManagerResolver(userManager, emptyIndexValue);
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public Comparator<User> getComparator()
    {
        return new UserResolvingComparator(userResolver, getLocale());
    }

    public boolean isValidValue(final User value)
    {
        return true;
    }

    public User getValueFromLuceneField(final String documentValue)
    {
        return userResolver.apply(documentValue);
    }

    public boolean isFieldAlwaysPartOfAnIssue()
    {
        return true;
    }

    Locale getLocale()
    {
        return jiraAuthenticationContext.getLocale();
    }

    public SearchRequest getSearchUrlSuffix(final User value, final SearchRequest searchRequest)
    {
        if (searchRequest == null)
        {
            return null;
        }
        else
        {
            final JqlClauseBuilder builder = JqlQueryBuilder.newBuilder(searchRequest.getQuery()).where().defaultAnd();
            if (value != null)
            {
                builder.addStringCondition(getClauseName(), (value).getName());
            }
            else
            {
                builder.addEmptyCondition(getClauseName());
            }
            return new SearchRequest(builder.buildQuery());
        }
    }

    protected String getClauseName()
    {
        return clauseName;
    }

    public String getDocumentConstant()
    {
        return indexedField;
    }

    TerminalClause getUserClause(final String name)
    {
        return new TerminalClauseImpl(clauseName, Operator.EQUALS, name);
    }

    TerminalClause getEmptyUserClause()
    {
        return new TerminalClauseImpl(clauseName, Operator.IS, EmptyOperand.EMPTY);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final UserStatisticsMapper that = (UserStatisticsMapper) o;

        if (!clauseName.equals(that.clauseName))
        {
            return false;
        }
        if (!indexedField.equals(that.indexedField))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = clauseName.hashCode();
        result = 31 * result + indexedField.hashCode();
        return result;
    }

    /**
     * Get a User given a user name.
     */
    interface UserResolver extends Function<String, User>
    {}

    static class OSUserManagerResolver implements UserResolver
    {
        private final UserManager userManager;
        private final String emptyIndexValue;

        public OSUserManagerResolver(final UserManager userManager, final String emptyIndexValue)
        {
            this.userManager = userManager;
            this.emptyIndexValue = emptyIndexValue;
        }

        public User apply(final String documentValue)
        {
            if ((emptyIndexValue != null) && emptyIndexValue.equals(documentValue))
            {
                return null;
            }
            if (documentValue == null)
            {
                return null;
            }
            return userManager.getUser(documentValue);
        }
    }

    static final class UserResolvingComparator implements Comparator<User>
    {
        private final UserBestNameComparator nameComparator;
        private final UserResolver resolver;

        public UserResolvingComparator(final UserResolver resolver, final Locale locale)
        {
            nameComparator = new UserBestNameComparator(locale);
            this.resolver = notNull("userResolver", resolver);
        }

        public int compare(final User o1, final User o2)
        {
            if (o1 == o2)
            {
                return 0;
            }
            if (o1 == null)
            {
                return 1;
            }
            if (o2 == null)
            {
                return -1;
            }
            return nameComparator.compare(resolver.apply(o1.getName()), resolver.apply(o2.getName()));
        }
    }
}
