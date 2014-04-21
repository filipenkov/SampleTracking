/**
 * @method copyObject
 * @namespace AJS
 * @module Util
 * @param {Object} object - to copy
 * @param {Boolean} deep - weather to copy objects within object
 */

AJS.copyObject = function (object, deep) {

    var copiedObject = {};

        AJS.$.each(object, function(name, property) {
            if (typeof property !== "object" || property === null || property instanceof AJS.$) {
                copiedObject[name] = property;
            } else if (deep !== false) {
                copiedObject[name] = AJS.copyObject(property, deep);
            }
        });

    return copiedObject;
};
