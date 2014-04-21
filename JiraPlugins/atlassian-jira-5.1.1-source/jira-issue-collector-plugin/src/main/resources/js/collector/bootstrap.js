
(function($) {
    // Import jQuery to local scope
    $ = jQuery.noConflict(true);
    // Leave jQuery on the window if it was just removed from it.
    if (typeof window.jQuery === "undefined") window.jQuery = $;
    if (typeof window.$ === "undefined") window.$ = $;

    // Create the ATL_JQ object that the IssueDialog will be attached to.
    // Legacy: The ATL_JQ object masquerades as jQuery.
    var ATL_JQ = function() {
        return $.apply($, arguments);
    };

    var baseUrl = "@baseUrl";
    var css = ".atlwdg-blanket {background:#000;height:100%;left:0;opacity:.5;position:fixed;top:0;width:100%;z-index:1000000;}\n.atlwdg-popup {background:#fff;border:1px solid #666;position:fixed;top:50%;left:50%;z-index:10000011;}\n.atlwdg-popup.atlwdg-box-shadow {-moz-box-shadow:10px 10px 20px rgba(0,0,0,0.5);-webkit-box-shadow:10px 10px 20px rgba(0,0,0,0.5);box-shadow:10px 10px 20px rgba(0,0,0,0.5);background-color:#fff;}\n.atlwdg-hidden {display:none;}\n.atlwdg-trigger {position: fixed; background: #013466; padding: 5px;border: 2px solid white;border-top: none; font-weight: bold; color: white; display:block;white-space:nowrap;text-decoration:none; font-family:arial, FreeSans, Helvetica, sans-serif;font-size:12px;box-shadow: 5px 5px 10px rgba(0, 0, 0, 0.5);-webkit-box-shadow:5px 5px 10px rgba(0, 0, 0, 0.5); -moz-box-shadow:5px 5px 10px rgba(0, 0, 0, 0.5);border-radius: 0 0 5px 5px; -moz-border-radius: 0 0 5px 5px;}\na.atlwdg-trigger {text-decoration:none;}\n.atlwdg-trigger.atlwdg-TOP {left: 45%;top:0; }\n.atlwdg-trigger.atlwdg-RIGHT {left:100%; top:40%; -webkit-transform-origin:top left; -webkit-transform: rotate(90deg); -moz-transform: rotate(90deg); -moz-transform-origin:top left;-ms-transform: rotate(90deg); -ms-transform-origin:top left; }\n.atlwdg-trigger.atlwdg-SUBTLE { right:0; bottom:0; border: 1px solid #ccc; border-bottom: none; border-right: none; background-color: #f5f5f5; color: #444; font-size: 11px; padding: 6px; box-shadow: -1px -1px 2px rgba(0, 0, 0, 0.5); border-radius: 2px 0 0 0; }\n.atlwdg-loading {position:absolute;top:220px;left:295px;}";
    var cssIE = ".atlwdg-trigger {position:absolute;}\n.atlwdg-blanket {position:absolute;filter:alpha(opacity=50);width:110%;}\n.atlwdg-popup {position:absolute;}\n.atlwdg-trigger.atlwdg-RIGHT { left:auto;right:0; filter: progid:DXImageTransform.Microsoft.BasicImage(rotation=1); }";

    ATL_JQ.isQuirksMode = function() {
        return document.compatMode !='CSS1Compat';
    };

    ATL_JQ.IssueDialog = function(options) {
        var $body = $("body"), that = this, showDialog = function() {
            that.show();
            return false;
        };
        this.options = options;
        this.frameUrl = baseUrl + "/rest/collectors/1.0/template/form/" + this.options.collectorId + "?os_authType=none";

        $("head").append("<style type='text/css'>" + css + "</style>");

        var triggerPosition = "atlwdg-";
        if(typeof this.options.triggerPosition !== "function") {
            triggerPosition += this.options.triggerPosition;
        }

        var $trigger = $("<a href='#' id='atlwdg-trigger'/>").addClass("atlwdg-trigger " + triggerPosition).text(this.options.triggerText);
        var $iframeContainer = $("<div id='atlwdg-container'/>").addClass("atlwdg-popup atlwdg-box-shadow atlwdg-hidden");
        var $blanket = $("<div id='atlwdg-blanket' class='atlwdg-blanket'/>").hide();


        if(typeof this.options.triggerPosition === "function") {
            this.options.triggerPosition(showDialog);
        } else {
            $body.append($trigger);
            $trigger.click(showDialog);
        }
        $body.append($blanket).append($iframeContainer);

        //Need to add IE specific behaviour for all versions of IE including IE9 when in Quirksmode!
        if($.browser.msie && (ATL_JQ.isQuirksMode() || $.browser.version < 9)) {
            $("head").append("<style type='text/css'>" + cssIE + "</style>");
            //fixed position ain't working in IE so need to do this shit manually :(
            var triggerAdjuster = function(e) {};
            if(this.options.triggerPosition === "TOP") {
                triggerAdjuster = function(e) {
                    $("#atlwdg-trigger").css("top", $(window).scrollTop() + "px");
                };
            } else if(this.options.triggerPosition === "RIGHT") {
                triggerAdjuster = function(e) {
                    var $trigger = $("#atlwdg-trigger");
                    $trigger.css("top", ($(window).height()/2 - $trigger.outerWidth()/2 + $(window).scrollTop()) + "px");
                    //IE8 is especially borked in standards mode :(
                    if(!ATL_JQ.isQuirksMode() && $.browser.version === "8.0") {
                        $trigger.css("right", - ($trigger.outerHeight() - $trigger.outerWidth()) + "px");
                    }
                }
            } else if(this.options.triggerPosition === "SUBTLE") {
                var outerHeight = $trigger.outerHeight();
                triggerAdjuster = function(e) {
                    var $window = $(window);
                    $trigger.css("top", ($window.scrollTop() + $window.height() - outerHeight) + "px");
                };
            }

            $(window).bind("scroll resize", triggerAdjuster);
            triggerAdjuster();
        }
    };

    ATL_JQ.IssueDialog.prototype = {
        hideDialog:undefined,

        updateContainerPosition: function() {
            var width = 810, height=450;
            $("#atlwdg-container").css({
                "height":height + "px",
                "width":width + "px",
                "margin-top": - Math.round(height / 2) +"px",
                "margin-left": - Math.round(width / 2) + "px"
            });

            $("#atlwdg-frame").height("100%").width("100%");
        },

        show: function() {
            var that = this,
                $iframeContainerElem = $("#atlwdg-container"),
                $body = $("body"),
                $iframeElem = $('<iframe id="atlwdg-frame" frameborder="0" src="'+this.frameUrl+'"></iframe>'),
                $loadingImage = $('<img class="atlwdg-loading" style="display:none;" src="@baseUrl/images/throbber/loading_barber_pole_horz.gif">');

            hideDialog = function(e) {
                if (e.keyCode === 27) {
                    that.hide();
                }
            };

            $iframeContainerElem.append($loadingImage);
            var throbberTimeout = setTimeout(function() {
                $loadingImage.show();
            }, 300);

            //get rid of scrollbars
            $body.css("overflow", "hidden").keydown(hideDialog);
            window.scroll(0,0);

            var feedbackString = "";
            if(this.options.collectFeedback) {
                var feedback = this.options.collectFeedback();
                for(var prop in feedback) {
                    if(feedback.hasOwnProperty(prop) && feedback[prop] !== undefined && feedback[prop] !== "" && typeof feedback[prop] === "string") {
                        feedbackString += "*" + prop + "*: " + feedback[prop] + "\n";
                    }
                }
            }
            var fieldValues = {};
            if (this.options.fieldValues && !$.isEmptyObject(this.options.fieldValues)) {
                ATL_JQ.extend(fieldValues, this.options.fieldValues);
            }
            $iframeElem.load(function() {
                var message = {
                    "feedbackString": feedbackString,
                    "fieldValues": fieldValues
                };

                $iframeElem[0].contentWindow.postMessage(JSON.stringify(message), "@baseUrl");
                $(window).bind("message", function(e) {
                    if(e.originalEvent.data && e.originalEvent.data === "cancelFeedbackDialog") {
                        that.hide();
                    }
                })
            });

			$iframeElem.load(function(e) {
                clearTimeout(throbberTimeout);
                $loadingImage.hide();
                $iframeElem.show();
            });

			var currentMilis = new Date().getTime();
			var dummyElement = document.createElement('a');
			dummyElement.href = "@baseUrl";

			$iframeContainerElem.append($iframeElem);

			this.updateContainerPosition();

            $iframeContainerElem.show();
            $("#atlwdg-blanket").show();
		},
        
        hide: function() {
            //return scrollbars
            $("body").css("overflow", "auto").unbind("keydown", hideDialog);
            $("#atlwdg-container").hide().empty();
            $("#atlwdg-blanket").hide();
        }
    };

    // Remove any non-string properties from the given object.
    var filterStrings = function(obj) {
        for (var key in obj) {
            if (typeof obj[key] !== "string") {
                console.log("bootstrap.js:filterStrings ignoring key for value '" + key + "'; typeof must be string");
                delete obj[key];
            }
        }
        return obj;
    };

    //these @properties are substituted by the Collector(WebResource)Transformer
    if("@collectorId" !== "") {
        var collectFeedback = false;
        var defaultFieldValues = {};
        if(@shouldCollectFeedback) {
            var environmentProps =  {
                "Location":window.location.href,
                "User-Agent":navigator.userAgent,
                "Referrer":document.referrer,
                "Screen Resolution":screen.width + " x " + screen.height
            };

            //users can add additional properties to capture.
            if(window.ATL_JQ_PAGE_PROPS) {
                var feedbackProps = window.ATL_JQ_PAGE_PROPS.environment;
                defaultFieldValues = window.ATL_JQ_PAGE_PROPS.fieldValues;
                //check if there's collector specific properties. They should override global ones!
                //This is so that all this will still work with 2 collectors on the same page.
                if(window.ATL_JQ_PAGE_PROPS.hasOwnProperty("@collectorId")) {
                    feedbackProps = window.ATL_JQ_PAGE_PROPS["@collectorId"];
                    defaultFieldValues = feedbackProps.fieldValues;
                }

                if($.isFunction(feedbackProps)) {
                    $.extend(environmentProps, feedbackProps());
                } else {
                    $.extend(environmentProps, feedbackProps);
                }

                // clients can specify default values for fields.
                if ($.isFunction(defaultFieldValues)) {
                    $.extend(defaultFieldValues, filterStrings(defaultFieldValues()));
                } else if ($.isPlainObject(defaultFieldValues)) {
                    $.extend(defaultFieldValues, filterStrings(defaultFieldValues));
                }
            }

            collectFeedback = function() {
                return environmentProps;
            };
        }

        ATL_JQ(function() {
            new ATL_JQ.IssueDialog({
                collectorId:"@collectorId",
                collectFeedback: collectFeedback,
                fieldValues: defaultFieldValues,
                triggerText: "@triggerText",
                triggerPosition: @triggerPosition
            });
        });
    } else {
        //this is here for backwards compatibility if the collector is included in a page without the ?collectorId=
        //appended to the URL and the javascript inline.
        window.ATL_JQ = ATL_JQ;
    }
})(jQuery);

// json2.js

var JSON;
if (!JSON) {
	JSON = {};
}

(function () {
	'use strict';

	function f(n) {
		// Format integers to have at least two digits.
		return n < 10 ? '0' + n : n;
	}

	if (typeof Date.prototype.toJSON !== 'function') {

		Date.prototype.toJSON = function (key) {

			return isFinite(this.valueOf())
					? this.getUTCFullYear()     + '-' +
					f(this.getUTCMonth() + 1) + '-' +
					f(this.getUTCDate())      + 'T' +
					f(this.getUTCHours())     + ':' +
					f(this.getUTCMinutes())   + ':' +
					f(this.getUTCSeconds())   + 'Z'
					: null;
		};

		String.prototype.toJSON      =
				Number.prototype.toJSON  =
						Boolean.prototype.toJSON = function (key) {
							return this.valueOf();
						};
	}

	var cx = /[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
			escapable = /[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
			gap,
			indent,
			meta = {    // table of character substitutions
				'\b': '\\b',
				'\t': '\\t',
				'\n': '\\n',
				'\f': '\\f',
				'\r': '\\r',
				'"' : '\\"',
				'\\': '\\\\'
			},
			rep;


	function quote(string) {

// If the string contains no control characters, no quote characters, and no
// backslash characters, then we can safely slap some quotes around it.
// Otherwise we must also replace the offending characters with safe escape
// sequences.

		escapable.lastIndex = 0;
		return escapable.test(string) ? '"' + string.replace(escapable, function (a) {
			var c = meta[a];
			return typeof c === 'string'
					? c
					: '\\u' + ('0000' + a.charCodeAt(0).toString(16)).slice(-4);
		}) + '"' : '"' + string + '"';
	}


	function str(key, holder) {

// Produce a string from holder[key].

		var i,          // The loop counter.
				k,          // The member key.
				v,          // The member value.
				length,
				mind = gap,
				partial,
				value = holder[key];

// If the value has a toJSON method, call it to obtain a replacement value.

		if (value && typeof value === 'object' &&
				typeof value.toJSON === 'function') {
			value = value.toJSON(key);
		}

// If we were called with a replacer function, then call the replacer to
// obtain a replacement value.

		if (typeof rep === 'function') {
			value = rep.call(holder, key, value);
		}

// What happens next depends on the value's type.

		switch (typeof value) {
			case 'string':
				return quote(value);

			case 'number':

// JSON numbers must be finite. Encode non-finite numbers as null.

				return isFinite(value) ? String(value) : 'null';

			case 'boolean':
			case 'null':

// If the value is a boolean or null, convert it to a string. Note:
// typeof null does not produce 'null'. The case is included here in
// the remote chance that this gets fixed someday.

				return String(value);

// If the type is 'object', we might be dealing with an object or an array or
// null.

			case 'object':

// Due to a specification blunder in ECMAScript, typeof null is 'object',
// so watch out for that case.

				if (!value) {
					return 'null';
				}

// Make an array to hold the partial results of stringifying this object value.

				gap += indent;
				partial = [];

// Is the value an array?

				if (Object.prototype.toString.apply(value) === '[object Array]') {

// The value is an array. Stringify every element. Use null as a placeholder
// for non-JSON values.

					length = value.length;
					for (i = 0; i < length; i += 1) {
						partial[i] = str(i, value) || 'null';
					}

// Join all of the elements together, separated with commas, and wrap them in
// brackets.

					v = partial.length === 0
							? '[]'
							: gap
							? '[\n' + gap + partial.join(',\n' + gap) + '\n' + mind + ']'
							: '[' + partial.join(',') + ']';
					gap = mind;
					return v;
				}

// If the replacer is an array, use it to select the members to be stringified.

				if (rep && typeof rep === 'object') {
					length = rep.length;
					for (i = 0; i < length; i += 1) {
						if (typeof rep[i] === 'string') {
							k = rep[i];
							v = str(k, value);
							if (v) {
								partial.push(quote(k) + (gap ? ': ' : ':') + v);
							}
						}
					}
				} else {

// Otherwise, iterate through all of the keys in the object.

					for (k in value) {
						if (Object.prototype.hasOwnProperty.call(value, k)) {
							v = str(k, value);
							if (v) {
								partial.push(quote(k) + (gap ? ': ' : ':') + v);
							}
						}
					}
				}

// Join all of the member texts together, separated with commas,
// and wrap them in braces.

				v = partial.length === 0
						? '{}'
						: gap
						? '{\n' + gap + partial.join(',\n' + gap) + '\n' + mind + '}'
						: '{' + partial.join(',') + '}';
				gap = mind;
				return v;
		}
	}

// If the JSON object does not yet have a stringify method, give it one.

	if (typeof JSON.stringify !== 'function') {
		JSON.stringify = function (value, replacer, space) {

// The stringify method takes a value and an optional replacer, and an optional
// space parameter, and returns a JSON text. The replacer can be a function
// that can replace values, or an array of strings that will select the keys.
// A default replacer method can be provided. Use of the space parameter can
// produce text that is more easily readable.

			var i;
			gap = '';
			indent = '';

// If the space parameter is a number, make an indent string containing that
// many spaces.

			if (typeof space === 'number') {
				for (i = 0; i < space; i += 1) {
					indent += ' ';
				}

// If the space parameter is a string, it will be used as the indent string.

			} else if (typeof space === 'string') {
				indent = space;
			}

// If there is a replacer, it must be a function or an array.
// Otherwise, throw an error.

			rep = replacer;
			if (replacer && typeof replacer !== 'function' &&
					(typeof replacer !== 'object' ||
							typeof replacer.length !== 'number')) {
				throw new Error('JSON.stringify');
			}

// Make a fake root object containing our value under the key of ''.
// Return the result of stringifying the value.

			return str('', {'': value});
		};
	}


// If the JSON object does not yet have a parse method, give it one.

	if (typeof JSON.parse !== 'function') {
		JSON.parse = function (text, reviver) {

// The parse method takes a text and an optional reviver function, and returns
// a JavaScript value if the text is a valid JSON text.

			var j;

			function walk(holder, key) {

// The walk method is used to recursively walk the resulting structure so
// that modifications can be made.

				var k, v, value = holder[key];
				if (value && typeof value === 'object') {
					for (k in value) {
						if (Object.prototype.hasOwnProperty.call(value, k)) {
							v = walk(value, k);
							if (v !== undefined) {
								value[k] = v;
							} else {
								delete value[k];
							}
						}
					}
				}
				return reviver.call(holder, key, value);
			}


// Parsing happens in four stages. In the first stage, we replace certain
// Unicode characters with escape sequences. JavaScript handles many characters
// incorrectly, either silently deleting them, or treating them as line endings.

			text = String(text);
			cx.lastIndex = 0;
			if (cx.test(text)) {
				text = text.replace(cx, function (a) {
					return '\\u' +
							('0000' + a.charCodeAt(0).toString(16)).slice(-4);
				});
			}

// In the second stage, we run the text against regular expressions that look
// for non-JSON patterns. We are especially concerned with '()' and 'new'
// because they can cause invocation, and '=' because it can cause mutation.
// But just to be safe, we want to reject all unexpected forms.

// We split the second stage into 4 regexp operations in order to work around
// crippling inefficiencies in IE's and Safari's regexp engines. First we
// replace the JSON backslash pairs with '@' (a non-JSON character). Second, we
// replace all simple value tokens with ']' characters. Third, we delete all
// open brackets that follow a colon or comma or that begin the text. Finally,
// we look to see that the remaining characters are only whitespace or ']' or
// ',' or ':' or '{' or '}'. If that is so, then the text is safe for eval.

			if (/^[\],:{}\s]*$/
					.test(text.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g, '@')
					.replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, ']')
					.replace(/(?:^|:|,)(?:\s*\[)+/g, ''))) {

// In the third stage we use the eval function to compile the text into a
// JavaScript structure. The '{' operator is subject to a syntactic ambiguity
// in JavaScript: it can begin a block or an object literal. We wrap the text
// in parens to eliminate the ambiguity.

				j = eval('(' + text + ')');

// In the optional fourth stage, we recursively walk the new structure, passing
// each name/value pair to a reviver function for possible transformation.

				return typeof reviver === 'function'
						? walk({'': j}, '')
						: j;
			}

// If the text is not JSON parseable, then a SyntaxError is thrown.

			throw new SyntaxError('JSON.parse');
		};
	}
}());
