var concentricLayoutOptions = {
    name: 'concentric',
    fit: false,  // 是否刚好填满画
    // minNodeSpacing:50,//  节点间最小间距
    nodeDimensionsIncludeLabels: true, // 布局时是否考虑标签大小
    concentric: function (ele) {
        return ele.data('weight');
    },
    levelWidth: function (nodes) {
        return 10;
    },
    padding: 10 // 视图与布画的内间距
};
var coseLayoutOptions = {
    name: 'cose',
    animate: true,
    nodeDimensionsIncludeLabels: true,
    padding: 0 // 视图与布画的内间距
};

function buildMap(data) {
    var cy = window.cy = cytoscape({
        container: document.getElementById('map_body'), // 容器id
        minZoom: 0.2,// 缩放最小比例
        maxZoom: 5, // 缩放最大比例
        wheelSensitivity: 0.1,
        boxSelectionEnabled: true,// 是否允许框选 按住ctrl或shift 拖动鼠标框选
        style: fetch('/css/map.cycss?v=1').then(function (value) {
            return value.text();
        }),
        layout: coseLayoutOptions,
        elements: data
    });

    // 选中节点 后
    cy.on('select', 'node.code', function (event) {
        let arr = event.target.outgoers("node.code").sort((a, b)=>a.data('order').localeCompare(b.data('order')));
        for (let i = 1; i <= arr.length; i++) {
            event.target.edgesTo(arr[i]).style('label',i);
        }
    });
    // 取消选中 只删除当前取消节点
    cy.on('unselect', 'node.code', function (event) {

    });

    // 自定义设置选项
    cy.settings = {
        subSelectUnionNode: function () {
            return false;
        }
    };
    cy.loadElement = loadElement;
    cy.doFind = doFind;
    cy.doRefresh = doRefresh;
    return cy;
}

function loadElement(url, select, fullLayout) {
    fetch(url)   // 加载数据
    .then(function (res) {  //封装json
       return res.json();
    }).then(function (data) { // 添加节点
        return cy.add(data);
    }).then(function (eles) { // 布局
        if (fullLayout) {
            doRefresh();
        } else {
            eles.layout({
                name: "circle",
                fit: false,
                nodeDimensionsIncludeLabels: true
            }).run();
        }
        cy.center(eles);
        return eles;
    }).then(function (eles) { // 选中
        if (select) {
            cy.nodes(':selected').unselect();
            eles.select();
        }
    });
}

// 查找元素
function doFind(key) {
    cy.batch(function () {
        cy.nodes('.find').removeClass('find');
        if (key != "") {
            // 正则匹配
            key = key.replace(/\*/g, ".*");
            key = "^" + key + ".*$";
            key = key.toLowerCase(); //勿略大小写

            var findNodes = cy.filter(function (element, i) {
                if (!element.isNode()) {
                    return false;
                }
                var name = element.data('name').trim();
                return name.toLowerCase().search(key) >= 0;
            });
            findNodes.addClass("find");
            $("#find_element label").html(findNodes.length);
        }
        $("#map_body").focus();
    });
}

function showHot() {
    cy.batch(function () {
        cy.filter(function (element, i) {
            return element.isNode() && !element.hasClass("snapshot");
        }).addClass('hot');
    });
}

function closeHot() {
    cy.batch(function () {
        cy.nodes('.hot').removeClass('hot');
    });
}




function doRefresh() {
    cy.layout(coseLayoutOptions).run();
}
// 在iframe 嵌入模式下，网页全屏展开
function doFullScreen() {
    if ($("body.iframe").length == 0) {
        return;
    }
    // 找到父元素的iframe
    let $iframe=null;
    window.parent.$("iframe").each((i,element)=>{
        if (window.parent.$(element).contents().find("body") == $("body")) {
            $iframe=window.parent.$(element);
        }
    })
    if ($iframe != null) {
        $iframe.css("position","fixed");
        $iframe.css("top","0");
        $iframe.css("bottom","0");
        $iframe.css("left","0");
        $iframe.css("right","0");
        $iframe.css(" z-index","999");
    }

}
