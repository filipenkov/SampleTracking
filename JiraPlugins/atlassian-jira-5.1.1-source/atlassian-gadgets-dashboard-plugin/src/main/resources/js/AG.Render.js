

AG.render = function () {

    var TEMPLATES_IFRAME_ID = "templates", parsers = {},

    setParser = function (template, parser) {
        parsers[template] = parser;
    },

    getParser = function (template) {
        return parsers[template];
    },

    setContext = function () {

        AG.render.ctx = "body";

        if(AJS.debug) {
            if (AG.render.ctx.length === 0) {
                console.error("AG.render: Rendering Failed! <iframe> storing templates is either not present OR has been" +
                        "included AFTER the call to this method");
            }
        }
    },

    bindMacros = function (template, ignore) {

        var str = "", macros = [];

        ignore = ignore || [];

        //can't use string replace here since htmlunit can't handle it.
        var matches = template.match(/(?:\$[\W]*)(\w+)(?:\s*\([^\}]+})/gi);

        if (matches) {
            AJS.$.each(matches, function () {
                var macroName = this.replace(/\$\{([\d\w]*).*/, "$1");
                 if (AJS.$.inArray(macroName, macros) === -1) {
                    if (AJS.$.inArray(macroName, ignore) === -1 ) {
                        macros.push(macroName);
                        ignore.push(macroName);
                    }
                }
            });
        }

        AJS.$.each(macros, function () {
            var macro = AJS.$("#" + this, AG.render.ctx).html();
            str += bindMacros(macro, ignore);
            str += ["<?macro " + this + "(data)?>", macro, "<?/macro?>"].join("");
        });

        if (arguments.length === 1) {
            str += template;
        }

        return str;
    },

    render = function (descriptor) {

        var templateSrc, template = descriptor.useTemplate, parser = getParser(template);

        if (!parser) {
            parser = new ZParse(Implementation);

            templateSrc = AJS.$("#" + template);

            if (template.length > 0) {
                parser.parse(bindMacros(templateSrc.html()));
                setParser(template, parser);
            }
            else if(AJS.debug) {
                console.error("AG.render: Rendering Failed! Template '" + template + "' does not exist.");
            }
        }

        else if(AJS.debug) {
            console.log("AG.render: Using cached parser");
        }

        return parser.process(descriptor);
    };

    return function (descriptor, data) {
        if (typeof descriptor !== "object") {
            descriptor = AG.render.getDescriptor(descriptor, data);
        }

        if (!AG.render.ctx) {
            setContext();
        }

        return render(descriptor);
    };
}();


AG.render.ready = function (func) {
    AG.render.ready.callbacks.push(func);
};

AG.render.ready.callbacks = [];

AG.render.initialize = function () {
    AJS.$.each(AG.render.ready.callbacks, function () {
        this();
    });
};

AG.render.getDescriptor = function () {


    var descriptors = {

        layoutDialog: function (args) {
            return AJS.$.extend({closeId: "dialog-close"}, args);
        },

        gadget: function (args) {

            function generateMenuItems () {

                var menu = [];

                function isEditable () {
                    return !!(isWritable() && args.hasNonHiddenUserPrefs);
                };

                function isWritable () {
                    return args.layout.writable;
                };

                function generateColorList () {
                    var colorList = [];
                    AJS.$.each(AG.Gadget.COLORS, function () {
                        var color = AG.Gadget.getColorAttrName(this);
                        colorList.push({
                            styleClass: color,
                            link: {
                                href: "#",
                                text: AG.param.get(color)
                            }
                        });
                    });
                    return {
                        styleClass: "item-link gadget-colors",
                        items: colorList
                    };
                }
                
                if (isEditable()) {
                    menu.push({
                        styleClass: "dropdown-item",
                        link: {
                            styleClass: "item-link edit",
                            href: "#gadget-" + args.id + "-edit",
                            text: AG.param.get("edit")
                        }
                    });
                }

                menu.push({
                    styleClass: "dropdown-item",
                    link: {
                        styleClass: "item-link " + (args.minimized ? "maximization" : "minimization"),
                        href: "#",
                        text: args.minimized ? AG.param.get("expand") : AG.param.get("minimize")
                    }
                });

                if (isWritable()) {
                    menu.push(generateColorList());
                    menu.push({
                        styleClass: "dropdown-item",
                        link: {
                            styleClass: "item-link delete",
                            href: "#",
                            text: AG.param.get("remove")
                        }
                    });
                }

                return menu;
            }

            return AJS.$.extend({
                menu: {
                    trigger: {
                        text: "Gadget menu",
                        href: "#"
                    },
                    list: {
                        items: generateMenuItems()
                    }
                }
            }, args);
        },

        dashboardMenu: function (args) {
           return args;
        }
    };


    return function (name, data) {
        var descriptor;

        if (AJS.$.isFunction(descriptors[name])) {
            descriptor = descriptors[name](data);
        } else if (descriptors[name]) {
            descriptor = descriptors[name];
        }
        else if(AJS.debug) {
            console.error("AG.render.getDescriptor: Could not find descriptor '" + name + "'");
        }

        if (descriptor) {
            return AJS.$.extend(descriptor, {
                useTemplate: name
            });
        }

    };

}();






