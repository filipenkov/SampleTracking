package org.jcvi.jira.plugins.customfield.shared.config;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.action.admin.customfields.AbstractEditConfigurationItemAction;
import org.jcvi.jira.plugins.config.ConfigManagerStore;
import org.jcvi.jira.plugins.utils.typemapper.TypeMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * <h1>Overview</h1>
 * <p>Handle creation and processing of the configuration page</p>
 * <h1>Notes on struts</h1>
 * <p>This class implements a struts action. An action is the
 * Controller in the MVC model. It run to process the HTML request. It updates
 * the Model and selects the view to process. The view part is normally in
 * the form of a template. The controller is also responsible for populating
 * the environment of the view, passing the model to it.</p>
 * <p>Owing to a lack of context information (in JIRA's configuration system)
 * other than the url used for the action a seperate implementation is needed
 * per group of options per Custom Field.</p>
 * <p> For all of the Custom Fields implemented so far only one group of
 * configuration options exists and so only one Action per Custom Field Type is
 * needed.</p>
 * <h1>JCVI Implementation specific aspects</h1>
 * <h3>ConfigurationManager</h3>
 * <p>The 'standard' method of associating configuration with a Custom Field
 * is to access it via fieldConfigSchemeManager.getRelevantConfig this is
 * limited to 256 characters per value though.</p>
 * <p>The JCVI Custom Fields use ConfigurationManagerStore instead. This has
 * no limits on the size of configuration values. It is a custom wrapper around
 * {@link GenericConfigManager}.</p>
 * <h3>ConfigurationParameter</h3>
 * <p>ConfigurationParameters are the objects that describe the key value pairs
 * that can be set in the interface. They are accessed from the CFConfigItem.</p>
 */
public abstract class CFConfigAction
        extends AbstractEditConfigurationItemAction {
    //strangely not a constant already
    public static final String SECURITY_BREACH = "securitybreach";

    public static final String SAVE    = "Save";
    public static final String CANCEL  = "Cancel";
    public static final String RESET   = "Reset";

    public static final String UNSAVED = "unsaved";

    public abstract CFConfigItem getConfigItem();

    /**
     * This is the base for the key used when storing and retrieving the form's contents from
     * the session object. To ensure that this doesn't clash with another customfield it is
     * best to use the implementing classes full name,
     * e.g. org.jcvi.jira.plugins.customfield.shared.config.CFConfigAction
     *
     * @return A String that is unique to this CustomField
     */
    protected abstract String getSessionKey();

    protected CFConfigAction() {
    }

    protected GenericConfigManager getGenericConfigManager() {
        return ComponentManager.getComponentInstanceOfType(
                GenericConfigManager.class);
    }

    protected ConfigManagerStore getConfigManagerStore() {
        return new ConfigManagerStore(getGenericConfigManager(),
                                      getFieldConfig(),
                                      getConfigItem());
    }

    //Note: This must be public as it is used by Velocity
    public ConfigurationParameter[] getConfigurableParameters() {
        return getConfigItem().getConfigurableProperties();
    }

    //todo
    protected void doValidation() {
    }

    /**
     * <p>Acts on the submission of the form. The form could have been
     * submitted for several reasons</p>
     * <table>
     *     <hr><th>Reason</th>    <th>Action                              </th><th>Next Page         </th></hr>
     *     <tr><td>Change Tab</td><td>Copy to Session                     </td><td>Selected Tab      </td></tr>
     *     <tr><td>Reset form</td><td>Delete from Session                 </td><td>Same Page         </td></tr>
     *     <tr><td>Cancel</td>    <td>Delete from Session                 </td><td>Config select page</td></tr>
     *     <tr><td>Save</td>      <td>Delete from Session and Store Config</td><td>Config select page</td></tr>
     * </ul>
     * @return The next view
     */
    protected String doExecute() {
        getLocale();

        try {
            if (!isHasPermission(Permissions.ADMINISTER)) {
                return SECURITY_BREACH;
            }

            if (getFieldConfig() == null) {
                addErrorMessage("No field selected");
                return ERROR;
            }
            //------------------------------------------------------------------
            // Action
            //------------------------------------------------------------------
            //form -> config
            if (wasSubmit(request,SAVE)) {
                // todo: make sure Validation returns any errors before
                // getting here; get the form elements
                Map<ConfigurationParameter, String>configValues
                                            = getConfigValuesFromForm(request);
                saveConfigValues(configValues);
            }

            // delete session
            if (wasSubmit(request,SAVE) ||
                wasSubmit(request,CANCEL) ||
                wasSubmit(request, RESET)) {
                //get rid of the values stored in the session
                // X -> session
                clearSession(request.getSession());
            }  else {
            // form -> Session
                saveConfigValuesToSession(request.getSession(), getConfigValuesFromForm(request));
            }

            //------------------------------------------------------------------
            // Next page
            //------------------------------------------------------------------
            // config select page; redirect to
            if (wasSubmit(request,SAVE) ||
                wasSubmit(request,CANCEL)) {
                //going back to the menu
                // Redirect to the custom field configuration screen
                // todo: the first time the page is requested the referer
                // could be captured. It can then be forwarded each call to ensure
                // that it's still available once we have finished.

                setReturnUrl("/secure/admin/ConfigureCustomField!default.jspa?customFieldId="
                        + getFieldConfig().getCustomField().getIdAsLong().toString());
                //getReturnUrlForCancelLink();
                return getRedirect("not used");
            }


            //Reset, Change Tab

            //output the form
            return INPUT;
        } catch (InputException ie) {
            addErrorMessage(ie.getMessage());
        }
        return ERROR;
    }

    /**
     * <p>This is called via the velocity template, e.g. edit-config.vm.</p>
     * <p>It is the interface between the view and the model.</p>
     * <p>The location the values come from depends on how we arrived at the
     * page.</p>
     * <table>
     *     <hr><th>From     </th><th>load from    </th><th>Session</th></hr>
     *     <tr><td>SamePage </td><td>session      </td><td>ignore </td></tr>
     *     <tr><td>OtherPage</td><td>stored config</td><td>delete </td></tr>
     * </ul>
     * <p>The final case is where the user has clicked on link other than
     * the submit button, such as a menu link, and then has returned to the page
     * either via the back button or the normal links.</p>
     * <p>Unfortunately there isn't a reliable way to tell which page they were
     * on. As an alternative we instead keep track of if they are part way through
     * by only having the values in the session if they submitted the form
     * but weren't saving, canceling or resetting.</p>
     * <p>This leaves the situation where they have started filling in the form.
     * When they leave the form its values get stored in the session. To
     * indicate that the session data is from this event an extra value is added
     * to the map (UNSAVED). If this key is in the map then the data does not
     * match the saved data. This can be used in the template to flag to the
     * user that they have unsaved data</p>
     * @return a map that can be used to populate the form
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public Map<String,String>getValuesForForm() {
        ConfigManagerStore config = getConfigManagerStore();
        Map<ConfigurationParameter,String> configValues
            = config.getAllContextsValues();
        Map<ConfigurationParameter,String> SessionValues
                            = getConfigValuesFromSession(request.getSession());
        if (SessionValues == null ||
            SessionValues.size() == 0 ||
            mapsMatch(SessionValues,configValues)) {
            return remapToStrings(configValues);
        }
        Map<String,String> sessionStrings = remapToStrings(SessionValues);
        sessionStrings.put(UNSAVED,"");
        return sessionStrings;
    }


    /**
     * Converts a map of ConfigurationParameter->String into a map of type
     * String->String. The ConfigurationParameters are converted using
     * their getStorageKey method.
     * @param configParamMap    The Map to convert
     * @return The converted map, If the configParamMap was null an empty map
     * is returned
     */
    private Map<String,String> remapToStrings(
                           Map<ConfigurationParameter,String> configParamMap) {
        Map<String,String> stringMap = new HashMap<String,String>();
        if (configParamMap == null) {
            return stringMap;
        }

        TypeMapper<ConfigurationParameter,String> toStorageKeyMapper
                            = new TypeMapper<ConfigurationParameter,String>() {
            @Override
            public String convert(ConfigurationParameter value) {
                return value.getStorageKey();
            }
        };

        for(ConfigurationParameter key : configParamMap.keySet()) {
            String value = configParamMap.get(key);
            stringMap.put(toStorageKeyMapper.convert(key),value);
        }
        return stringMap;
    }

    //---------------------------------------------------------------------
    //          Read from form
    //---------------------------------------------------------------------
    //Struts actions normally have been like get and set methods that struts
    //uses to auto-magically pass the forms contents into the action.
    //Unfortunately the life cycle of this action isn't very clearly defined
    //by atlassian and so for now I'll avoid bean auto gets and sets in case
    //a method needs the value but is called before the auto populate process
    //has occurred.
    private Map<ConfigurationParameter,String> getConfigValuesFromForm(HttpServletRequest req)
            throws InputException {
        Map<ConfigurationParameter,String> configValues
                            = new HashMap<ConfigurationParameter,String>();
        for(ConfigurationParameter type : getConfigurableParameters()) {
            String[] values = req.getParameterValues(type.getStorageKey());
            if (values != null && values.length > 0) {
                if (values.length == 1) {
                    configValues.put(type, values[0]);
                } else {
                    throw new InputException(
                            "HTTP Parameter: '"+type.getStorageKey()+"' "+
                            "had more than one value");
                }
            }
        }
        return configValues;
    }

    //---------------------------------------------------------------------
    //          Read/Write Config
    //---------------------------------------------------------------------
    private void saveConfigValues(Map<ConfigurationParameter, String> configValues) {
        ConfigManagerStore configStore = getConfigManagerStore();
    //todo: return ERROR messages?
        for(ConfigurationParameter configValue : configValues.keySet()) {
            String velocityCode = configValues.get(configValue);
            if (velocityCode != null &&
                velocityCode.trim().length() > 0) {
                //store the form values
                configStore.storeValue(configValue,velocityCode);
            }
        }
    }

    //---------------------------------------------------------------------
    //          Read/Write Session
    //---------------------------------------------------------------------
    @SuppressWarnings({"unchecked"})
    private Map<ConfigurationParameter,String> getConfigValuesFromSession(
                                                            HttpSession sesh) {
        Object configValues = sesh.getAttribute(getSessionKey());
        if (configValues != null &&
            configValues instanceof Map) {
            //type erasure makes it hard to tell the typing on objects within the
            //Map, it is unlikely though that someone else would have used that key
            //and inserted a different Map
            return (Map<ConfigurationParameter,String>)configValues;
        }
        //ensure we can tell the difference between an empty form and
        //no form
        return null;
    }

    private void saveConfigValuesToSession(HttpSession sesh,
                            Map<ConfigurationParameter, String> configValues) {
        sesh.setAttribute(getSessionKey(),configValues);
    }

    private void clearSession(HttpSession sesh) {
        sesh.removeAttribute(getSessionKey());
    }

    //---------------------------------------------------------------------
    //          util methods
    //---------------------------------------------------------------------
    private static boolean wasSubmit(HttpServletRequest req, String name) {
        String submitStr = req.getParameter(name);
        return submitStr != null && name.equalsIgnoreCase(submitStr);
    }

    private static boolean mapsMatch(Map input1,
                                     Map input2) {
        //fast checks first
        if (input1 == null && input2 == null) {
            return true;
        }
        if (input1 == null || input2 == null) {
            return false;
        }
        if (input1.size() != input2.size()) {
            return false;
        }
        //now the slow one
        for(Object type : input1.keySet()) {
            Object value1 = input1.get(type);
            Object value2 = input2.get(type);
            if (!nullEqual(value1, value2)) {
                return false;
            }
        }
        return true;
    }

    //Simple method to compare objects that handles nulls
    //true if a & b == null or a.equals(b)
    private static boolean nullEqual(Object a, Object b){
        //both null or
        return (a == null && b == null) ||
               //both not null and equal
               (a != null && b != null & a.equals(b));
    }

    private static class InputException extends Exception {
        private InputException(String message) {
            super(message);
        }
    }

    protected CustomFieldTypeModuleDescriptor getDescriptor() {
        return getCustomField().getCustomFieldType().getDescriptor();
    }
}
