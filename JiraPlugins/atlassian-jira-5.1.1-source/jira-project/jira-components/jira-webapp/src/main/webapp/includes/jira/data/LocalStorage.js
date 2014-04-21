// Define the localStorage interface so that JIRA doesn't fall over for older browsers that don't support it.
if (typeof localStorage === "undefined") {
    localStorage = {
        getItem: jQuery.noop,
        setItem: jQuery.noop,
        removeItem: jQuery.noop,
        clear: jQuery.noop
    }
}