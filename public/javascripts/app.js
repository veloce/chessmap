var stats = {
    nMoves: 0,
    humanMoves: 0,
    iaMoves: 0,
    countries: {}
};

$(function() {
    var worldWith = window.innerWidth - 30,
    mapRatio = 0.4,
    worldHeight = worldWith * mapRatio,
    scale = worldWith / 1000;
    var paper = Raphael(document.getElementById("worldmap"), worldWith, worldHeight);
    paper.rect(0, 0, worldWith, worldHeight, 10).attr({
        stroke: "none"
    });
    paper.setStart();
    for (var country in worldmap.shapes) {
        paper.path(worldmap.shapes[country]).attr({stroke: "#343C40", fill: "#67777F", "stroke-opacity": 0.25}).transform("s" + scale + "," + scale + " 0,0");
    }
    var world = paper.setFinish();
    world.getXY = function (lat, lon) {
        return {
            cx: lon * (2.6938 * scale) + (465.4 * scale),
            cy: lat * (-2.6938 * scale) + (227.066 * scale)
        };
    };

    // position legend and stats
    var worldOffset = $('#worldmap').offset();
    $('#stats').offset({
        top: worldOffset.top + 250 * scale,
        left: worldOffset.left + 7.6 * scale
    }).attr({width: 195 * scale, height: 172 * scale});

    if (!!window.EventSource) {
        var density = {};
        var source = new EventSource("/stream");

        source.addEventListener('message', function(e) {
            var data = JSON.parse(e.data);
            var densityKey = data.lat + "" + data.lon;
            if (typeof density[densityKey] == 'undefined') density[densityKey] = 1;
            else density[densityKey]++;
            var dot = paper.circle().attr({
                fill: data.isIA ? "#be4701" : "#FE7727",
                r: density[densityKey] + 2,
                'stroke-width': 0
            });
            var orig = world.getXY(data.lat, data.lon);
            dot.attr(orig);
            setTimeout(function() {
                dot.remove();
                density[densityKey]--;
            }, 1000);
            if (data.oLat) {
                var dest = world.getXY(data.oLat, data.oLon);
                var str = "M" + orig.cx + "," + orig.cy + "T" + dest.cx + "," + dest.cy;
                var line = paper.path(str);
                line.attr({
                    opacity: 0.35,
                    stroke: "#FE7727",
                    'arrow-end': 'oval-wide-long'
                });
                setTimeout(function() { line.remove(); }, 500);
            }

            // moves
            stats.nMoves++;
            if (data.isIA) stats.iaMoves++;
            else stats.humanMoves++;
            $('#moves > span').text(stats.nMoves);
            $('#humanMoves > span').text(stats.humanMoves);
            $('#iaMoves > span').text(stats.iaMoves);
            // top countries
            if (data.country in stats.countries) stats.countries[data.country]++;
            else stats.countries[data.country] = 1;
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
