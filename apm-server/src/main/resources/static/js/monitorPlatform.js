var selectTraceId = null;

/*
 * 打开监控详情
 * */
function openMonitorDetail(projectid, traceId) {

    // 初始化界面
    $("#emptyTip").hide();
    $("#monitorDetail").show();
    $("#svg-canvas").children().remove();
    // 初始化画布大小
    $("#svg-canvas").attr('width', $("#monitorDetail").width());
    // 清除节点详情信息
    $("#nodeDetail").children("div.entry").remove();
    // 装载监控数据
    var monitorData = $.ajax({
        url: "/p/" + projectid + "/monitor/getTraceGraph?traceId=" + traceId,
        async: false
    }).responseJSON;

    // 设置标题
    $("#monitorDetailTitle").text(monitorData.title);

    // 构建流程图
    buildFlow(projectid, traceId, monitorData);

    // 打开默认节点详情
    openNodeDetails(projectid, traceId, monitorData.showDefaultNode.id);
    selectTraceId = traceId;
    $(".button.save.snapshot").removeClass("disabled");
}

function buildFlow(projectId, traceId, data) {
    var g = buildTopo("svg-canvas", data, {
            nodeClick: function (id, index, array) {
                var node = g.node(id);
                openNodeDetails(projectId, traceId, id);
            }
        }
    );
}

/**
 * 打开监控详情中的某个节点
 * @param nodeId
 * @param nodeType
 */
function openNodeDetails(projectid, traceId, nodeId) {
    $("#nodeDetail").children("div.entry").remove();
    var nodeDiv = $('<div></div>');
    nodeDiv.attr("nodeId", nodeId);
    nodeDiv.attr("class", "entry");
    var htmlobj = $.ajax({url: "/p/" + projectid + "/monitor/" + traceId + "/" + nodeId + ".html", async: false});
    nodeDiv.html(htmlobj.responseText);
    $("#nodeDetail").append(nodeDiv);

}

var lastIndex = 0
var lastUpdateTime = 0;

//刷新监控列表
function refreshMonitorList(projectid) {
    $("#monitorListBody").children().remove();
    //var newItems = $("#itemFilter").ajaxSubmit({ async: false}).responseJSON;
    // var form = new FormData(document.getElementById("itemFilter"));
    var dataForm = $("#itemFilter").serialize();
    var newItems = $.ajax({
        url: "/p/" + projectid + "/monitor/getNodeByTime",
        data: $("#itemFilter").serialize(),
        async: false
    }).responseJSON;
    appendItems(projectid, newItems);
    lastUpdateTime = new Date().getTime();

    // $("#monitorListBody tr").on("click", function () {
    //     $(this).parent().find("tr.focus").toggleClass("focus");//取消原先选中行
    //     $(this).toggleClass("focus");//设定当前行为选中行
    // });
}

// 拉取新的监控数据
function pullNewItem(projectid) {
    // 如果最后更新时间超过两分钟，拉取则换成更新
    if (lastIndex == 0 || (new Date().getTime() - lastUpdateTime) > (2 * 60 * 1000)) {
        refreshMonitorList(projectid);
        return;
    }

    var newItems = $.ajax({
        url: "/p/" + projectid + "/monitor/getNodeByIndex?lastIndex=" + lastIndex + "&maxSize=200",
        async: false
    }).responseJSON;
    appendItems(projectid, newItems);
    lastUpdateTime = new Date().getTime();
}

function appendItems(projectid, newItems) {
    if (newItems === undefined || newItems.length == 0) {
        return;
    }

    newItems.some(function (value, index, array) {
        var itemText = "<tr onclick=\"doSelect(this); openMonitorDetail('" + projectid + "','" + value.traceId + "')\">" + value.title + "<td title='" + value.title + "'>" + value.title + "</td></tr>";
        $("#monitorListBody").prepend(itemText);
    });
    lastIndex = newItems[newItems.length - 1].index;
}

function clearItem() {
    $("#monitorListBody").children().remove();
}

// 添加样式选中效果
function doSelect(t) {
    $("#monitorListBody tr.focus").toggleClass("focus");//取消原先选中行
    $(t).toggleClass("focus");//设定当前行为选中行
}

function doSaveSnapshot(projectid) {
    var resultInform = $.ajax({
        url: "/p/" + projectid + "/snapshot/save",
        data: $("#newSnapshotForm").serialize() + "&traceId=" + selectTraceId,
        async: false
    }).responseJSON;
    if (resultInform.result) {
        alert(resultInform.message);
    } else {
        alert(resultInform.errorMessage);
    }
}
function openCreateSnapshot(){
    $("#snapshotDialog").modal('show');
}


// 打开创建系统快照事件窗口
function openCreateSystemSnapshot() {
    $("#systemSnapshotDialog").load('monitor/openSystemSnapshot?traceId=' + selectTraceId);
    $("#systemSnapshotDialog").modal('show');
}


/*function doSaveSystemSnapshot(projectid) {
    var resultInform = $.ajax({
        url: "/p/" + projectid + "/monitor/doSaveSystemSnapshot",
        data: $("#systemSnapshotForm").serialize(),
        async: false
    }).responseJSON;
    if (resultInform.result) {
        $("#systemSnapshotDialog").modal('hide');
        alert(resultInform.message);
    } else {
        alert(resultInform.errorMessage);
    }
}*/



