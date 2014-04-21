package com.atlassian.jira.web.action.admin.whitelist;

import com.atlassian.gadgets.directory.spi.ExternalGadgetSpec;
import com.atlassian.jira.bc.portal.GadgetApplinkUpgradeUtil;
import com.atlassian.jira.bc.whitelist.WhitelistService;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Web action to allow configuration of the whitelist used for gadgets.
 *
 * @since v4.3
 */
@WebSudoRequired
public class ConfigureWhitelist extends JiraWebActionSupport
{
    private final WhitelistService whitelistService;
    private final GadgetApplinkUpgradeUtil gadgetApplinkUpgradeUtil;

    private boolean disableWhitelist;
    private String rules;
    private boolean showUpgrade;
    private boolean whitelistSaved = false;

    public ConfigureWhitelist(final WhitelistService whitelistService,
            final GadgetApplinkUpgradeUtil gadgetApplinkUpgradeUtil)
    {
        this.whitelistService = whitelistService;
        this.gadgetApplinkUpgradeUtil = gadgetApplinkUpgradeUtil;
    }

    @Override
    public String doDefault() throws Exception
    {
        if(showUpgrade)
        {
            gadgetApplinkUpgradeUtil.disableUpgradeCheck();
        }
        final WhitelistService.WhitelistResult result = whitelistService.getRules(getJiraServiceContext());
        if(result.isValid())
        {
            rules = convertToString(result);
            disableWhitelist = whitelistService.isDisabled();

            return INPUT;
        }
        return INPUT;
    }

    @Override
    protected void doValidation()
    {
        whitelistService.validateUpdateRules(getJiraServiceContext(), convertToList(rules), disableWhitelist);
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        final WhitelistService.WhitelistUpdateValidationResult result = whitelistService.validateUpdateRules(getJiraServiceContext(), convertToList(rules), disableWhitelist);
        if(result.isValid())
        {
            rules = convertToString(whitelistService.updateRules(result));
            disableWhitelist = whitelistService.isDisabled();
            whitelistSaved = true;
        }
        return SUCCESS;
    }

    public boolean isDisableWhitelist()
    {
        return disableWhitelist;
    }

    public boolean isWhitelistSaved()
    {
        return whitelistSaved;
    }

    public void setDisableWhitelist(boolean disableWhitelist)
    {
        this.disableWhitelist = disableWhitelist;
    }

    public String getRules()
    {
        return rules;
    }

    public void setRules(String rules)
    {
        this.rules = rules;
    }

    public boolean isShowUpgrade()
    {
        return showUpgrade;
    }

    public void setShowUpgrade(boolean showUpgrade)
    {
        this.showUpgrade = showUpgrade;
    }

    public Map<URI, List<ExternalGadgetSpec>> getGroupedExternalGadgets()
    {
        return gadgetApplinkUpgradeUtil.getExternalGadgetsRequiringUpgrade();
    }

    private List<String> convertToList(final String rules)
    {
        final List<String> ret = new ArrayList<String>();
        final String[] ruleStrings = StringUtils.split(rules, null);
        if(ruleStrings != null)
        {
            for (String ruleString : ruleStrings)
            {
                if(StringUtils.isNotBlank(ruleString))
                {
                    ret.add(ruleString);
                }
            }
        }
        return ret;
    }

    private String convertToString(WhitelistService.WhitelistResult result)
    {
        final StringBuilder builder = new StringBuilder();
        for (String rule : result.getRules())
        {
            builder.append(rule).append("\n");
        }
        return builder.toString();
    }
}
