
var eb;

function start() {
    eb = new EventBus('http://localhost:8080/eventbus');
    eb.onopen = function () {
        // set a handler to receive a message
        // eb.registerHandler('some-address', function (error, message) {
        //     console.log('received a message: ' + JSON.stringify(message));
        // });
        // send a message
        // eb.send('some-address', {name: 'tim', age: 587});
        // eb.send('address', {a: "op"});

        eb.registerHandler('new_url', function (error, msg) {
            console.log('received a message: ' + JSON.stringify(msg));
            $("#urls").append("<tr><td>" + msg.body.id + "</td><td>" + msg.body.url + "</td></tr>");
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
        // eb.send('start', {a: "prevent"});
    });

    $(document.body).on('click', '#start', function(e) {
        // e.preventDefault();
        // console.log(e);
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
