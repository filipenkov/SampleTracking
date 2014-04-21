package org.jcvi.jira.plugins.customfield.velocity;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.security.JiraAuthenticationContext;

import java.util.*;
import java.util.List;

import static org.jcvi.jira.plugins.customfield.velocity.VelocityContextProperties.*;
/**
 * Created by IntelliJ IDEA.
 * User: pedworth
 * Date: 8/29/11
 * retrieves data to add to the context and ensures consistent naming.
 */
public class VelocityContextFactory {
    //context variables
    private static final String VALUE                  = "value";
    public static void setContextValue(Map<String, Object> initialContext, Object value) {
        initialContext.put(VALUE, value);
    }

    /**
     * <p>Creates a map that can be returned from getVelocityParameters or
     * used to generate a velocity instance.</p>
     * <p>It contains the combination of getUtilities, getInitialVelocityContext, addCustomFieldContextParameters and:</p>
     * <ul>
     *     <li><B>"fields"</b>
     *         <p>Map&lt;String,Object&gt; of field.name -> value and
     *         of field.id -> value</p></li>
     *     <li><B>"vconfig"</b>
     *          <p>Map&lt;String,String&gt; of configuration field
     *          name -> value</p></li>
     *</ul>
     * @param config      The configuration to use to populate VCONFIG
     * @param issue       The issue to use to ensure the correct parameters are used
     *                    to generate both VCONFIG and FIELDS
     * @param customField Used for DESCRIPTOR and CUSTOMFIELD
     * @param auth        Used getInitialVelocityContext's variables
     * @return A Map containing VCONFIG and FIELDS
     */
    public static Map<String, Object> getContext(Map<String,String> config,
                                                 Issue issue,
                                                 CustomField customField,
                                                 JiraAuthenticationContext auth) {
        Map<String, Object> context = new HashMap<String,Object>();

        //todo: SettingType.All?
        List<VelocityContextProperties> propertiesToAdd = new ArrayList<VelocityContextProperties>();
        propertiesToAdd.addAll(Arrays.asList(VelocityContextProperties.SettingType.CONFIG                .getAssociatedProperties()));
        propertiesToAdd.addAll(Arrays.asList(SettingType.CURRENT_ISSUE         .getAssociatedProperties()));
        propertiesToAdd.addAll(Arrays.asList(SettingType.CUSTOM_FIELD          .getAssociatedProperties()));
        propertiesToAdd.addAll(Arrays.asList(SettingType.AUTHENTICATION_CONTEXT.getAssociatedProperties()));
        propertiesToAdd.addAll(Arrays.asList(SettingType.COMPONENT_MANAGER     .getAssociatedProperties()));
        propertiesToAdd.addAll(Arrays.asList(SettingType.UTILITIES             .getAssociatedProperties()));

        VelocityContextProperties.Settings contextSettings =
                new VelocityContextProperties.Settings(config, issue, customField, auth);

        for(VelocityContextProperties property : propertiesToAdd) {
            String name  = property.getContextName();
            Object value = property.getContextValue(contextSettings);
            context.put(name,value);
        }
        return context;
    }
}
