AJS.namespace("JIRA.Issues.BaseModel");
AJS.namespace("JIRA.Issues.BaseCollection");
AJS.namespace("JIRA.Issues.BaseView");
AJS.namespace("JIRA.Issues.BaseRouter");

(function () {

    // Applies all mixins to the given constructor's prototype.
    function applyMixin(ctor, mixin) {
        _.forEach(_.keys(mixin), function(key) {
            var proto = ctor.prototype;

            // `initialize` is not mixed in - we compose the mixin's initialize with the existing initialize method (if it exists).
            if ("initialize" === key) {
                var oldInitialize = proto.initialize;
                proto.initialize = function() {
                    mixin.initialize.apply(this, arguments);
                    if (oldInitialize) {
                        oldInitialize.apply(this, arguments);
                    }
                };
                return;
            }
            // `validate` is not mixed in - we compose the mixin's validate with the existing validate method (if it exists).
            if ("validate" === key) {
                var oldValidate = proto.validate;
                proto.validate = function() {
                    var errors = mixin.validate.apply(this, arguments);
                    if (errors) {
                        return errors;
                    }
                    if (oldValidate) {
                        return oldValidate.apply(this, arguments);
                    }
                };
                return;
            }
            // `defaults` are not mixed in - we compose the mixin's defaults with existing defaults if they exist
            if ("defaults" === key) {
                var defaults = proto.defaults || (proto.defaults = {});
                var mixinDefaults = mixin[key];
                for (var id in mixinDefaults) {
                    if (defaults.hasOwnProperty(id)) {
                        throw "Mixin error: object " + ctor + " already has default " + id + " defined for mixin " + mixin;
                    }
                    defaults[id] = mixinDefaults[id];
                }
                return;
            }
            // `properties` are added to the mixin, and we mixin in getters and setters for each property.
            if ("properties" === key) {
                // `properties` must be an array
                if (!_.isArray(mixin[key])) {
                    throw "Expects properties member on mixin to be an array";
                }
                if (!proto.properties) {
                    proto.properties = [];
                }
                proto.properties = proto.properties.concat(mixin[key]);
                return;
            }

            // `namedEvents` are added to the mixin, and we mix in bind and trigger methods for each property.
            if ("namedEvents" === key) {
                // `events` must be an array
                if (!_.isArray(mixin[key])) {
                    throw "Expects events member on mixin to be an array";
                }
                if (!proto.namedEvents) {
                    proto.namedEvents = [];
                }
                proto.namedEvents = proto.namedEvents.concat(mixin[key]);
                return;
            }
            // Name collisions with other mixins or or the object we're mixing into result in violent and forceful disapproval.
            if (proto.hasOwnProperty(key)) {
                throw "Mixin error: object " + ctor + " already has property " + key + " for mixin " + mixin;
            }
            proto[key] = mixin[key];
        }, this);
    }

    /*
     * Generates an `extend` method that overrides Backbone's default `extend`. The new extend calls Backbone's `extend`, then:
     * <ul>
     *     <li>Adds all mixins specified in the `mixins` array.</li>
     *     <li>Adds a `JIRA.Issues.EventsMixinCreator` to mix in bind and trigger methods for events specified in the `namedEvents` array,</li>
     *     <li>Adds a `JIRA.Issues.AttributesMixinCreator` to mix in get and set methods for attributes specified in the `attributes` array,</li>
     * </ul>
     */
    function generateMixinExtend(oldExtend) {
        return function(protoProps, classProps) {
            var child;
            var cleanProtoProps = _.extend({}, protoProps);
            // Remove `mixins` - we don't want to see them on the created prototype. Note that we do want to see `properties` and `namedEvents` for debugging
            var mixins;
            if (protoProps && protoProps.mixins) {
                mixins = protoProps.mixins;
                delete cleanProtoProps.mixins;
            }
            child = oldExtend.call(this, cleanProtoProps, classProps);
            if (mixins) {
                _.each(protoProps.mixins, function(mixin) {
                    applyMixin(child, mixin);
                });
            }
            if (child.prototype.namedEvents) {
                applyMixin(child, JIRA.Issues.EventsMixinCreator.create(child.prototype.namedEvents));
            }
            if (child.prototype.properties) {
                applyMixin(child, JIRA.Issues.AttributesMixinCreator.create(child.prototype.properties));
            }
            child.extend = arguments.callee;
            return child;
        };
    }

    // Overrides Backbone's default `get` and `set` methods to validate that the attribute being get / set is a valid property.
    function overrideSetGet(ctor, childCtor) {
        var proto = ctor.prototype;
        var childProto = childCtor.prototype;

       var oldSet = proto.set;
        childProto.set = function(key, value, options) {
            // TODO: has, escape, unset
            var attrs,
                properties = this.properties;
            if (properties) {
                if (_.isObject(key) || key == null) {
                    attrs = key;
                } else {
                    attrs = {};
                    attrs[key] = value;
                }
                for (var attr in attrs) {
                    if (_.indexOf(properties, attr) < 0) {
                        throw "Property '" + attr + "' does not exist";
                    }
                }
            }
            return oldSet.apply(this, arguments);
        };

        var oldGet = proto.get;
        childProto.get = function(attr) {
            if (this.properties && _.indexOf(this.properties, attr) < 0) {
                throw "Property '" + attr + "' does not exist";
            }
            return oldGet.apply(this, arguments);
        };
    }

    // Applies extensions to the given constructor function:
    // <ul>
    //   <li>Sets `extend` to a method generated by `generateMixinExtend`</li>
    // </ul>
    function applyExtensions(ctor) {
        var child = ctor.extend();
        var oldExtend = ctor.extend;
        child.extend = generateMixinExtend(oldExtend);
        return child;
    }


    // Applies extensions to the given constructor function:
    // <ul>
    //   <li>Sets `extend` to a method generated by `generateMixinExtend`</li>
    // </ul>
    function applyModelExtensions(ctor) {
        var child = applyExtensions(ctor);
        overrideSetGet(ctor, child);
        return child;
    }


    // Extend base Backbone classes
    JIRA.Issues.BaseModel = applyModelExtensions(Backbone.Model);
    JIRA.Issues.BaseCollection = applyExtensions(Backbone.Collection);
    JIRA.Issues.BaseView = applyExtensions(Backbone.View);
    JIRA.Issues.BaseRouter = applyExtensions(Backbone.Router);

    // add some mixins to underscore
    _.mixin({
        lambda: function(x) {
            return function() {return x;}
        },
        isNotBlank: function(object) {
            return !!object;
        }
    });
})();
