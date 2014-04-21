/**
 *
 */
package com.sysbliss.jira.plugins.workflow.service;

import com.sysbliss.jira.plugins.workflow.exception.FlexLoginException;
import com.sysbliss.jira.plugins.workflow.exception.FlexNoPermissionException;
import com.sysbliss.jira.plugins.workflow.exception.FlexNotLoggedInException;
import com.sysbliss.jira.plugins.workflow.exception.WorkflowDesignerServiceException;
import com.sysbliss.jira.plugins.workflow.model.*;
import com.sysbliss.jira.plugins.workflow.model.layout.JWDLayout;

import java.util.List;
import java.util.Map;

/**
 * @author jdoklovic
 */
public interface WorkflowDesignerService {
    String ping();

    public List getWorkflows(String token) throws FlexNotLoggedInException, FlexNoPermissionException;

    FlexJiraWorkflow loadWorkflow(final FlexJiraWorkflow fwd, String token) throws FlexNotLoggedInException, FlexNoPermissionException, WorkflowDesignerServiceException;

    public List getAllStatuses(String token) throws FlexNotLoggedInException, FlexNoPermissionException;

    public List getFieldScreens(String token) throws FlexNotLoggedInException, FlexNoPermissionException;

    public FlexJiraServerInfo getJiraServerInfo();

    public FlexJiraUserPrefs getUserPrefs(String token) throws FlexNotLoggedInException, FlexNoPermissionException, WorkflowDesignerServiceException;

    public FlexJiraWorkflow copyWorkflow(final String newName, final String newDesc, final FlexJiraWorkflow fjw, String token) throws WorkflowDesignerServiceException, FlexNotLoggedInException,
            FlexNoPermissionException;

    public FlexJiraWorkflow createDraftWorkflow(final FlexJiraWorkflow fjw, String token) throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException;

    public void deleteWorkflow(final FlexJiraWorkflow fjw, String token) throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException;

    public FlexJiraWorkflow publishDraftWorkflow(final FlexJiraWorkflow fjw, final boolean enableBackup, final String backupName, String token) throws WorkflowDesignerServiceException,
            FlexNotLoggedInException, FlexNoPermissionException;

    public FlexJiraWorkflow createNewWorkflow(final String name, final String desc, String token) throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException;

    public FlexJiraWorkflow addStep(final FlexJiraStep fjs, final FlexJiraWorkflow fjw, JWDLayout layout, String token) throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException;

    public Map addTransition(final String name, final String desc, final String view, final FlexJiraStep fjFromStep, final FlexJiraStep fjToStep, final FlexJiraWorkflow fjw, String token)
            throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException;

    public Map addGlobalTransition(final String name, final String desc, final int resultId, final String view, final FlexJiraWorkflow fjw, String token)
            throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException;


    public FlexJiraWorkflow deleteStepsAndActions(final FlexJiraDeleteRequest deleteRequest, final FlexJiraWorkflow fjw, JWDLayout layout, String token) throws WorkflowDesignerServiceException,
            FlexNotLoggedInException, FlexNoPermissionException;

    public FlexJiraWorkflow updateStep(FlexJiraStep fjs, String newName, String newStatus, FlexJiraWorkflow fjw, String token) throws WorkflowDesignerServiceException, FlexNotLoggedInException,
            FlexNoPermissionException;

    public void updateIssueEditable(FlexJiraStep fjs,Boolean editable,FlexJiraWorkflow fjw, String token) throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException;
    public FlexJiraWorkflow updateAction(FlexJiraAction fja, String newName, String newDesc, FlexJiraStep newDestStep, String newView, final FlexJiraWorkflow fjw, String token)
            throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException;

    public FlexJiraStatus createNewStatus(final String name, final String desc, final String iconUrl, String token) throws WorkflowDesignerServiceException, FlexNotLoggedInException,
            FlexNoPermissionException;

    public FlexJiraStatus updateStatus(final String id, final String name, final String desc, final String iconUrl, String token) throws WorkflowDesignerServiceException, FlexNotLoggedInException,
            FlexNoPermissionException;

    public FlexJiraStatus deleteStatus(final String id, String token) throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException;

    public FlexJiraWorkflow updateProperties(final FlexJiraMetadataContainer mdc, final Map metadata, final FlexJiraWorkflow fjw, String token) throws WorkflowDesignerServiceException,
            FlexNotLoggedInException, FlexNoPermissionException;

    public String login(String username, String password) throws FlexLoginException;

    public String getUserSession() throws FlexNotLoggedInException;

    public JWDLayout calculateLayout(final JWDLayout jwdLayout);

    public void saveActiveLayout(String name, JWDLayout layout, final String token) throws WorkflowDesignerServiceException, FlexNoPermissionException, FlexNotLoggedInException;

    JWDLayout loadLayout(final FlexJiraWorkflow fjw, final String token) throws WorkflowDesignerServiceException, FlexNoPermissionException, FlexNotLoggedInException;

    public void saveDraftLayout(String name, JWDLayout layout, final String token) throws WorkflowDesignerServiceException, FlexNoPermissionException, FlexNotLoggedInException;

    public Map cloneTransition(String name, String desc, int actionIdToClone, FlexJiraStep fjFromStep, FlexJiraStep fjToStep, FlexJiraWorkflow fjw, String token)
            throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException;

    public Map cloneGlobalTransition(String name, String desc, int actionIdToClone, FlexJiraWorkflow fjw, String token)
            throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException;

    public FlexJiraWorkflow updateGlobalAction(FlexJiraAction fja, String newName, String newDesc, int newDestStepId, String newView, final FlexJiraWorkflow fjw, String token)
            throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException;


    public Map useCommonTransition(int commonActionId, FlexJiraStep fjFromStep, FlexJiraWorkflow fjw,String token)
            throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException;

    public FlexJiraWorkflow deleteGlobalAction(final int actionId, final FlexJiraWorkflow fjw, String token) throws WorkflowDesignerServiceException,
            FlexNotLoggedInException, FlexNoPermissionException;

    void addAnnotationToWorkflow(FlexJiraWorkflow workflow, WorkflowAnnotation annotation, JWDLayout layout, String token) throws WorkflowDesignerServiceException, FlexNoPermissionException, FlexNotLoggedInException;
    void removeAnnotationFromWorkflow(FlexJiraWorkflow workflow, WorkflowAnnotation annotation, JWDLayout layout, String token) throws WorkflowDesignerServiceException, FlexNoPermissionException, FlexNotLoggedInException;
    void updateAnnotationForWorkflow(FlexJiraWorkflow workflow, WorkflowAnnotation annotation, JWDLayout layout, String token) throws WorkflowDesignerServiceException, FlexNoPermissionException, FlexNotLoggedInException;
}
