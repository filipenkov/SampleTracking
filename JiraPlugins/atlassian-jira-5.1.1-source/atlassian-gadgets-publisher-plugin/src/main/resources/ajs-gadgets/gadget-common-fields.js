AJS.$.namespace("AJS.gadget.fields");

AJS.gadget.fields.period = function(gadget, userpref){
    return {
        userpref: userpref,
        label: gadget.getMsg("gadget.common.period.name.label"),
        description:gadget.getMsg("gadget.common.period.name.description"),
        type: "select",
        selected: gadget.getPref(userpref),
        options:[
            {
                label:gadget.getMsg("gadget.common.period.hourly"),
                value:"hourly"
            },
            {
                label:gadget.getMsg("gadget.common.period.daily"),
                value:"daily"
            },
            {
                label:gadget.getMsg("gadget.common.period.weekly"),
                value:"weekly"
            },
            {
                label:gadget.getMsg("gadget.common.period.monthly"),
                value:"monthly"
            },
            {
                label:gadget.getMsg("gadget.common.period.quarterly"),
                value:"quarterly"
            },
            {
                label:gadget.getMsg("gadget.common.period.yearly"),
                value:"yearly"
            }
        ]
    };
};

AJS.gadget.fields.days = function(gadget, userpref, optMsgKeys){
    return {
        userpref: userpref,
        label: gadget.getMsg(optMsgKeys && optMsgKeys.label ? optMsgKeys.label : "gadget.common.days.label"),
        description: gadget.getMsg(optMsgKeys && optMsgKeys.description ? optMsgKeys.description : "gadget.common.days.description"),
        type: "text",
        value: gadget.getPref(userpref)
    };
};

AJS.gadget.fields.numberToShow = function(gadget, userpref){
    return {
        userpref: userpref,
        label: gadget.getMsg("gadget.common.num.label"),
        description:gadget.getMsg("gadget.common.num.description"),
        type: "text",
        value: gadget.getPref(userpref)
    };
};

AJS.gadget.fields.cumulative = function(gadget, userpref){
    return {
        userpref: userpref,
        label: gadget.getMsg("gadget.common.cumulative.label"),
        description:gadget.getMsg("gadget.common.cumulative.description"),
        type: "select",
        selected: gadget.getPref(userpref),
        options:[
            {
                label:gadget.getMsg("gadget.common.yes"),
                value:"true"
            },
            {
                label:gadget.getMsg("gadget.common.no"),
                value:"false"
            }
        ]
    };
};

AJS.gadget.fields.nowConfigured = function(){
    return {
        userpref: "isConfigured",
        type: "hidden",
        value: "true"
    };
};

AJS.gadget.fields.applyOverLabel = function(overLabelId){
    AJS.$("#" + overLabelId).each(function (){
        var label = AJS.$(this).removeClass("overlabel").addClass("overlabel-apply").click(function(){
            AJS.$("#" + AJS.$(this).attr("for")).focus();
        });
        var field = AJS.$("#" + label.attr("for")).focus(function(){
            label.hide();
        }).blur(function(){
            if (AJS.$(this).val() === ""){
                label.show();
            }
        });
        if (field.val() !== ""){
            label.hide();
        }
    });
};
