package com.atlassian.jira.appconsistency.integrity.check;

import org.quartz.SimpleTrigger;

/**
 * A integrity checker amendment useful for deleting SimpleTriggers.
 */
public class DeleteTriggerAmendmentImpl extends AbstractAmendment
{
    private SimpleTrigger entity;


    public DeleteTriggerAmendmentImpl(int type, String message, SimpleTrigger entity)
    {
        super(type, "", message);
        this.entity = entity;
    }

    public SimpleTrigger getEntity()
    {
        return entity;
    }

    public void setEntity(SimpleTrigger entity)
    {
        this.entity = entity;
    }

    /**
     * Two DeleteTriggerAmendments are equal if they are the same trigger. (Note: Mainly used for testing).
     * @param o
     * @return
     */
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        DeleteTriggerAmendmentImpl that = (DeleteTriggerAmendmentImpl) o;

        if (entity != null ? !entity.equals(that.entity) : that.entity != null)
        {
            return false;
        }
        if(getType() != that.getType())
        {
            return false;
        }
        if (getMessage()!=null && !getMessage().equals(that.getMessage()))
        {
            return false;
        }

        return true;
    }


    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (entity != null ? entity.hashCode() : 0);
        return result;
    }
}
