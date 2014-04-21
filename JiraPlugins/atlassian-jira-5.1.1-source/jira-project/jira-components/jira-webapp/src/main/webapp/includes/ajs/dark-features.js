/**
 * Dark features are features that can enabled and disabled per user via a feature key. Their main use is to allow
 * in-development features to be rolled out to production in a low-risk fashion.
 */
(function ($) {
    var featuresArray = ENABLED_DARK_FEATURES_SUBSTITUTION;

    var features = {}
    $.each(featuresArray, function () {
        features[this] = true;
    });

    AJS.DarkFeatures = {
        isEnabled: function (key) {
            return !!features[key];
        },

        enable: function (key) {
            if (key && !features[key])
                features[key] = true;
        },

        disable: function (key) {
            if (key && features[key])
                delete features[key];
        }
    };
})(AJS.$);
