
var eb;

function start() {
    eb = new EventBus('http://localhost:8080/eventbus');
    eb.onopen = function () {
        eb.registerHandler('new_url', function (error, msg) {
            console.log('received a message: ' + JSON.stringify(msg));
            $("#urls").append("<tr><td>"
                + msg.body.id + "</td><td>"
                + msg.body.url + "</td><td>"
                + msg.body.time + "</td><td>"
                + msg.body.size + "</td><td>"
                + msg.body.status + "</td></tr>");
        });
    }
    eb.onclose = function () {
        console.log("closed");
        setTimeout(function(){
            start();
            console.log("reconnected");
        }, 1000);
    }
}

$(function () {
    start();
    $(document.body).on('click', 'button', function (e) {
        e.preventDefault();
        console.log(e);
    });
    $(document.body).on('click', '#start', function(e) {
        eb.send('start', {
            url: $("#url").val(),
            deep: parseInt($("#deep").val()),
            limit: parseInt($("#limit").val()),
            speed: parseInt($("#speed").val())
        });
    });
    $(document.body).on('click', '#clear', function(e) {
        $("#urls").empty();
    });
});
