package org.jcvi.jira.importer.mappers;

import org.jcvi.glk.ctm.TaskType;
import org.jcvi.jira.importer.jiramodel.IssueType;
import org.jcvi.jira.importer.jiramodel.Status;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts from ctm_task_type.name to JIRA subtasks
 */
public class IssueTypeMapper {
    private static final int POS_PATTERN      = 0;
    private static final int POS_STATUS       = 1;
    private static final int POS_SUBTASK      = 2;
//    private static final int POS_IMPLIEDFIELDVALUES = 99; //TODO

    //static data tables, these are transformed later into lookup tables
    //and objects. This is structured very similarly to the table in
    //StatusMapper. The patterns this time are for Task.getTaskType().getName
    //values.
    private static final String[][] TABLE_PATTERN_STATUS_SUBTASK = {
        //todo: add all the TaskTypes needed for the archived samples
        //todo: tasks that have both a Status and a SubTask
        //pattern              //status        //subtask
        {"PCR",                null,           "PCR Task"       },
        {"RT-PCR",             null,           "RT-PCR Task"    },
        {"Validate",           "Validate",     null             },
        //These are all mapped wrongly. They are here to check if anything
        //else is found when the samples are processed further
        {"5' RACE PCR",	"Move",	null},
        {"Assemble",	"Move",	null},
        {"Close Gap",	"Move",	null},
        {"Cover 1x area",	"Move",	null},
        {"Cover 1x Area",	"Move",	null},
        {"Coverage Analysis",	"Move",	null},
        {"Custom Primers",	"Move",	null},
        {"Edit BAC",	"Move",	null},
        {"Final Check",	"Move",	null},
        {"Insert Ligated End Sequences",	"Move",	null},
        {"Reassemble",	"Move",	null},
        {"redo RT-PCR sequencing",	"Move",	null},
        {"Redo Segment",	"Move",	null},
        {"Repeat Amplicon",	"Move",	null},
        {"Resequence",	"Move",	null},
        {"Resolve Ambiguity",	"Move",	null},
        {"Resolve discrepancy",	"Move",	null},
        {"Resolve frameshift",	"Move",	null},
        {"RT-PCR Amplicon",	"Move",	null},
        {"SAP",	"Move",	null},
        {"Sequence",	"Move",	null},
        {"Submit",	"Move",	null},
        {"Submit Assembly",	"Move",	null},
        {"TOPO Clone",	"Move",	null},
        //added just for hrv2!
        {"Close 5 end",	"Move",	null},
        {"Close 3 end",	"Move",	null},
        {"Close GAP",	"Move",	null},
        {"Begin Closure",	"Move",	null},

    };
    private static Map<String,String[]> ctmNameToJIRA
            = new HashMap<String, String[]>();
    static {
        for(String[] row : TABLE_PATTERN_STATUS_SUBTASK) {
            ctmNameToJIRA.put(
                    row[POS_PATTERN], row);
        }
    }


    public static IssueType getTaskType(TaskType type)
            throws UnmappedField {
        String[] jiraData = getJIRAData(type);
        return IssueType.getTaskType(jiraData[POS_SUBTASK]);
    }

    public static Status getImpliedStatus(TaskType type)
            throws UnmappedField {
        String[] jiraData = getJIRAData(type);
        return Status.getStatus(jiraData[POS_STATUS]);
    }

    private static String[] getJIRAData(TaskType type)
            throws UnmappedField {
        String ctmName = type.getName();
        String[] jiraData = ctmNameToJIRA.get(ctmName);
        if (jiraData == null) {
            throw new UnmappedField("TaskType",ctmName);
        }
        return jiraData;
    }
}
