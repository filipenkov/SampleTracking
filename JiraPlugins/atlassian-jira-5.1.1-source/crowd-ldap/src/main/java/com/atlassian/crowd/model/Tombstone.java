package com.atlassian.crowd.model;

/**
 * Active Directory deleted object (tombstone).
 */
public class Tombstone
{
    private final String objectGUID;
    private final Long usnChanged;

    public Tombstone(String objectGUID, String usnChanged)
    {
        this.objectGUID = objectGUID;
        this.usnChanged = Long.parseLong(usnChanged);
    }

    public String getObjectGUID()
    {
        return objectGUID;
    }

    public Long getUsnChanged()
    {
        return usnChanged;
    }

    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer();
        sb.append("Tombstone");
        sb.append("{objectGUID='").append(objectGUID).append('\'');
        sb.append(", usnChanged='").append(usnChanged).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
