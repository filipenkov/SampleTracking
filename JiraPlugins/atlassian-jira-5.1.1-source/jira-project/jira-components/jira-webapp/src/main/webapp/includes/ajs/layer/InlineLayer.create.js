/**
 * Static factory method to create multiple dropdowns at one time.
 *
 * @method create
 * @namespace AJS.InlineLayer
 * @param options - @see AJS.InlineLayer.OptionsDescriptor
 * @return {Array}
 */
AJS.InlineLayer.create = function (options) {

    var inlineLayers = [];

    if (options.content) {
        options.content = AJS.$(options.content);
        AJS.$.each(options.content, function () {
            var instanceOptions = AJS.copyObject(options);
            instanceOptions.content = AJS.$(this);
            inlineLayers.push(new AJS.InlineLayer(instanceOptions));
        });
    }

    if (inlineLayers.length == 1) {
        return inlineLayers[0];
    } else {
        return inlineLayers;
    }
};
