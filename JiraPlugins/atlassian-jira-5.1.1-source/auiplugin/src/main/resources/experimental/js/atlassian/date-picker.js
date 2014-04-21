(function ($) {

    'use strict';

    var datePickerCounter = 0;

    AJS.DatePicker = function (field, options) {

        var datePicker, initPolyfill, $field, datePickerUUID;

        datePicker = {};

        datePickerUUID = datePickerCounter++;

        // ---------------------------------------------------------------------
        // fix up arguments ----------------------------------------------------
        // ---------------------------------------------------------------------

        $field = $(field);
        $field.data("aui-dp-uuid", datePickerUUID);
        options = $.extend(undefined, AJS.DatePicker.prototype.defaultOptions, options);

        // ---------------------------------------------------------------------
        // expose arguments with getters ---------------------------------------
        // ---------------------------------------------------------------------

        datePicker.getField = function () {
            return $field;
        };

        datePicker.getOptions = function () {
            return options;
        };

        // ---------------------------------------------------------------------
        // exposed methods -----------------------------------------------------
        // ---------------------------------------------------------------------

        initPolyfill = function () {

            var calendar, handleDatePickerFocus, handleFieldBlur, handleFieldFocus,
                    handleFieldUpdate, initCalendar, isForcingHide, isSuppressingShow,
                    isTrackingDatePickerFocus, popup, popupContents;

            // -----------------------------------------------------------------
            // expose methods for controlling the popup ------------------------
            // -----------------------------------------------------------------

            datePicker.hide = function () {
                isForcingHide = true;
                popup.hide();
                isForcingHide = false;
            };

            datePicker.show = function () {
                popup.show();
            };

            // -----------------------------------------------------------------
            // initialise the calendar -----------------------------------------
            // -----------------------------------------------------------------

            initCalendar = function () {

                popupContents.off();
                calendar = $('<div/>');
                calendar.data("aui-dp-popup-uuid", datePickerUUID);
                popupContents.append(calendar);

                var config = {
                    'dateFormat': $.datepicker.W3C, // same as $.datepicker.ISO_8601
                    'defaultDate': $field.val(),
                    'maxDate': $field.attr('max'),
                    'minDate': $field.attr('min'),
                    'nextText': '>',
                    'onSelect': function (dateText, inst) {
                        $field.val(dateText);
                        datePicker.hide();
                        isSuppressingShow = true;
                        $field.focus();
                    },
                    'prevText': '<'
                };

                if (!(options.languageCode in AJS.DatePicker.prototype.localisations)) {
                    options.languageCode = '';
                }
                $.extend(config, AJS.DatePicker.prototype.localisations[options.languageCode]);

                if (options.firstDay > -1) {
                    config.firstDay = options.firstDay;
                }

                if (typeof $field.attr('step') !== 'undefined') {
                    AJS.log('WARNING: The AJS date picker polyfill currently does not support the step attribute!');
                }

                calendar.datepicker(config);

                // bind additional field processing events
                $field.on('blur', handleFieldBlur);
                $field.on('propertychange keyup input paste', handleFieldUpdate);

            };

            // -----------------------------------------------------------------
            // event handler wrappers ------------------------------------------
            // -----------------------------------------------------------------

            handleDatePickerFocus = function (event) {
                var $eventTarget = $(event.target);
                if (!($eventTarget.closest(popupContents).length || $eventTarget.is($field))) {
                    if ($eventTarget.closest('body').length) {
                        datePicker.hide();
                    }
                }
            };

            handleFieldBlur = function (event) {
                if (!(isTrackingDatePickerFocus)) {
                    // hide the date picker if whatever's got focus isn't inside our popup, but does exist inside the page
                    // MSIE fucks this up by somehow only firing the SECOND time an element is focused, but what can you do?
                    $('body').on('focus blur click mousedown', '*', handleDatePickerFocus);
                    isTrackingDatePickerFocus = true;
                }
            };

            handleFieldFocus = function (event) {
                if (!(isSuppressingShow)) {
                    datePicker.show();
                } else {
                    isSuppressingShow = false;
                }
            };

            handleFieldUpdate = function (event) {
                calendar.datepicker('setDate', $field.val());
                calendar.datepicker('option', {
                    'maxDate': $field.attr('max'),
                    'minDate': $field.attr('min')
                });
            };

            // -----------------------------------------------------------------
            // undo (almost) everything ----------------------------------------
            // -----------------------------------------------------------------

            datePicker.destroyPolyfill = function () {

                // goodbye, cruel world!
                datePicker.hide();

                $field.attr('placeholder', null);

                $field.off('propertychange keyup input paste', handleFieldUpdate);
                $field.off('focus click', handleFieldFocus);
                $field.off('blur', handleFieldBlur);

                $field.attr('type', 'date');

                if (typeof calendar !== 'undefined') {
                    calendar.datepicker('destroy');
                }

                // TODO: figure out a way to tear down the popup (if necessary)

                delete datePicker.destroyPolyfill;

                delete datePicker.show;
                delete datePicker.hide;

            };

            // -----------------------------------------------------------------
            // polyfill bootstrap ----------------------------------------------
            // -----------------------------------------------------------------

            isForcingHide = false; // used in conjunction with the inline dialog pre-hide arbitrator callback to suppress popup-initiated hide attempts
            isSuppressingShow = false; // used to stop the popover from showing when focus is restored to the field after a date has been selected
            isTrackingDatePickerFocus = false; // used to prevent multiple bindings of handleDatePickerFocus within handleFieldBlur

            popup = AJS.InlineDialog($field, undefined, function (contents, trigger, showPopup) {
                if (typeof calendar === 'undefined') {
                    popupContents = contents;
                    initCalendar();
                }
                showPopup();
            }, {
                'hideCallback': function () {
                    $('body').off('focus blur click mousedown', '*', handleDatePickerFocus);
                    isTrackingDatePickerFocus = false;
                },
                'hideDelay': null,
                'noBind': true,
                'preHideCallback': function () {
                    return isForcingHide;
                },
                'width': 300
            });

            // bind what we need to start off with
            $field.on('focus click', handleFieldFocus); // the click is for fucking opera... Y U NO FIRE FOCUS EVENTS PROPERLY???

            // give users a hint that this is a date field; note that placeholder isn't technically a valid attribute
            // according to the spec...
            $field.attr('placeholder', 'YYYY-MM-DD');

            // override the browser's default date field implementation (if applicable)
            // since IE doesn't support date input fields, we should be fine...
            if (options.overrideBrowserDefault && AJS.DatePicker.prototype.browserSupportsDateField) {
                $field[0].type = 'text';
            }

        };

        datePicker.reset = function () {

            if (typeof datePicker.destroyPolyfill === 'function') {
                datePicker.destroyPolyfill();
            }

            if ((!(AJS.DatePicker.prototype.browserSupportsDateField)) || options.overrideBrowserDefault) {
                initPolyfill();
            }

        };

        // ---------------------------------------------------------------------
        // bootstrap -----------------------------------------------------------
        // ---------------------------------------------------------------------

        datePicker.reset();

        return datePicker;

    };

    // -------------------------------------------------------------------------
    // things that should be common --------------------------------------------
    // -------------------------------------------------------------------------

    AJS.DatePicker.prototype.browserSupportsDateField = ($('<input type="date" />')[0].type === 'date');

    AJS.DatePicker.prototype.defaultOptions = {
        overrideBrowserDefault: false,
        firstDay: -1,
        languageCode: jQuery('html').attr('lang') || 'en-AU'
    };

    // adapted from the jQuery UI Datepicker widget (v1.8.16), with the following changes:
    //   - dayNamesShort -> dayNamesMin
    //   - unnecessary attributes omitted
    AJS.DatePicker.prototype.localisations = {
        "": {
            "dayNamesMin": ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"],
            "firstDay": 0,
            "isRTL": false,
            "monthNames": ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "af": {
            "dayNamesMin": ["Son", "Maa", "Din", "Woe", "Don", "Vry", "Sat"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Januarie", "Februarie", "Maart", "April", "Mei", "Junie", "Julie", "Augustus", "September", "Oktober", "November", "Desember"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "ar-DZ": {
            "dayNamesMin": ["الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت"],
            "firstDay": 6,
            "isRTL": true,
            "monthNames": ["جانفي", "فيفري", "مارس", "أفريل", "ماي", "جوان", "جويلية", "أوت", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "ar": {
            "dayNamesMin": ["الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت"],
            "firstDay": 6,
            "isRTL": true,
            "monthNames": ["كانون الثاني", "شباط", "آذار", "نيسان", "مايو", "حزيران", "تموز", "آب", "أيلول", "تشرين الأول", "تشرين الثاني", "كانون الأول"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "az": {
            "dayNamesMin": ["B", "Be", "Ça", "Ç", "Ca", "C", "Ş"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Yanvar", "Fevral", "Mart", "Aprel", "May", "İyun", "İyul", "Avqust", "Sentyabr", "Oktyabr", "Noyabr", "Dekabr"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "bg": {
            "dayNamesMin": ["Нед", "Пон", "Вто", "Сря", "Чет", "Пет", "Съб"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Януари", "Февруари", "Март", "Април", "Май", "Юни", "Юли", "Август", "Септември", "Октомври", "Ноември", "Декември"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "bs": {
            "dayNamesMin": ["Ned", "Pon", "Uto", "Sri", "Čet", "Pet", "Sub"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Januar", "Februar", "Mart", "April", "Maj", "Juni", "Juli", "August", "Septembar", "Oktobar", "Novembar", "Decembar"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "ca": {
            "dayNamesMin": ["Dug", "Dln", "Dmt", "Dmc", "Djs", "Dvn", "Dsb"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Gener", "Febrer", "Mar&ccedil;", "Abril", "Maig", "Juny", "Juliol", "Agost", "Setembre", "Octubre", "Novembre", "Desembre"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "cs": {
            "dayNamesMin": ["ne", "po", "út", "st", "čt", "pá", "so"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["leden", "únor", "březen", "duben", "květen", "červen", "červenec", "srpen", "září", "říjen", "listopad", "prosinec"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "da": {
            "dayNamesMin": ["Søn", "Man", "Tir", "Ons", "Tor", "Fre", "Lør"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Januar", "Februar", "Marts", "April", "Maj", "Juni", "Juli", "August", "September", "Oktober", "November", "December"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "de": {
            "dayNamesMin": ["So", "Mo", "Di", "Mi", "Do", "Fr", "Sa"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "el": {
            "dayNamesMin": ["Κυρ", "Δευ", "Τρι", "Τετ", "Πεμ", "Παρ", "Σαβ"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Ιανουάριος", "Φεβρουάριος", "Μάρτιος", "Απρίλιος", "Μάιος", "Ιούνιος", "Ιούλιος", "Αύγουστος", "Σεπτέμβριος", "Οκτώβριος", "Νοέμβριος", "Δεκέμβριος"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "en-AU": {
            "dayNamesMin": ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "en-GB": {
            "dayNamesMin": ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "en-NZ": {
            "dayNamesMin": ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "eo": {
            "dayNamesMin": ["Dim", "Lun", "Mar", "Mer", "Ĵaŭ", "Ven", "Sab"],
            "firstDay": 0,
            "isRTL": false,
            "monthNames": ["Januaro", "Februaro", "Marto", "Aprilo", "Majo", "Junio", "Julio", "Aŭgusto", "Septembro", "Oktobro", "Novembro", "Decembro"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "es": {
            "dayNamesMin": ["Dom", "Lun", "Mar", "Mi&eacute;", "Juv", "Vie", "S&aacute;b"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "et": {
            "dayNamesMin": ["Pühap", "Esmasp", "Teisip", "Kolmap", "Neljap", "Reede", "Laup"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Jaanuar", "Veebruar", "Märts", "Aprill", "Mai", "Juuni", "Juuli", "August", "September", "Oktoober", "November", "Detsember"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "eu": {
            "dayNamesMin": ["Iga", "Ast", "Ast", "Ast", "Ost", "Ost", "Lar"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Urtarrila", "Otsaila", "Martxoa", "Apirila", "Maiatza", "Ekaina", "Uztaila", "Abuztua", "Iraila", "Urria", "Azaroa", "Abendua"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "fa": {
            "dayNamesMin": ["ي", "د", "س", "چ", "پ", "ج", "ش"],
            "firstDay": 6,
            "isRTL": true,
            "monthNames": ["فروردين", "ارديبهشت", "خرداد", "تير", "مرداد", "شهريور", "مهر", "آبان", "آذر", "دي", "بهمن", "اسفند"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "fi": {
            "dayNamesMin": ["Su", "Ma", "Ti", "Ke", "To", "Pe", "Su"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Tammikuu", "Helmikuu", "Maaliskuu", "Huhtikuu", "Toukokuu", "Kes&auml;kuu", "Hein&auml;kuu", "Elokuu", "Syyskuu", "Lokakuu", "Marraskuu", "Joulukuu"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "fo": {
            "dayNamesMin": ["Sun", "Mán", "Týs", "Mik", "Hós", "Frí", "Ley"],
            "firstDay": 0,
            "isRTL": false,
            "monthNames": ["Januar", "Februar", "Mars", "Apríl", "Mei", "Juni", "Juli", "August", "September", "Oktober", "November", "Desember"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "fr-CH": {
            "dayNamesMin": ["Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "fr": {
            "dayNamesMin": ["Dim.", "Lun.", "Mar.", "Mer.", "Jeu.", "Ven.", "Sam."],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "gl": {
            "dayNamesMin": ["Dom", "Lun", "Mar", "M&eacute;r", "Xov", "Ven", "S&aacute;b"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Xaneiro", "Febreiro", "Marzo", "Abril", "Maio", "Xuño", "Xullo", "Agosto", "Setembro", "Outubro", "Novembro", "Decembro"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "he": {
            "dayNamesMin": ["א'", "ב'", "ג'", "ד'", "ה'", "ו'", "שבת"],
            "firstDay": 0,
            "isRTL": true,
            "monthNames": ["ינואר", "פברואר", "מרץ", "אפריל", "מאי", "יוני", "יולי", "אוגוסט", "ספטמבר", "אוקטובר", "נובמבר", "דצמבר"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "hr": {
            "dayNamesMin": ["Ned", "Pon", "Uto", "Sri", "Čet", "Pet", "Sub"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Siječanj", "Veljača", "Ožujak", "Travanj", "Svibanj", "Lipanj", "Srpanj", "Kolovoz", "Rujan", "Listopad", "Studeni", "Prosinac"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "hu": {
            "dayNamesMin": ["Vas", "Hét", "Ked", "Sze", "Csü", "Pén", "Szo"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Január", "Február", "Március", "Április", "Május", "Június", "Július", "Augusztus", "Szeptember", "Október", "November", "December"],
            "showMonthAfterYear": true,
            "yearSuffix": ""
        },
        "hy": {
            "dayNamesMin": ["կիր", "երկ", "երք", "չրք", "հնգ", "ուրբ", "շբթ"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Հունվար", "Փետրվար", "Մարտ", "Ապրիլ", "Մայիս", "Հունիս", "Հուլիս", "Օգոստոս", "Սեպտեմբեր", "Հոկտեմբեր", "Նոյեմբեր", "Դեկտեմբեր"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "id": {
            "dayNamesMin": ["Min", "Sen", "Sel", "Rab", "kam", "Jum", "Sab"],
            "firstDay": 0,
            "isRTL": false,
            "monthNames": ["Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "Nopember", "Desember"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "is": {
            "dayNamesMin": ["Sun", "M&aacute;n", "&THORN;ri", "Mi&eth;", "Fim", "F&ouml;s", "Lau"],
            "firstDay": 0,
            "isRTL": false,
            "monthNames": ["Jan&uacute;ar", "Febr&uacute;ar", "Mars", "Apr&iacute;l", "Ma&iacute", "J&uacute;n&iacute;", "J&uacute;l&iacute;", "&Aacute;g&uacute;st", "September", "Okt&oacute;ber", "N&oacute;vember", "Desember"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "it": {
            "dayNamesMin": ["Dom", "Lun", "Mar", "Mer", "Gio", "Ven", "Sab"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno", "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "ja": {
            "dayNamesMin": ["日", "月", "火", "水", "木", "金", "土"],
            "firstDay": 0,
            "isRTL": false,
            "monthNames": ["1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"],
            "showMonthAfterYear": true,
            "yearSuffix": "年"
        },
        "ko": {
            "dayNamesMin": ["일", "월", "화", "수", "목", "금", "토"],
            "firstDay": 0,
            "isRTL": false,
            "monthNames": ["1월(JAN)", "2월(FEB)", "3월(MAR)", "4월(APR)", "5월(MAY)", "6월(JUN)", "7월(JUL)", "8월(AUG)", "9월(SEP)", "10월(OCT)", "11월(NOV)", "12월(DEC)"],
            "showMonthAfterYear": false,
            "yearSuffix": "년"
        },
        "kz": {
            "dayNamesMin": ["жкс", "дсн", "ссн", "срс", "бсн", "жма", "снб"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Қаңтар", "Ақпан", "Наурыз", "Сәуір", "Мамыр", "Маусым", "Шілде", "Тамыз", "Қыркүйек", "Қазан", "Қараша", "Желтоқсан"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "lt": {
            "dayNamesMin": ["sek", "pir", "ant", "tre", "ket", "pen", "šeš"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Sausis", "Vasaris", "Kovas", "Balandis", "Gegužė", "Birželis", "Liepa", "Rugpjūtis", "Rugsėjis", "Spalis", "Lapkritis", "Gruodis"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "lv": {
            "dayNamesMin": ["svt", "prm", "otr", "tre", "ctr", "pkt", "sst"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Janvāris", "Februāris", "Marts", "Aprīlis", "Maijs", "Jūnijs", "Jūlijs", "Augusts", "Septembris", "Oktobris", "Novembris", "Decembris"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "ml": {
            "dayNamesMin": ["ഞായ", "തിങ്ക", "ചൊവ്വ", "ബുധ", "വ്യാഴം", "വെള്ളി", "ശനി"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["ജനുവരി", "ഫെബ്രുവരി", "മാര്ച്ച്", "ഏപ്രില്", "മേയ്", "ജൂണ്", "ജൂലൈ", "ആഗസ്റ്റ്", "സെപ്റ്റംബര്", "ഒക്ടോബര്", "നവംബര്", "ഡിസംബര്"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "ms": {
            "dayNamesMin": ["Aha", "Isn", "Sel", "Rab", "kha", "Jum", "Sab"],
            "firstDay": 0,
            "isRTL": false,
            "monthNames": ["Januari", "Februari", "Mac", "April", "Mei", "Jun", "Julai", "Ogos", "September", "Oktober", "November", "Disember"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "nl": {
            "dayNamesMin": ["zon", "maa", "din", "woe", "don", "vri", "zat"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["januari", "februari", "maart", "april", "mei", "juni", "juli", "augustus", "september", "oktober", "november", "december"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "no": {
            "dayNamesMin": ["søn", "man", "tir", "ons", "tor", "fre", "lør"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["januar", "februar", "mars", "april", "mai", "juni", "juli", "august", "september", "oktober", "november", "desember"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "pl": {
            "dayNamesMin": ["Nie", "Pn", "Wt", "Śr", "Czw", "Pt", "So"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Styczeń", "Luty", "Marzec", "Kwiecień", "Maj", "Czerwiec", "Lipiec", "Sierpień", "Wrzesień", "Październik", "Listopad", "Grudzień"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "pt-BR": {
            "dayNamesMin": ["Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "S&aacute;b"],
            "firstDay": 0,
            "isRTL": false,
            "monthNames": ["Janeiro", "Fevereiro", "Mar&ccedil;o", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "pt": {
            "dayNamesMin": ["Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "S&aacute;b"],
            "firstDay": 0,
            "isRTL": false,
            "monthNames": ["Janeiro", "Fevereiro", "Mar&ccedil;o", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "rm": {
            "dayNamesMin": ["Dum", "Gli", "Mar", "Mes", "Gie", "Ven", "Som"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Schaner", "Favrer", "Mars", "Avrigl", "Matg", "Zercladur", "Fanadur", "Avust", "Settember", "October", "November", "December"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "ro": {
            "dayNamesMin": ["Dum", "Lun", "Mar", "Mie", "Joi", "Vin", "Sâm"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Ianuarie", "Februarie", "Martie", "Aprilie", "Mai", "Iunie", "Iulie", "August", "Septembrie", "Octombrie", "Noiembrie", "Decembrie"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "ru": {
            "dayNamesMin": ["вск", "пнд", "втр", "срд", "чтв", "птн", "сбт"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "sk": {
            "dayNamesMin": ["Ned", "Pon", "Uto", "Str", "Štv", "Pia", "Sob"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Január", "Február", "Marec", "Apríl", "Máj", "Jún", "Júl", "August", "September", "Október", "November", "December"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "sl": {
            "dayNamesMin": ["Ned", "Pon", "Tor", "Sre", "&#x10C;et", "Pet", "Sob"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Januar", "Februar", "Marec", "April", "Maj", "Junij", "Julij", "Avgust", "September", "Oktober", "November", "December"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "sq": {
            "dayNamesMin": ["Di", "Hë", "Ma", "Më", "En", "Pr", "Sh"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Janar", "Shkurt", "Mars", "Prill", "Maj", "Qershor", "Korrik", "Gusht", "Shtator", "Tetor", "Nëntor", "Dhjetor"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "sr-SR": {
            "dayNamesMin": ["Ned", "Pon", "Uto", "Sre", "Čet", "Pet", "Sub"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Januar", "Februar", "Mart", "April", "Maj", "Jun", "Jul", "Avgust", "Septembar", "Oktobar", "Novembar", "Decembar"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "sr": {
            "dayNamesMin": ["Нед", "Пон", "Уто", "Сре", "Чет", "Пет", "Суб"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Јануар", "Фебруар", "Март", "Април", "Мај", "Јун", "Јул", "Август", "Септембар", "Октобар", "Новембар", "Децембар"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "sv": {
            "dayNamesMin": ["Sön", "Mån", "Tis", "Ons", "Tor", "Fre", "Lör"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Januari", "Februari", "Mars", "April", "Maj", "Juni", "Juli", "Augusti", "September", "Oktober", "November", "December"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "ta": {
            "dayNamesMin": ["ஞாயிறு", "திங்கள்", "செவ்வாய்", "புதன்", "வியாழன்", "வெள்ளி", "சனி"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["தை", "மாசி", "பங்குனி", "சித்திரை", "வைகாசி", "ஆனி", "ஆடி", "ஆவணி", "புரட்டாசி", "ஐப்பசி", "கார்த்திகை", "மார்கழி"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "th": {
            "dayNamesMin": ["อา.", "จ.", "อ.", "พ.", "พฤ.", "ศ.", "ส."],
            "firstDay": 0,
            "isRTL": false,
            "monthNames": ["มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน", "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "tj": {
            "dayNamesMin": ["якш", "душ", "сеш", "чор", "пан", "ҷум", "шан"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Январ", "Феврал", "Март", "Апрел", "Май", "Июн", "Июл", "Август", "Сентябр", "Октябр", "Ноябр", "Декабр"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "tr": {
            "dayNamesMin": ["Pz", "Pt", "Sa", "Ça", "Pe", "Cu", "Ct"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "uk": {
            "dayNamesMin": ["нед", "пнд", "вів", "срд", "чтв", "птн", "сбт"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["Січень", "Лютий", "Березень", "Квітень", "Травень", "Червень", "Липень", "Серпень", "Вересень", "Жовтень", "Листопад", "Грудень"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "vi": {
            "dayNamesMin": ["CN", "T2", "T3", "T4", "T5", "T6", "T7"],
            "firstDay": 0,
            "isRTL": false,
            "monthNames": ["Tháng Một", "Tháng Hai", "Tháng Ba", "Tháng Tư", "Tháng Năm", "Tháng Sáu", "Tháng Bảy", "Tháng Tám", "Tháng Chín", "Tháng Mười", "Tháng Mười Một", "Tháng Mười Hai"],
            "showMonthAfterYear": false,
            "yearSuffix": ""
        },
        "zh-CN": {
            "dayNamesMin": ["周日", "周一", "周二", "周三", "周四", "周五", "周六"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
            "showMonthAfterYear": true,
            "yearSuffix": "年"
        },
        "zh-HK": {
            "dayNamesMin": ["周日", "周一", "周二", "周三", "周四", "周五", "周六"],
            "firstDay": 0,
            "isRTL": false,
            "monthNames": ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
            "showMonthAfterYear": true,
            "yearSuffix": "年"
        },
        "zh-TW": {
            "dayNamesMin": ["周日", "周一", "周二", "周三", "周四", "周五", "周六"],
            "firstDay": 1,
            "isRTL": false,
            "monthNames": ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
            "showMonthAfterYear": true,
            "yearSuffix": "年"
        }
    };

    // -------------------------------------------------------------------------
    // finally, integrate with jQuery for convenience --------------------------
    // -------------------------------------------------------------------------

    $.fn.datePicker = function (options) {
        return (new AJS.DatePicker(this, options));
    };

}(jQuery));