$(document).ready(function()
{
    $('.slider').draggable({
        axis:"x",
        containment:"parent",
        cursor:"pointer",
        stop: function (event, ui) {
            var range = $(ui.helper).parent().width() - 46;
            var value = (ui.position.left + range / 2) / range;
            $("#answerField").val(value);
        }
    });

    $('.quad .selector').draggable({
        containment:"parent",
        cursor:"pointer",
        stop: function (event, ui) {
            updateQuadAnswer(ui.position.left, ui.position.top);
        },
        drag: function (event, ui) {
            var x = ui.position.left - 125;
            var y = -(ui.position.top - 125);
            var signY = y<0?-1:1;
            var signX = x<0?-1:1;
            ui.position.left = signX * Math.min(signX*x, 125) + 125;
            x = ui.position.left - 125;
            ui.position.top = -(signY * Math.min(Math.abs(y), Math.abs(Math.sqrt(125*125 - x*x))) - 125);
        }
    });
    $('.quad').mousedown(function(e) {
        var x = e.offsetX - 135;
        var y = -(e.offsetY - 135);
        if (Math.sqrt(x*x+y*y) < 135) {
            $(this).children('.selector').offset({left: e.pageX - 10, top: e.pageY - 10});
            updateQuadAnswer(e.offsetX - 10, e.offsetY - 10);
        }
    });

    var previousAnswer = $("#answerField").val();
    if (previousAnswer != undefined) {
        if (!isNaN(previousAnswer)) {
            var range = $('.sliderQuestion .sliderbar').width() - 46;
            var left = previousAnswer * range - range / 2;
            $('.sliderQuestion .slider').css('left', left);
        } else {
            var index = previousAnswer.indexOf(',')
            if (index != -1) {
                var x = previousAnswer.substr(0, index);
                var y = previousAnswer.substr(index + 1);
                if (!isNaN(x) && !isNaN(y)) {
                    var posX = x * 125 + 125;
                    var posY = -y * 125 + 125;
                    $('.quadQuestion .selector').css({left:posX,top:posY});
                }

            }
        }
    }

    $('.imageQuestion img.answer').hover(function (e) {
        // Enter
        if (!$(e.target).hasClass('selected')) {
            var url = e.target.src;
            e.target.src = url.replace('.jpg', '-hover.jpg');
        }
    },
    function (e) {
        // Exit
        if (!$(e.target).hasClass('selected')) {
            var url = e.target.src;
            e.target.src = url.replace('-hover.jpg', '.jpg');
        }
    });

    var video = document.getElementById("herovideo");
    if (video != null) {
        video.removeAttribute("controls");
    }

    $("#banner").slideDown();
});

function notice(msg)
{
    $('#noticebox p span').html(msg)
    $('#noticebox').slideDown();
}

function updateQuadAnswer(offsetX, offsetY)
{
    var x = (offsetX - 125) / 125;
    var y = -(offsetY - 125) / 125;
    var value = x + "," + y;
    $("#answerField").val(value);
}

function selectAnswer(element)
{
    // Cleanup image selections
    $(element).parent('.imageQuestion').children('.selected').each(function(i, e) {
            var url = e.src;
            e.src = url.replace('-hover.jpg', '.jpg');
        });

    $(element).siblings('.selected').removeClass('selected');
    $(element).addClass('selected');
    $("#answerField").val(element.id);
}

function previousQuestion()
{
    $("#direction").val("previous");
    sendAnswer(false);
}

function nextQuestion()
{
    $("#direction").val("next");
    sendAnswer(true);
}

function sendAnswer(required)
{
    var selectedAnswer = $("#answerField").val();

    if (required && selectedAnswer == '') {
        notice('Please select an answer before moving to the next question');
        return;
    }

    $("#questionForm").submit();
}

function playVideo(videoId)
{
    $.ajax({
        type: "GET",
        url: location.href + "/videos/" + videoId + ".json",
        dataType: "json"
    }).done(function(data) {
        if (data == null) {
            notice("Unable to load the video at this time. Please check your internet connection and try again later. If the problem persists, please contact us.");
            return;
        }

        var player = $('#videoplayer video')[0];
        for (var i in data.urls) {
            var video = data.urls[i];
            if (player.canPlayType(video.type) != '') {
                player.src = video.src;
                player.load();
                player.play();
                player.addEventListener('ended', function(){ reportVideoComplete(data); });
                displayPlayer();
                return;
            }
        }

        notice("We could not find a video format that will work with your browser. Please try another browser or contact us.");
    }).error(function(jqXHR, error) {
        notice('Could not load video due to ' + error + '. If the problem persists, please contact us.');
    });
}

function displayPlayer()
{
    // Display Player
    $("#content").fadeOut(100);
    $("body").animate({backgroundColor: '#181818'}, 500, 'linear', function(){
        $("#videoplayer").fadeIn(200);
    });
}

function reportVideoComplete(data)
{
    notice("You finished \u201C" + data.title + ".\u201D");
    var completedBefore = $('#videos article .completed').length;

    $('#' + data.id).addClass('completed');

    var completed = $('#videos article .completed').length;
    var total = $('#videos article').length;
    var percent = Math.floor(completed * 100 / total) + "%";
    $('#chapterprogress .progress').width(percent);
    $('#chapterprogress .progresslabel').css('left', percent);
    $('#chapterprogress .progresslabel').html(percent);

    closeVideo();

    $.ajax({
        type: "POST",
        url: location.href + "/videos/" + data.id + ".json",
        dataType: "json",
        data: {'completed':'true'}
    }).error(function(jqXHR, error) {
        notice('Could not record video completiton due to ' + error + '. If the problem persists, please contact us.');
    });

    if (completed == total && completedBefore != completed) {
        chapterComplete();
    }
}

function closeVideo()
{
    $("#videoplayer video")[0].pause();
    $("#videoplayer").fadeOut(100);
    $("body").animate({backgroundColor: '#FFFFFF'}, 500, 'linear', function(){
        $("#content").fadeIn(200);
    });
}

function chapterComplete()
{
    notice("You've completed this chapter!");
    location.href += "/completed";
}
