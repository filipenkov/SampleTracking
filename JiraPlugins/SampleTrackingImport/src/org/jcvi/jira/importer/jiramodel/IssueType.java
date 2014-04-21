package org.jcvi.jira.importer.jiramodel;

import noNamespace.EntityEngineXmlType;
import noNamespace.IssueTypeType;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class IssueType extends NameIDPair {
    private static Map<String,IssueType> issueTypesByName
            = new HashMap<String, IssueType>();

    public static final String SAMPLE_ISSUE_TYPE_NAME = "Sample";

    public static IssueType getTaskType(String name) {
        return issueTypesByName.get(name);
    }

    //a bit naughty this only acts on static state but to use an interface
    //it needs to be an instance. Really I should have created separate
    //Factory classes
    public void populateFromXML(EntityEngineXmlType xml) {
        staticPopulateFromXML(xml);
    }
    public static void staticPopulateFromXML(EntityEngineXmlType xml) {
        for (IssueTypeType type: xml.getIssueTypeArray()) {
            IssueType jiraType = new IssueType(type);
            issueTypesByName.put(jiraType.getName(),jiraType);
        }
    }

    //we only actually care about the id and the name
    //private final short id;
    //private final String name;
    //Other parameters
    //description
    //sequence
    //iconurl
    //style //if this appears it is 'jira_subtask'

    public IssueType(IssueTypeType xmlForIssueType) {
        super(xmlForIssueType.getId(),xmlForIssueType.getName());
    }
}
