/*
 * Copyright (c) 2011 Automated Logic Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


function runSearch(rebuild, all) {
    inProgress = true;
    lastSearch = $('#input').attr('value');

    params = {value: lastSearch}
    if (rebuild) { params.reinit = true;}
    if (all) { params.all = true; }
//    console.log("running search with '"+lastSearch+"'");
    $.get('servlets/search',params, function(content) {
        inProgress = false;
//        console.log("search response - good");

        var start = new Date()
        var wrapper = $('#result')
        //wrapper.css('display','none')
        wrapper.html(content);
        //$('#result')[0].innerHTML = content;
        var end = new Date()
//        console.log('inserting content took '+ (end.getTime() - start.getTime()) + " mSec")
        //wrapper.css('display','block')
        queueSearch(0);
    })
}

function searchTimerHandler() {
    searchTimer = null;
    var currentSearch = $('#input').attr('value');
//    console.log("searchTimer triggered - current:"+currentSearch +", last:"+lastSearch)
    if (!inProgress && (lastSearch != currentSearch)) {
        runSearch(false, false)
    }
}

function queueSearch(delay) {
    if (searchTimer)  {
        clearTimeout(searchTimer);
    }
//    console.log("Setting timer for "+delay);
    searchTimer = setTimeout(searchTimerHandler, delay);
}

var lastSearch = null;
var inProgress = false;
var searchTimer = null;

$(function(){
    $(document).ajaxError(function() {
        inProgress = false;
//        console.log("ajax error");
        queueSearch(200);
    });

    $('#input').keyup( function() { queueSearch(100) });
    
    $('#clear').click(function(){
        $('#input').attr('value','');
        queueSearch(0);
    });

    $(document).keydown(function(e) {
        var doPrevent = false;
        if (e.keyCode == 8) {
            var d = e.srcElement || e.target;
            doPrevent = (d.tagName.toUpperCase() != 'INPUT')
        }

        if (doPrevent)
            e.preventDefault();
    });


    $('#rebuild').click(function() { runSearch(true, false) } );

    $('#input')[0].focus();

    queueSearch(0);
});



    