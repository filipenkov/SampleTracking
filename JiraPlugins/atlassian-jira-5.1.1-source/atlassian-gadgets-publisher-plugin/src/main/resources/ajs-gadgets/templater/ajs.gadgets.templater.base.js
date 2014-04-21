/**
 * sdfg
 * @class Templater
 * @private
 */
AJS.gadgets.templater.Base = function () {

    var

    /**
     * Specifies if the result of the template rendering should be cached for next time.
     * @property  cache
     * @private
     * @type Boolean
     */
    cache,

    /**
     * Template url
     *
     * @property  template
     * @private
     * @type String
     */
    template,

    /**
     * Reference to descriptor function
     *
     * @property descriptor
     * @private
     * @type Function
     */
    descriptor = function (args) {
        return args;
    },

    /**
     * Callback for when element has been built
     *
     * @property callbacks
     * @private
     * @type Array
     */
    callbacks = [],

    /**
     * An array of objects that specify remote datasources that will need to be passed to the descriptor
     *
     * @property remoteArgs
     * @private
     * @type Array
     */
    remoteArgs = [],



    /**
     * An array of objects that will be passed to the descriptor.
     *
     * @property args
     * @private
     * @type Array
     */
    args = [],

    /**
     * Using a provided template and descriptor returns a html string
     *
     * @method renderTemplate
     * @private
     * @param {String} str - template
     * @param {Object} data - json object returned from descriptor
     * @return {String} html
     */
    renderTemplate = function () {

        var cache = {};

        return function (str, data){
            var fn;
            if (!/\W/.test(str)){
                fn = cache[str];
            } else {
                var fnString = "var p=[], print=function(){p.push.apply(p,arguments);};" +

                // Introduce the data as local variables using with(){}
                "with(obj){p.push('" +

                // Convert the template into pure JavaScript
                str
                  .replace(/[\r\t\n]/g, " ")
                  .split("<%").join("\t")
                  .replace(/((^|%>)[^\t]*)'/g, "$1\r")
                  .replace(/\t=(.*?)%>/g, "',$1,'")
                  .split("\t").join("');")
                  .split("%>").join("p.push('")
                  .split("\r").join("\\'")
                    + "');}return p.join('');";

                fn = new Function("obj", fnString);
            }
            // Provide some basic currying to the user
            return data ? fn( data ) : fn;
        };
    }();


    return {

        /**
         * Retrieves any remote arguments via ajax along with already populated arguments. Formats each argument into
         * a key value pair, passing them onto the callback.
         *
         * The format of remote arguments can be specified in a number of different forms.
         *
         * Examples:
         *
         * 1. {key: "myRemoteArgument", ajaxOptions: {url: "http://www.example.com"}}
         * 2. {key: "myRemoteArgument", ajaxOptions: "http://www.example.com"}
         * 3. {key: "myRemoteArgument", ajaxOptions: function() { return {url: "http://www.example.com"}};
         *
         * Local arguments are specified as:

         * {key: "myLocalArgument", data: "Some data"}
         *
         * @method getDescriptorData
         * @private
         * @param {Function} callback - the function to be called with formatted arguments
         */
        getDescriptorData: function (callback) {

            var argLengthComparator = 0, that = this, formattedArgs = {};

            if (remoteArgs.length < 1) {
                AJS.$(args).each(function(){
                    formattedArgs[this.key] = this.data;
                });
                if (callback) {
                    callback.call(that, descriptor(formattedArgs));
                } else {
                    return descriptor(formattedArgs);
                }
            }

            AJS.$(remoteArgs).each(function(i, item){
                var reqOptions;

                if (typeof item.ajaxOptions === "string") {
                    reqOptions = {url: this.ajaxOptions};
                } else if (AJS.$.isFunction(item.ajaxOptions)) {
                    reqOptions = item.ajaxOptions();
                } else {
                    reqOptions = item.ajaxOptions;
                }
                reqOptions.success = function (response) {
                    ++argLengthComparator;
                    formattedArgs[remoteArgs[i].key] = response;
                    if (argLengthComparator === remoteArgs.length) {
                        var excuter = function () {
                            if (callback) {
                                callback.call(that, descriptor(formattedArgs));
                            } else {
                                return descriptor(formattedArgs);
                            }
                            return arguments.callee;
                        }();
                        if (cache !== false) {
                            that.getDescriptorData = excuter;
                        }
                    }
                };
                AJS.$.ajax(reqOptions);
            });
        },

        disableCache: function () {
            cache = false;
        },

        /**
         * Sets the url to load template from
         *
         * @method setTemplate
         */
        setTemplate: function (templ) {
            template = templ;
        },

        /**
         * Sets the descriptor. The descriptor is a function that returns a json object that describes to the template
         * how to construct the html. Each template is likely to have a different structure.
         *
         * @method setDescriptor
         * @param {Function} desc
         */
        setDescriptor: function (desc) {
            descriptor = desc;
        },

        /**
         * Method to be called after the templated element is built. The method will be called in the context of this object.
         *
         * @method setCallback
         * @param {Function} func
         */

        addCallback: function (func) {
            callbacks.push(func);
        },

        /**
         * Often the descriptor needs additional data in order to form the correct json. This method allows you to specify
         * params to request remote json data. The response of this request is appened, using the key provided, to a json
         * object that is passed as the first argument to the descriptor. For ajax options please refer to
         * <a href="http://docs.jquery.com/Ajax/jQuery.ajax#options">jQuery documentation</a>
         *
         * @method setRemoteDescriptorArg
         * @param {String} key - Key to be mapped to json object
         * @param {Object} ajaxOptions - request options for remote data
         */
        setRemoteDescriptorArg: function (key, ajaxOptions) {
            remoteArgs.push({key: key, ajaxOptions: ajaxOptions});
        },

        setDescriptorArg: function (arg) {
            args.push(arg);
        },

        /**
         * Builds html string using template and data returned from descriptor. All the specified remote arguments
         * are requested and given to the descriptor in the form of a json object.
         *
         * Any subsequent times this method is called template and remote data is restored from internal cache.
         *
         * @method build
         * @param {Object} data
         */
        build: function (data) {
            var that = this;

            if (!data && data !== false) {
                this.getDescriptorData(this.build);
            } else if (data !== false) {

                if (typeof template === "string" && document.getElementById(template)) {
                    that.build.template = AJS.$("#" + template).html();
                    var fragment = renderTemplate(that.build.template, data);
                    AJS.$(callbacks).each(function(){
                        this.call(that, AJS.$(fragment), data);
                    });
                } else if (AJS.$.isFunction(template)) {
                    template(data);
                    AJS.$(callbacks).each(function(){
                        this.call(that, data);
                    });
                // this is really dodgy
                } else if (template.length > 50) {
                    that.build.template = template;
                    var fragment = renderTemplate(that.build.template, data);
                    AJS.$(callbacks).each(function(){
                        this.call(that, AJS.$(fragment), data);
                    });
                } else {
                     if (!that.build.template) {
                        AJS.$.ajax({
                            url: template,
                            dataType: "text",
                            success: function (response) {
                                that.build.template = response;
                                var fragment = renderTemplate(that.build.template, data);
                                AJS.$(callbacks).each(function(){
                                    this.call(that, AJS.$(fragment), data);
                                });
                            }
                        });
                    } else {
                        var fragment = renderTemplate(that.build.template, data);
                        AJS.$(callbacks).each(function(){
                            this.call(that, AJS.$(fragment), data);
                        });
                    }
                }
            }
        }
    };
};