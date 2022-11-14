//加载资源树的子节点
function loadTreeSubNode(item, succeed) {
    var i = $(item);
    i.attr('loaded', 'loading');// 正在加载
    fetch(i.attr('path') + "?child").then(function (response) {
        return response.text();
    }).then(function (html) {
        i.append(html);
        i.attr('loaded', 'true')
        if (succeed) {
            succeed.apply();
        }
    });
}

// 点击折叠按钮
function clickFoldNode(fold) {
    var item = $(fold).parent(".item");
    // 加载子节点
    if (item.is("[loaded='false'][hasChild='true']")) {
        loadTreeSubNode(item, function () {
            $(fold).nextAll('.list').toggle();
        });
    } else {
        $(fold).nextAll('.list').toggle();
    }
    // 加载子子节点
    item.find(">.list>.item[loaded='false'][hasChild='true']").each(function (index, item) {
        loadTreeSubNode(item);
    });
    var e = window.event || arguments.callee.caller.arguments[0];
    e.stopPropagation();
}


// 折叠全部 导航树
function doFoldAllNavTree() {
    $("#leftTreeContent .item.root").find(".list").hide();
    $("#leftTreeContent .item.lib").find(".list").hide();


}

// 使用导航树的 A 链接失效
function Disabled_Href() {
    var event = getEevent();
    if (event != null) {
        // event.stopPropagation();  // 阻止事件扩散
        event.preventDefault(); //阻止默认事件触发
    }
}

// 点击 资源树
function clickItemNode(item) {
    if ($(item).is(".dir")) { // 点击目录
        clickFoldNode($(item).children(".fold")[0]);
    } else if ($(item).is(".file")) {
        openFile($(item).attr('path'));
    }

    // 标识选中状态
    $("#leftTree .list .item ").removeClass('active');
    $(item).addClass('active');
    var e = window.event || arguments.callee.caller.arguments[0];
    if (e != null && e.stopPropagation) {
        e.stopPropagation();
    }
}

function getEevent() {
    var e = window.event || arguments.callee.caller.arguments[0];
    if (e != null && e.stopPropagation) {
        return e;
    }
}

// function doubleClickItemNode(item) {
//     if ($(item).is(".dir")) { // 双击目录
//         clickFoldNode($(item).children(".fold")[0]);
//     } else if ($(item).is(".file")) {
//         openFile($(item).attr('path'));
//     }
//     var e = window.event || arguments.callee.caller.arguments[0];
//     e.stopPropagation();
// }

function openDefaultFile(){
    // 初始化视图
    $(function () {
        let codeTabView = $("#rightContentView .active.tab");

        if (window.location.pathname.endsWith(".java")) {
            buildJavaView(codeTabView);
        } else {
            codeTabView.code({
                language: "other",
                escape: true // 特殊字符转义
            });
        }
        var code = codeTabView[0].code;
        let hash=window.location.hash==""?"#":window.location.hash;
        findDeclaration(code, hash).each((index, item) => {
            gotoItem(code, $(item));//  跳转至默认元素
        });
        initTab(codeTabView);
        codeTabView.show();
    })
}

// 打开新页面
function openFile(path) {
    // 添加选项页
    if (path.startsWith("#")) {
        path = window.location.pathname + path;
    }
    // 解析url
    var href = document.createElement('a');
    href.href = path;
    var name = href.pathname.substring(path.lastIndexOf("/") + 1);
    var pathName = href.pathname.replace(/\//g, "|") // 替换 所有的 / 替换成|

    var toolbarItem = $("#rightContentToolbar .item[data-tab='" + pathName + "']");

    if (toolbarItem.length > 0) { // 文件已经存在
        changeTab(pathName);
        if (href.hash !== "") { // 跳转至锚点定位
            var code = $("#rightContentView .tab.active")[0].code;
            findDeclaration(code, href.hash).each((index, item) => {
                gotoItem(code, $(item));//  跳转至定义
            });
        }
    } else {

        loadFile(name, href.pathname, pathName, href.hash === "" ? "#" : href.hash);
        loadTreeNodeByPath(href.pathname);// 加载该文件对应的树节点
    }
    // 保留打开历史记录
    saveOpenHistory(path);

    // 设置浏览器URL
    var browserUrl = path;
    if (path.startsWith("/jre") || path.startsWith("/lib")) {
        if (ide.projectRoot != null) {
            browserUrl = ide.projectRoot + "/$" + path.substring(1);
        }
    }
    window.history.pushState(null, null, browserUrl);
    setTitle();
}

function setTitle() {
    let pathname = window.location.pathname;
    let fileName=pathname.substring(pathname.lastIndexOf("/")+1)
    $("title").text(fileName+" -源码阅读网" );
}

// 加载指定路径下所有的tree 节点
function loadTreeNodeByPath(path) {
    // p jre lib
    var parentPath;
    if (path.startsWith("/p/")) {
        parentPath = $("#leftTreeContent .list .item.project.root").attr("path");
    } else if (path.startsWith("/jre/")) {
        parentPath = $("#leftTreeContent .list .item.jre.root").attr("path");
    } else if (path.startsWith("/lib/")) {
        parentPath = $("#leftTreeContent .list .item.lib.root").attr("path");
    }

    var paths = path.substring(parentPath.length + 1).split("/");
    for (var i in paths) {
        parentPath += "/" + paths[i];
        var item = $("#leftTreeContent .list .item[path='" + parentPath + "'][loaded='false'][hasChild='true']");
        if (item.length > 0) { // 当前节点未加载
            loadTreeSubNode(item, function () {
                loadTreeNodeByPath(path);// 递归加载该路径下 所有Tree节点
            });
        }
    }
}

// 事件初始化
$(function () {
    window.addEventListener('popstate', function (e) {
        var path = location.pathname;
        var pathName = path.replace(/\//g, "|") // 替换 所有的 \ ,替换成|
        var toolbarItem = $("#rightContentToolbar .item[data-tab='" + pathName + "']");
        if (toolbarItem.length > 0) { // 文件已经存在
            changeTab(pathName);
            // $("#rightContentToolbar").prepend(toolbarItem); // 将元素插入至开头
        }
    });
    // 切换 选项页
});

function saveOpenHistory(path) {
    var historyText = localStorage.getItem("openHistory");
    openHistory = historyText == null ? [] : historyText.split("\r\n");
    if (openHistory.indexOf(path) > -1) {
        openHistory.splice(openHistory.indexOf(path), 1);// 删除旧记录
    }
    var newLength = openHistory.unshift(path); // 添加新历史记录
    if (newLength > 20) { // 限定历史记录保存大小
        openHistory.splice(21, newLength - 20);
    }

    localStorage.setItem("openHistory", openHistory.join("\r\n"));
}

// 动态加载文件，并打开新的标签页
function loadFile(name, path, pathName, anchor, succeed) {
    var html = '<a class="item" data-tab="' + pathName + '" path="' + path + '" >' + name + '<i class="link  close   grey icon " onclick="closeTab(\'' + pathName + '\')"></i></a>'
    $("#rightContentToolbar").prepend(html);
    var type = "other";
    var loadUrl = encodeURI(path) + "?source";
    if (name.endsWith(".java")) {
        type = "java";
        // if (path.startsWith("/jre")) {
        loadUrl = encodeURI(path) + "?pretty";
        // }
    }
    fetch(loadUrl).then(function (value) {
        return value.text();
    }).then(function (value) { // 填充页面内容
        let contentHtml = format('<div class="ui tab code"  data-tab="{0}" path="{1}"> </div>', [pathName, path]);
        let $codeView = $(contentHtml);
        if (type === "java") {
            buildJavaView($codeView, value);
        } else {
            $codeView.code({
                src: value,
                language: type,
                escape: !loadUrl.endsWith("pretty") // 特殊字符转义
            })
        }


        $("#rightContentView").prepend($codeView);
        // 跳转至锚点定位
        var code = $codeView[0].code;
        findDeclaration(code, anchor).each((index, item) => {
            gotoItem(code, $(item));//  跳转至定义
            focusRelationItem(code, $(item));// 聚焦关联元素
        });
        return $codeView;
    }).then(function (codeTabView) {
        initTab(codeTabView);
        // 触发成功回调事件
        if (succeed) {
            succeed.call(codeTabView);
        }
    });
}

function buildJavaView($codeView, src) {
    $codeView.code({
        src: src,
        language: "java",
        escape: false,// 特殊字符转义
        openFile: function (code) {// 在标签页中打开文件
            let href = $(this).attr("href");
            openFile(href);
        }
    });

    return $codeView;


}


// 初始化选项卡
function initTab(codeTabView) {
    let tabPath = codeTabView.attr('data-tab');
    // 初始选项卡
    $("#rightContentToolbar .item[data-tab='" + tabPath + "']").tab({
        onVisible: function (path) {
            // 设置浏览器状态
            var url = $("#rightContentToolbar .item[data-tab='" + path + "']").attr('path');
            if (!url.startsWith(ide.rootPath)) {
                url=ide.rootPath+"/$"+url.substring(1,url.length);
            }
            window.history.pushState(null, null,
                url);
            setTitle();
        }
    });
    changeTab(tabPath);// 激活选项卡
}


// 切换标签页
function changeTab(pathName) {
    $("#rightContentToolbar .item[data-tab]").removeClass("active");
    $("#rightContentToolbar .item[data-tab='" + pathName + "']").addClass("active");
    $("#rightContentView .tab").removeClass("active");
    $("#rightContentView .tab[data-tab='" + pathName + "']").addClass("active");
    // $("#rightContentToolbar .item[data-tab]").tab('change tab',pathName);
}

//清除所有标签页
function clearAllTab() {
    $("#rightContentToolbar .item[data-tab]").remove();
    $("#rightContentView .tab[data-tab]").remove();
}

// 关闭指定标签页
function closeTab(pathName) {

    var item = $("#rightContentToolbar .item[data-tab='" + pathName + "']");
    $("#rightContentView .tab[data-tab='" + pathName + "']").remove();
    if (item.is(".active")) {
        if (item.prev().length > 0) {
            changeTab(item.prev().attr('data-tab'));
        } else if (item.next().length > 0) {
            changeTab(item.next().attr('data-tab'));
        }
    }
    item.remove();
}

// 定位当前文件 在导航树中的位置
function doActiveTreeItem() {
    if ($("#rightContentToolbar .item.active").length <= 0) {
        return;
    }
    var path = $("#rightContentToolbar .item.active").attr('path');
    activeItem(path);
}

function activeItem(path) {
    var item = $("#leftTreeContent .list .item[path='" + path + "']");
    //  显示所有父节点
    item.parents("#leftTreeContent .list").show();
    // 选中当前元素
    $("#leftTree .list .item ").removeClass('active');
    item.addClass('active');

    // 将当前元素显示在最中间
    var container = $("#leftTreeContent");
    container.animate({
        scrollTop: item.offset().top - container.offset().top + container.scrollTop() - (container.height() / 2)
    }, 5);
}


/**
 * {@link https://wcoder.github.io/notes/string-format-for-string-formating-in-javascript}
 * @param {string} format
 * @param {array} args
 */
function format(format, args) {
    return format.replace(/\{(\d+)\}/g, function (m, n) {
        return args[n] ? args[n] : m;
    });
}

// 打开搜索框
function openSearchDialog() {
    $('.ui.small.search.modal')
        .modal('show');
}

// 定义上下文
ide = {
    projectRoot: null,
    rootPath: null
};


function isOutTop(el, container) {
    return el.offset().top < container.offset().top;
}

function isOutBottom(el, container) {
    return el.offset().top - container.offset().top > container.height();
}

function isInShowView(el, container) {
    return !(isOutTop(el, container) || isOutBottom(el, container));
}

// 初始化搜索框
function initSearchDialog() {
    // 弹出框初始化
    $('.ui.search.modal').modal({
        dimmerSettings: {
            opacity: 0.1
        },
        onShow: function () {
            $('.ui.search.modal .ui.dropdown input.search').val('');
            $(".search.modal .menu>.item.nonProject>i.icon").removeClass('check');
        }
    });
    $(".search.modal .menu>.item.nonProject").click(function (e) {
        $(this).children('i').toggleClass('check');
        if ($('.ui.search.modal .ui.dropdown input.search').val().trim().length >= 2) {
            $('.ui.search.modal .ui.dropdown').api('query');
            $('.ui.search.modal .ui.dropdown').dropdown('show');
        }
        $('.ui.search.modal .ui.dropdown input.search').focus();
    });

    // 初始化搜索菜单初始化
    var searchPath = ide.rootPath + '/search?key={query}&nonProject={non}';
    $('.ui.search.modal .ui.dropdown')
        .dropdown({
            on: "custom",
            showOnFocus: false, // Focus 事件打开下拉框
            minCharacters: 2, // 自定义搜索
            forceSelection: false, // 强制选择
            selectOnKeydown: false,//基于方向键 强制选择
            action: function (text, value, element) {
                $(this).dropdown('clear');
                $(this).dropdown('hide');
                $(".search.modal").modal('hide');
                openFile(value);
            },
            apiSettings: {
                url: searchPath,
                urlData: {
                    non: function () {
                        return $(".search.modal .menu>.item.nonProject>.icon.check").length > 0;
                    }
                },
                dataType: 'json'/*,
                beforeSend: function (settings) {
                    // 包含项目外部资源
                    var nonProject =;
                    settings.data = {
                        nonProject: nonProject
                    }
                    return settings;
                }*/,
                cache: 'local'
            },
            saveRemoteData: false,
            message: {
                noResults: '啥也没找到'
            }
        });
}

// 初始化类结构弹出框
function initStructureDialog() {
    $(".structure.modal").modal({
        dimmerSettings: {
            opacity: 0.1
        },
        delay: {
            hide: 0,
            show: 0,
            search: 50,
            touch: 50
        }
    });
    // 初始化结构查找菜单
    $(".structure.modal .selection.dropdown").dropdown({
        fullTextSearch: false,
        selectOnKeydown: false,
        match: "text",
        sortSelect: true,
        action: function (text, value, element) {
            $(this).dropdown('clear');
            $(this).dropdown('hide');
            $(".structure.modal").modal("hide");
            var href = $(element).attr('href');
            var rootPath = $("#rightContentToolbar .item.active").attr('path');
            openFile(rootPath + href);
        }
    });
}

function openStructureDialog() {
    var path = $("#rightContentToolbar .item.active").attr('path');
    fetch(path + "?str").then(function (response) {
        return response.text();
    }).then(function (html) {
        $(".structure.modal  .search.selection.dropdown .menu").html(html);
        $(".structure.modal").modal("show"); // 打开弹出框
    })
}

$(function () {
    initSearchDialog();
    initStructureDialog();
});

