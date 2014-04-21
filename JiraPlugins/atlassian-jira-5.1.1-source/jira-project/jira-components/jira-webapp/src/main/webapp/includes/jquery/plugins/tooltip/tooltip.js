jQuery.fn.tooltip = function () {
    var defaults = {activeClass: "active", delay: 0.8};
    return function (options) {
        var tts = [];
        options = jQuery.extend(defaults, options);
        this.each(function(){
            var $this = jQuery(this);
            $this.extend($this,{
                showToolTip: function () {
                    if (!$this.hasClass(options.activeClass)) {
                       $this.showToolTip.timer = setTimeout(function (){
                           $this.addClass(options.activeClass);
                           if (options.onShow) {
                               options.onShow.call($this);
                           }
                       }, options.delay * 1000);
                    } else {
                       clearTimeout($this.hideToolTip.timer);
                    }
                },
                hideToolTip: function () {
                    if(!$this.hasClass(options.activeClass)) {
                       clearTimeout($this.showToolTip.timer);
                    } else {
                       $this.hideToolTip.timer = setTimeout(function (){
                           $this.removeClass(options.activeClass);
                           if (options.onHide) {
                               options.onHide.call($this);
                           }
                       }, options.delay * 1000);
                    }
                }
            });
            $this.click(function (){
                clearTimeout($this.showToolTip.timer);
                $this.removeClass(options.activeClass);
               if (options.onHide) {
                   options.onHide.call($this);
               }
            });
            $this.hover($this.showToolTip, $this.hideToolTip);
            tts.push($this);
        });
        return jQuery(tts);
    };
}();