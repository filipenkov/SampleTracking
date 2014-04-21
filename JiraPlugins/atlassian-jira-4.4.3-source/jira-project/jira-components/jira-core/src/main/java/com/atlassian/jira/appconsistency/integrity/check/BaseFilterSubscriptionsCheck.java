package com.atlassian.jira.appconsistency.integrity.check;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.appconsistency.integrity.amendment.DeleteEntityAmendment;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Base class for filter subscriptions checks.
 */
public abstract class BaseFilterSubscriptionsCheck extends CheckImpl
{
    private static final Logger log = Logger.getLogger(BaseFilterSubscriptionsCheck.class);

    protected BaseFilterSubscriptionsCheck(OfBizDelegator ofBizDelegator, int id)
    {
        super(ofBizDelegator, id);
    }

    public List preview() throws IntegrityException
    {
        return doCheck(false);
    }

    public List correct() throws IntegrityException
    {
        return doCheck(true);
    }

    public boolean isAvailable()
    {
        return true;
    }

    public String getUnavailableMessage()
    {
        return "";
    }

    // Ensure that the filter subscriptions table does not contain references to search requests that have been deleted.
    protected List doCheck(boolean correct) throws IntegrityException
    {
        List messages = new ArrayList();
        // get all filter subscriptions
        OfBizListIterator listIterator = null;

        try
        {
            // Retrieve all issues
            // As documented in org.ofbiz.core.entity.EntityListIterator.hasNext() the best way to find out
            // if there are any results left in the iterator is to iterate over it until null is returned
            // (i.e. not use hasNext() method)
            // The documentation mentions efficiency only - but the functionality is totally broken when using
            // hsqldb JDBC drivers (hasNext() always returns true).
            // So listen to the OfBiz folk and iterate until null is returned.
            listIterator = ofBizDelegator.findListIteratorByCondition("FilterSubscription", null);
            GenericValue subscription = (GenericValue) listIterator.next();
            while (subscription != null)
            {
                // let the child class do its thing
                this.doRealCheck(correct, subscription, messages);
                subscription = (GenericValue) listIterator.next();
            }
        }
        catch (Exception e)
        {
            throw new IntegrityException("Error occurred while performing check.", e);
        }
        finally
        {
            if (listIterator != null)
            {
                // Close the iterator
                listIterator.close();
            }

        }

        if (correct && !messages.isEmpty())
        {
            for (Iterator iterator = messages.iterator(); iterator.hasNext();)
            {
                DeleteEntityAmendment amendment = (DeleteEntityAmendment) iterator.next();
                GenericValue subscriptionGV = amendment.getEntity();
                try
                {
                    ManagerFactory.getSubscriptionManager().deleteSubscription(subscriptionGV.getLong("id"));
                }
                catch (Exception e)
                {
                    log.warn("Unable to remove the subscription from the database", e);
                    throw new IntegrityException(e);
                }
            }
        }

        return messages;
    }

    protected abstract void doRealCheck(boolean correct, GenericValue subscription, List messages) throws IntegrityException;
}
