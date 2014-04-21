package org.jcvi.jira.importer.jiramodel;

import noNamespace.CustomFieldType;
import noNamespace.EntityEngineXmlType;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class CustomField extends NameIDPair {
    private static Map<String,CustomField> customFieldLookup
            = new HashMap<String, CustomField>();

    public CustomField(CustomFieldType customFieldXML) {
        super(customFieldXML.getId(), customFieldXML.getName());
    }

    public static void staticPopulateFromXML(EntityEngineXmlType xml) {
        for (CustomFieldType customFieldXML: xml.getCustomFieldArray()) {
            CustomField customField = new CustomField(customFieldXML);
            customFieldLookup.put(customField.getName(),customField);
        }
    }

    public static CustomField getCustomField(String name) {
        return customFieldLookup.get(name);
    }
}
