
var runLessRun, init;

var exports = {}; // exports for parser.js
var require;
(function() {
    var tree = {};
    require = function(arg) {
        if (arg == "less/tree") {
            return tree;
        }
        throw "Attempt to require module other than 'less/tree': " + arg;
    };

    var ourImporter = function (path, paths, callback, env)
    {
        path = env.relpath + path;
        // TODO we should really resolve .. etc in path so that alreadySeen works

        var match = /^(.*\/)([^/]+)?$/.exec(path); // basename of path becomes new relpath
        var newrelpath = match ? match[1] : "";

        var data;
        if (env.alreadySeen[path]) {
            data = "/* skipping already included " + path + " */\n";
        } else {
            env.alreadySeen[path] = true;
            data = env.ourLoader.load(path);
            data = data + ""; // converts to proper native string
        }

        var newenv = {
            ourLoader: env.ourLoader,
            relpath: newrelpath,
            alreadySeen: env.alreadySeen,
            filename: path
        };


        var parser = new exports.Parser(newenv);
        parser.parse(data, function(e, root)
        {
            if (e) {
                throw e;
            }
            callback(root);
        });
    };

    var fixedFormat = function (quoted /* arg, arg, ...*/) {
        var args = Array.prototype.slice.call(arguments, 1),
                str = quoted.value;

        var out = "";
        var j = 0;
        for (var i = 0; i < str.length; i++) {
            var c = str[i];
            if ((c == "%") && (j < args.length)) {
                var d = i+1 < str.length ? str[i+1] : null;
                if (d == "%") {
                    i++;
                    out += "%";
                } else if (d == "s") {
                    i++;
                    out += args[j++].value;
                } else if ((d == "d") || (d == "a")) {
                    i++;
                    out += args[j++].toCSS();
                } else {
                    out += c;
                }
            } else {
                out += c;
            }
        }

        return new(tree.Quoted)('"' + out + '"', out);
    };

    var patchLess = function() {

        // disable inline Javascript in less for now (by default: no remote code exploits)
        tree.JavaScript.prototype.toCSS = tree.JavaScript.prototype.eval = function () {
            throw { message: "JavaScript evaluation in Less is disabled",
                index: this.index };
        };

        // fix up the version of %() based on this pull request:
        // https://github.com/cloudhead/less.js/pull/199
        tree.functions['%'] = fixedFormat;

        // custom functions (all names are lowercased before lookup in tree.functions)
        tree.functions.encodeuricomponent = function(a) {
            var str = encodeURIComponent(a.value ? a.value : a.toCSS());
            return new(tree.Quoted)('"' + str + '"', str);
        };
        tree.functions.encodeuri= function(a) {
            var str = encodeURI(a.value ? a.value : a.toCSS());
            return new(tree.Quoted)('"' + str + '"', str);
        };
//        tree.functions.encodeURI = ;
//            tree.functions.spud = function(a) { return new(tree.Anonymous)(a.toSource())}; // example custom function
    };

    init = function() {
        exports.Parser.importer = ourImporter;
        patchLess();
    };

    runLessRun = function(filename, loader, css, compress) {

        var alreadySeen = {};
        var env = {
            ourLoader: loader,
            relpath: "", // blank or ends in a slash
            filename: filename,
            alreadySeen: alreadySeen
        };
        var parser = new exports.Parser(env);

        var result;
        parser.parse(css, function (e, root) {
            if (e) {
                throw e;
            }
            result = root.toCSS({compress: compress});
        });
        return result;
    }

})();

