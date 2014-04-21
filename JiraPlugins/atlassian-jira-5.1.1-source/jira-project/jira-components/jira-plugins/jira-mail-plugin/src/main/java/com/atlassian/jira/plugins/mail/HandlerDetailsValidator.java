/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.mail;

import com.atlassian.jira.plugins.mail.model.HandlerDetailsModel;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

public class HandlerDetailsValidator
{
    private final UserManager userManager;
    private final JiraAuthenticationContext authenticationContext;
    private final UserUtil userUtil;

    public HandlerDetailsValidator(UserManager userManager,
            JiraAuthenticationContext authenticationContext, UserUtil userUtil) {
        this.userManager = userManager;
        this.authenticationContext = authenticationContext;
        this.userUtil = userUtil;
    }

    public ErrorCollection validateDetails(HandlerDetailsModel details) {
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final I18nHelper i18nHelper = authenticationContext.getI18nHelper();

        if (details.isCreateusers())
        {
            // Check that the default reporter is NOT configured
            // As if it is configured and creating users is set to true,
            // it is ambiguous whether to create a new user or use the default reporter
            final boolean extUserMgmt = !userManager.hasWritableDirectory();
            if (StringUtils.isNotBlank(details.getReporterusername()))
            {
                if (extUserMgmt)
                {
                    errorCollection.addError("createusers", i18nHelper.getText("jmp.editHandlerDetails.error.external.user"));
                }
                else
                {
                    errorCollection.addError("reporterusername", i18nHelper.getText("jmp.editHandlerDetails.create.users.is.enabled"));
                }
            }
            else if (extUserMgmt)
            {
                errorCollection.addError("createusers", i18nHelper.getText("jmp.editHandlerDetails.cant.create.users"));
            }
        }

        if (StringUtils.isNotBlank(details.getReporterusername()) && !userUtil.userExists(details.getReporterusername())) {
            errorCollection.addError("reporterusername", i18nHelper.getText("admin.errors.users.user.does.not.exist"));
        }

        if (StringUtils.isNotBlank(details.getCatchemail()) && !TextUtils.verifyEmail(TextUtils.noNull(details.getCatchemail()).trim())) {
            errorCollection.addError("catchemail", i18nHelper.getText("admin.errors.invalid.email"));
        }

        if ("forward".equals(details.getBulk()) && StringUtils.isBlank(details.getForwardEmail())) {
            errorCollection.addError("bulk", i18nHelper.getText("jmp.editHandlerDetails.forwardEmail.is.not.set"));
        }

        if (StringUtils.isNotBlank(details.getForwardEmail()) && !TextUtils.verifyEmail(TextUtils.noNull(details.getForwardEmail()).trim())) {
            errorCollection.addError("forwardEmail", i18nHelper.getText("admin.errors.invalid.email"));
        }
        return errorCollection;
    }

}
