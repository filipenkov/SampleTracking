/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.UserUtils;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.Validator;
import com.opensymphony.workflow.WorkflowContext;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.ValidatorDescriptor;

import java.util.Map;

/**
 * An OSWorkflow validator that validates a given permission is correct.
 * Sample usage:
 * <pre>
 * &lt;validator type="class">
 * &lt;arg name="class.name">com.atlassian.jira.workflow.validator.PermissionValidator&lt;/arg>
 * &lt;arg name="permission">Create Issue&lt;/arg>
 * &lt;!-- Optionally force constant username: &lt;arg name="username">joe&lt;/arg> -->
 * &lt;/validator>
 * </pre>
 */
public class PermissionValidator extends AbstractPermissionValidator implements Validator
{
    public static ValidatorDescriptor makeDescriptor(String permission)
    {
        ValidatorDescriptor permValidator = DescriptorFactory.getFactory().createValidatorDescriptor();
        permValidator.setType("class");
        permValidator.getArgs().put("class.name", PermissionValidator.class.getName());
        permValidator.getArgs().put("permission", permission);
        return permValidator;
    }

    public void validate(Map transientVars, Map args, PropertySet ps) throws InvalidInputException
    {
        WorkflowContext context = (WorkflowContext) transientVars.get("context");
        String username = (String) args.get("username");

        if (!TextUtils.stringSet(username))
        {
            username = context.getCaller();
        }

        User user = null;

        if (username != null)
        {
            user = UserUtils.getUser(username);
            if (user == null)
            {
                throw new InvalidInputException("You don't have the correct permissions - user (" + username + ") not found");
            }
        }

        hasUserPermission(args, transientVars, user);
    }
}
