

/*global AJS, document, setTimeout */
if (!console.error) {
    console.log = console.warn = console.error = console.time = console.timeEnd = function () {};
}


// Library from http://blog.stevenlevithan.com/archives/parseuri

AJS.parseUri = function (uri, strict) {
    var unesc = window.decodeURIComponent || unescape;
    var esc = window.encodeURIComponent || escape;
    
    function parseUri (str) {
        var	o   = parseUri.options,
            m   = o.parser[o.strictMode ? "strict" : "loose"].exec(str),
            uri = {},
            i   = 14;

        while (i--) uri[o.key[i]] = m[i] || "";

        uri[o.q.name] = {};
        uri[o.key[12]].replace(o.q.parser, function ($0, $1, $2) {
            if ($1) uri[o.q.name][unesc($1)] = unesc($2);
        });

        return uri;
    };

    parseUri.options = {
        strictMode: !!strict,
        key: ["source","protocol","authority","userInfo","user","password","host","port","relative","path","directory","file","query","anchor"],
        q:   {
            name:   "queryKey",
            parser: /(?:^|&)([^&=]*)=?([^&]*)/g
        },
        parser: {
            strict: /^(?:([^:\/?#]+):)?(?:\/\/((?:(([^:@]*)(?::([^:@]*))?)?@)?([^:\/?#]*)(?::(\d*))?))?((((?:[^?#\/]*\/)*)([^?#]*))(?:\?([^#]*))?(?:#(.*))?)/,
            loose:  /^(?:(?![^:@]+:[^:@\/]*@)([^:\/?#.]+):)?(?:\/\/)?((?:(([^:@]*)(?::([^:@]*))?)?@)?([^:\/?#]*)(?::(\d*))?)(((\/(?:[^?#](?![^?#\/]*\.[^?#\/.]+(?:[?#]|$)))*\/?)?([^?#\/]*))(?:\?([^#]*))?(?:#(.*))?)/
        }
    };

    uri = parseUri(uri);

    uri.toString = function () {
        var params = [];
        AJS.$.each(uri.queryKey, function (name, value) {
            params.push(esc(name) + "=" + esc(value));
        });
        
        return uri.protocol + "://" + uri.authority + uri.path + "?" + params.join("&") + "#" + uri.anchor;
    };
    
    return uri;
};





