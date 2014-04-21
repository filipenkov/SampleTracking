/**
 * For building elements, using custom templates
 *
 * @class Custom
 * @namespace AJS.gadgets.templater
 * @constructor
 * @param {Object} options
 */
AJS.gadgets.templater.Custom = function (options) {
    var templater = AJS.gadgets.templater.Base();
    templater.setTemplate(options.template);
    if (options.descriptor) {
        templater.setDescriptor(options.descriptor);
    }
    if (options.callback) {
        templater.addCallback(options.callback);
    }
    if (options.cache === false) {
        templater.disableCache();
    }
    AJS.$.extend(templater, {
        getTarget: function () {
            return options.target;
        }
    });
    if (options.args) {
		AJS.$(options.args).each(function() {
            if (this.ajaxOptions) {
			    templater.setRemoteDescriptorArg(this.key, this.ajaxOptions);
            } else {
                templater.setDescriptorArg(this);
            }
		});
	}
	return templater;
};