/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

/**
 * Calls an array of asynchronous functions and calls the continuation
 * function when all are done.
 * @param {Array} functions Array of asynchronous functions, each taking
 *     one argument that is the continuation function that handles the result
 *     That is, each function is something like the following:
 *     function(continuation) {
 *       // compute result asynchronously
 *       continuation(result);
 *     }
 * @param {Function} continuation Function to call when all results are in.  It
 *     is pass an array of all results of all functions
 * @param {Object} opt_this Optional object used as "this" when calling each
 *     function
 */
gadgets.callAsyncAndJoin = function(functions, continuation, opt_this) {
  var pending = functions.length;
  var results = [];
  for (var i = 0; i < functions.length; i++) {
    // we need a wrapper here because i changes and we need one index
    // variable per closure
    var wrapper = function(index) {
      functions[index].call(opt_this, function(result) {
        results[index] = result;
        if (--pending == 0) {
          continuation(results);
        }
      });
    };
    wrapper(i);
  }
};

// ----------------
// IfrGadgetService

/**
 * Implementation of setTitle for IfrGadgetService.
 */
gadgets.IfrGadgetService.prototype.setTitle = function(title) {
    jQuery('#' + this.f + '-chrome .dashboard-item-title').text(title);
};

/**
 * Implementation of setHeight for IfrGadgetService.
 */
gadgets.IfrGadgetService.prototype.setHeight = function(height) {
    if (height > gadgets.container.maxheight_) {
        height = gadgets.container.maxheight_;
    }

    var element = document.getElementById(this.f);
    if (element) {
        element.style.height = height + 'px';
    }

    AG.Cookie.save(this.f + "-fh", height);

    // As gadgets are absolutely positioned we need to resize the dashboard chrome whenever a gadgets height is changed.
    // We do this by firing an event, see AG.DashboardManager for handling.
    jQuery(AG).trigger("AG.iframeResize", [element, height]);
};

/**
* Sets one or more user preferences
* @param {String} editToken
* @param {String} name Name of user preference
* @param {String} value Value of user preference
* More names and values may follow
*/
gadgets.IfrGadgetService.prototype.setUserPref = function(editToken, name, value) {
    var nameValues = Array.prototype.slice.call(arguments, 1);
    AJS.$("#" + this.f).trigger("setUserPref", nameValues);
};
