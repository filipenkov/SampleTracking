//this contains the code to handle the list of values for the multivalue
//CustomField type

/**
 * Functions for managing a list of values for a form element. The items
 * are represented by divs that contain the name of the value, a hidden
 * input for the item and a graphic for a remove button.
 * The object has a package prefix to ensure that this doesn't interfere
 * with other javascript functions from other customfields.
 * @param customFieldID This object / set of functions is for use with a
 *                      JIRA custom searcher. The customFieldID ensures that
 *                      multiple uses don't interfere with each other.
 */
new function() {
    var ValuesList_Hash = new Array();


    this.create = function(customFieldID, contextPath) {
        //check if it has been created
        if (typeof ValuesList_Hash[customFieldID] == 'undefined') {
            //lower case used as it's from the java package
            //noinspection JSLowercasedConstructorCall
            ValuesList_Hash[customFieldID]
                    = new ActualObject(customFieldID);
            ValuesList_Hash[customFieldID].setContextPath(contextPath);
        }
    };
    this.get = function(customFieldID) {
        return ValuesList_Hash[customFieldID];
    };

function ActualObject(customFieldID) {
    this.customFieldID = customFieldID;
    this.contextPath = '/'; //default

    this.setContextPath = function (path) {
        if (path) {
            if (path.charAt(path.length -1) != '/') {
                path = path + '/';
            }
            this.contextPath = path;
        } else {
            this.contextPath = '/';
        }
    };

    /*
     * returns the div that contains the values' tags
     */
    this.getContainerForValues = function () {
        return document.getElementById("display-values-" + this.customFieldID);
    };

    /*
     * Takes a String and adds it to the list of values searched for.
     * The current list of values is checked and a new tag is only
     * added if one doesn't already exist for the value.
     */
    this.addValue = function (value) {
        var container = this.getContainerForValues();
        if (value && value.trim().length > 0) {
            value = value.trim();
            var existingValueTag = this.getValuesTag(value);
            if (! existingValueTag) {
                //create the entry for this value
                var newValueTag = this.buildValuesTag(value);
                var valueInserted = false; //only add it once
                if (container.hasChildNodes()) {
                    //one or more children
                    var otherDivs = container.childNodes;
                    //find where to add the div to keep them in order
                    for (var j = 0; j < otherDivs.length && !valueInserted; j++) {
                        if (otherDivs[j] && otherDivs[j].id &&
                                otherDivs[j].id > newValueTag.id) {
                            container.insertBefore(newValueTag, otherDivs[j]);
                            valueInserted = true;
                        }
                    }
                }
                //if it was greater than all the existing values
                //(possibly because there weren't any other values)
                //then it needs to be added to the end
                if (!valueInserted) {
                    container.appendChild(newValueTag);
                }
            }
        }
        //ensure that the clear button is displayed
        this.displayClearButton(container.hasChildNodes());
    };

    /**
     * Takes a value as a String and returns the div tag that contains that
     * value in the on screen list. The value may have the customfield-id
     * as a prefix but it is not required.
     */
    this.getValuesTag = function (value) {
        if (value && value.trim().length > 0) {
            //add customfield identifier if necessary
            if (value.indexOf(this.customFieldID + "-") < 0) {
                value = this.customFieldID + "-" + value;
            }
            return document.getElementById("div-" + value);
        }
        return null;
    };

    /**
     * Creates the div that will represent the value in the on-screen list.
     */
    this.buildValuesTag = function (value) {
        var divTag = document.createElement("div");
        divTag.id = "div-" + this.customFieldID + "-" + value;
        divTag.className = "option-" + this.customFieldID;

        var image = document.createElement("img");
        image.alt = "Remove";
        image.src = "images/remove.png";
        image.id = this.customFieldID + "-" + value;
        image.width = 10;
        image.height = 10;
        image.align = "right";
        image.onclick = function (event) {
        	
        	org_jcvi_jira_plugins_customfield_multivalue_ValuesList.get(customFieldID).removeValue(event.target.id);
        };

        var input = document.createElement("input");
        input.type = "hidden";
        input.id = "input-" + this.customFieldID + "-" + value;
        input.name = this.customFieldID;
        input.value = value;

        //assemble the parts
        divTag.innerHTML = value;
        divTag.appendChild(input);
        divTag.appendChild(image);
        return divTag;
    };

    /*
     * Removes all of the values from the list searched for and the on-screen list.
     */
    this.clearAll = function () {
        this.getContainerForValues().innerHTML = "";
        this.displayClearButton(false);
    };

    this.displayClearButton = function (display) {
        var button = document.getElementById("clear-values-" + this.customFieldID);
        //don't keep setting display
        if (button) {
            var style = button.style.display;
            if (display && "block" != style) {
                button.style.display = "block";
            }
            if (!display && "none" != style) {
                button.style.display = "none";
            }
        }
    };

    /*
     * Takes a value, passed in as a String,  out of the list searched
     * for and removes it from the on screen list.
     */
    this.removeValue = function (value) {
        var container = this.getContainerForValues();
        var tag = this.getValuesTag(value);
        if (tag == null) {
            alert("value: '"+value+"' didn't match a tag");
            return;
        }
        container.removeChild(this.getValuesTag(value));
        //ensure that the clear button is removed at the right time
        this.displayClearButton(container.hasChildNodes());
    };

}
};
