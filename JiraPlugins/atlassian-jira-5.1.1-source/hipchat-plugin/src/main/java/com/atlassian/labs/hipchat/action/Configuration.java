package com.atlassian.labs.hipchat.action;

import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.labs.hipchat.HipChatApiClient;
import com.atlassian.labs.hipchat.components.ConfigurationManager;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.base.Strings;

@WebSudoRequired
public class Configuration extends JiraWebActionSupport {

    private final ConfigurationManager configurationManager;
    private final HipChatApiClient hipChatApiClient;

    private String hipChatAuthToken;
    private boolean success;

    public Configuration(ConfigurationManager configurationManager, HipChatApiClient hipChatApiClient) {
        this.configurationManager = configurationManager;
        this.hipChatApiClient = hipChatApiClient;
    }

    public void setHipChatAuthToken(String hipChatAuthToken) {
        this.hipChatAuthToken = hipChatAuthToken;
    }

    public String getHipChatAuthToken() {
        if (hipChatAuthToken != null)
            return hipChatAuthToken;
        return getFakeHipChatAuthToken();
    }

    public String getFakeHipChatAuthToken() {
        return Strings.repeat("#", Strings.nullToEmpty(configurationManager.getHipChatApiToken()).length());
    }

    @Override
    protected void doValidation() {
        try {
            if (!getFakeHipChatAuthToken().equals(getHipChatAuthToken()) && !hipChatApiClient.isAuthTokenValid(getHipChatAuthToken())) {
                addErrorMessage(this.getText("hipchat.admin.invalid.token"));
            }
        } catch (ResponseException e) {
            addErrorMessage(e.getLocalizedMessage());
        }
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception {
        // only change the token if this is a real update
        if (!getFakeHipChatAuthToken().equals(getHipChatAuthToken())) {
            configurationManager.updateHipChatApiToken(getHipChatAuthToken());
            setHipChatAuthToken(null);
            success = true;
        }
        return SUCCESS;
    }

    public boolean isSuccess() {
        return success;
    }
}