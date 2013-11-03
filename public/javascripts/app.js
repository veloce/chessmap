$(function() {
    var worldWith = window.innerWidth - 30,
    mapRatio = 0.4,
    worldHeight = worldWith * mapRatio,
    wScale = worldWith / 1000,
    hScale = worldHeight / 400;
    var paper = Raphael(document.getElementById("worldmap"), worldWith, worldHeight);
    paper.rect(0, 0, worldWith, worldHeight, 10).attr({
        stroke: "none",
        fill: "0-#9bb7cb-#adc8da"
    });
    var over = function () {
        this.c = this.c || this.attr("fill");
        this.stop().animate({fill: "#bacabd"}, 500);
    },
    out = function () {
        this.stop().animate({fill: this.c}, 500);
    };
    paper.setStart();
    var hue = Math.random();
    for (var country in worldmap.shapes) {
        paper.path(worldmap.shapes[country]).attr({stroke: "#ccc6ae", fill: "#f0efeb", "stroke-opacity": 0.25}).transform("s" + wScale + "," + hScale + " 0,0");
    }
    var world = paper.setFinish();
    world.hover(over, out);
    world.getXY = function (lat, lon) {
        return {
            cx: lon * (2.6938 * wScale) + (465.4 * wScale),
            cy: lat * (-2.6938 * hScale) + (227.066 * hScale)
        };
    };
    world.getLatLon = function (x, y) {
        return {
            lat: (y - 227.066) / -2.6938,
            lon: (x - 465.4) / 2.6938
        };
    };

    if (!!window.EventSource) {
        var density = {};
        var source = new EventSource("/stream");
        source.addEventListener('message', function(e) {
            var data = JSON.parse(e.data);
            var densityKey = data.latitude + "" + data.longitude;
            if (typeof density[densityKey] == 'undefined') density[densityKey] = 1;
            else density[densityKey]++;
            var dot = paper.circle().attr({
              fill: "#FE7727",
              r: density[densityKey] + 2,
              'stroke-width': 0
            });
            var orig = world.getXY(data.latitude, data.longitude);
            dot.attr(orig);
            setTimeout(function() { 
              dot.remove(); 
              density[densityKey]--;
            }, 1000);
            if (data.oLatitude) {
              var dest = world.getXY(data.oLatitude, data.oLongitude);
              var str = "M" + orig.cx + "," + orig.cy + "T" + dest.cx + "," + dest.cy;
              var line = paper.path(str);
              line.attr({
                opacity: 0.15,
                'arrow-end': 'oval-wide-long'
              });
              setTimeout(function() { line.remove(); }, 500);
            }
        }, false);
        source.addEventListener('open', function(e) {
            // Connection was opened.
            console.log("connection opened");
        }, false);
        source.addEventListener('error', function(e) {
            if (e.readyState == EventSource.CLOSED) {
                // Connection was closed.
                console.log("connection closed");
            }
        }, false);
    }
});
