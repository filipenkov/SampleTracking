/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.mail;

import com.atlassian.core.user.GroupUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.comparator.UserComparator;
import com.atlassian.jira.mail.Email;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.mail.MailException;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.user.User;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebSudoRequired
public class SendBulkMail extends JiraWebActionSupport
{
    private static final transient Logger log = Logger.getLogger(SendBulkMail.class);
    private boolean sendToRoles = true;
    private String[] groups;
    private String[] projects;
    private String[] roles;
    private String subject;
    private String messageType;
    private String message;
    private String status;
    private String replyTo;
    private boolean sendBlind = false;
    private List users;

    private int RECIPIENT_BATCH_SIZE = 100;
    protected static final int MAX_MULTISELECT_SIZE = 6;

    private final MailServerManager mailServerManager;
    private final PermissionManager permissionManager;
    private final ProjectRoleService projectRoleService;
    private final ProjectManager projectManager;
    private final UserUtil userUtil;

    public SendBulkMail(final MailServerManager mailServerManager, final PermissionManager permissionManager, final ProjectRoleService projectRoleService, final ProjectManager projectManager, final UserUtil userUtil)
    {
        this.mailServerManager = mailServerManager;
        this.permissionManager = permissionManager;
        this.projectRoleService = projectRoleService;
        this.projectManager = projectManager;
        this.userUtil = userUtil;

        try
        {
            RECIPIENT_BATCH_SIZE = Integer.parseInt(getApplicationProperties().getDefaultBackedString(APKeys.JIRA_SENDMAIL_RECIPENT_BATCH_SIZE));
        }
        catch (Exception e)
        {
            log.warn("Exception whilst trying to get property for " + APKeys.JIRA_SENDMAIL_RECIPENT_BATCH_SIZE + ". Defaulting to using " + RECIPIENT_BATCH_SIZE);
        }
    }

    public String doDefault()
    {
        sendBlind = true;
        return INPUT;
    }

    protected void doValidation()
    {
        // Ensure there is a mail server configures
        if (!isHasMailServer())
        {
            // Check if no other error messages exists
            if (!invalidInput())
            {
                // If no error messages exist, i.e. no exception have occurred, then add the error
                addErrorMessage(getText("admin.errors.no.mail.server"));
            }
            return;
        }

        if (isSendToRoles())
        {
            boolean projectNotSelected = getProjects() == null || getProjects().length == 0;
            boolean roleNotSelected = getRoles() == null || getRoles().length == 0;

            if (projectNotSelected && roleNotSelected)
            {
                addError("sendToRoles", getText("admin.errors.select.one.project.and.role"));
            }
            else if (projectNotSelected)
            {
                addError("sendToRoles", getText("admin.errors.select.one.project"));
            }
            else if (roleNotSelected)
            {
                addError("sendToRoles", getText("admin.errors.select.one.role"));
            }

            if (!invalidInput())
            {
                // Get a SET of users that belong to the selected project roles
                // First resolve the selected roles
                User remoteUser = getRemoteUser();
                Set projectRoles = new HashSet();
                for (Iterator iterator = getAsCollection(getRoles()).iterator(); iterator.hasNext();)
                {
                    Long roleId = Long.valueOf((String) iterator.next());
                    projectRoles.add(projectRoleService.getProjectRole(remoteUser, roleId, this));
                }

                // Iterate through the selected projects and get the users with the selected project roles
                Set recipientUsers = new HashSet();
                for (Iterator projectIterator = getAsCollection(getProjects()).iterator(); projectIterator.hasNext();)
                {
                    Long projectId = Long.valueOf((String) projectIterator.next());
                    Project project = projectManager.getProjectObj(projectId);
                    for (Iterator roleIterator = projectRoles.iterator(); roleIterator.hasNext();)
                    {
                        ProjectRole projectRole = (ProjectRole) roleIterator.next();
                        ProjectRoleActors roleActors = projectRoleService.getProjectRoleActors(remoteUser, projectRole, project, this);
                        recipientUsers.addAll(roleActors.getUsers());
                    }
                }

                users = new ArrayList(recipientUsers);

                // Check to ensure we have users to e-mail
                if (users.isEmpty())
                {
                    addError("sendToRoles", getText("admin.errors.empty.projectroles"));
                }
            }
        }
        else
        {
            if (getGroups() == null || getGroups().length == 0)
            {
                addError("sendToRoles", getText("admin.errors.select.one.group"));
            }
            else
            {
                // Get a SET of users that are members of the selected groups
                users = new ArrayList(userUtil.getUsersInGroupNames(getAsCollection(getGroups())));

                // Check to ensure we have users to e-mail
                if (users.isEmpty())
                {
                    addError("sendToRoles", getText("admin.errors.empty.groups"));
                }
            }
        }

        if (!invalidInput())
        {
            Collections.sort(users, new UserComparator());
        }

        if (TextUtils.stringSet(getReplyTo()))
        {
            // If the reply to is specified ensure it is a correct email format
            if (!TextUtils.verifyEmail(getReplyTo()))
            {
                addError("replyTo", getText("admin.errors.invalid.email"));
            }
        }

        if (!TextUtils.stringSet(getSubject()))
        {
            addError("subject", getText("admin.errors.no.subject"));
        }
        if (!TextUtils.stringSet(getMessageType()))
        {
            addError("messageType", getText("admin.errors.no.message.type"));
        }
        if (!TextUtils.stringSet(getMessage()))
        {
            addError("message", getText("admin.errors.no.body"));
        }
        super.doValidation();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        StringBuffer mailSentRecipients = new StringBuffer();
        Iterator recipients = users.iterator();
        while (recipients.hasNext())
        {
            //send batchSize number of recipients at a time only - JRA-9189
            StringBuffer toList = new StringBuffer();
            for (int i = 0; i < RECIPIENT_BATCH_SIZE && recipients.hasNext(); i++)
            {
                User user = (User) recipients.next();
                toList.append(user.getEmail()).append(",");
            }

            // Remove the last ","
            if (toList.length() > 0)
            {
                toList.deleteCharAt(toList.length() - 1);
            }

            try
            {
                String currentUserEmail = getRemoteUser().getEmail();
                SMTPMailServer server = mailServerManager.getDefaultSMTPMailServer();

                String mimeType = "text/plain";
                if (NotificationRecipient.MIMETYPE_HTML.equals(getMessageType()))
                {
                    mimeType = "text/html";
                }

                Email email = new Email((sendBlind ? null : toList.toString()), null, (sendBlind ? toList.toString() : null));
                email.setFrom(currentUserEmail);

                // Only set the reply-to e-mail address if one was given
                if (TextUtils.stringSet(getReplyTo()))
                {
                    email.setReplyTo(getReplyTo());
                }

                email.setSubject(getSubject());
                email.setMimeType(mimeType);
                email.setBody(getMessage());

                // NOTE: The message is sent directly, i.e. is NOT queued
                server.send(email);

                status = getText("admin.errors.message.sent.successfully");
                mailSentRecipients.append(toList.toString());

                /*status = "Your message has been sent successfully to the following users:";
                log.debug("Email sent to : " + toList);*/
            }
            catch (Exception e)
            {
                status = getText("admin.errors.failed.to.send", "<font color=\"bb0000\">", "</font>");
                addErrorMessage(getText("admin.errors.the.error.was") + " " + e.getMessage());

                /*status = "<font color=\"bb0000\">FAILED</font> to send email to the list of users displayed below.";
                addErrorMessage("The error was: " + e.getMessage());*/

                log.error("Failed to send email to : " + toList);
                log.error("Error sending e-mail.", e);
            }
        }
        if (mailSentRecipients.length() > 0)
            log.debug("Email successfully sent to : " + mailSentRecipients);

        return getResult();
    }

    private Collection getAsCollection(String[] array)
    {
        if (array != null)
        {
            return Arrays.asList(array);
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }

    public Collection getAllGroups()
    {
        return GroupUtils.getGroups();
    }

    public Collection getAllProjects()
    {
        return permissionManager.getProjects(Permissions.BROWSE, getRemoteUser());
    }

    public Collection getAllRoles()
    {
        return projectRoleService.getProjectRoles(getRemoteUser(), this);
    }

    public boolean isSendToRoles()
    {
        return sendToRoles;
    }

    public void setSendToRoles(boolean sendToRoles)
    {
        this.sendToRoles = sendToRoles;
    }

    public String[] getGroups()
    {
        return groups;
    }

    public void setGroups(String[] groups)
    {
        this.groups = groups;
    }

    public String[] getProjects()
    {
        return projects;
    }

    public void setProjects(String[] projects)
    {
        this.projects = projects;
    }

    public String[] getRoles()
    {
        return roles;
    }

    public void setRoles(String[] roles)
    {
        this.roles = roles;
    }

    public String getSubject()
    {
        return subject;
    }

    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    public String getMessageType()
    {
        return messageType;
    }

    public void setMessageType(String messageType)
    {
        this.messageType = messageType;
    }

    public Map getMimeTypes()
    {
        return EasyMap.build(NotificationRecipient.MIMETYPE_HTML, NotificationRecipient.MIMETYPE_HTML_DISPLAY, NotificationRecipient.MIMETYPE_TEXT, NotificationRecipient.MIMETYPE_TEXT_DISPLAY);
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getStatus()
    {
        return status;
    }

    public int getGroupsFieldSize()
    {
        return Math.min(getAllGroups().size() + 1, MAX_MULTISELECT_SIZE);
    }

    public int getProjectsRolesFieldSize()
    {
        int largestFieldSize = Math.max(getAllProjects().size() + 1, getAllRoles().size() + 1);
        return Math.min(largestFieldSize, MAX_MULTISELECT_SIZE);
    }

    public String getReplyTo()
    {
        return replyTo;
    }

    public void setReplyTo(String replyTo)
    {
        this.replyTo = replyTo;
    }

    public boolean isSendBlind()
    {
        return sendBlind;
    }

    public void setSendBlind(boolean sendBlind)
    {
        this.sendBlind = sendBlind;
    }

    public boolean isHasMailServer()
    {
        try
        {
            return (mailServerManager.getDefaultSMTPMailServer() != null);
        }
        catch (MailException e)
        {
            addErrorMessage(getText("admin.errors.error.occured.retrieving.server.info"));
            log.error("Error occurred while retrieving mail server information.", e);
            return false;
        }
    }

    public Collection getUsers()
    {
        return users;
    }
}
