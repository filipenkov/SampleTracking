package org.jcvi.jira.plugins.customfield.shared.config;

/**
 * This is used with enum to create a list of the configuration sub-screens
 * available from the 'Configure Custom Field' page. It is displayed as part of
 * a table containing:
 * <Table>
 *   <hr>
 *     <td>Sub-screens Title</td>
 *     <td>Current Configuration</td>
 *     <td>link to sub-screen</td>
 *   </hr>
 *   <tr>
 *     <td>{CFConfigItem.getDisplayName()}</td>
 *     <td>{ConfigurationParameter.getSummary()}</td>
 *     <td>&lt;a href={CFConfigItem.getBaseEditUrl()}&gt;Edit {CFConfigItem.getDisplayName()}&lt;/a&gt;</td>
 *   </tr>
 * </Table>
 *
 * ConfigurationParameter is a basic interface used to ensure that the
 * Objects returned from CFConfigAction.getConfigurableParameters() can be used to
 * <ul>
 * <li>uniquely identify the option when storing/retrieving its value</li>
 * <li>display the configuration sub-screens title on the 'Configure Custom Field' screen.</li>
 * <li>display the current value / a summary on the 'Configure Custom Field' screen.</li>
 * </ul>
 */
public interface ConfigurationParameter {
    /**
     * The storageKey is used when storing the value/values for the parameter
     * @return A String that is unique across all ConfigurationParameters
     * associated with a single CFType
     */
    public String getStorageKey();

    /**
     * The value of this is used in displaying the parameters line in the
     * com.atlassian.jira.index.Configuration's section of the
     * 'Configuration Scheme' on the 'Configure Custom Field' screen.
     * @return A human readable value
     */
    public String getDisplayName();

    /**
     * parses the value stored in the configuration into a value
     * to display in the HTML on the configuration summary page
     * todo: what is the context of the HTML?
     * @param value The string from the config store
     * @return The HTML to insert
     */
    public String getSummary(String value);
//    /**
//     * parses the value entered in the configuration form into the
//     * value to store in the configuration file.
//     * This and convertStorageToDisplayValue do not have to be symmetrical
//     * The path is:
//     * [user input] -> convertDisplayToStorageValue -> [config store]
//     * [config store] -> convertStorageToDisplayValue -> [htm
//     * @param value The string from the config store
//     * @return The HTML to insert
//     */
//    public String convertDisplayToStorageValue(String value);
//    //types etc can be added later

    /**
     * Not all elements of the configuration need to be placed in the
     * table
     * @return true iff this parameter should be rendered in the
     * view-config.vm template.
     */
    public boolean isDisplayedInView();
}
