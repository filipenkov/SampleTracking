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

var gadgets = gadgets || {};

gadgets.error = {};
gadgets.error.SUBCLASS_RESPONSIBILITY = 'subclass responsibility';
gadgets.error.TO_BE_DONE = 'to be done';

gadgets.log = function(message) {
  if (window.console && console.log) {
    console.log(message);
  } else {
    var logEntry = document.createElement('div');
    logEntry.className = 'gadgets-log-entry';
    logEntry.innerHTML = message;
    document.body.appendChild(logEntry);
  }
};


//----------
//Extensible

gadgets.Extensible = function() {
};

/**
* Sets the dependencies.
* @param {Object} dependencies Object whose properties are set on this
*     container as dependencies
*/
gadgets.Extensible.prototype.setDependencies = function(dependencies) {
    for (var p in dependencies) {
        this[p] = dependencies[p];
    }
};

/**
* Returns a dependency given its name.
* @param {String} name Name of dependency
* @return {Object} Dependency with that name or undefined if not found
*/
gadgets.Extensible.prototype.getDependencies = function(name) {
    return this[name];
};


//-------------
//UserPrefStore

/**
* User preference store interface.
* @constructor
*/
gadgets.UserPrefStore = function() {
};

/**
* Gets all user preferences of a gadget.
* @param {Object} gadget Gadget object
* @return {Object} All user preference of given gadget
*/
gadgets.UserPrefStore.prototype.getPrefs = function(gadget) {
throw Error(gadgets.error.SUBCLASS_RESPONSIBILITY);
};

/**
* Saves user preferences of a gadget in the store.
* @param {Object} gadget Gadget object
* @param {Object} prefs User preferences
*/
gadgets.UserPrefStore.prototype.savePrefs = function(gadget) {
throw Error(gadgets.error.SUBCLASS_RESPONSIBILITY);
};


//-------------
//DefaultUserPrefStore

/**
* User preference store implementation.
* TODO: Turn this into a real implementation that is production safe
* @constructor
*/
gadgets.DefaultUserPrefStore = function() {
gadgets.UserPrefStore.call(this);
};
gadgets.DefaultUserPrefStore.inherits(gadgets.UserPrefStore);

gadgets.DefaultUserPrefStore.prototype.getPrefs = function(gadget) { };

gadgets.DefaultUserPrefStore.prototype.savePrefs = function(gadget) { };


//-------------
//GadgetService

/**
* Interface of service provided to gadgets for resizing gadgets,
* setting title, etc.
* @constructor
*/
gadgets.GadgetService = function() {
};

gadgets.GadgetService.prototype.setHeight = function(elementId, height) {
    throw Error(gadgets.error.SUBCLASS_RESPONSIBILITY);
};

gadgets.GadgetService.prototype.setTitle = function(gadget, title) {
    throw Error(gadgets.error.SUBCLASS_RESPONSIBILITY);
};

gadgets.GadgetService.prototype.setUserPref = function(id) {
    throw Error(gadgets.error.SUBCLASS_RESPONSIBILITY);
};

//----------------
//IfrGadgetService

/**
* Base implementation of GadgetService.  This implementation does not implement setting the title or user prefs as it
* is meant for gadgets that are standalone on a page.  
* @constructor
*/
gadgets.IfrGadgetService = function() {
    gadgets.GadgetService.call(this);
    
    /**
     * This is a bit funky looking but we need to be sure we always call the right method, even if the implementation
     * has been overridden after an IfrGadgetService is instantiated.
     */
    var service = this;
    gadgets.rpc.register('resize_iframe', function() { service.setHeight.apply(this, arguments); });
    gadgets.rpc.register('set_pref', function() { service.setUserPref.apply(this, arguments); });
    gadgets.rpc.register('set_title', function() { service.setTitle.apply(this, arguments); });
    gadgets.rpc.register('requestNavigateTo', function() { service.requestNavigateTo.apply(this, arguments); });
};

gadgets.IfrGadgetService.inherits(gadgets.GadgetService);

gadgets.IfrGadgetService.prototype.setHeight = function(height) {
    if (height > gadgets.container.maxheight_) {
        height = gadgets.container.maxheight_;
    }

    var element = document.getElementById(this.f);
    if (element) {
        element.style.height = height + 'px';
    }
};
    
/**
* Navigates the page to a new url based on a gadgets requested view and
* parameters.
*/
gadgets.IfrGadgetService.prototype.requestNavigateTo = function(view, opt_params) {
    var id = this.getGadgetIdFromModuleId(this.f);
    var url = this.getUrlForView(view);

    if (opt_params) {
        var paramStr = JSON.stringify(opt_params);
        if (paramStr.length > 0) {
            url += '&appParams=' + encodeURIComponent(paramStr);
        }
    }

    if (url && document.location.href.indexOf(url) == -1) {
        document.location.href = url;
    }
};

/**
* This is a silly implementation that will need to be overriden by almost all
* real containers.
* TODO: Find a better default for this function
*
* @param view The view name to get the url for
*/
gadgets.IfrGadgetService.prototype.getUrlForView = function(view) {
    if (view === 'canvas') {
        return '/canvas';
    } else if (view === 'profile') {
        return '/profile';
    } else {
        return null;
    }
};

gadgets.IfrGadgetService.prototype.getGadgetIdFromModuleId = function(moduleId) {
    // Quick hack to extract the gadget id from module id
    return parseInt(moduleId.match(/_([0-9]+)$/)[1], 10);
};

//-------------
//LayoutManager

/**
* Layout manager interface.
* @constructor
*/
gadgets.LayoutManager = function() {
};

/**
* Gets the HTML element that is the chrome of a gadget into which the content
* of the gadget can be rendered.
* @param {Object} gadget Gadget instance
* @return {Object} HTML element that is the chrome for the given gadget
*/
gadgets.LayoutManager.prototype.getGadgetChrome = function(gadget) {
    throw Error(gadgets.error.SUBCLASS_RESPONSIBILITY);
};

//-------------------
//StaticLayoutManager

/**
* Static layout manager where gadget ids have a 1:1 mapping to chrome ids.
* @constructor
*/
gadgets.StaticLayoutManager = function() {
    gadgets.LayoutManager.call(this);
};

gadgets.StaticLayoutManager.inherits(gadgets.LayoutManager);

/**
* Sets chrome ids, whose indexes are gadget instance ids (starting from 0).
* @param {Array} gadgetIdToChromeIdMap Gadget id to chrome id map
*/
gadgets.StaticLayoutManager.prototype.setGadgetChromeIds = function(gadgetChromeIds) {
    this.gadgetChromeIds_ = gadgetChromeIds;
};

gadgets.StaticLayoutManager.prototype.getGadgetChrome = function(gadget) {
    var chromeId = this.gadgetChromeIds_[gadget.id];
    return chromeId ? document.getElementById(chromeId) : null;
};


//----------------------
//FloatLeftLayoutManager

/**
* FloatLeft layout manager where gadget ids have a 1:1 mapping to chrome ids.
* @constructor
* @param {String} layoutRootId Id of the element that is the parent of all
*     gadgets.
*/
gadgets.FloatLeftLayoutManager = function(layoutRootId) {
    gadgets.LayoutManager.call(this);
    this.layoutRootId_ = layoutRootId;
};

gadgets.FloatLeftLayoutManager.inherits(gadgets.LayoutManager);

gadgets.FloatLeftLayoutManager.prototype.getGadgetChrome = function(gadget) {
    var layoutRoot = document.getElementById(this.layoutRootId_);
    if (layoutRoot) {
        var chrome = document.createElement('div');
        chrome.className = 'gadgets-gadget-chrome';
        chrome.style.cssFloat = 'left';
        layoutRoot.appendChild(chrome);
        return chrome;
    } else {
        return null;
    }
};

