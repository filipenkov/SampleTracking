
(function () {

    // A utility to log out errors and logs into html so that we can analyse them in the selenium dump

    function appendMsg(msg) {
        var logger = document.getElementById("js-logger");

        if (!logger) {
            logger = document.createElement("div");
            logger.className = "hidden";
            logger.id = "js-logger";
            document.body.appendChild(logger);
        }

        var logEl = document.createElement("div");
        logEl.innerHTML = msg;
        logger.appendChild(logEl);
    }


    window.onerror = appendMsg;

    var _console = window.console;
    window.console = {
        log: function (msg) {
            appendMsg(msg);
            if (_console != null) {
                try {
                    _console.log.apply(_console, arguments);
                } catch (err) {
                    _console.log(msg);
                }
            }
        }
    };

})();
