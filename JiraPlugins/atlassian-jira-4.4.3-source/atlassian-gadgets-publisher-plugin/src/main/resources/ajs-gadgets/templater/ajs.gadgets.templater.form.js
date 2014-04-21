/**
 * For building forms
 *
 * @class Form
 * @namespace AJS.gadgets.templater
 * @constructor
 * @param {Object} options
 */
AJS.gadgets.templater.Form = function (options) {
	return AJS.gadgets.templater.Custom(AJS.$.extend({
		template: AJS.gadgets.templater.Form.template,
        baseUrl: AJS.gadget.getBaseUrl(),
		callback: function(response, data) {
            if (options.target) {
                options.target.html(response);
                if (options.submit) {
                    AJS.$("form", options.target).submit(options.submit);
                }
                if (options.cancel) {
                    AJS.$("input.cancel", options.target).click(options.cancel);
                }
                AJS.$(data.fields).each(function(){

                    if (this.type === "callbackBuilder"){
                        var targetDiv = AJS.$("div#" + this.id);
                        this.callback(targetDiv);
                   }
                });
            } else {
                return response;
            }
        }
	}, options));
};