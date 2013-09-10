var com_controlj_addon_jumpto = function() {
    var $ = jQuery.noConflict(true);
    var addonName;
    var dlgDiv;
    var lastSearch = null;
    var inProgress = false;
    var searchTimer = null;
    var currentSelection;

    function showDialog(addon) {
        addonName = addon;
        dlgDiv = $('<div id="find-dialog"><input type="search"/><div/></div>');
        dlgDiv.dialog(
            {
                appendTo: "body",
                width: $(window).width() * 0.5,
                position: [$(window).width() * 0.25, $(window).height() * 0.25],
                dialogClass: "find-dialog",
                modal: true,
                draggable: false,
                resizable: false,
                close: function()
                {
                    dlgDiv.remove();
                    $(window).off("resize.jumpto");
                }
            }
        );

        dlgDiv.css({'max-height': $(window).height() * 0.5});
        dlgDiv.find("> div").css({'max-height': $(window).height() * 0.5 - dlgDiv.find("> input").outerHeight(true)});

        $(window).on("resize.jumpto", function() {
            dlgDiv.dialog({
                width: $(window).width() * 0.5,
                position: [$(window).width() * 0.25, $(window).height() * 0.25]
            });
            dlgDiv.css({'max-height': $(window).height() * 0.5});
            dlgDiv.find("> div").css({'max-height': $(window).height() * 0.5 - dlgDiv.find("> input").outerHeight(true)});
        });

        $(".ui-widget-overlay").on("click.jumpto", function() { dlgDiv.dialog("close"); }).css({opacity: 0.7});

        $(document).ajaxError(function() {
            inProgress = false;
            queueSearch(200);
        });

        if (lastSearch) {
            dlgDiv.find('> input').val(lastSearch);
            runSearch();
        }

        dlgDiv.find('> input').on("keyup.jumpto", function() { queueSearch(100); });

        dlgDiv.on("mouseover.jumpto", "> div > div", function() {
            changeSelection($(this));
        });
        dlgDiv.on("click.jumpto", "> div > div", function() {
            navigateTo($(this));
        });

        dlgDiv.find('> input').on("keydown.jumpto", function(e) {
            var newSelection;
            if (e.which == 13) { // enter key
                if (currentSelection) navigateTo(currentSelection);
            } else {
                if (e.which == 40) { // down arrow
                    if (currentSelection && currentSelection.next().length != 0)
                        newSelection = currentSelection.next();
                    else
                        newSelection = dlgDiv.find("> div > div:first");
                } else if (e.which == 38) { // up arrow
                    if (currentSelection && currentSelection.prev().length != 0)
                        newSelection = currentSelection.prev();
                    else
                        newSelection = dlgDiv.find("> div > div:last");
                }

                changeSelection(newSelection);
                if (newSelection)
                    currentSelection.scrollintoview();
            }
        });
    }

    function changeSelection( to) {
        if (currentSelection) currentSelection.removeClass("find-dialog-selection");
        currentSelection = to;
        if (currentSelection) currentSelection.addClass("find-dialog-selection");
    }

    function navigateTo(selection) {
        var gql = selection.attr('gql');
        addonUtility.navigate(gql);
        dlgDiv.dialog("close");
    }

    function runSearch() {
        inProgress = true;
        lastSearch = dlgDiv.find('> input').val();

        var params = {value: lastSearch};
        $.get('/'+addonName+'/servlets/search',params, function(response) {
            inProgress = false;

            var results = JSON.parse(response);
            var wrapper = dlgDiv.find('> div').empty().detach();
            for (var i = 0; i < results.length; i++) {
                var result = results[i];
                $("<div/>", {
                    text: result.disp,
                    gql: result.gql
                }).appendTo(wrapper);
            }

            wrapper.appendTo(dlgDiv);
            queueSearch(0); // in case the query changed while we were running this search
        })
    }

    function searchTimerHandler() {
        searchTimer = null;
        var currentSearch = dlgDiv.find('> input').val();
        if (!inProgress && (lastSearch != currentSearch))
            runSearch()
    }

    function queueSearch(delay) {
        if (searchTimer)
            clearTimeout(searchTimer);
        searchTimer = setTimeout(searchTimerHandler, delay);
    }

    return {
        showDialog: showDialog,
        loadCss: function(src) {
            var head = $("head");
            if (head.find("link[href=\""+src+"\"]").length == 0) {
                $("<link>")
                  .appendTo(head)
                  .attr({type : 'text/css', rel : 'stylesheet'})
                  .attr('href', src);
            }
        }
    };
}();