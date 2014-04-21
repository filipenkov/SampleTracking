package org.jcvi.jira.importer.jiramodel;

import noNamespace.EntityEngineXmlType;
import noNamespace.IssueLinkTypeType;

/**
 * We only use one IssueLinkType, sub-tasks
 */
public class IssueLinkType {
    public static IssueLinkType subTaskIssueLinkType
            = new IssueLinkType("jira_subtask",(short)10000);

    private final short id;
    private final String baseName;

    public IssueLinkType(String name, short number) {
        this.id = number;
        this.baseName = name;
    }

    public short getId() {
        return id;
    }

    public String getLinkName() {
        return baseName+"_link";
    }

    public String getInwardName() {
        return baseName+"_inward";
    }

    public String getOutwardName() {
        return baseName+"_outward";
    }

    public String getStyleName() {
        return baseName;
    }

    public void addToXML(EntityEngineXmlType xml) {
        IssueLinkTypeType type = xml.addNewIssueLinkType();
        type.setId(getId());
        type.setLinkname(getLinkName());
        type.setInward(getInwardName());
        type.setOutward(getOutwardName());
        type.setStyle(getStyleName());
    }
}
