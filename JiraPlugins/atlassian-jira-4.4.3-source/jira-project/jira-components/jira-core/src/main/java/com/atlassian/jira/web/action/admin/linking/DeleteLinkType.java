/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.linking;

import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.exception.StoreException;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeDestroyer;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

@WebSudoRequired
public class DeleteLinkType extends JiraWebActionSupport
{
    private final IssueLinkTypeManager issueLinkTypeManager;
    private final IssueLinkTypeDestroyer issueLinkTypeDestroyer;
    private final IssueLinkManager issueLinkManager;

    Long id;
    boolean confirm;
    private IssueLinkType linkType;
    private Collection links;
    private Long swapLinkTypeId;
    String action = "swap";

    public DeleteLinkType(IssueLinkTypeManager issueLinkTypeManager, IssueLinkTypeDestroyer issueLinkTypeDestroyer, IssueLinkManager issueLinkManager)
    {
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.issueLinkTypeDestroyer = issueLinkTypeDestroyer;
        this.issueLinkManager = issueLinkManager;
    }

    protected void doValidation()
    {
        try
        {
            if (getLinkType() == null)
            {
                addErrorMessage(getText("admin.errors.linking.link.type.not.found",id));
            }
        }
        catch (StoreException e)
        {
            log.error("Error occurred: " + e, e);
            addErrorMessage(getText("admin.errors.error.occurred") + " " + e);
        }

        if (action.equalsIgnoreCase("swap"))
        {
            if (swapLinkTypeId.equals(id))
                addError("swapLinkTypeId", getText("admin.errors.linking.move.links.to.link.type.being.deleted"));
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (confirm)
        {
            try
            {
                IssueLinkType swapLinkType = null;
                if (action.equalsIgnoreCase("swap"))
                    swapLinkType = issueLinkTypeManager.getIssueLinkType(swapLinkTypeId);

                issueLinkTypeDestroyer.removeIssueLinkType(getId(), swapLinkType, getRemoteUser());
            }
            catch (RemoveException e)
            {
                log.error("Error occurred while removing link type with id '" + getId() + "'.", e);
                addErrorMessage(getText("admin.errors.linking.error.occured.deleting"));
            }
        }

        if (getHasErrorMessages())
            return ERROR;
        else
            return getRedirect("ViewLinkTypes!default.jspa");
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public IssueLinkType getLinkType() throws StoreException
    {
        if (linkType == null)
        {
            linkType = issueLinkTypeManager.getIssueLinkType(id);
        }

        return linkType;
    }

    public Collection getLinks() throws StoreException, GenericEntityException
    {
        if (links == null)
        {
            links = issueLinkManager.getIssueLinks(getId());

            if (links == null)
            {
                links = Collections.EMPTY_LIST;
            }
        }

        return links;
    }

    public boolean isConfirm()
    {
        return confirm;
    }

    public void setConfirm(boolean confirm)
    {
        this.confirm = confirm;
    }

    public Long getSwapLinkTypeId()
    {
        return swapLinkTypeId;
    }

    public void setSwapLinkTypeId(Long swapLinkTypeId)
    {
        this.swapLinkTypeId = swapLinkTypeId;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public Collection getOtherLinkTypes()
    {
        Collection otherTypes = new ArrayList();

        try
        {
            Collection linkTypes =  issueLinkTypeManager.getIssueLinkTypes();
            for (Iterator iterator = linkTypes.iterator(); iterator.hasNext();)
            {
                IssueLinkType linkType = (IssueLinkType) iterator.next();
                if (!linkType.equals(getLinkType()))
                    otherTypes.add(linkType);
            }
        }
        catch (StoreException e)
        {
            log.error("Error occurred while retrieving other link types.");
            addErrorMessage(getText("admin.errors.linking.error.occured.retrieving"));
        }

        return otherTypes;
    }
}
