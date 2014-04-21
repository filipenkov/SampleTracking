var atlassian = atlassian || {};

atlassian.util = function() {

    var config = config || {};

    function init(configuration) {
        config = configuration["atlassian.util"];
    }

    var requiredConfig = {
        baseUrl: gadgets.config.NonEmptyStringValidator
    };
    gadgets.config.register("atlassian.util", requiredConfig, init);

    return {
      getRendererBaseUrl : function() {
        return config.baseUrl;  
      }
    };

}();