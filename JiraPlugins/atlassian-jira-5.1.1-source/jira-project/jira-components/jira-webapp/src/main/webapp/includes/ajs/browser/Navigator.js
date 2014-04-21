/**
 * Represents the browser being used to access the page.
 */
AJS.Navigator = jQuery.extend({}, AJS.Navigator, {

    /**
     * The family to which this browser belongs to.
     *
     * @return {String} A textual description of the family, possible return values are described by
     * AJS.Navigator.Families
     */
    family : function(){
       if (jQuery.browser["msie"]) return this.Families.INTERNET_EXPLORER;
       if (jQuery.browser["mozilla"]) return this.Families.MOZILLA;
       if (jQuery.browser["webkit"]) return this.Families.WEBKIT;
       if (jQuery.browser["opera"]) return this.Families.OPERA;
       return this.Families.UNKNOWN;
    },

    /**
     * The modifier key used to trigger access-keys defined in a page.
     *
     * @return {String} A textual description of the modifier key used by this browser.
     * e.g. "Alt" or "Alt+Shift"
     */
    modifierKey : function() {
        if (this.family() === this.Families.INTERNET_EXPLORER) {
            return "Alt";
        }

        if (this.family() === this.Families.MOZILLA) {
            if (jQuery.os.mac) {
                return "Ctrl";
            }
            else {
                return "Alt+Shift";
            }
        }
        if (this.family() === this.Families.WEBKIT){
           if (jQuery.os.windows) {
                return "Alt";
           }
           else {
               return "Ctrl+Alt";
           }
        }
        if (this.family() === this.Families.OPERA) {
            return "Shift+Esc";
        }
        return "Alt";
    }
});

/**
 * Represents the list of known browser families.
 *
 * The value UNKNOWN is used to classify browsers which do not fit into any of the known families.
 */
AJS.Navigator.Families = jQuery.extend({}, AJS.Navigator.Families, {
    INTERNET_EXPLORER :  "msie",
    MOZILLA : "mozilla",
    WEBKIT : "webkit",
    OPERA : "opera",
    UNKNOWN : "unknown"
});