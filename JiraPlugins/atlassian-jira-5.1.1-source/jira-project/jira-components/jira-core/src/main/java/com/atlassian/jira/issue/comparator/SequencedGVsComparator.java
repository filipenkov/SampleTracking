package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.comparator.util.DelegatingComparator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * For a list of issues, get the related entities for a given issue relation constant.
 * For the related entity, sort by the seqence number of that GV.
 *
 * @see com.atlassian.jira.issue.IssueRelationConstants
 */
public abstract class SequencedGVsComparator implements Comparator
{
    /**
     * Comparator that compares projects first, then fix-for-versions
     */
    public static final Comparator FIX_FOR_VERSION_COMPARATOR =
            new DelegatingComparator(new ProjectComparator(), new FixForVersionIssueGVsComparator());

    /**
     * Comparator that compares projects first, then raised-in-versions
     */
    public static final Comparator RAISED_IN_COMPARATOR =
            new DelegatingComparator(new ProjectComparator(), new RaisedInVersionIssueGVsComparator());

    /**
     * Comparator that compares projects first, then components
     */
    public static final Comparator COMPONENT_COMPARATOR = new
            DelegatingComparator(new ProjectComparator(), new ComponentIssueGVsComparator());

    /**
     * Null-safe comparator
     */
    private static final Comparator NULL_COMPARATOR = new NullComparator();

    //todo - this should be rewritten to use Version Objects at some point in the future.
    public int compare(Object o1, Object o2)
    {
        if (!(o1 instanceof GenericValue) || !(o2 instanceof GenericValue))
        {
            throw new IllegalArgumentException("SequencedGVsComparator can only be used to compare two issues.  Instead got " + o1 + " " + o2);
        }

        GenericValue issue1 = (GenericValue) o1;
        GenericValue issue2 = (GenericValue) o2;

        Collection GVs1 = null;
        Collection GVs2 = null;
        try
        {
            GVs1 = getIssueManager().getEntitiesByIssue(getIssueRelationConstant(), issue1);
            GVs2 = getIssueManager().getEntitiesByIssue(getIssueRelationConstant(), issue2);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }

        Comparable earliestSequence1 = getEarliestInCollection(GVs1);
        Comparable earliestSequence2 = getEarliestInCollection(GVs2);

        return NULL_COMPARATOR.compare(earliestSequence1, earliestSequence2);
    }

    private IssueManager getIssueManager()
    {
        return ComponentAccessor.getIssueManager();
    }

    private Comparable getEarliestInCollection(Collection versions1)
    {
        Comparable earliestObject = null;
        for (Iterator iterator = versions1.iterator(); iterator.hasNext();)
        {
            GenericValue gv = (GenericValue) iterator.next();
            Comparable current = getComparableField(gv);

            if (earliestObject == null || current.compareTo(earliestObject) < 1)
            {
                earliestObject = current;
            }
        }
        return earliestObject;
    }

    protected abstract Comparable getComparableField(GenericValue gv);

    protected abstract String getIssueRelationConstant();

    public static class FixForVersionIssueGVsComparator extends SequencedGVsComparator
    {
        protected Comparable getComparableField(GenericValue gv)
        {
            return gv.getLong("sequence");
        }

        protected String getIssueRelationConstant()
        {
            return IssueRelationConstants.FIX_VERSION;
        }
    }

    public static class RaisedInVersionIssueGVsComparator extends SequencedGVsComparator
    {
        protected Comparable getComparableField(GenericValue gv)
        {
            return gv.getLong("sequence");
        }

        protected String getIssueRelationConstant()
        {
            return IssueRelationConstants.VERSION;
        }
    }

    public static class ComponentIssueGVsComparator extends SequencedGVsComparator
    {
        protected Comparable getComparableField(GenericValue gv)
        {
            return gv.getString("name");
        }

        protected String getIssueRelationConstant()
        {
            return IssueRelationConstants.COMPONENT;
        }
    }

}
