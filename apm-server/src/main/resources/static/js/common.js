/**
 * 构建拓扑流程图 依赖 d3.v4.min.js 与dagre-d3.0.6.1.min.js
 * @param svgId
 * @param data
 * @param operation
 * @returns {*}
 */
function buildTopo(svgId, data, operation) {
    // 创建 dagreD3 对象
    var g = new dagreD3.graphlib.Graph()
        .setGraph({
            rankdir: "LR"
        })
        .setDefaultEdgeLabel(function () {
            return {};
        });
    // 添加节点
    data.nodes.some(function (value, index, array) {
        var labelVal = "<div class='labelContent'>" +
                            "<i class='fas fa-" + value.icon + " fa-2x labelIcon'></i>" +
                            " <span class='labelTitle'>" +value.title + "</span>" +
                            "<br> " +
                            "<span class='subTitle'> " + value.subTitle + "</span>" +
                        " </div>";
        g.setNode(value.id,  {label: labelVal, labelType: "html", class:value.state ,backgroundColor: "white",rx: 5, ry: 5});
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
    inner=d3.select("svg g");
    //执行渲染
    render(inner, g);

    // 设置缩放支持
    var zoom = d3.zoom().on("zoom", function() {
        inner.attr("transform", d3.event.transform);
    });
    svg.call(zoom);

    // 将渲染后的图表居中显示
    var xCenterOffset = (svg.attr("width") - g.graph().width) / 2;
    svgGroup.attr("transform", "translate(" + xCenterOffset + ", 20)");
    svg.attr("height", g.graph().height + 40);

    //  设置缩放支持
    var initialScale = 0.75;
    svg.call(zoom.transform, d3.zoomIdentity.translate((svg.attr("width") - g.graph().width * initialScale) / 2, 20).scale(initialScale));


    // 触发点击事件
    svg.selectAll("g.node")
        .on('click', function (id, index, array) {
            // 选中样式变化
            for (i = 0; i < array.length; i++) {
                d3.select(array[i].children[0]).style("stroke", null).style("stroke-width", null);
            }
            d3.select(this.children[0]).style("stroke", "dodgerblue").style("stroke-width", "2.5px");

            if (operation != null) {
                operation.nodeClick(id, index, array)
            }
        });
    return g;
}