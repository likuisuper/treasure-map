<!DOCTYPE html>
<html lang="en" xmlns="http://www.w3.org/1999/html">
<head>
    <meta charset="UTF-8">
    <title>监控列表</title>
    <script src="/js/jquery.min.js"></script>
    <script src="/js/semantic.min.js"></script>
    <script src="/js/d3.V4.7.3.min.js"></script>
    <script src="/js/dagre-d3V0.6.1.min.js"></script>
    <script src="/js/tipsy.js?v=${.now}"></script>
    <script src="/js/topo.js"></script>
    <script src="/js/treeTable.js"></script>

    <link href="/css/tipsy.css" rel="stylesheet">
    <link href="/css/semantic.min.css" rel="stylesheet">
    <link href="/css/treeTable.css" rel="stylesheet">
</head>
<style>
    body {
        padding: 0;
        margin: 0;
    }

    #topArea {
        height: 50px;
        border: black 1px dashed;
    }

    #leftArea {
        position: absolute;
        top: 50px;
        left: 0px;
        width: 300px;
        bottom: 0px;
        border: cadetblue 1px dashed;
    }

    #rightArea {
        position: absolute;
        top: 50px;
        right: 0px;
        left: 300px;
        bottom: 0px;
        border: darkblue 1px dashed;
    }

    .node.error rect {
        stroke: red;
    }

    .node rect:hover {
        /*fill: azure;*/
        stroke: dodgerblue;
        stroke-width: 1.5px;
    }

    .node .label {
        pointer-events: none;
    }

    .node text {
        font-weight: 300;
        font-family: "Helvetica Neue", Helvetica, Arial, sans-serf;
        font-size: 14px;
        pointer-events: none;
    }

    .edgePath path {
        stroke: #333;
        stroke-width: 1.5px;
    }

    #stackNodeDetail.max {
        left: 0px;
        right: 0px;
        padding: 20px;
        width: 100vw;
    }

    tr.selected td {
        background-color: #ffe48d;
    }
</style>
<body>
<#--头部过滤区域-->
<div id="topArea">
    <h3 style="line-height: 50px;text-align: center">监控列表</h3>
</div>
<#--左边列表树-->
<div id="leftArea" style="overflow: auto">
    <table class="ui selectable single line table">
        <tbody>
        <#list nodes as node>
            <tr>
                <td data-label="urlRequest">
                    <a href="javascript:void(0)" onclick="openDetail('${node.traceId}')">${node.url}</a>
                </td>
                <td data-label="time">${node.beginTime?string('HH:mm')}</td>
            </tr>
        </#list>
        </tbody>
    </table>
</div>
<#--中心内容区域-->
<div id="rightArea">
    <div class="ui top pointing secondary menu">
        <a class="active item" data-tab="first">拓扑图</a>
        <a class="item" data-tab="stack">详细列表</a>
    </div>
    <div class="ui active tab segment" data-tab="first">
        <svg id="svg-canvas" style="min-height: calc(100vh - 150px);padding: 0px;" width="1201"></svg>
    </div>


    <div class="ui tab segment" data-tab="stack" style="padding: 2px">

    </div>
</div>
<script>
    <#-- 初始化UI -->
    $(function () {
        $("#rightArea .top.menu .item").tab();
        $("#stackNodeDetail").sidebar({
                "transition": 'overlay',
                "dimPage": false
            }
        );
    })
</script>
<script>
    //打开详情
    function openDetail(traceId) {
        $.ajax({
            url: "detail/graph/"+traceId,
            // 返回值类型
            dataType: "json",
            //成功后的回调函数,参数为返回的数据
            success: function (monitorData) {
                $("#svg-canvas").children().remove();
                buildTopo("svg-canvas",monitorData);
            }
        })
        buildStackTable(traceId);
    }

    // 构建表格树
    function buildStackTable(traceId) {
        let stackTab = $("#rightArea .ui.tab[data-tab='stack']");
        // 加载内容
        $.ajax({
            "url": "detail/stack/" + traceId,
            "dataType": "html",
            "success": function (responseHtml) {
                //  清空选项卡
                // stackTab.empty();
                stackTab.html(responseHtml);
                // 构建树表格
                treeTable(stackTab.find('.tree.table'));

                $(".tree.table tbody tr").click(function (e) {
                    e.stopPropagation();//阻止事件冒泡
                    $(this).parent().children("tr").removeClass("selected");
                    $(this).addClass("selected");
                    $('#stackNodeDetail').sidebar('show');
                    $('#stackNodeDetail .ui.content')
                        .first()
                        .load("node?traceId=" + traceId + "&nodeId=" + $(this).attr('nodeId'))
                });
            }
        });
    }
    function openCodeMap(traceId,spanId){
        let url="/map/code?traceId="+traceId+"&spanId="+spanId;
        window.open(url,'codeMapDialog','toolbar=no,location=no,resizable=no, height=500, width=680,,scrollbars=yes ,left=380,top=100');

    }
</script>
</body>
</html>