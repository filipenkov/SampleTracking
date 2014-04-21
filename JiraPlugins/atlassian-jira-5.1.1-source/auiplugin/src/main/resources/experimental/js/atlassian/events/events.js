(function ($) {

    /**
     * Triggers a custom event on the AJS object
     *
     * @param {String} name - name of event
     * @param {Array} args - args for event handler
     */
    AJS.triggerEvt = function (name, args) {
        $(AJS).trigger(name, args);
    };
    
    /**
     * Binds handler to the AJS object
     *
     * @param {String} name
     * @param {Function} func
     */
    AJS.bindEvt = function (name, func) {
        $(AJS).bind(name, func);
    };
    
    /**
     * Some generic error handling that fires event in multiple contexts
     * - on AJS object
     * - on Instance
     * - on AJS object with prefixed id.
     *
     * @param evt
     * @param inst
     * @param args
     */
    AJS.triggerEvtForInst = function (evt, inst, args) {
        $(inst).trigger(evt, args);
        AJS.triggerEvt(evt, args);
        if (inst.id) {
            AJS.triggerEvt(inst.id + "_" + evt, args);
        }
    };
    
})(AJS.$);
