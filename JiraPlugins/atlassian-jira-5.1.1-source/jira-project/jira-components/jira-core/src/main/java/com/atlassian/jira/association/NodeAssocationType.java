package com.atlassian.jira.association;

/**
 * Config for a particular Node Association.
 *
 * @since v4.4
 */
public class NodeAssocationType
{
    private final String name;
    private final String sourceEntityName;
    private final String sinkEntityName;

    public NodeAssocationType(String name, String sourceEntityName, String sinkEntityName)
    {
        this.name = name;
        this.sourceEntityName = sourceEntityName;
        this.sinkEntityName = sinkEntityName;
    }

    /**
     * Returns the identifying name of the Association Type.
     *
     * @return the identifying name of the Association Type.
     */
    String getName()
    {
        return name;
    }

    /**
     * Returns the entity name of the source entity.
     *
     * @return the entity name of the source entity.
     */
    String getSourceEntityName()
    {
        return sourceEntityName;
    }

    /**
     * Returns the entity name of the sink (destination) entity.
     *
     * @return the entity name of the sink entity.
     */
    String getSinkEntityName()
    {
        return sinkEntityName;
    }
}
