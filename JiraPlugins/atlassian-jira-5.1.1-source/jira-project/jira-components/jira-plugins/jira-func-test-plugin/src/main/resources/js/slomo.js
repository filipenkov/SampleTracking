JIRA.Slomo = {};
JIRA.Slomo.activate = function() {
    AJS.$("body").prepend(JIRA.Templates.Slomo.slomoUi());
    var $el = AJS.$("#slomo-ui");
    var form = AJS.$("#slomo-form"),
            range = AJS.$('#slomo-val'),
            reset = AJS.$('#slomo-reset'),
            button = form.find(":submit");

    function setDelayTo(delay) {
        button.val("Set speed to " + delay + "ms");
    }

    range.change(function() {
        setDelayTo(range.val());
    });

    var sendSlowmo = function () {
        AJS.$.ajax({
            url:contextPath + "/rest/func-test/1.0/systemproperty/atlassian.slomo" + "?" + form.serialize(),
            type:"POST"
        });
        $el.toggle();
    };

    reset.click(function(){
        range.val(0);
        sendSlowmo();
    });

    form.submit(function (e) {
        sendSlowmo();
        e.preventDefault();
    });

    function fetchFromServer()
    {
        AJS.$.ajax({
            url:contextPath + "/rest/func-test/1.0/systemproperty/atlassian.slomo",
            type:"GET",
            success:function (data)
            {
                var currentSpeed = data ? parseInt(data, 10) : 0;
                setDelayTo(currentSpeed);
                range.val(currentSpeed);
            }
        });
    }

    fetchFromServer();

    JIRA.Slomo.activate = function() { // from now on just toggle
        fetchFromServer();
        $el.toggle();
    }

};