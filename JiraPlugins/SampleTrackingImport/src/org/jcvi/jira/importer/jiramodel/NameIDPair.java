package org.jcvi.jira.importer.jiramodel;

/**
 * For most of the JIRA/XML Objects we don't really care about anything except
 * the id and the name
 */
public class NameIDPair {
    private final short id;
    private final String name;

    public NameIDPair(short id, String name) {
        this.id   = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }
    public short getID() {
        return id;
    }
}
