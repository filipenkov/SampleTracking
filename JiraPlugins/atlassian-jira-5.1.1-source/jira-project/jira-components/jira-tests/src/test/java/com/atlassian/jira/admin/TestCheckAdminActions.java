package com.atlassian.jira.admin;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.ClassLoaderUtil;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertNotNull;

public class TestCheckAdminActions extends ListeningTestCase
{
    private static final String ROLES_REQUIRED_ATTR = "roles-required";
    private static final String actionsPackage = "com.atlassian.jira.web.action.";


    @Test
    public void checkAllActionsRequiringAdminRoleHaveRequireWebsudoAnnotation() throws ClassNotFoundException
    {
        Document actionsDocument = parseActionsXmlFile("actions");

        // Get list of actions
        final NodeList actions = actionsDocument.getElementsByTagName("action");

        final String rootRolesRequired = overrideRoles(null, actionsDocument.getDocumentElement());

        // Build list of views
        for (int i = 0; i < actions.getLength(); i++)
        {
            final Element action = (Element) actions.item(i);
            final String actionName = action.getAttribute("name");
            final String actionRolesRequired = overrideRoles(rootRolesRequired, action);

            if (actionRolesRequired != null && !actionName.equals("admin.WebSudoAuthenticate"))
            {
                if (actionRolesRequired.contains("admin")||actionRolesRequired.contains("sysadmin"))
                {
                    Class actionClass = Class.forName(actionsPackage+actionName);
                    assertNotNull(actionsPackage+actionName+" has no web sudo annotation",actionClass.getAnnotation(WebSudoRequired.class));
                }
            }

        }

    }

    /**
     * Returns newRolesRequired if it isn't empty, and rolesRequired otherwise.
     */
    private String overrideRoles(final String rolesRequired, final Element action)
    {
        if (action.hasAttribute(ROLES_REQUIRED_ATTR))
        {
            return action.getAttribute(ROLES_REQUIRED_ATTR);
        }
        else
        {
            return rolesRequired;
        }
    }

    private Document parseActionsXmlFile(final String actionsXmlFile)
    {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        URL fileUrl = ClassLoaderUtil.getResource(actionsXmlFile + ".xml", this.getClass());

        if (fileUrl == null)
        {
            fileUrl = ClassLoaderUtil.getResource("/" + actionsXmlFile + ".xml", this.getClass());
        }

        if (fileUrl == null)
        {
            throw new IllegalArgumentException("No such XML file:/" + actionsXmlFile + ".xml");
        }

        try
        {
            return factory.newDocumentBuilder().parse(fileUrl.toString());
        }
        catch (final SAXException e)
        {
            throw new RuntimeException(e);
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
        catch (final ParserConfigurationException e)
        {
            throw new RuntimeException(e);
        }
    }
    
}
