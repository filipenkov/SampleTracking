package org.jcvi.jira.importer.mappers;

import org.jcvi.glk.ctm.ReferenceStatus;
import org.jcvi.glk.ctm.TaskStatus;
import org.jcvi.jira.importer.jiramodel.*;
import org.jcvi.jira.importer.utils.CSVToHash;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: pedworth
 * Date: 2/25/13
 * Time: 9:15 AM
 * Contains the logic that maps a ctm status to various JIRA objects
 */
public class StatusMapper {
    private static Map<String,JIRAParts> ctmNameToJIRAParts
                = new HashMap<String, JIRAParts>();
    //for once it has been matched
    private static Map<Status,JIRAParts> statusToJIRAParts
                = new HashMap<Status, JIRAParts>();

    private static Map<Pattern,JIRAParts> patternToJIRAParts
                = new HashMap<Pattern, JIRAParts>();

    public static void staticPopulateFromCSV(CSVToHash table) {
        //add the entries to the three lookups, ctmNamesToJIRAParts, statusToJIRAParts and patternToFields
        for(String ctmStatus: table.getKeys()) {
            Map<String,String> fields = table.getValuesFor(ctmStatus);
            String status = fields.get("STATUS");
            if (status == null || status.trim().length() == 0) {
                continue;
            }
            JIRAParts data = new JIRAParts(status,
                                           fields.get("RESOLUTION"),
                                           fields.get("INCLUDES_USER"));

            if (ctmStatus.contains(".*")) {
                //it's a pattern
                patternToJIRAParts.put(Pattern.compile(ctmStatus),
                                       data);
            } else {
                //direct lookup
                ctmNameToJIRAParts.put(ctmStatus,data);
                statusToJIRAParts.put(data.getStatus(),data);
            }
        }


//        System.out.println("PATTERN TO JIRA PARTS");
//        for(Map.Entry<Pattern,JIRAParts>tuple : patternToJIRAParts.entrySet()) {
//            System.out.println(tuple.getKey().toString()+":"+tuple.getValue());
//        }
//
//        System.out.println("CTM TO JIRA PARTS");
//        for(Map.Entry<String,JIRAParts>tuple : ctmNameToJIRAParts.entrySet()) {
//            System.out.println(tuple.getKey()+":"+tuple.getValue());
//        }
//
//        System.out.println("JIRA TO JIRA PARTS");
//        for(Map.Entry<Status,JIRAParts>tuple : statusToJIRAParts.entrySet()) {
//            Status key = tuple.getKey();
//            JIRAParts value = tuple.getValue();
//            System.out.println(key.toString()+":"+value);
//        }
//

    }

    private static class JIRAParts {
        private Status status = null;
        private Resolution resolution = null;
        private final boolean includesAssignee;
        //public JIRAUser assignee; --calculate assignees fresh each time
        private JIRAParts(String statusName, String issueTypeName, String incName) {
            this.status      = Status.getStatus(statusName);
            this.resolution = Resolution.getResolution(issueTypeName);
            this.includesAssignee = incName != null;
        }
        public Status getStatus() {
            return status;
        }
        public Resolution getResolution() {
            return resolution;
        }
        public boolean getIncludesAssignee() {
            return includesAssignee;
        }
        public String toString() {
            return "JIRAParts "+status+"\t"+resolution+"\t"+includesAssignee;
        }
    }

    //only used for Samples
    public static Status getStatus(ReferenceStatus status)
            throws UnmappedField {
        if (status == null) {
            return null;
        }
        return lookupJIRAStatus(status.getName());
    }

    //sub-task only method
    public static Status getStatus(TaskStatus status)
            throws UnmappedField {
        if (status == null) {
            return null;
        }

        return lookupJIRAStatus(status.getName());
    }

    //recover embedded assignees from Edit and Closure Tasks
    //requires the ReferenceStatus as the name may need to be parsed out of the
    //status name
    public static JIRAUser getImpliedAssignee(ReferenceStatus status)
            throws UnmappedField {
        if (status == null) {
            return null;
        }
        JIRAParts jiraParts = lookupJIRAParts(status.getName());

        if(jiraParts != null && jiraParts.getIncludesAssignee()) {
            //todo: include in pattern a capture group for the initials
            //split off the last characters and try looking them up
            String[] nameParts = status.getName().split("[ -]");
            if (nameParts.length < 2) {
//                System.err.println("Type has implied Assignee but none was found: "+status.getName());
                return null;
            }
            String assigneeInitials = nameParts[nameParts.length-1];//last part
            //todo: hack
            if ("Edit".equals(assigneeInitials) ||
                "Closure".equals(assigneeInitials) ||
                "Design".equals(assigneeInitials) ||
                "Hold".equals(assigneeInitials)) {
                return null;
            }
            return UserMapper.getUserFromInitials(assigneeInitials);
        }
        return null;
    }

    //check if the status implies a value for the resolution field
    public static Resolution getImpliedResolution(Status status) {
        if (status == null) {
            return null;
        }
        JIRAParts parts = lookupJIRAParts(status);
        if (parts != null) {
            return parts.getResolution();
        }
        //System.err.println("Failed to get Parts for "+status.getName());
        //a warning message should already have been displayed
        return null;
    }

    //check if the status implies a customField value
    //doesn't yet lookup the status, more just a place holder.
    //it may not be necessary.
    @SuppressWarnings("UnusedParameters")
    public static Set<ChangeItem> getImpliedFieldValues(ReferenceStatus status) {
        return new HashSet<ChangeItem>();
    }

    private static Status lookupJIRAStatus(String ctmName)
            throws UnmappedField {
        JIRAParts parts = lookupJIRAParts(ctmName);
        if (parts != null) {
            return parts.getStatus();
        }
        return null;
    }

    private static JIRAParts lookupJIRAParts(String ctmName)
            throws UnmappedField {
        //First type a littoral lookup
        JIRAParts jiraPart = ctmNameToJIRAParts.get(ctmName.trim());
        if (jiraPart != null) {
            return jiraPart;
        }
        //try using a pattern
        for(Map.Entry<Pattern,JIRAParts> tuple: patternToJIRAParts.entrySet()) {
            Matcher match = tuple.getKey().matcher(ctmName);
            if (match.matches()) {
                //add into the lookup table
                JIRAParts parts = tuple.getValue();
                ctmNameToJIRAParts.put(ctmName, parts);
                statusToJIRAParts.put(parts.getStatus(),parts);
                return parts;
            }
        }
        //Nothing matched
        throw new UnmappedField("Status",ctmName);
//        ctmNameToJIRAParts.put(ctmName, null);
//        return null;
    }

    private static JIRAParts lookupJIRAParts(Status status) {
        //it has to have been created for the status to be known
        return statusToJIRAParts.get(status);
    }

}
