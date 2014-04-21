/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.managers;

import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.plugins.importer.external.ExternalException;
import com.atlassian.jira.plugins.importer.managers.CreateConstantsManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.admin.issuetypes.ViewIssueTypes;
import com.atlassian.jira.web.action.admin.statuses.ViewStatuses;
import com.google.common.collect.Maps;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

public class CreateConstanstManagerImpl implements CreateConstantsManager {

    // New statuses are given ids starting from 10000 - avoids conflict with future system statuses.
    private static final Long NEW_STATUS_START_ID = Long.valueOf(10000);

    private final ConstantsManager constantsManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public CreateConstanstManagerImpl(ConstantsManager constantsManager, JiraAuthenticationContext jiraAuthenticationContext) {
        this.constantsManager = constantsManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    @Nullable
    public IssueConstant getConstant(final String constantIdOrName, final String constantType) {
        final IssueConstant constantById = constantsManager.getConstantObject(constantType, constantIdOrName);
        if (constantById != null) {
            return constantById;
        }

        // now "nice" thing about JIRA - it is case sensitive while getting constants by name, but it's case-insensitive
        // while allowing creation of a new constant (so you cannot have "Duplicate" and "DUPLICATE" at the same time
        @SuppressWarnings("unchecked")
        final Collection<IssueConstant> constants = constantsManager.getConstantObjects(constantType);
        for (IssueConstant constant : constants) {
            if (constant.getName().equalsIgnoreCase(constantIdOrName)) {
                return constant;
            }
        }

        return null;
    }

    public String addConstant(final String constantName, final String constantType) throws ExternalException {
        final Map<String, Object> params = Maps.newHashMap();

        if (ConstantsManager.ISSUE_TYPE_CONSTANT_TYPE.equals(constantType)) {
            params.put("iconurl", ViewIssueTypes.NEW_ISSUE_TYPE_DEFAULT_ICON);
        } else if (ConstantsManager.PRIORITY_CONSTANT_TYPE.equals(constantType)) {
            params.put("iconurl", "/images/icons/priority_major.gif");
            params.put("statusColor", "#009900");
        } else if (ConstantsManager.RESOLUTION_CONSTANT_TYPE.equals(constantType)) {
            // nothing here for resolutions
        } else {
            throw new ExternalException("Unknown contantType:" + constantType);
        }

        return addConstant(constantName, constantType, params);
    }

    protected String addConstant(final String constantName, final String constantType,
            @Nullable final Map<String, Object> extraParams)
            throws ExternalException {

        throwExceptionIfInvalid(constantName, constantType);

        try {
            final Map<String, Object> parameters = Maps.newHashMap(extraParams);
            parameters.put("name", constantName);
            parameters.put("description", constantName);

            // create new constant
            // Ensure newly added statuses have ids that will not conflict with future system status ids
            // New user statuses will be created from id 10000 onwards
            if (TextUtils.stringSet(constantType)) {
                if (constantType.equals(ViewStatuses.STATUS_ENTITY_NAME)
                    && Long.valueOf(EntityUtils.getNextStringId(constantType)) < NEW_STATUS_START_ID) {
                    parameters.put("id", NEW_STATUS_START_ID.toString());
                } else {
                    parameters.put("id", EntityUtils.getNextStringId(constantType));
                }
            } else {
                throw new IllegalArgumentException("Unable to create an entity without a valid name.");
            }

            // populate the rest of the fields to create the new entity
            parameters.put("sequence", Long.valueOf(getMaxSequenceNo(constantType) + 1));

            GenericValue created = EntityUtils.createValue(constantType, parameters);

            clearCaches(constantType);

            return created.getString("id");
        } catch (final Exception e) {
            throw new ExternalException("Unable to create " + constantType + " " + constantName, e);
        }
    }

    protected void throwExceptionIfInvalid(String name, String constantType) throws ExternalException {
        if (!TextUtils.stringSet(name)) {
            //NOTE-these translations mightn't work well in other languages :S
            throw new ExternalException(
                    jiraAuthenticationContext.getI18nHelper().getText(
                            "admin.errors.must.specify.a.name.for.the.to.be.added",
                            constantType));
        } else {
            if(getConstant(name, constantType) != null) {
                throw new ExternalException(
                        jiraAuthenticationContext.getI18nHelper().getText("admin.errors.constant.already.exists", constantType));
            }
        }
    }

    protected void clearCaches(String constantType)
    {
        if (ConstantsManager.ISSUE_TYPE_CONSTANT_TYPE.equals(constantType)) {
            getConstantsManager().refreshIssueTypes();
        } else if (ConstantsManager.RESOLUTION_CONSTANT_TYPE.equals(constantType)) {
            getConstantsManager().refreshResolutions();
        } else if (ConstantsManager.PRIORITY_CONSTANT_TYPE.equals(constantType)) {
            getConstantsManager().refreshPriorities();
        } else if (ConstantsManager.STATUS_CONSTANT_TYPE.equals(constantType)){
            getConstantsManager().refreshStatuses();
        }
    }

    public ConstantsManager getConstantsManager() {
        return constantsManager;
    }

    private long getMaxSequenceNo(String constantType)
    {
        Collection<IssueConstant> constants = constantsManager.getConstantObjects(constantType);
        long maxSequence = 0;
        for (IssueConstant constant : constants) {
			final Long sequence = constant.getSequence();
			if (sequence != null) {
				long thisSequence = sequence.longValue();
				if (thisSequence > maxSequence)
					maxSequence = thisSequence;
			}
        }
        return maxSequence;
    }
}
