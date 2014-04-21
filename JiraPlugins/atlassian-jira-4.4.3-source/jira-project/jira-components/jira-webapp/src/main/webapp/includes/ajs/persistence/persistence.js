/**
 * A way to pass data from one page to another without involving the server.
 *
 * @usage
 *
 * AJS.nextPage("blurSearch", true);
 *
 * // Then on the following page you can get to this value
 *
 * AJS.thisPage("blurSearch");
 *
 */
AJS.nextPage = function () {

    var data = [],
        oldBeforeUnload = window.onbeforeunload;

        window.onbeforeunload = function () {

            if (window.sessionStorage) {
                    sessionStorage.setItem("AJS.thisPage", JSON.stringify(data));
            } else {
                saveCookie("AJS.thisPage", JSON.stringify(data));
            }

            if (oldBeforeUnload) {
                oldBeforeUnload();
            }
        };

    return function (name, value) {

        var replaced;

        jQuery.each(data, function () {
            if (this.name === name) {
                this.value = value;
                replaced = true;
            }
        });

        if (!replaced) {
            data.push({
                name: name,
                value: value
            });
        }
    };

}();

AJS.thisPage = function () {

    var i,
        value,
        unformattedData,
        data = {};

    if (window.sessionStorage) {
        unformattedData = sessionStorage.getItem("AJS.thisPage");
        sessionStorage.removeItem("AJS.thisPage");


    } else {
        unformattedData = readCookie("AJS.thisPage");
        eraseCookie("AJS.thisPage")
    }



    if (unformattedData) {
        unformattedData = JSON.parse(unformattedData);
        for (i=0; i < unformattedData.length; i++) {
            data[unformattedData[i].name] = unformattedData[i].value;
        }
    }

    return function (key) {
        return data[key];
    }
    
}();