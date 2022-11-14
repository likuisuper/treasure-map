

function buildTopo(svgId, data, operation) {

    // 创建 dagreD3 对象
    var g = new dagreD3.graphlib.Graph()
        .setGraph({
            rankdir: "LR"
        })
        .setDefaultEdgeLabel(function () {
            return {};
        });
    // 设置图标
    data.nodes.some(function (value) {
        if (value.type == "browser") {
            value.icon = "desktop";
        } else if (value.type == "http server") {
            value.icon = "server";
        } else if (value.type == "dubbo server") {
            value.icon = "server";
        } else if (value.type == "database") {
            value.icon = "database";
        } else {
            value.icon = "item";
        }

    });
    // 添加节点
    data.nodes.some(function (value, index, array) {
        var labelVal = "<div class='labelContent' ><i class='ui icon big grey " + value.icon + " ' style='display: inline'></i> <span class='labelTitle'>" +
            value.title + "</span><br> <span class='subTitle'> " + value.subTitle + "</span> </div>";

        g.setNode(value.id, {label: labelVal, labelType: "html", class: value.state, rx: 5, ry: 5,tips:value.tips});
    });
    // 添加关系
    data.edges.some(function (value, index, array) {
        g.setEdge(value.from, value.to, value)
    });

    g.nodes().forEach(function (v) {
        var node = g.node(v);
        // 设置矩形的圆角
        node.rx = node.ry = 5;
    });
    // 获取svg 画布
    var svg = d3.select("#" + svgId);
    var svgGroup = svg.append("g");


    //创建渲染器
    var render = new dagreD3.render();
    inner = d3.select("svg g");
    //执行渲染
    render(inner, g);
    inner.selectAll("g.node")
        .attr("title", function (v) {
            return g.node(v).tips;
        })
        .each(function (v) {
            $(this).tipsy({gravity: "w", opacity: 1, html: true});
        });

    // 设置缩放支持
    var zoom = d3.zoom().on("zoom", function () {
        inner.attr("transform", d3.event.transform);
    });
    svg.call(zoom);


    // 将渲染后的图表居中显示
    // 图像的真实宽度
    offsetWidth = $("#" + svgId).width();
    offsetHeight = $("#" + svgId).height();
    /* var xCenterOffset = (offsetWidth - g.graph().width) / 2;
     var yCenterOffset = (offsetHeight - g.graph().height) / 2;
     svgGroup.attr("transform", "translate(" + xCenterOffset + ", " + yCenterOffset + ")");*/

    //svg.attr("height", g.graph().height + 40);

    var initialScale = 0.75;
    svg.call(zoom.transform, d3.zoomIdentity.translate((offsetWidth - g.graph().width * initialScale) / 2, (offsetHeight - g.graph().height * initialScale) / 2).scale(initialScale));

    if (operation != null && "nodeClick" in operation) {
        svg.selectAll("g.node")
            .on('click', function (id, index, array) {
                for (i = 0; i < array.length; i++) {
                    d3.select(array[i].children[0]).style("stroke", null).style("stroke-width", null);
                }
                d3.select(this.children[0]).style("stroke", "dodgerblue").style("stroke-width", "2.5px");
                operation.nodeClick(id, index, array)
            });
    }
    return g;
}
