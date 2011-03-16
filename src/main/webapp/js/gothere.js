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


function runSearch(rebuild) {
    inProgress = true;
    lastSearch = $('#input').attr('value')
    
    params = {value: lastSearch}
    if (rebuild) { params.reinit = true;}
    $.get('servlets/search',params, function(content) {
        $('#result').html(content)
        resetAndQueueSearch(1)
    })
}

function resetAndQueueSearch(delay) {
    inProgress = false
    if (lastSearch != $('#input').attr('value')) {
        setTimeout("runSearch(false)", delay)
    }
}


var lastSearch = ""
var inProgress = false;

function requestSearch(rebuild) {
    if (!inProgress) {
        runSearch(rebuild)
    }
}

$(function(){
    $(document).ajaxError(function() { resetAndQueueSearch(200) })
    $('#input').keyup( function() { runSearch(false) })
    
    $('#clear').click(function(){
        $('#input').attr('value','');
        runSearch(false);
    })

    $(document).keydown(function(e) {
        var doPrevent = false;
        if (e.keyCode == 8) {
            var d = e.srcElement || e.target;
            doPrevent = (d.tagName.toUpperCase() != 'INPUT')
        }

        if (doPrevent)
            e.preventDefault();
    });


    $('#rebuild').click(function() { runSearch(true) } )

    $('#input')[0].focus()

    runSearch();
});



    