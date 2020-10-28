//var Particle = require('particle-api-js');
var particle = new Particle();
var token = '';
var id = '';
var isSmsOn = false;

function getData() {
    particle.getVariable({deviceId: id, name: 'temp', auth: token}).then(
        function (data) {
            element = document.getElementById("tempRoom");
            element.textContent = "temp in room: " + data.body.result + "C";
        },
        function (err) {
            console.log('Retrieving variable failed', err);
        }
    )

    particle.getVariable({deviceId: id, name: 'moistureval', auth: token}).then(
        function (data) {
            element = document.getElementById("moisturePlant");
            element.textContent = "moisture in plant: " + data.body.result;
        },
        function (err) {
            console.log('Retrieving variable failed', err);
        }
    )

    particle.getVariable({deviceId: id, name: 'humidityval', auth: token}).then(
        function (data) {
            element = document.getElementById("humidityRoom");
            element.textContent = "humidity inside: " + data.body.result + "%";
        },
        function (err) {
            console.log('Retrieving variable failed', err);
        }
    )
}

getData();


var rangeslider = document.getElementById("sliderRange");
var output = document.getElementById("demo");
var switchBtn = document.getElementById("switch");

particle.getVariable({deviceId: id, name: 'limit', auth: token}).then(
    function (data) {
        rangeslider.value = data.body.result;
        output.innerHTML = rangeslider.value;
    },
    function (err) {
        console.log('Retrieving variable failed', err);
    }
)

particle.getVariable({deviceId: id, name: 'smsonoff', auth: token}).then(
    function (data) {
        switchBtn.checked = data.body.result;
    },
    function (err) {
    console.log("retrieving smsonoff failed", err);
    }
)


rangeslider.oninput = function () {
    output.innerHTML = this.value;
}

rangeslider.onchange = function () {
    particle.callFunction({
        deviceId: id, name: 'changelimit', argument: rangeslider.value.toString(),
        auth: token
    }).then(
        function (data) {
            console.log('Function called successfully: ', data);
        },
        function (err) {
            console.log('Function call failed', err);
        }
    )
}



switchBtn.onclick = function() {

    var argument;

    if(switchBtn.checked) {
        argument = "on";
    } else if(!switchBtn.checked) {
        argument = "off";
    } else {
        return;
    }

    particle.callFunction({
        deviceId: id, name: 'turnoffsms', argument: argument,
        auth: token
    }).then(
    function (data) {
        console.log("smsonoff called successfully: ", data);
    },
    function (err) {
        console.log('smsonoff call failed', err);
    })
}


setInterval(getData, 10000);



