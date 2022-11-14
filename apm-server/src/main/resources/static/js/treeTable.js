function treeTable(table) {

    items = table.find("tr[nodeId]");

    // 找出所有的父节点
    items.each(function (index, element) {
        //添加折叠(minus)/展开(plus)按钮
        nodeId = $(element).attr('nodeId');

        if (items.is("[parentId='" + nodeId + "']")) {
            $(element).children("td:first").prepend("<i class='fold small plus square outline link icon '></i>");
        } else {
            $(element).children("td:first").prepend("<i class='mini grey minus icon'></i>");
        }

        $(element).find('td:first .fold.icon').click(function (e) {
            e.stopPropagation();//阻止事件冒泡
            expandOperation(this);
        })
    });

    // 遍历根节点
    table.find("tbody tr[parentId='root']")
        .each(function (index, element) {
            // 设置缩进状态
            setRetract(table, $(element), 0);
        })
    // 刷新节点显示状态
    refresh_show(table);
    // 展开所有节点
    expandAll(table);
}

// 递归设置 缩进
function setRetract(table, node, level) {
    node.children("td:first").css('padding-left', (level * 0.7) + 'em');
    nodeId = node.attr('nodeId');
    table.find("[parentId='" + nodeId + "']")
        .each(function (index, element) {
            setRetract(table, $(element), level + 1)
        });
}



// _show
// 刷新显示状态：逻辑为如果父节点为显示且 Class 包含expand
function refresh_show(table) {
    // 查找所有子节点
    table.find("tbody tr[parentId!='root']").each(function (index, element) {
        let parentId = $(element).attr('parentId');
        let parentNode = table.find("[nodeId='" + parentId + "']");
        if (parentNode.hasClass('expand') && !parentNode.hasClass('hidden')) {
            $(element).removeClass('hidden');
        } else {
            $(element).addClass('hidden');
        }
    })
}

function expandOperation(that) {
    tr = $(that).parents("tr:first");
    table = tr.parents("table:first");
    tr.toggleClass('expand');
    $(that).toggleClass('plus minus');
    refresh_show(table);
}
// 展开所有节点
function expandAll(table){
    table.find("tbody tr[nodeId]").each(function (index,element) {
        $(element).addClass('expand');
        // 修改图标样式
        $(element).find("td:first>i.fold:first").addClass('minus').removeClass('plus');
    })
    refresh_show(table);
}

//关闭所有节点
function closeAll(table){
    table.find("tbody tr[nodeId]").each(function (index,element) {
        $(element).removeClass('expand');
        // 修改图标样式
        $(element).find("td:first>i.fold:first").addClass('plus').removeClass('minus');
    })
    refresh_show(table);
}