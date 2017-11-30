$.fn.dropdown = function (options) {
    let callIfExists = function (method, param1, param2, param3) {
        if (typeof method !== 'undefined') {
            return method(param1, param2, param3);
        }

        return undefined;
    };

    let value;
    let possibleValues;
    let hoverCSSClass;
    let displayInline;

    if (typeof options !== 'undefined') {
        if (typeof options.CSSClass !== 'undefined') {
            $(this).addClass(options.CSSClass);
        }

        value = options.value;
        possibleValues = options.possibleValues;
        hoverCSSClass = options.hoverCSSClass;
        if (typeof options.displayInline !== 'undefined') {
            displayInline = options.displayInline;
        } else {
            displayInline = false;
        }
    }

    if (typeof hoverCSSClass === 'undefined') {
        hoverCSSClass = 'dropdown_hover';
    }

    let makeListItem = function (text, lastFlag) {
        if (lastFlag) {
            return '<li><a class="dropdown_selection_last" href="#">' + text + '</a></li>';
        } else {
            return '<li><a href="#">' + text + '</a></li>';
        }
    };

    let buildDropdownList = function () {
        let wrapperTag = '<div';
        if (displayInline) {
            wrapperTag = '<span';
        }
        let representation = wrapperTag + ' class="dropdown_wrapper">';

        representation += '<a class="dropdown_selected" href="#">' + value + '</a>';
        representation += '<ul class="dropdown_selection">';

        for (let i = 0; i < possibleValues.length; ++i) {
            if (i === possibleValues.length - 1) {
                representation += makeListItem(possibleValues[i], true);
            } else {
                representation += makeListItem(possibleValues[i]);
            }
        }

        representation += '</ul></div>';

        return representation;
    };

    $(this).html(buildDropdownList());

    /** Display list on hover */
    $(this).hover(function () {
        // only if ul:first is smaller than containing element, adapt width.
        if ($('ul:first', this).outerWidth() < $(this).outerWidth()) {
            $('ul:first', this).outerWidth($(this).outerWidth());
        }

        $(this).addClass(hoverCSSClass);
        $('ul:first', this).css('visibility', 'visible');
        let leftPad = $(this).css('padding-left');
        $('ul:first', this).css('margin-left', '-' + leftPad);

        callIfExists(options.show);
    }, function () {
        $(this).removeClass(hoverCSSClass);
        $('ul:first', this).css('visibility', 'hidden');
        callIfExists(options.hide);
    });

    /** Select next item, dispatch callback events. */
    let that = this;
    $('ul li', this).click(function () {
        let changeValue = $(this).text();
        let oldValue = $('a:first', that).text();
        let changeValueIndex = $(this).index();

        /** No events are dispatched when the value is not actually changed. */
        if (changeValue !== oldValue) {
            let retVal = callIfExists(options.valueAboutToChange, changeValue, oldValue, changeValueIndex);

            if (typeof retVal !== 'undefined') {
                changeValue = retVal;
            }

            $('div a:first, span a:first', that).text(changeValue);

            $('div a:first, span a:first', that).position({
                my: 'center',
                at: 'center',
                of: $(that)
            });

            callIfExists(options.valueChanged, changeValue, oldValue, changeValueIndex);
        }

        $(that).removeClass(hoverCSSClass);
        $('ul:first', that).css('visibility', 'hidden');
        callIfExists(options.hide);

        return false;
    });
}
