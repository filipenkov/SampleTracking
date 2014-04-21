var ActivityStreams = ActivityStreams || {};

ActivityStreams.i18n = ActivityStreams.i18n || (function() {
    var i18n = {};

    /**
     * Get i18nized text for a given key.
     *
     * @param key the i18n key
     */
    function get(key) {
        return i18n[key] || key;
    }

    /**
     * Store a i18n value
     *
     * @param key the i18n key
     * @param value the i18n value
     */
    function put(key, value) {
        i18n[key] = value;
    }

    return {
        //expose get() and put() methods
        get: get,
        put: put
    };
})();
