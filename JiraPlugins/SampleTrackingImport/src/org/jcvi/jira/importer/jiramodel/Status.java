package org.jcvi.jira.importer.jiramodel;

import noNamespace.EntityEngineXmlType;
import noNamespace.StatusType;

import java.util.*;

/**
 *
 */
public class Status extends NameIDPair {
    private static Map<String,Status> statusesByName
            = new HashMap<String, Status>();

    public static Status getStatus(String name) {
        if (name == null) {
            return null;
        }
        return statusesByName.get(name);
    }

    public static Set<Status> getAllStatuses() {
        Collection<Status> values = statusesByName.values();
        Set<Status> valuesSet = new HashSet<Status>(values.size());
        valuesSet.addAll(values);
        return valuesSet;
    }

    public static void staticPopulateFromXML(EntityEngineXmlType xml) {
        for (StatusType statusType : xml.getStatusArray()) {
            Status status = new Status(statusType);
            statusesByName.put(status.getName(), status);
        }
    }

    public Status(StatusType statusType) {
        super(statusType.getId(), statusType.getName());
    }

    public boolean equals(Object o) {
        return o != null &&
                o instanceof Status &&
                ((Status) o).getID() == this.getID();
    }
}
