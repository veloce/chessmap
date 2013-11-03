$(function() {
    var worldWith = window.innerWidth - 30,
    mapRatio = 0.4,
    worldHeight = worldWith * mapRatio,
    wScale = worldWith / 1000,
    hScale = worldHeight / 400;
    var paper = Raphael(document.getElementById("worldmap"), worldWith, worldHeight);
    paper.rect(0, 0, worldWith, worldHeight, 10).attr({
        stroke: "none"
    });
    paper.setStart();
    var hue = Math.random();
    for (var country in worldmap.shapes) {
        paper.path(worldmap.shapes[country]).attr({stroke: "#343C40", fill: "#67777F", "stroke-opacity": 0.25}).transform("s" + wScale + "," + hScale + " 0,0");
    }
    var world = paper.setFinish();
    world.getXY = function (lat, lon) {
        return {
            cx: lon * (2.6938 * wScale) + (465.4 * wScale),
            cy: lat * (-2.6938 * hScale) + (227.066 * hScale)
        };
    };

    if (!!window.EventSource) {
        var density = {},
        nMoves = 0;
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
                opacity: 0.65,
                stroke: "#FE7727",
                'arrow-end': 'oval-wide-long'
              });
              setTimeout(function() { line.remove(); }, 500);
            }

            // stats
            nMoves++;
            $('#moves > span').text(nMoves);
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
