(function () {

    // ### JIRA.Issues.EventsMixinCreator ###
    JIRA.Issues.EventsMixinCreator = {

        // Creates a mixin of bind and trigger methods for each item in the given list of events.
        create: function(events) {
            var eventMethods = {};
             var createEvent = function(eventName) {
                 var binder = JIRA.Issues.Mixins.createMethodName("bind", eventName);
                 eventMethods[binder] = function() {
                     return this.bind.apply(this, [eventName].concat(_.toArray(arguments)));
                 };

                 var unbinder = JIRA.Issues.Mixins.createMethodName("unbind", eventName);
                 eventMethods[unbinder] = function() {
                     return this.unbind.apply(this, [eventName].concat(_.toArray(arguments)));
                 };

                 var trigger = JIRA.Issues.Mixins.createMethodName("trigger", eventName);
                 eventMethods[trigger] = function() {
                     return this.trigger.apply(this, [eventName].concat(_.toArray(arguments)));
                 };
                 var one = JIRA.Issues.Mixins.createMethodName("one", eventName);
                 eventMethods[one] = function() {
                     var instance = this;
                     var originalHandler = arguments[0];
                     var unbindingHandler = function() {
                         instance.unbind(eventName, unbindingHandler);
                         originalHandler(arguments);
                     };

                     var rest = _.toArray(arguments).slice(1);
                     return this.bind.apply(this, [eventName, unbindingHandler].concat(rest));
                 }
             };
            _.each(events, _.bind(createEvent,this));

            return eventMethods;
        }
    };

})();
