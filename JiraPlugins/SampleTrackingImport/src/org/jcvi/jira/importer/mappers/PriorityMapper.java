package org.jcvi.jira.importer.mappers;

import org.jcvi.glk.ctm.TaskPriority;
import org.jcvi.jira.importer.jiramodel.Priority;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps from a ctm_task_history.priority to a JIRA priority
 */
public class PriorityMapper {
    private static Map<String,String> ctmNameToJIRAName = new HashMap<String, String>();
    //load the map, the mappings are static
    static {
        ctmNameToJIRAName.put("critical","Critical");
        ctmNameToJIRAName.put("high","Major");
        ctmNameToJIRAName.put("medium","Minor");
        ctmNameToJIRAName.put("low","Trivial");
    }

    //MAPPING
    public static Priority getPriority(TaskPriority priority)
            throws UnmappedField {
        String name = ctmNameToJIRAName.get(priority.name());
        Priority result = Priority.getPriority(name);
        if (result == null) {
            throw new UnmappedField("Priority",priority.name());
        }
        return result;
    }
}
