package org.jcvi.jira.importer.jiramodel;

import noNamespace.*;
import org.jcvi.jira.importer.utils.UID;

/**
 */
public class CustomFieldValue {
    private int id;
    private Issue issue;
    private CustomField customField;
    private String value;

    public CustomFieldValue(Issue issue,
                            CustomField customField,
                            String value) {
        this.id = UID.getUID(CustomFieldValue.class);
        this.issue = issue;
        this.customField = customField;
        this.value = value;
    }

    public void addToXml(EntityEngineXmlType xml) {
        CustomFieldValueType customFieldValue = xml.addNewCustomFieldValue();
        customFieldValue.setId(id);
        customFieldValue.setIssue(issue.getUid());
        customFieldValue.setCustomfield(customField.getID());
        customFieldValue.setStringvalue(value);
    }
}
