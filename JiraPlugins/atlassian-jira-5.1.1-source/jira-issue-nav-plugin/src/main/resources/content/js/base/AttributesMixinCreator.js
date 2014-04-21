AJS.namespace("JIRA.Issues.AttributesMixinCreator");

(function () {

    // ### JIRA.Issues.AttributesMixinCreator ###
    JIRA.Issues.AttributesMixinCreator = {

        // Creates a mixin of getter and setter methods for each item in the given property list.
        // A getter and setter for `id` is always generated.
        create: function(properties) {
            var methods = {};

            if (_.indexOf(properties, "id") === -1) {
                properties.unshift("id");
            }

            _.each(properties, function (property) {
                var setter = JIRA.Issues.Mixins.createMethodName("set", property);
                methods[setter] = function (val,options) {
                    var obj = {};
                    obj[property] = val;
                    this.set(obj,options);
                    return this;
                };
                var getter = JIRA.Issues.Mixins.createMethodName("get", property);
                methods[getter] = function () {
                    return this.get(property);
                };
            });
            return methods;
        }
    };

})();
