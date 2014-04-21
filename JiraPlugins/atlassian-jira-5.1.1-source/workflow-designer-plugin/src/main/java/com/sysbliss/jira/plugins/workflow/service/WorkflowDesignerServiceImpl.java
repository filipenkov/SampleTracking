package com.sysbliss.jira.plugins.workflow.service;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants;
import com.atlassian.jira.plugin.workflow.WorkflowFunctionModuleDescriptor;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.workflow.ConfigurableJiraWorkflow;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.jira.workflow.WorkflowTransitionUtil;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import com.opensymphony.workflow.loader.ConditionsDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.RestrictionDescriptor;
import com.opensymphony.workflow.loader.ResultDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;
import com.sysbliss.jira.plugins.workflow.WorkflowDesignerConstants;
import com.sysbliss.jira.plugins.workflow.auth.UserTokenManager;
import com.sysbliss.jira.plugins.workflow.exception.FlexLoginException;
import com.sysbliss.jira.plugins.workflow.exception.FlexNoPermissionException;
import com.sysbliss.jira.plugins.workflow.exception.FlexNotLoggedInException;
import com.sysbliss.jira.plugins.workflow.exception.WorkflowDesignerServiceException;
import com.sysbliss.jira.plugins.workflow.manager.CachingWorkflowImageManagerImpl;
import com.sysbliss.jira.plugins.workflow.manager.WorkflowAnnotationManager;
import com.sysbliss.jira.plugins.workflow.manager.WorkflowImageManager;
import com.sysbliss.jira.plugins.workflow.manager.WorkflowLayoutManager;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraAction;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraDeleteActionRequest;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraDeleteRequest;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraFieldScreen;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraFieldScreenImpl;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraMetadataContainer;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraServerInfo;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraServerInfoImpl;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraStatus;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraStatusImpl;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraStep;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraUserPrefs;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraUserPrefsImpl;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflowImpl;
import com.sysbliss.jira.plugins.workflow.model.FlexWorkflowObject;
import com.sysbliss.jira.plugins.workflow.model.WorkflowAnnotation;
import com.sysbliss.jira.plugins.workflow.model.layout.JWDLayout;
import com.sysbliss.jira.plugins.workflow.util.MetadataUtils;
import com.sysbliss.jira.plugins.workflow.util.StatusUtils;
import com.sysbliss.jira.plugins.workflow.util.WorkflowConverter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author jdoklovic
 */
public class WorkflowDesignerServiceImpl implements WorkflowDesignerService
{
    private static Logger log = Logger.getLogger(WorkflowDesignerService.class);
    private final WorkflowManager workflowManager;
    private final WorkflowService workflowService;
    private final WorkflowSchemeManager workflowSchemeManager;
    private final FieldScreenManager fieldScreenManager;
    private final ConstantsManager constantsManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final PermissionManager permissionManager;
    private final BuildUtilsInfo buildUtils;
    private final PluginAccessor pluginAccessor;
    private final WorkflowLayoutManager workflowLayoutManager;
    private final WorkflowImageManager workflowImageManager;
    private final WorkflowAnnotationManager workflowAnnotationManager;
    private final CrowdService crowdService;
    private final UserPropertyManager userPropertyManager;
    private final UserTokenManager userTokenManager;

    public WorkflowDesignerServiceImpl(final WorkflowManager workflowManager,
            final FieldScreenManager fieldScreenManager, final PluginAccessor pluginAccessor,
            final WorkflowService workflowService, final WorkflowSchemeManager workflowSchemeManager,
            final JiraAuthenticationContext jiraAuthenticationContext, final PermissionManager permissionManager,
            final BuildUtilsInfo buildUtils, final ConstantsManager constantsManager,
            final WorkflowLayoutManager workflowLayoutManager, final WorkflowImageManager workflowImageManager,
            final WorkflowAnnotationManager workflowAnnotationManager, final CrowdService crowdService,
            final UserPropertyManager userPropertyManager, final UserTokenManager userTokenManager)
    {

        this.workflowManager = workflowManager;
        this.workflowService = workflowService;
        this.workflowSchemeManager = workflowSchemeManager;
        this.fieldScreenManager = fieldScreenManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.permissionManager = permissionManager;
        this.pluginAccessor = pluginAccessor;
        this.buildUtils = buildUtils;
        this.constantsManager = constantsManager;
        this.workflowLayoutManager = workflowLayoutManager;
        this.workflowImageManager = workflowImageManager;
        this.workflowAnnotationManager = workflowAnnotationManager;
        this.crowdService = crowdService;
        this.userPropertyManager = userPropertyManager;
        this.userTokenManager = userTokenManager;
    }


    public String ping()
    {
        return "pingy";
    }

    public String getUserSession() throws FlexNotLoggedInException
    {
        final User user = jiraAuthenticationContext.getUser();
        String token;

        if (user == null)
        {
            throw new FlexNotLoggedInException("You must be logged into Jira to use this app.");
        }

        try
        {
            token = userTokenManager.createToken(user);
        }
        catch (final Exception e)
        {
            throw new FlexNotLoggedInException("You must be logged into Jira to use this app.");
        }

        return token;
    }

    public FlexJiraServerInfo getJiraServerInfo()
    {
        log.debug("getting server info");
        final FlexJiraServerInfo info = new FlexJiraServerInfoImpl();
        info.setIsEnterprise(true);
        info.setIsProfessional(false);
        info.setIsStandard(false);
        info.setVersion(buildUtils.getVersion());

        log.debug("returning server info");
        return info;
    }

    private void checkUser(final String tokenToTest) throws FlexNotLoggedInException, FlexNoPermissionException
    {
        User user;
        if (StringUtils.isBlank(tokenToTest))
        {
            user = jiraAuthenticationContext.getUser();
        }
        else
        {
            user = userTokenManager.getUserFromToken(tokenToTest);
        }

        if (user == null)
        {
            throw new FlexNotLoggedInException("You must be logged into Jira to use this app.");
        }

        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            throw new FlexNoPermissionException("You must be an Administrator to use this app.");
        }
    }

    public String login(final String username, final String password) throws FlexLoginException
    {
        String token;

        final FlexLoginException le = new FlexLoginException(username + " could not be logged in.");
        try
        {
            User user = crowdService.authenticate(username, password);
            if (user == null)
            {
                throw new Exception("authentication failed");
            }
            token = userTokenManager.createToken(user);
        }
        catch (final Exception e)
        {
            throw le;
        }

        return token;
    }

    public FlexJiraUserPrefs getUserPrefs(final String token)
            throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException
    {
        checkUser(token);
        final User user = jiraAuthenticationContext.getUser();

        String username;
        if (user != null)
        {
            username = user.getName();
        }
        else
        {
            throw new WorkflowDesignerServiceException("You must be logged in.");
        }

        final PropertySet pset = userPropertyManager.getPropertySet(user);
        boolean hasOurPrefs;
        final Collection prefKeys = pset.getKeys(WorkflowDesignerConstants.PREFS_PREFIX);
        hasOurPrefs = (prefKeys.size() > 0);

        if (!hasOurPrefs)
        {
            try
            {
                addDefaultUserPrefs(pset);
            }
            catch (final Exception e)
            {
                throw new WorkflowDesignerServiceException(e);
            }
        }

        final FlexJiraUserPrefs prefs = new FlexJiraUserPrefsImpl();
        prefs.setUsername(username);

        String prefKey;
        String prefName;
        for (final Iterator it = prefKeys.iterator(); it.hasNext(); )
        {
            prefKey = (String) it.next();
            prefName = StringUtils.substringAfter(prefKey, WorkflowDesignerConstants.PREFS_PREFIX);

            if (prefName.equals("confirmDeleteSelection"))
            {
                prefs.setConfirmDeleteSelection(pset.getBoolean(prefKey));
            }
            else if (prefName.equals("confirmDeleteWorkflow"))
            {
                prefs.setConfirmDeleteWorkflow(pset.getBoolean(prefKey));
            }
        }

        return prefs;
    }

    void addDefaultUserPrefs(final PropertySet props)
    {
        props.setBoolean(WorkflowDesignerConstants.PREFS_PREFIX + "confirmDeleteSelection", true);
        props.setBoolean(WorkflowDesignerConstants.PREFS_PREFIX + "confirmDeleteWorkflow", true);
    }

    public List getWorkflows(final String token) throws FlexNotLoggedInException, FlexNoPermissionException
    {
        checkUser(token);
        final List ret = new ArrayList();
        for (final Iterator iterator = workflowManager.getWorkflows().iterator(); iterator.hasNext(); )
        {
            final JiraWorkflow jiraWorkflow = (JiraWorkflow) iterator.next();
            final FlexJiraWorkflow wfd = WorkflowConverter.convertMinimalWorkflow(jiraWorkflow, workflowSchemeManager);

            ret.add(wfd);

            final JiraWorkflow draftWorkflow = workflowManager.getDraftWorkflow(jiraWorkflow.getName());
            if (draftWorkflow != null)
            {
                final FlexJiraWorkflow dwfd = WorkflowConverter.convertMinimalWorkflow(draftWorkflow, workflowSchemeManager);

                ret.add(dwfd);
            }
        }
        return ret;
    }

    public FlexJiraWorkflow loadWorkflow(final FlexJiraWorkflow fwd, final String token)
            throws FlexNotLoggedInException, FlexNoPermissionException,
            WorkflowDesignerServiceException
    {
        checkUser(token);
        FlexJiraWorkflow fjw;
        final JiraWorkflow jiraWorkflow = getJiraWorkflow(fwd);
        /*
       * if (jiraWorkflow.isEditable()) { fjw = createAllUUIDs(jiraWorkflow, token); } else { fjw =
       * WorkflowConverter.convertFullWorkflow(jiraWorkflow, workflowSchemeManager); }
       */
        fjw = WorkflowConverter.convertFullWorkflow(jiraWorkflow, workflowSchemeManager, workflowAnnotationManager);
        return fjw;

    }

    public List getAllStatuses(final String token) throws FlexNotLoggedInException, FlexNoPermissionException
    {
        checkUser(token);
        final List ret = new ArrayList();

        final List statusList = new ArrayList(constantsManager.getStatusObjects());

        for (final Iterator iterator = statusList.iterator(); iterator.hasNext(); )
        {
            final Status status = (Status) iterator.next();
            final FlexJiraStatus fjs = new FlexJiraStatusImpl();
            fjs.setId(status.getId());
            fjs.setName(status.getName());
            fjs.setDescription(status.getDescription());
            fjs.setIconUrl(status.getIconUrl());
            fjs.setIsActive(StatusUtils.isActive(status.getGenericValue()));
            ret.add(fjs);
        }

        return ret;
    }

    public FlexJiraStatus createNewStatus(final String name, final String desc, final String iconUrl, final String token)
            throws WorkflowDesignerServiceException, FlexNotLoggedInException,
            FlexNoPermissionException
    {
        checkUser(token);
        final StatusUtils statusUtils = new StatusUtils();
        GenericValue gv;
        try
        {
            gv = statusUtils.addStatus(name, desc, iconUrl);
        }
        catch (final GenericEntityException e)
        {
            throw new WorkflowDesignerServiceException(e);
        }

        final FlexJiraStatus fjs = new FlexJiraStatusImpl();
        fjs.setId(gv.getString("id"));
        fjs.setName(gv.getString("name"));
        fjs.setDescription(gv.getString("description"));
        fjs.setIconUrl(gv.getString("iconurl"));

        return fjs;
    }

    public FlexJiraStatus deleteStatus(final String id, final String token)
            throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException
    {
        checkUser(token);
        final StatusUtils statusUtils = new StatusUtils();
        GenericValue gv;
        try
        {
            gv = statusUtils.deleteStatus(id);
        }
        catch (final GenericEntityException e)
        {
            throw new WorkflowDesignerServiceException(e);
        }

        final FlexJiraStatus fjs = new FlexJiraStatusImpl();
        fjs.setId(gv.getString("id"));
        fjs.setName(gv.getString("name"));
        fjs.setDescription(gv.getString("description"));
        fjs.setIconUrl(gv.getString("iconurl"));

        return fjs;
    }

    public FlexJiraStatus updateStatus(final String id, final String name, final String desc, final String iconUrl, final String token)
            throws WorkflowDesignerServiceException,
            FlexNotLoggedInException, FlexNoPermissionException
    {
        checkUser(token);
        final StatusUtils statusUtils = new StatusUtils();
        GenericValue gv;
        try
        {
            gv = statusUtils.updateStatus(id, name, desc, iconUrl);
        }
        catch (final GenericEntityException e)
        {
            throw new WorkflowDesignerServiceException(e);
        }

        final FlexJiraStatus fjs = new FlexJiraStatusImpl();
        fjs.setId(gv.getString("id"));
        fjs.setName(gv.getString("name"));
        fjs.setDescription(gv.getString("description"));
        fjs.setIconUrl(gv.getString("iconurl"));

        return fjs;
    }

    public List getFieldScreens(final String token) throws FlexNotLoggedInException, FlexNoPermissionException
    {
        checkUser(token);
        final List ret = new ArrayList();

        final List screenList = new ArrayList(fieldScreenManager.getFieldScreens());
        for (final Iterator iterator = screenList.iterator(); iterator.hasNext(); )
        {
            final FieldScreen screen = (FieldScreen) iterator.next();
            final FlexJiraFieldScreen fjfs = new FlexJiraFieldScreenImpl();
            fjfs.setId(screen.getId().toString());
            fjfs.setName(screen.getName());

            ret.add(fjfs);
        }

        return ret;
    }

    public FlexJiraWorkflow copyWorkflow(final String newName, final String newDesc, final FlexJiraWorkflow fjw, final String token)
            throws WorkflowDesignerServiceException, FlexNotLoggedInException,
            FlexNoPermissionException
    {
        checkUser(token);
        final JiraWorkflow jw = getJiraWorkflow(fjw);
        JiraWorkflow newJiraWorkflow;
        try
        {
            newJiraWorkflow = workflowService.copyWorkflow(getJiraServiceContext(token), newName, newDesc, jw);
        }
        catch (final Exception e)
        {
            throw new WorkflowDesignerServiceException(e);
        }

        return WorkflowConverter.convertFullWorkflow(newJiraWorkflow, workflowSchemeManager, workflowAnnotationManager);
    }

    public FlexJiraWorkflow createDraftWorkflow(final FlexJiraWorkflow fjw, final String token)
            throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException
    {
        checkUser(token);
        JiraWorkflow newJiraWorkflow;
        try
        {
            newJiraWorkflow = workflowService.createDraftWorkflow(getJiraServiceContext(token), fjw.getName());
        }
        catch (final Exception e)
        {
            throw new WorkflowDesignerServiceException(e);
        }

        //let's try to copy the layout if needed.
        try
        {
            workflowLayoutManager.copyLayoutForDraftWorkflow(fjw.getName());
        }
        catch (final Exception e)
        {
            throw new WorkflowDesignerServiceException("Error Loading Saved Layout", e);
        }

        return WorkflowConverter.convertFullWorkflow(newJiraWorkflow, workflowSchemeManager, workflowAnnotationManager);
    }

    public void deleteWorkflow(final FlexJiraWorkflow fjw, final String token)
            throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException
    {
        checkUser(token);
        final JiraWorkflow jw = getJiraWorkflow(fjw);
        if (jw.isDraftWorkflow())
        {
            workflowManager.deleteDraftWorkflow(jw.getName());
            return;
        }

        if (jw.isEditable())
        {
            // Ensure that the workflow is not associated with any schemes
            final Collection workflowSchemes = workflowSchemeManager.getSchemesForWorkflow(jw);
            if (workflowSchemes == null || workflowSchemes.isEmpty())
            {
                workflowManager.deleteWorkflow(jw);
                return;
            }
            else
            {
                final StringBuffer schemes = new StringBuffer();
                for (final Iterator iterator = workflowSchemes.iterator(); iterator.hasNext(); )
                {
                    final GenericValue schemeGV = (GenericValue) iterator.next();
                    schemes.append('\'').append(schemeGV.getString("name")).append('\'').append(", ");
                }
                schemes.delete(schemes.length() - 2, schemes.length() - 1);
                throw new WorkflowDesignerServiceException(jiraAuthenticationContext.getI18nBean().getText("admin.errors.cannot.delete.this.workflow") + " "
                        + schemes);
            }
        }

    }

    public FlexJiraWorkflow publishDraftWorkflow(final FlexJiraWorkflow fjw, final boolean enableBackup, final String backupName, final String token)
            throws WorkflowDesignerServiceException,
            FlexNotLoggedInException, FlexNoPermissionException
    {
        checkUser(token);

        try
        {
            if (enableBackup)
            {
                final JiraWorkflow activeWorkflow = workflowService.getWorkflow(getJiraServiceContext(token), fjw.getName());
                workflowService.copyWorkflow(getJiraServiceContext(token), backupName, null, activeWorkflow);
            }

            final JiraServiceContext context = getJiraServiceContext(token);
            workflowService.overwriteActiveWorkflow(context, fjw.getName());

            if (context.getErrorCollection().hasAnyErrors())
            {
                final Collection messages = context.getErrorCollection().getErrorMessages();
                final Iterator it = messages.iterator();
                final String msg = (String) it.next();

                throw new WorkflowDesignerServiceException(msg);
            }
        }
        catch (final Exception e)
        {
            throw new WorkflowDesignerServiceException(e);
        }

        final JiraWorkflow parentWorkflow = workflowManager.getWorkflow(fjw.getName());

        return WorkflowConverter.convertFullWorkflow(parentWorkflow, workflowSchemeManager, workflowAnnotationManager);
    }

    public FlexJiraWorkflow createNewWorkflow(final String name, final String desc, final String token)
            throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException
    {
        checkUser(token);
        final ConfigurableJiraWorkflow newWorkflow = new ConfigurableJiraWorkflow(name, workflowManager);
        newWorkflow.setDescription(desc);
        try
        {
            workflowManager.createWorkflow(getJiraServiceContext(token).getLoggedInUser(), newWorkflow);
        }
        catch (final Exception e)
        {
            throw new WorkflowDesignerServiceException(e);
        }

        final JiraWorkflow jw = workflowManager.getWorkflow(name);

        return WorkflowConverter.convertFullWorkflow(jw, workflowSchemeManager, workflowAnnotationManager);
    }

    public FlexJiraWorkflow addStep(final FlexJiraStep fjs, final FlexJiraWorkflow fjw, JWDLayout layout, final String token)
            throws WorkflowDesignerServiceException, FlexNotLoggedInException,
            FlexNoPermissionException
    {
        checkUser(token);

        final JiraWorkflow jw = getJiraWorkflow(fjw);
        final StepDescriptor newStep = DescriptorFactory.getFactory().createStepDescriptor();

        newStep.setName(fjs.getName());
        newStep.setId(WorkflowUtil.getNextId(jw.getDescriptor().getSteps()));
        newStep.getMetaAttributes().put(JiraWorkflow.STEP_STATUS_KEY, fjs.getLinkedStatus());

        newStep.setParent(jw.getDescriptor());

        final JiraWorkflow mutable = getMutableWorkflow(jw);
        // final JiraWorkflow mutable = workflowManager.getWorkflowClone(jw.getName());
        mutable.getDescriptor().addStep(newStep);

        JiraServiceContext context;
        try
        {
            context = getJiraServiceContext(token);
        }
        catch (final Exception e)
        {
            throw new WorkflowDesignerServiceException(e);
        }
        workflowService.updateWorkflow(context, mutable);

        if (context.getErrorCollection().hasAnyErrors())
        {
            final Collection messages = context.getErrorCollection().getErrorMessages();
            final Iterator it = messages.iterator();
            final String msg = (String) it.next();

            throw new WorkflowDesignerServiceException(msg);
        }

        try
        {
            saveLayout(mutable, layout);
        }
        catch (Exception e)
        {
            throw new WorkflowDesignerServiceException("Error Saving Layout", e);
        }

        return WorkflowConverter.convertFullWorkflow(mutable, workflowSchemeManager, workflowAnnotationManager);
    }

    public Map addTransition(final String name, final String desc, final String view, final FlexJiraStep fjFromStep, final FlexJiraStep fjToStep, final FlexJiraWorkflow fjw, final String token)
            throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException
    {
        checkUser(token);

        final JiraWorkflow workflow = getJiraWorkflow(fjw);
        final JiraWorkflow mutable = getMutableWorkflow(workflow);
        // final JiraWorkflow mutable = workflowManager.getWorkflowClone(workflow.getName());
        final StepDescriptor fromStep = mutable.getDescriptor().getStep(fjFromStep.getId());
        final StepDescriptor toStep = mutable.getDescriptor().getStep(fjToStep.getId());

        // setup the transition action
        final ActionDescriptor action = DescriptorFactory.getFactory().createActionDescriptor();
        action.setId(mutable.getNextActionId());
        action.setName(name);
        action.getMetaAttributes().put(JiraWorkflow.WORKFLOW_DESCRIPTION_ATTRIBUTE, desc);

        if (!StringUtils.isBlank(view))
        {
            final FieldScreen fieldScreen = fieldScreenManager.getFieldScreen(new Long(view));
            action.setView(WorkflowTransitionUtil.VIEW_SCREEN);
            action.getMetaAttributes().put("jira.fieldscreen.id", fieldScreen.getId().toString());
        }
        else
        {
            action.setView(null);
        }

        // setup the result
        final ResultDescriptor result = DescriptorFactory.getFactory().createResultDescriptor();
        action.setUnconditionalResult(result);
        result.setStep(toStep.getId());
        result.setOldStatus("Not Done");
        result.setStatus("Done");

        try
        {
            createDefaultPostFunctions(action);
            createDefaultRestriction(action);
        }
        catch (final PluginParseException ppe)
        {
            throw new WorkflowDesignerServiceException(ppe);
        }

        action.setParent(fromStep);
        fromStep.getActions().add(action);

        JiraServiceContext context;
        try
        {
            context = getJiraServiceContext(token);
        }
        catch (final Exception e)
        {
            throw new WorkflowDesignerServiceException(e);
        }

        workflowService.updateWorkflow(context, mutable);

        if (context.getErrorCollection().hasAnyErrors())
        {
            final Collection messages = context.getErrorCollection().getErrorMessages();
            final Iterator it = messages.iterator();
            final String msg = (String) it.next();

            throw new WorkflowDesignerServiceException(msg);
        }

        final FlexJiraAction newAction = WorkflowConverter.convertAction(action);
        final FlexJiraWorkflow newWorkflow = WorkflowConverter.convertFullWorkflow(mutable, workflowSchemeManager, workflowAnnotationManager);
        final HashMap<String, FlexWorkflowObject> returnMap = new HashMap<String, FlexWorkflowObject>();
        returnMap.put("action", newAction);
        returnMap.put("workflow", newWorkflow);


        return returnMap;

    }

    public Map addGlobalTransition(String name, String desc, int resultId, String view, FlexJiraWorkflow fjw, String token)
            throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException
    {
        checkUser(token);

        final JiraWorkflow workflow = getJiraWorkflow(fjw);
        final JiraWorkflow mutable = getMutableWorkflow(workflow);

        final ActionDescriptor action = DescriptorFactory.getFactory().createActionDescriptor();
        action.setId(mutable.getNextActionId());
        action.setName(name);
        action.getMetaAttributes().put(JiraWorkflow.WORKFLOW_DESCRIPTION_ATTRIBUTE, desc);

        if (!StringUtils.isBlank(view))
        {
            final FieldScreen fieldScreen = fieldScreenManager.getFieldScreen(new Long(view));
            action.setView(WorkflowTransitionUtil.VIEW_SCREEN);
            action.getMetaAttributes().put("jira.fieldscreen.id", fieldScreen.getId().toString());
        }
        else
        {
            action.setView(null);
        }

        // setup the result
        final ResultDescriptor result = DescriptorFactory.getFactory().createResultDescriptor();
        action.setUnconditionalResult(result);
        result.setStep(resultId);
        result.setOldStatus("Not Done");
        result.setStatus("Done");

        try
        {
            createDefaultPostFunctions(action);
            createDefaultRestriction(action);
        }
        catch (final PluginParseException ppe)
        {
            throw new WorkflowDesignerServiceException(ppe);
        }

        action.setParent(mutable.getDescriptor());
        mutable.getDescriptor().addGlobalAction(action);

        JiraServiceContext context;
        try
        {
            context = getJiraServiceContext(token);
        }
        catch (final Exception e)
        {
            throw new WorkflowDesignerServiceException(e);
        }

        workflowService.updateWorkflow(context, mutable);

        if (context.getErrorCollection().hasAnyErrors())
        {
            final Collection messages = context.getErrorCollection().getErrorMessages();
            final Iterator it = messages.iterator();
            final String msg = (String) it.next();

            throw new WorkflowDesignerServiceException(msg);
        }

        final FlexJiraAction newAction = WorkflowConverter.convertAction(action);
        final FlexJiraWorkflow newWorkflow = WorkflowConverter.convertFullWorkflow(mutable, workflowSchemeManager, workflowAnnotationManager);
        final HashMap<String, FlexWorkflowObject> returnMap = new HashMap<String, FlexWorkflowObject>();
        returnMap.put("action", newAction);
        returnMap.put("workflow", newWorkflow);

        return returnMap;
    }

    public Map cloneTransition(final String name, final String desc, final int actionIdToClone, final FlexJiraStep fjFromStep, final FlexJiraStep fjToStep, final FlexJiraWorkflow fjw, final String token)
            throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException
    {
        checkUser(token);

        final JiraWorkflow workflow = getJiraWorkflow(fjw);
        final JiraWorkflow mutable = getMutableWorkflow(workflow);
        final StepDescriptor fromStep = mutable.getDescriptor().getStep(fjFromStep.getId());
        final StepDescriptor toStep = mutable.getDescriptor().getStep(fjToStep.getId());

        //get the action to copy
        final ActionDescriptor actionToCopy = mutable.getDescriptor().getAction(actionIdToClone);

        // setup the transition action
        final ActionDescriptor newAction = DescriptorFactory.getFactory().createActionDescriptor();
        newAction.setId(mutable.getNextActionId());
        newAction.setName(name);
        newAction.getMetaAttributes().put(JiraWorkflow.WORKFLOW_DESCRIPTION_ATTRIBUTE, desc);

        String viewIdToCopy = (String) actionToCopy.getMetaAttributes().get("jira.fieldscreen.id");
        if (!StringUtils.isBlank(viewIdToCopy))
        {
            final FieldScreen fieldScreen = fieldScreenManager.getFieldScreen(new Long(viewIdToCopy));
            newAction.setView(WorkflowTransitionUtil.VIEW_SCREEN);
            newAction.getMetaAttributes().put("jira.fieldscreen.id", fieldScreen.getId().toString());
        }
        else
        {
            newAction.setView(null);
        }

        // setup the result
        final ResultDescriptor result = DescriptorFactory.getFactory().createResultDescriptor();
        newAction.setUnconditionalResult(result);
        result.setStep(toStep.getId());
        result.setOldStatus("Not Done");
        result.setStatus("Done");

        try
        {
            createDefaultRestriction(newAction);
            copyActionDetails(actionToCopy, newAction);
        }
        catch (final PluginParseException ppe)
        {
            throw new WorkflowDesignerServiceException(ppe);
        }

        newAction.setParent(fromStep);
        fromStep.getActions().add(newAction);

        JiraServiceContext context;
        try
        {
            context = getJiraServiceContext(token);
        }
        catch (final Exception e)
        {
            throw new WorkflowDesignerServiceException(e);
        }

        workflowService.updateWorkflow(context, mutable);

        if (context.getErrorCollection().hasAnyErrors())
        {
            final Collection messages = context.getErrorCollection().getErrorMessages();
            final Iterator it = messages.iterator();
            final String msg = (String) it.next();

            throw new WorkflowDesignerServiceException(msg);
        }

        final FlexJiraAction flexAction = WorkflowConverter.convertAction(newAction);
        final FlexJiraWorkflow newWorkflow = WorkflowConverter.convertFullWorkflow(mutable, workflowSchemeManager, workflowAnnotationManager);
        final HashMap<String, FlexWorkflowObject> returnMap = new HashMap<String, FlexWorkflowObject>();
        returnMap.put("action", flexAction);
        returnMap.put("workflow", newWorkflow);

        return returnMap;

    }

    public Map cloneGlobalTransition(final String name, final String desc, final int actionIdToClone, final FlexJiraWorkflow fjw, final String token)
            throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException
    {
        checkUser(token);

        final JiraWorkflow workflow = getJiraWorkflow(fjw);
        final JiraWorkflow mutable = getMutableWorkflow(workflow);

        //get the action to copy
        final ActionDescriptor actionToCopy = mutable.getDescriptor().getAction(actionIdToClone);

        // setup the transition action
        final ActionDescriptor newAction = DescriptorFactory.getFactory().createActionDescriptor();
        newAction.setId(mutable.getNextActionId());
        newAction.setName(name);
        newAction.getMetaAttributes().put(JiraWorkflow.WORKFLOW_DESCRIPTION_ATTRIBUTE, desc);

        String viewIdToCopy = (String) actionToCopy.getMetaAttributes().get("jira.fieldscreen.id");
        if (!StringUtils.isBlank(viewIdToCopy))
        {
            final FieldScreen fieldScreen = fieldScreenManager.getFieldScreen(new Long(viewIdToCopy));
            newAction.setView(WorkflowTransitionUtil.VIEW_SCREEN);
            newAction.getMetaAttributes().put("jira.fieldscreen.id", fieldScreen.getId().toString());
        }
        else
        {
            newAction.setView(null);
        }

        // setup the result
        final ResultDescriptor result = DescriptorFactory.getFactory().createResultDescriptor();
        newAction.setUnconditionalResult(result);
        result.setStep(actionToCopy.getUnconditionalResult().getStep());
        result.setOldStatus("Not Done");
        result.setStatus("Done");

        try
        {
            createDefaultRestriction(newAction);
            copyActionDetails(actionToCopy, newAction);
        }
        catch (final PluginParseException ppe)
        {
            throw new WorkflowDesignerServiceException(ppe);
        }

        newAction.setParent(mutable.getDescriptor());
        mutable.getDescriptor().addGlobalAction(newAction);

        JiraServiceContext context;
        try
        {
            context = getJiraServiceContext(token);
        }
        catch (final Exception e)
        {
            throw new WorkflowDesignerServiceException(e);
        }

        workflowService.updateWorkflow(context, mutable);

        if (context.getErrorCollection().hasAnyErrors())
        {
            final Collection messages = context.getErrorCollection().getErrorMessages();
            final Iterator it = messages.iterator();
            final String msg = (String) it.next();

            throw new WorkflowDesignerServiceException(msg);
        }

        final FlexJiraAction flexAction = WorkflowConverter.convertAction(newAction);
        final FlexJiraWorkflow newWorkflow = WorkflowConverter.convertFullWorkflow(mutable, workflowSchemeManager, workflowAnnotationManager);
        final HashMap<String, FlexWorkflowObject> returnMap = new HashMap<String, FlexWorkflowObject>();
        returnMap.put("action", flexAction);
        returnMap.put("workflow", newWorkflow);

        return returnMap;

    }

    public Map useCommonTransition(int commonActionId, FlexJiraStep fjFromStep, FlexJiraWorkflow fjw, String token)
            throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException
    {
        checkUser(token);

        final JiraWorkflow workflow = getJiraWorkflow(fjw);
        final JiraWorkflow mutable = getMutableWorkflow(workflow);
        final StepDescriptor fromStep = mutable.getDescriptor().getStep(fjFromStep.getId());

        //get the action to copy
        final ActionDescriptor commonAction = mutable.getDescriptor().getAction(commonActionId);

        StepDescriptor originalParent;
        if (!commonAction.isCommon())
        {

            originalParent = (StepDescriptor) commonAction.getParent();
            //need to remove it from the parent and add it back as a common transition
            originalParent.getActions().remove(commonAction);

            //add it to the workflow
            mutable.getDescriptor().addCommonAction(commonAction);
            commonAction.setParent(mutable.getDescriptor());

            //add it back to the parent
            originalParent.getActions().add(commonAction);
            originalParent.getCommonActions().add(commonAction.getId());
        }

        //finally add it to the new step
        fromStep.getActions().add(commonAction);
        fromStep.getCommonActions().add(commonAction.getId());

        JiraServiceContext context;
        try
        {
            context = getJiraServiceContext(token);
        }
        catch (final Exception e)
        {
            throw new WorkflowDesignerServiceException(e);
        }

        workflowService.updateWorkflow(context, mutable);

        if (context.getErrorCollection().hasAnyErrors())
        {
            final Collection messages = context.getErrorCollection().getErrorMessages();
            final Iterator it = messages.iterator();
            final String msg = (String) it.next();

            throw new WorkflowDesignerServiceException(msg);
        }

        final FlexJiraAction flexAction = WorkflowConverter.convertAction(commonAction);
        final FlexJiraWorkflow newWorkflow = WorkflowConverter.convertFullWorkflow(mutable, workflowSchemeManager, workflowAnnotationManager);
        final HashMap<String, FlexWorkflowObject> returnMap = new HashMap<String, FlexWorkflowObject>();
        returnMap.put("action", flexAction);
        returnMap.put("workflow", newWorkflow);

        return returnMap;
    }

    public FlexJiraWorkflow deleteGlobalAction(int actionId, FlexJiraWorkflow fjw, String token)
            throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException
    {
        checkUser(token);
        final JiraWorkflow workflow = getJiraWorkflow(fjw);
        final JiraWorkflow mutable = getMutableWorkflow(workflow);

        ActionDescriptor action = mutable.getDescriptor().getAction(actionId);
        if (action != null && mutable.getDescriptor().getGlobalActions().contains(action))
        {
            mutable.getDescriptor().removeAction(action);
        }

        JiraServiceContext context;
        try
        {
            context = getJiraServiceContext(token);
        }
        catch (final Exception e)
        {
            throw new WorkflowDesignerServiceException(e);
        }

        workflowService.updateWorkflow(context, mutable);

        if (context.getErrorCollection().hasAnyErrors())
        {
            final Collection messages = context.getErrorCollection().getErrorMessages();
            final Iterator it = messages.iterator();
            final String msg = (String) it.next();

            throw new WorkflowDesignerServiceException(msg);
        }

        return WorkflowConverter.convertFullWorkflow(mutable, workflowSchemeManager, workflowAnnotationManager);
    }

    private void copyActionDetails(ActionDescriptor actionToCopy, ActionDescriptor newAction)
    {
        List<FunctionDescriptor> preFunctions = actionToCopy.getUnconditionalResult().getPreFunctions();
        List<FunctionDescriptor> postFunctions = actionToCopy.getUnconditionalResult().getPostFunctions();
        List<ValidatorDescriptor> validators = actionToCopy.getUnconditionalResult().getValidators();

        List<ConditionDescriptor> conditions = Collections.<ConditionDescriptor>emptyList();
        String type = "AND";
        RestrictionDescriptor restriction = actionToCopy.getRestriction();
        if (restriction != null)
        {
            ConditionsDescriptor conditionsDescriptor = restriction.getConditionsDescriptor();
            if (conditionsDescriptor != null)
            {
                conditions = conditionsDescriptor.getConditions();
                type = conditionsDescriptor.getType();
            }
        }

        Map metaToCopy = actionToCopy.getMetaAttributes();

        for (FunctionDescriptor preFunction : preFunctions)
        {
            newAction.getUnconditionalResult().getPreFunctions().add(preFunction);
        }

        for (FunctionDescriptor postFunction : postFunctions)
        {
            newAction.getUnconditionalResult().getPostFunctions().add(postFunction);
        }

        for (ValidatorDescriptor validator : validators)
        {
            newAction.getUnconditionalResult().getValidators().add(validator);
        }

        newAction.getRestriction().getConditionsDescriptor().setType(type);
        if (conditions.size() > 0)
        {
            for (ConditionDescriptor condition : conditions)
            {
                newAction.getRestriction().getConditionsDescriptor().getConditions().add(condition);
            }
        }


        Iterator it = metaToCopy.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry entry = (Map.Entry) it.next();
            newAction.getMetaAttributes().put(entry.getKey(), entry.getValue());
        }
    }

    public FlexJiraWorkflow updateStep(final FlexJiraStep fjs, final String newName, final String newStatus, final FlexJiraWorkflow fjw, final String token)
            throws WorkflowDesignerServiceException,
            FlexNotLoggedInException, FlexNoPermissionException
    {
        checkUser(token);
        final JiraWorkflow workflow = getJiraWorkflow(fjw);
        final JiraWorkflow mutable = getMutableWorkflow(workflow);
        // final JiraWorkflow mutable = workflowManager.getWorkflowClone(workflow.getName());

        final StepDescriptor jiraStep = mutable.getDescriptor().getStep(fjs.getId());
        jiraStep.setName(newName);
        jiraStep.getMetaAttributes().put(JiraWorkflow.STEP_STATUS_KEY, newStatus);

        JiraServiceContext context;
        try
        {
            context = getJiraServiceContext(token);
        }
        catch (final Exception e)
        {
            throw new WorkflowDesignerServiceException(e);
        }

        workflowService.updateWorkflow(context, mutable);

        if (context.getErrorCollection().hasAnyErrors())
        {
            final Collection messages = context.getErrorCollection().getErrorMessages();
            final Iterator it = messages.iterator();
            final String msg = (String) it.next();

            throw new WorkflowDesignerServiceException(msg);
        }

        return WorkflowConverter.convertFullWorkflow(mutable, workflowSchemeManager, workflowAnnotationManager);
    }

    public FlexJiraWorkflow updateAction(final FlexJiraAction fja, final String newName, final String newDesc, final FlexJiraStep newDestStep, final String newView, final FlexJiraWorkflow fjw,
            final String token)
            throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException
    {
        checkUser(token);
        final JiraWorkflow workflow = getJiraWorkflow(fjw);
        final JiraWorkflow mutable = getMutableWorkflow(workflow);
        // final JiraWorkflow mutable = workflowManager.getWorkflowClone(workflow.getName());
        final ActionDescriptor jiraAction = mutable.getDescriptor().getAction(fja.getId());
        final StepDescriptor jiraDestStep = mutable.getDescriptor().getStep(newDestStep.getId());

        jiraAction.setName(newName);
        jiraAction.getMetaAttributes().put(JiraWorkflow.WORKFLOW_DESCRIPTION_ATTRIBUTE, newDesc);

        if (!StringUtils.isBlank(newView))
        {
            final FieldScreen fieldScreen = fieldScreenManager.getFieldScreen(new Long(newView));
            jiraAction.setView(WorkflowTransitionUtil.VIEW_SCREEN);
            jiraAction.getMetaAttributes().put("jira.fieldscreen.id", fieldScreen.getId().toString());
        }
        else
        {
            jiraAction.setView(null);
        }

        final ResultDescriptor result = jiraAction.getUnconditionalResult();
        result.setStep(jiraDestStep.getId());

        JiraServiceContext context;
        try
        {
            context = getJiraServiceContext(token);
        }
        catch (final Exception e)
        {
            throw new WorkflowDesignerServiceException(e);
        }

        workflowService.updateWorkflow(context, mutable);

        if (context.getErrorCollection().hasAnyErrors())
        {
            final Collection messages = context.getErrorCollection().getErrorMessages();
            final Iterator it = messages.iterator();
            final String msg = (String) it.next();

            throw new WorkflowDesignerServiceException(msg);
        }

        return WorkflowConverter.convertFullWorkflow(mutable, workflowSchemeManager, workflowAnnotationManager);

    }

    public FlexJiraWorkflow updateGlobalAction(final FlexJiraAction fja, final String newName, final String newDesc, final int newDestStepId, final String newView, final FlexJiraWorkflow fjw,
            final String token)
            throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException
    {
        checkUser(token);
        final JiraWorkflow workflow = getJiraWorkflow(fjw);
        final JiraWorkflow mutable = getMutableWorkflow(workflow);

        final ActionDescriptor jiraAction = mutable.getDescriptor().getAction(fja.getId());

        jiraAction.setName(newName);
        jiraAction.getMetaAttributes().put(JiraWorkflow.WORKFLOW_DESCRIPTION_ATTRIBUTE, newDesc);

        if (!StringUtils.isBlank(newView))
        {
            final FieldScreen fieldScreen = fieldScreenManager.getFieldScreen(new Long(newView));
            jiraAction.setView(WorkflowTransitionUtil.VIEW_SCREEN);
            jiraAction.getMetaAttributes().put("jira.fieldscreen.id", fieldScreen.getId().toString());
        }
        else
        {
            jiraAction.setView(null);
        }

        final ResultDescriptor result = jiraAction.getUnconditionalResult();
        result.setStep(newDestStepId);

        JiraServiceContext context;
        try
        {
            context = getJiraServiceContext(token);
        }
        catch (final Exception e)
        {
            throw new WorkflowDesignerServiceException(e);
        }

        workflowService.updateWorkflow(context, mutable);

        if (context.getErrorCollection().hasAnyErrors())
        {
            final Collection messages = context.getErrorCollection().getErrorMessages();
            final Iterator it = messages.iterator();
            final String msg = (String) it.next();

            throw new WorkflowDesignerServiceException(msg);
        }

        return WorkflowConverter.convertFullWorkflow(mutable, workflowSchemeManager, workflowAnnotationManager);

    }

    public FlexJiraWorkflow deleteStepsAndActions(final FlexJiraDeleteRequest deleteRequest, final FlexJiraWorkflow fjw, JWDLayout layout, final String token)
            throws WorkflowDesignerServiceException,
            FlexNotLoggedInException, FlexNoPermissionException
    {
        checkUser(token);
        final JiraWorkflow jiraWorkflow = getJiraWorkflow(fjw);
        final JiraWorkflow mutable = getMutableWorkflow(jiraWorkflow);
        // final JiraWorkflow mutable = workflowManager.getWorkflowClone(jiraWorkflow.getName());
        JiraServiceContext context;
        try
        {
            context = getJiraServiceContext(token);
        }
        catch (final Exception e)
        {
            throw new WorkflowDesignerServiceException(e);
        }

        final ArrayList jiraStepsToDelete = new ArrayList();

        CollectionUtils.collect(deleteRequest.getSteps(), new FlexToJiraStepTransformer(mutable), jiraStepsToDelete);

        deleteActions(deleteRequest.getActionRequests(), mutable);
        deleteSteps(jiraStepsToDelete, mutable);

        workflowService.updateWorkflow(context, mutable);

        if (context.getErrorCollection().hasAnyErrors())
        {
            final Collection messages = context.getErrorCollection().getErrorMessages();
            final Iterator mit = messages.iterator();
            final String msg = (String) mit.next();

            throw new WorkflowDesignerServiceException(msg);
        }

        try
        {
            saveLayout(mutable, layout);
        }
        catch (Exception e)
        {
            throw new WorkflowDesignerServiceException("Error Saving Layout", e);
        }

        return WorkflowConverter.convertFullWorkflow(mutable, workflowSchemeManager, workflowAnnotationManager);
    }

    private void deleteSteps(final List steps, final JiraWorkflow workflow)
    {
        final Iterator it = steps.iterator();
        StepDescriptor step;
        while (it.hasNext())
        {
            step = (StepDescriptor) it.next();
            for (final Iterator it2 = workflow.getActionsWithResult(step).iterator(); it2.hasNext(); )
            {
                final ActionDescriptor action = (ActionDescriptor) it2.next();
                log.debug("step is result of " + action.getName() + "(" + action.getId() + ")");
            }
            workflow.removeStep(step);
        }
    }

    private void deleteActions(final List actionRequests, final JiraWorkflow workflow)
    {
        final Iterator it = actionRequests.iterator();
        FlexJiraDeleteActionRequest actionRequest;
        FlexJiraAction fja;
        FlexJiraStep fjs;
        ActionDescriptor action;
        StepDescriptor step;

        while (it.hasNext())
        {
            actionRequest = (FlexJiraDeleteActionRequest) it.next();
            fja = actionRequest.getAction();
            fjs = actionRequest.getStep();
            action = workflow.getDescriptor().getAction(fja.getId());
            step = workflow.getDescriptor().getStep(fjs.getId());

            // actually remove it
            step.getActions().remove(action);

            if (action.isCommon())
            {
                step.getCommonActions().remove(new Integer(action.getId()));
                // Check if the common action is not referenced from any steps in the workflow
                if (workflow.getStepsForTransition(action).isEmpty())
                {
                    // If so, delete it from the workflow completely
                    workflow.getDescriptor().getCommonActions().remove(action.getId());
                }
            }

            if (step.getActions().isEmpty() && step.getCommonActions().isEmpty())
            {
                // Need to call this method to let workflow know that this step does not have
                // any actions, otherwise validation will fail
                step.removeActions();
            }

        }
    }

    private void createDefaultPostFunctions(final ActionDescriptor actionDescriptor) throws PluginParseException
    {
        final ResultDescriptor unconditionalResult = actionDescriptor.getUnconditionalResult();

        final List postFunctions = unconditionalResult.getPostFunctions();

        final Map functionsToAdd = new TreeMap();

        // Find all the 'default' functions and add them ot the transition
        final List moduleDescriptors = pluginAccessor.getEnabledModuleDescriptorsByType(JiraWorkflowPluginConstants.MODULE_NAME_WORKFLOW_FUNCTION);
        for (final Iterator iterator = moduleDescriptors.iterator(); iterator.hasNext(); )
        {
            final WorkflowFunctionModuleDescriptor functionModuleDescriptor = (WorkflowFunctionModuleDescriptor) iterator.next();
            if (functionModuleDescriptor.isDefault())
            {
                // Build Function Descriptor
                final FunctionDescriptor functionDescriptor = DescriptorFactory.getFactory().createFunctionDescriptor();
                functionDescriptor.setType("class");
                final WorkflowPluginFunctionFactory functionFactory = functionModuleDescriptor.getModule();
                functionDescriptor.getArgs().put("class.name", functionModuleDescriptor.getImplementationClass().getName());
                functionDescriptor.getArgs().putAll(functionFactory.getDescriptorParams(Collections.EMPTY_MAP));

                if (functionModuleDescriptor.getWeight() != null)
                {
                    functionsToAdd.put(functionModuleDescriptor.getWeight(), functionDescriptor);
                }
                else
                {
                    functionsToAdd.put(Integer.MAX_VALUE, functionDescriptor);
                }
            }
        }

        for (final Iterator iterator = functionsToAdd.entrySet().iterator(); iterator.hasNext(); )
        {
            final Map.Entry entry = (Map.Entry) iterator.next();
            postFunctions.add(entry.getValue());
        }

    }

    private void createDefaultRestriction(final ActionDescriptor actionDescriptor)
    {
        //create the default restriction
        RestrictionDescriptor restriction = new RestrictionDescriptor();
        ConditionsDescriptor conditionsDescriptor = DescriptorFactory.getFactory().createConditionsDescriptor();
        restriction.setConditionsDescriptor(conditionsDescriptor);
        actionDescriptor.setRestriction(restriction);
    }

    public FlexJiraWorkflow updateProperties(final FlexJiraMetadataContainer mdc, final Map metadata, final FlexJiraWorkflow fjw, final String token)
            throws WorkflowDesignerServiceException,
            FlexNotLoggedInException, FlexNoPermissionException
    {
        checkUser(token);
        final JiraWorkflow workflow = getJiraWorkflow(fjw);
        final JiraWorkflow mutable = getMutableWorkflow(workflow);
        // final JiraWorkflow mutable = workflowManager.getWorkflowClone(workflow.getName());

        boolean isAction = false;
        if (mdc instanceof FlexJiraAction)
        {
            isAction = true;
        }

        final Map jiraMD;

        if (!isAction)
        {
            final StepDescriptor jiraStep = mutable.getDescriptor().getStep(((FlexWorkflowObject) mdc).getId());
            jiraMD = jiraStep.getMetaAttributes();
            updateMetadata(jiraMD, metadata, mutable);
        }
        else
        {
            final ActionDescriptor jiraAction = mutable.getDescriptor().getAction(((FlexWorkflowObject) mdc).getId());
            jiraMD = jiraAction.getMetaAttributes();
            updateMetadata(jiraMD, metadata, mutable);
        }

        JiraServiceContext context;
        try
        {
            context = getJiraServiceContext(token);
        }
        catch (final Exception e)
        {
            throw new WorkflowDesignerServiceException(e);
        }

        workflowService.updateWorkflow(context, mutable);

        if (context.getErrorCollection().hasAnyErrors())
        {
            final Collection messages = context.getErrorCollection().getErrorMessages();
            final Iterator mit = messages.iterator();
            final String msg = (String) mit.next();

            throw new WorkflowDesignerServiceException(msg);
        }

        return WorkflowConverter.convertFullWorkflow(mutable, workflowSchemeManager, workflowAnnotationManager);

    }

    public void updateIssueEditable(FlexJiraStep fjs, Boolean editable, FlexJiraWorkflow fjw, final String token)
            throws WorkflowDesignerServiceException, FlexNotLoggedInException, FlexNoPermissionException
    {
        checkUser(token);
        final JiraWorkflow workflow = getJiraWorkflow(fjw);
        final JiraWorkflow mutable = getMutableWorkflow(workflow);
        final StepDescriptor jiraStep = mutable.getDescriptor().getStep(fjs.getId());
        final Map jiraMD = jiraStep.getMetaAttributes();

        jiraMD.put("jira.issue.editable", Boolean.toString(editable));

        JiraServiceContext context;
        try
        {
            context = getJiraServiceContext(token);
        }
        catch (final Exception e)
        {
            throw new WorkflowDesignerServiceException(e);
        }

        workflowService.updateWorkflow(context, mutable);

        if (context.getErrorCollection().hasAnyErrors())
        {
            final Collection messages = context.getErrorCollection().getErrorMessages();
            final Iterator mit = messages.iterator();
            final String msg = (String) mit.next();

            throw new WorkflowDesignerServiceException(msg);
        }
    }

    private void updateMetadata(final Map jiraMD, final Map flexMD, final JiraWorkflow workflow)
            throws WorkflowDesignerServiceException
    {
        final Set entries = flexMD.entrySet();
        final Iterator it = entries.iterator();
        Map.Entry entry;
        String flexKey;
        String flexValue;

        // update/add values
        while (it.hasNext())
        {
            entry = (Map.Entry) it.next();
            flexKey = (String) entry.getKey();
            flexValue = (String) entry.getValue();

            if (MetadataUtils.isReservedKey(flexKey))
            {
                throw new WorkflowDesignerServiceException
                        (
                                jiraAuthenticationContext.getI18nBean().getText
                                        (
                                                "admin.errors.workflows.attribute.key.has.reserved.prefix",
                                                "'" + JiraWorkflow.JIRA_META_ATTRIBUTE_KEY_PREFIX + "'"
                                        )
                        );
            }
            final ErrorCollection ec = new SimpleErrorCollection();

            // This is needed due to http://jira.opensymphony.com/browse/WF-476. We should consider removing this
            // if we upgrade osworkflow to a version that fixes WF-476.
            WorkflowUtil.checkInvalidCharacters(flexKey, "attributeKey", ec);
            WorkflowUtil.checkInvalidCharacters(flexValue, "attributeValue", ec);

            if (ec.hasAnyErrors())
            {

                final Map<String, String> errors = ec.getErrors();
                StringBuffer errorBuffer = new StringBuffer("");
                if (!errors.entrySet().isEmpty())
                {

                    for (Map.Entry error : errors.entrySet())
                    {
                        errorBuffer.append(error.getValue());
                        errorBuffer.append(",");
                    }

                }
                throw new WorkflowDesignerServiceException(errorBuffer.toString());
            }

            jiraMD.put(flexKey, flexValue);

        }

        // remove
        final Iterator jit = jiraMD.keySet().iterator();
        String jiraKey;
        while (jit.hasNext())
        {
            jiraKey = (String) jit.next();
            if (!flexMD.containsKey(jiraKey) && !MetadataUtils.isReservedKey(jiraKey))
            {
                jit.remove();
            }
        }
    }

    private JiraWorkflow getJiraWorkflow(final FlexJiraWorkflow fjw)
    {
        JiraWorkflow jiraWorkflow;
        if (fjw.getIsDraftWorkflow())
        {
            jiraWorkflow = workflowManager.getDraftWorkflow(fjw.getName());
        }
        else
        {
            jiraWorkflow = workflowManager.getWorkflow(fjw.getName());
        }

        return jiraWorkflow;
    }

    private JiraWorkflow getMutableWorkflow(final JiraWorkflow jw)
    {
        JiraWorkflow jiraWorkflow;
        if (jw.isDraftWorkflow())
        {
            jiraWorkflow = jw;
        }
        else
        {
            jiraWorkflow = workflowManager.getWorkflowClone(jw.getName());
        }

        return jiraWorkflow;
    }


    protected JiraServiceContext getJiraServiceContext(final String token) throws Exception
    {
        final User user = userTokenManager.getUserFromToken(token);
        return new JiraServiceContextImpl(user);
    }

    protected class IdPredicate implements Predicate
    {
        private final int _id;

        public IdPredicate(final int id)
        {
            this._id = id;
        }

        public boolean evaluate(final Object object)
        {
            return (((FlexWorkflowObject) object).getId() == _id);
        }
    }

    protected class FlexToJiraStepTransformer implements Transformer
    {
        private final JiraWorkflow _jiraWorkflow;

        public FlexToJiraStepTransformer(final JiraWorkflow jw)
        {
            this._jiraWorkflow = jw;
        }

        public Object transform(final Object input)
        {
            final FlexJiraStep fjs = (FlexJiraStep) input;
            return _jiraWorkflow.getDescriptor().getStep(fjs.getId());
        }
    }

    public JWDLayout loadLayout(final FlexJiraWorkflow fjw, final String token)
            throws WorkflowDesignerServiceException, FlexNoPermissionException, FlexNotLoggedInException
    {
        checkUser(token);
        JWDLayout returnLayout;

        try
        {
            final JiraWorkflow jiraWorkflow = getJiraWorkflow(fjw);
            returnLayout = workflowLayoutManager.getLayoutForWorkflow(jiraWorkflow);
        }
        catch (final Exception e)
        {
            throw new WorkflowDesignerServiceException("Error Loading Saved Layout", e);
        }

        return returnLayout;
    }

    public JWDLayout calculateLayout(final JWDLayout jwdLayout)
    {
        FlexJiraWorkflow fjw = new FlexJiraWorkflowImpl();
        fjw.setName(jwdLayout.getWorkflowName());
        fjw.setIsDraftWorkflow(jwdLayout.getIsDraftWorkflow());
        JiraWorkflow workflow = getJiraWorkflow(fjw);

        return workflowLayoutManager.calculateLayout(workflow, jwdLayout);
    }

    public void saveActiveLayout(final String name, final JWDLayout layout, final String token)
            throws WorkflowDesignerServiceException, FlexNoPermissionException, FlexNotLoggedInException
    {
        checkUser(token);
        try
        {
            workflowLayoutManager.saveActiveLayout(name, layout);
            clearImageCache(name);

        }
        catch (final Exception e)
        {
            throw new WorkflowDesignerServiceException("Error Saving Layout", e);
        }
    }

    public void saveDraftLayout(final String name, final JWDLayout layout, final String token)
            throws WorkflowDesignerServiceException, FlexNoPermissionException, FlexNotLoggedInException
    {
        checkUser(token);
        try
        {
            workflowLayoutManager.saveDraftLayout(name, layout);
            clearImageCache(name);
        }
        catch (final Exception e)
        {
            throw new WorkflowDesignerServiceException("Error Saving Layout", e);
        }
    }

    public void addAnnotationToWorkflow(final FlexJiraWorkflow fjw, final WorkflowAnnotation annotation, final JWDLayout layout, final String token)
            throws WorkflowDesignerServiceException, FlexNoPermissionException, FlexNotLoggedInException
    {
        checkUser(token);
        JiraWorkflow jiraWorkflow = getJiraWorkflow(fjw);
        try
        {
            workflowAnnotationManager.addAnnotationToWorkflow(jiraWorkflow, annotation);
            saveLayout(jiraWorkflow, layout);
        }
        catch (Exception e)
        {
            throw new WorkflowDesignerServiceException("Error Saving Annotation", e);
        }
    }

    public void removeAnnotationFromWorkflow(final FlexJiraWorkflow fjw, final WorkflowAnnotation annotation, final JWDLayout layout, final String token)
            throws WorkflowDesignerServiceException, FlexNoPermissionException, FlexNotLoggedInException
    {
        checkUser(token);
        JiraWorkflow jiraWorkflow = getJiraWorkflow(fjw);
        try
        {
            workflowAnnotationManager.removeAnnotationFromWorkflow(jiraWorkflow, annotation);
            saveLayout(jiraWorkflow, layout);
        }
        catch (Exception e)
        {
            throw new WorkflowDesignerServiceException("Error Removing Annotation", e);
        }
    }

    public void updateAnnotationForWorkflow(final FlexJiraWorkflow fjw, final WorkflowAnnotation annotation, final JWDLayout layout, final String token)
            throws WorkflowDesignerServiceException, FlexNoPermissionException, FlexNotLoggedInException
    {
        checkUser(token);
        JiraWorkflow jiraWorkflow = getJiraWorkflow(fjw);
        try
        {
            workflowAnnotationManager.updateAnnotationForWorkflow(jiraWorkflow, annotation);
            saveLayout(jiraWorkflow, layout);
        }
        catch (Exception e)
        {
            throw new WorkflowDesignerServiceException("Error Updating Annotation", e);
        }
    }

    protected void saveLayout(JiraWorkflow workflow, JWDLayout layout) throws Exception
    {
        if (workflow.isDraftWorkflow())
        {
            workflowLayoutManager.saveDraftLayout(workflow.getName(), layout);
        }
        else
        {
            workflowLayoutManager.saveActiveLayout(workflow.getName(), layout);
        }

        clearImageCache(workflow.getName());
    }

    protected void clearImageCache(String workflowName)
    {
        if (workflowImageManager instanceof CachingWorkflowImageManagerImpl)
        {
            ((CachingWorkflowImageManagerImpl) workflowImageManager).clearCacheForWorkflow(workflowName);
        }
    }
}
