package com.atlassian.jira.web.action.admin.scheme.comparison;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.notification.NotificationType;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.jira.scheme.distiller.DistilledSchemeResult;
import com.atlassian.jira.scheme.distiller.DistilledSchemeResults;
import com.atlassian.jira.scheme.distiller.SchemeDistiller;
import com.atlassian.jira.scheme.distiller.SchemeRelationships;
import com.atlassian.jira.security.type.SecurityType;
import com.atlassian.jira.web.action.admin.scheme.AbstractSchemeToolAction;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * This action services the comparison tool and shows all the information about the compared schemes.
 */
@WebSudoRequired
public class SchemeComparisonToolAction extends AbstractSchemeToolAction
{
    public static final String SCHEME_TOOL_NAME = "SchemeComparisonTool";
    private SchemeDistiller schemeDistiller;
    private DistilledSchemeResults distilledSchemeResults;

    public SchemeComparisonToolAction(SchemeManagerFactory schemeManagerFactory, SchemeFactory schemeFactory, SchemeDistiller schemeDistiller, ApplicationProperties applicationProperties)
    {
        super(schemeManagerFactory, schemeFactory, applicationProperties);
        this.schemeDistiller = schemeDistiller;
    }

    public String doDefault() throws Exception
    {
        getDistilledSchemeResults();
        return INPUT;
    }

    public DistilledSchemeResults getDistilledSchemeResults()
    {
        if (distilledSchemeResults == null)
        {
            Collection schemesToCompare = getSchemeObjs();
            if (schemesToCompare != null)
            {
                distilledSchemeResults = schemeDistiller.distillSchemes(schemesToCompare);
            }
        }
        return distilledSchemeResults;
    }

    public SchemeRelationships getSchemeRelationships()
    {
        return schemeDistiller.getSchemeRelationships(getDistilledSchemeResults());
    }

    public int getSchemeDifferencePercentage()
    {
        return (int)(getSchemeRelationships().getSchemeDifferencePercentage() * 100);
    }

    public String getSchemeComparisonDifference()
    {
        String differenceString = NumberFormat.getPercentInstance().format(getSchemeRelationships().getSchemeDifferencePercentage());
        if (getSchemeRelationships().getSchemeDifferencePercentage() == 0)
        {
            differenceString += " (" + getText("admin.scheme.picker.comparison.identical") + ")";
        }
        return differenceString;
    }

    public List getSchemeEntitiesByDisplayName(Collection schemeEntities)
    {
        List displayNames = new ArrayList();
        for (Iterator iterator = schemeEntities.iterator(); iterator.hasNext();)
        {
            SchemeEntity schemeEntity =  (SchemeEntity) iterator.next();
            displayNames.add(getSchemeTypeForEntity(schemeEntity));
        }
        Collections.sort(displayNames);
        return displayNames;
    }

    public int getTotalDistilledFromSchemes()
    {
        int i = 0;
        for (Iterator iterator = distilledSchemeResults.getDistilledSchemeResults().iterator(); iterator.hasNext();)
        {
            DistilledSchemeResult distilledSchemeResult = (DistilledSchemeResult) iterator.next();
            i += distilledSchemeResult.getOriginalSchemes().size();
        }
        return i;
    }

    public String getSchemeTypeForEntity(SchemeEntity schemeEntity)
    {
        String displayName = null;
        if (SchemeManagerFactory.NOTIFICATION_SCHEME_MANAGER.equals(getDistilledSchemeResults().getSchemeType()))
        {
            NotificationType notificationType = ManagerFactory.getNotificationTypeManager().getNotificationType(schemeEntity.getType());
            displayName = notificationType.getDisplayName() + ((schemeEntity.getParameter() == null) ? "" : " (" + notificationType.getArgumentDisplay(schemeEntity.getParameter())+ ")");
        }
        else if (SchemeManagerFactory.PERMISSION_SCHEME_MANAGER.equals(getDistilledSchemeResults().getSchemeType()))
        {
            SecurityType securityType = ManagerFactory.getPermissionTypeManager().getSecurityType(schemeEntity.getType());
            displayName = securityType.getDisplayName() + ((schemeEntity.getParameter() == null) ? "" : " (" + securityType.getArgumentDisplay(schemeEntity.getParameter())+ ")");
        }
        return displayName;
    }

    public String getSchemeDisplayName()
    {
        String displayName = null;
        if (SchemeManagerFactory.NOTIFICATION_SCHEME_MANAGER.equals(getDistilledSchemeResults().getSchemeType()))
        {
            displayName = getText("admin.schemes.notifications.notifications");
        }
        else if (SchemeManagerFactory.PERMISSION_SCHEME_MANAGER.equals(getDistilledSchemeResults().getSchemeType()))
        {
            displayName = getText("admin.common.words.permissions");
        }
        return displayName;
    }

    public String getComparisonToolDescription()
    {
        String displayName = null;
        if (SchemeManagerFactory.NOTIFICATION_SCHEME_MANAGER.equals(getDistilledSchemeResults().getSchemeType()))
        {
            displayName = getText("admin.scheme.comparsion.desc.1.notifications","<br/>");
        }
        else if (SchemeManagerFactory.PERMISSION_SCHEME_MANAGER.equals(getDistilledSchemeResults().getSchemeType()))
        {
            displayName = getText("admin.scheme.comparsion.desc.1.permissions","<br/>");
        }
        return displayName;
    }

    public String getEditPage()
    {
        String editPage = null;
        if (SchemeManagerFactory.NOTIFICATION_SCHEME_MANAGER.equals(getDistilledSchemeResults().getSchemeType()))
        {
            editPage = "EditNotifications";
        }
        else if (SchemeManagerFactory.PERMISSION_SCHEME_MANAGER.equals(getDistilledSchemeResults().getSchemeType()))
        {
            editPage = "EditPermissions";
        }

        return editPage;
    }

    public String getParameters()
    {
        StringBuilder params = new StringBuilder();
        params.append("?selectedSchemeType=");
        params.append(getSelectedSchemeType());

        return params.toString();
    }

    public String getColumnWidthPercentage()
    {
        Collection schemes = getSchemeRelationships().getSchemes();
        if (schemes != null)
        {
            return 100 / (schemes.size() + 1) + "%";
        }
        return "100%";
    }


    public String getToolName()
    {
        return SCHEME_TOOL_NAME;
    }
}
