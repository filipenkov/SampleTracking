package org.jcvi.jira.importer.jiramodel;

import noNamespace.EntityEngineXmlType;
import noNamespace.ResolutionType;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class Resolution extends NameIDPair {
    private static Map<String,Resolution> resolutionsByName
            = new HashMap<String, Resolution>();

    public static Resolution getResolution(String name) {
        if (name == null) {
            return null;
        }
        return resolutionsByName.get(name);
    }

    public static void staticPopulateFromXML(EntityEngineXmlType xml) {
        for (ResolutionType resolutionType : xml.getResolutionArray()) {
            Resolution resolution = new Resolution(resolutionType);
            resolutionsByName.put(resolution.getName(), resolution);
        }
    }

    public Resolution(ResolutionType resolutionType) {
        super(resolutionType.getId(),resolutionType.getName());
    }
}
