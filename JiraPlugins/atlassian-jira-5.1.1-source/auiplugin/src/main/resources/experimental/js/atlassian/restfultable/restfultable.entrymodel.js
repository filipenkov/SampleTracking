(function ($) {

    /**
     * A class provided to fill some gaps with the out of the box Backbone.Model class. Most notiably the inability
     * to send ONLY modified attributes back to the server.
     *
     * @class EntryModel
     * @namespace AJS.RestfulTable
     */
    AJS.RestfulTable.EntryModel = Backbone.Model.extend({

        /**
         * Overrides default save handler to only save (send to server) attributes that have changed.
         * Also provides some default error handling.
         *
         * @override
         * @param attributes
         * @param options
         */
        save: function (attributes, options) {


            options = options || {};

            var instance = this,
                Model,
                syncModel,
                error = options.error, // we override, so store original
                success = options.success;


            // override error handler to provide some defaults
            options.error = function (model, xhr) {

                var data = $.parseJSON(xhr.responseText || xhr.data);

                instance._serverErrorHandler(xhr);

                // call original error handler
                if (error) {
                    error.call(instance, instance, data, xhr);
                }
            };

            // if it is a new model, we don't have to worry about updating only changed attributes because they are all new
            if (this.isNew()) {

                // call super
                Backbone.Model.prototype.save.call(this, attributes, options);

            // only go to server if something has changed
            } else if (attributes) {

                // create temporary model
                Model = Backbone.Model.extend({
                    url: this.url()
                });

                syncModel = new Model({
                    id: this.id
                });

                options.success = function (model, xhr) {

                    // update original model with saved attributes
                    instance.clear().set(model.toJSON());

                    // call original success handler
                    if (success) {
                        success.call(instance, instance, xhr);
                    }
                };

                // update temporary model with the changed attributes
                syncModel.save(attributes, options);
            }
        },

        /**
         * Destroys the model on the server. We need to override the default method as it does not support sending of
         * query paramaters.
         *
         * @override
         * @param options
         * ... {function} success - Server success callback
         * ... {function} error - Server error callback
         * ... {object} data
         *
         * @return AJS.RestfulTable.EntryModel
         */
        destroy: function (options) {

            options = options || {};

            var instance = this,
                url = this.url(),
                data;

            if (options.data) {
                data = $.param(options.data);
            }

            if (data !== "") {
                // we need to add to the url as the data param does not work for jQuery DELETE requests
                url = url + "?" + data;
            }

            $.ajax({
                url: url,
                type: "DELETE",
                dataType: "json",
                success: function (data) {
                    if(instance.collection){
                        instance.collection.remove(instance);
                    }
                    if (options.success) {
                        options.success.call(instance, data);
                    }
                },
                error: function (xhr) {
                    instance._serverErrorHandler(xhr);
                    if (options.error) {
                        options.error.call(instance, xhr);
                    }
                }
            });

            return this;
        },


        /**
         * A more complex lookup for changed attributes then default backbone one.
         *
         * @param attributes
         */
        changedAttributes: function (attributes) {

            var changed = {},
                current = this.toJSON();

            $.each(attributes, function (name, value) {

                if (!current[name]) {
                    if (typeof value === "string") {
                        if ($.trim(value) !== "") {
                            changed[name] = value;
                        }
                    } else if ($.isArray(value)) {
                        if (value.length !== 0) {
                            changed[name] = value;
                        }
                    } else {
                        changed[name] = value;
                    }
                } else if (current[name] && current[name] !== value) {

                    if (typeof value === "object") {
                        if (!_.isEqual(value, current[name])) {
                            changed[name] = value;
                        }
                    } else {
                        changed[name] = value;
                    }
                }
            });

            if (!_.isEmpty(changed)) {
                this.addExpand(changed);
                return changed;
            }
        },

        /**
         * Useful point to override if you always want to add an expand to your rest calls.
         *
         * @param changed attributes that have already changed
         */
        addExpand: function (changed){},

        /**
         * Throws a server error event unless user input validation error (status 400)
         *
         * @param xhr
         */
        _serverErrorHandler: function (xhr) {
            var data;
            if (xhr.status !== 400) {
                data = $.parseJSON(xhr.responseText || xhr.data);
                AJS.triggerEvtForInst(AJS.RestfulTable.Events.SERVER_ERROR, this, [data, xhr]);
            }
        },

        /**
         * Fetches values, with some generic error handling
         *
         * @override
         * @param options
         */
        fetch: function (options) {

            options = options || {};

            var instance = this,
                error = options.error;

            this.clear(); // clear the model, so we do not merge the old with the new

            options.error = function (model, xhr) {
                instance._serverErrorHandler(xhr);
                if (error) {
                    error.apply(this, arguments);
                }
            };

            // call super
            Backbone.Model.prototype.fetch.call(this, options);
        }
    });

})(AJS.$);