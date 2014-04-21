package org.jcvi.jira.importer.jiramodel;

import noNamespace.EntityEngineXmlType;
import noNamespace.WorkflowType;

import java.util.*;

/**
 */
public class Workflow extends NameIDPair {
    public static final String WORKFLOW_FOR_SAMPLES = "ST - Sample Workflow";
            //"ST - Sample Workflow Updated";

    private static Map<String,Workflow> workflowsByName
            = new HashMap<String, Workflow>();
    //MAPPING
    private static Map<String,String> subTaskToWorkflow = new HashMap<String, String>();
    static {
        subTaskToWorkflow.put("454 Sequencing","ST - Laboratory Workflow");
        subTaskToWorkflow.put("Illumina Sequencing","ST - Laboratory Workflow");
        subTaskToWorkflow.put("Nextera Task","ST - Laboratory Workflow");
        subTaskToWorkflow.put("PCR Task","ST - Laboratory Workflow");
        subTaskToWorkflow.put("PGM Sequencing","ST - Laboratory Workflow");
        subTaskToWorkflow.put("RT-PCR Task","ST - Laboratory Workflow");
        subTaskToWorkflow.put("Sanger Sequencing","ST - Laboratory Workflow");
        subTaskToWorkflow.put("Closure Editing Task","ST - Closure Editing Workflow");
        subTaskToWorkflow.put("Custom Closure Task","ST - Custom Closure Workflow");
        subTaskToWorkflow.put("In-House Closure Task","ST - In-House Closure Workflow");
        //just in case
        subTaskToWorkflow.put("Sample",WORKFLOW_FOR_SAMPLES);
    }

    private static Map<Workflow,Set<Status>> workflowToStatuses = new HashMap<Workflow, Set<Status>>();
    //As this is setup before Workflows and Statuses have been loaded it is initially
    //just string mappings
    private static Map<String,String[]> rawWorkflowToStatuses = new HashMap<String, String[]>();
    static {
        setupRawWorkflowToStatuses("ST - Laboratory Workflow", "Open", "Complete", "Failed");
        setupRawWorkflowToStatuses("ST - Closure Editing Workflow", "Open", "Complete");
        setupRawWorkflowToStatuses("ST - Custom Closure Workflow", "Open", "Design Primer", "Order Primer", "Perform Reactions", "Complete");
        setupRawWorkflowToStatuses("ST - In-House Closure Workflow", "Open", "Perform Reactions", "Complete");
        //only worry about sub-tasks for now
    }

    private static void setupRawWorkflowToStatuses(String workflow, String ... statuses) {
        rawWorkflowToStatuses.put(workflow, statuses);
    }

    public static Workflow getWorkflow(IssueType type) {
        String name = subTaskToWorkflow.get(type.getName());
        if (name == null) {
            System.err.println("Unknown IssueType when searching for Workflow, "+name);
        }
        return getWorkflow(name);
    }

    public static Workflow getWorkflow(String name) {
        if (name == null) {
            return null;
        }
        Workflow workflow = workflowsByName.get(name);
        if (workflow == null) {
            System.err.println("Unknown Workflow: "+name);
        }
        return workflow;
    }

    public static void staticPopulateFromXML(EntityEngineXmlType xml) {

        for (WorkflowType workflowType : xml.getWorkflowArray()) {
            Workflow workflow = new Workflow(workflowType);
            workflowsByName.put(workflow.getName(), workflow);
        }
    }

    public Workflow(WorkflowType workflowType) {
        super(workflowType.getId(),workflowType.getName());
    }

    public Set<Status> getValidStatuses() {
//        if (WORKFLOW_FOR_SAMPLES.equals(getName())) {
//            return Status.getAllStatuses();
//        }
        Set<Status> workflowStatuses = workflowToStatuses.get(this);
        if (workflowStatuses == null) {
            //lazy init
            final Set<Status> statusSet;
            String[] statuses = rawWorkflowToStatuses.get(getName());
            if (statuses == null) {
                //!unknown workflow
                System.err.println("Could not find statuses for workflow " + getName() + " will treat statuses as valid");
                statusSet = Status.getAllStatuses();
            } else {
                statusSet = new HashSet<Status>(statuses.length);
                for (String statusName : statuses) {
                    Status status = Status.getStatus(statusName);
                    if (status == null) {
                        System.err.println("Unknown status used in a workflow, "+statusName+" it cannot be added to the list of valid statuses");
                    } else {
                        statusSet.add(status);
                    }
                }
            }
            workflowToStatuses.put(this,statusSet);
            workflowStatuses = statusSet;
        }
        return workflowStatuses;
    }
}
