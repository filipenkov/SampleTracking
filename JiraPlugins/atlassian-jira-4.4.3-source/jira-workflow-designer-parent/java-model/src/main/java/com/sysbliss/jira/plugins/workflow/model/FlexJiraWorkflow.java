package com.sysbliss.jira.plugins.workflow.model;

import java.util.List;

/**
 * @author jdoklovic
 * 
 */
public interface FlexJiraWorkflow extends FlexWorkflowObject {

    boolean getIsEditable();

    void setIsEditable(boolean canEdit);

    void setIsLoaded(boolean b);

    boolean getIsLoaded();

    void setIsActive(boolean b);

    boolean getIsActive();

    void setIsSystemWorkflow(boolean b);

    boolean getIsSystemWorkflow();

    void setIsDraftWorkflow(boolean b);

    boolean getIsDraftWorkflow();

    void setHasDraftWorkflow(boolean b);

    boolean getHasDraftWorkflow();

    void setAllSteps(List steps);

    List getAllSteps();

    void setAllActions(List actions);

    List getAllActions();

    void setGlobalActions(List actions);

    List getGlobalActions();

    void setInitialActions(List actions);

    List getInitialActions();

    void setUnlinkedStatuses(List statuses);

    List getUnlinkedStatuses();

    boolean getHasSchemes();

    void setHasSchemes(boolean b);

    List<WorkflowAnnotation> getWorkflowAnnotations();

    void setWorkflowAnnotations(List<WorkflowAnnotation> annotations);
}
