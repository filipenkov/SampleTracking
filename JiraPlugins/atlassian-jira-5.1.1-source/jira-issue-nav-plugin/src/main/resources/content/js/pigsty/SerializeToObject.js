/**
 * Serializes form fields within the given element to a JSON object
 *
 * {
 *    fieldName: "fieldValue"
 * }
 *
 * @returns {Object}
 */
jQuery.fn.serializeObject = function () {

    var data = {};

    var dataArray = this.find(":input").serializeArray();

    jQuery.each(dataArray, function() {
        if (data[this.name]) {
            if (!data[this.name].push) {
                data[this.name] = [data[this.name]];
            }
            data[this.name].push(this.value || '');
        } else {
            data[this.name] = this.value || '';
        }
    });

    return data;
};