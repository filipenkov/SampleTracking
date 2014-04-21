window.atl_soy = window.atl_soy || {};

atl_soy.concat = function(a, b) {
    // handle arrays
    if (a.concat) {
        return a.concat(b);
    }

    //handle object
    var ret = {};
    for (var key in a) if (a.hasOwnProperty(key)) ret[key] = a[key];
    for (var key in b) if (b.hasOwnProperty(key)) ret[key] = b[key];
    return ret;
};