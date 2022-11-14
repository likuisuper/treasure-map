// 初始化code view
$.fn.code = function (code) {
    code.$target = $(this);
    code.$target[0].code = code;
    buildCodeLines(code);
    buildCodeStyle(code);
    buildCodeBlock(code);
    buildDynamic(code);// 动态视图
    // 填充视图
    $(this).append(code.$numberView);
    $(this).append(code.$lineView);
    initEvents(code);
    refreshUI(code);
    return code.$target;
}


// 构建代码行
// 样式渲染
// 折叠块构造
// 构造动态滚动条
// 初始化事件
// 构建代码行
function buildCodeLines(code) {
    let src;
    if (code.src) {
        src = code.src;
        if (code.escape) {
            src = src.replace(/</g, "&lt;")
                .replace(/>/g, "&gt;");// 转义特殊符号
        }
    } else {
        src = code.$target.html();
        code.$target.html("");
    }

    let lineCount = getLinesCount(src);
    // 构建代码行
    let numbers = new Array();
    numbers.push("<div class='lineNumber'>");
    for (var i = 1; i <= lineCount; i++) {
        numbers.push("<div class='number'>" + i + "</div>")
    }
    numbers.push("</div>")

    let lines = src.replace(/\n/g, "\n</div><div  class='line'>");
    linesHtml = format("<pre><div class='line'>{0}</div></pre>", [lines]);
    code.lineCount = lineCount;
    code.$numberView = $(numbers.join("\n"));
    code.$lineView = $(linesHtml);
    return code;
}

// 构建代码样式
function buildCodeStyle(code) {
    if (code.language) {
        code.$lineView.addClass(code.language)
    }
    hljs.highlightBlock(code.$lineView[0]);// 高亮显示
    return code;
}

// 构建代码块，以获得代码折叠功能
function buildCodeBlock(code) {
    replaceBlockSymbol(code.$lineView);

    //  初始化折叠按钮事件
    code.$lineView.click(function (e) {
        if ($(e.target).is(".line .icon.fold")) {
            doFoldLine(code, $(e.target));
            e.stopPropagation();
        } else if ($(e.target).is(".line .fold.tag")) {
            doFoldLine(code, $(e.target).prev(".icon.fold"), true);
            e.stopPropagation();
        }
    });
}

// 构建动态滚动条
function buildDynamic(code) {
    initDynamicView(code);
}

function refreshUI(code) {
    refresh_Dynamic_view(code);
}

// 初始化事件
function initEvents(code) {
    if (code.language === "java") {
        initJavaView(code);
    }
}

// 选中行
function selectLine(code, lineNumber) {
    code.$lines.eq(lineNumber - 1).addClass("selected");
    code.$numbers.eq(lineNumber - 1).addClass("selected");
}

// 跳转至元素
function gotoItem(code, $item, focus) {
    var container = code.$target;
    if (document.hidden) {
        setTimeout(function () {
            container[0].scrollTop = $item.closest(".line")[0].top - (container.height() / 5);
        }, 500);
        if (focus === undefined || focus) {
            focusRelationItem(code, $item);
        }
    } else {
        container.animate({
            scrollTop: $item.closest(".line")[0].top - (container.height() / 5)
        }, 10);
        focusRelationItem(code, $item);
        if (focus === undefined || focus) {
            focusRelationItem(code, $item);
        }
    }

}

// 跳转至指定行
function gotoLine(code, lineNumber) {
    let container = code.$target;
    container.animate({
        scrollTop: code.$lines[lineNumber - 1].top - container.height() / 5
    }, 1);
}


/**
 *基于锚点描述查找元素
 * @param code
 * @param anchor #method=xxx,variable=XXX,field=XXX,class=XXX,line=xxx
 */
function findDeclaration(code, anchor) {
    if (anchor === "#") {
        return code.$lines.find(".declaration.top");
    }
    let split = anchor.substring(1, anchor.length).split("=");
    if (split[0] === "position") {
        return code.$lines.find(format(".declaration[position='{1}']", split));
    } else {
        return code.$lines.find(format(".{0}.declaration[name='{1}']", split));
    }
}

//


/**
 * 聚焦自身，以及关联元素
 * @param code
 * @param $item block 大括号, 元素声明,元素引用
 */
function focusRelationItem(code, $item) {
    code.$lines.find(".focus").removeClass("focus");
    code.$lines.filter(".focus").first().removeClass("focus");
    // 聚集所在行
    $item.closest(".line").addClass("focus");

    if ($item.is(".block")) { // 大括号
        let block_number = parseInt($item[0].id) * -1;
        $item.addClass("focus");
        code.$lines.find("#" + block_number + "_block").first().addClass("focus")
        return;
    }
    // 元素引用
    $item.closest("a.class,a.field,a.variable,a.method").each((i, e) => {
        let href = $(e).attr("href");
        // 相同的引用
        code.$lines.find("a[href='" + href + "']").addClass("focus");
        // 找到内部声明元素
        if (href.startsWith("#")) {
            findDeclaration(code, href).addClass("focus");
        }
    });
    // 元素声明
    $item.closest(".declaration").each((i, e) => {
        let href;
        let $element = $(e)
        if ($element.is(".class")) { // 元素声明标签
            href = $element.is(".top") ? "#" : "#class=" + $element.attr('name')
        } else if ($element.is(".field")) { // 元素声明标签
            href = "#field=" + $element.attr('name');
        } else if ($element.is(".method")) { // 元素声明标签
            href = "#method=" + $element.attr('name');
        } else if ($element.is(".variable")) { // 元素声明标签
            href = "#position=" + $element.attr('position');
        }
        $element.addClass("focus");
        code.$lines.find("a[href='" + href + "']").addClass("focus");
    });
}


// 动态滚动条
function initDynamicView(code) {
    let lines = code.$lineView.children();
    let numbers = code.$numberView.children();
    code.$lineView.empty(); // 删除原所有行
    code.$numberView.empty();
    // 绑定代码行
    lines.each(function (i, element) {
        element.index = i;
        element.top = i * dynamic_lineHeight;
        $(element).css("top", element.top);
        $(element).css("height", dynamic_lineHeight);
        $(numbers[i]).css("top", element.top);
        $(numbers[i]).css("height", dynamic_lineHeight);
    });
    code.$numberView.css("height", lines.length * dynamic_lineHeight);
    code.$lineView.css("height", lines.length * dynamic_lineHeight);
    code.$lines = lines;
    code.$numbers = numbers;
    // 滚动时动态刷新行
    code.$target.scroll(() => refresh_Dynamic_view(code));
    // refresh_Dynamic_view(codeTabView);
}

var dynamic_lineHeight = 19;                 //  行高
var dynamic_show_area_height = Math.max($(window).height(), 960); // 容器高度
var dynamic_buffer_Height = 200;

function refresh_Dynamic_view(code) {
    let lines = code.$lines;
    let numbers = code.$numbers;
    let scrollTop = code.$target[0].scrollTop;
    let lineView = code.$lineView;
    let numberView = code.$numberView;

    var startArea = scrollTop - dynamic_buffer_Height;
    var endArea = scrollTop + dynamic_show_area_height + dynamic_buffer_Height;
    lineView.children().each(function (i) {
        var top = this.top;
        if (this.hide === true || top < startArea || top > endArea) {
            $(this).remove();
            $(numbers[this.index]).remove();
        }
    });
    let lastItem;
    let lastNumberItem;
    for (let k = 0; k < lines.length; k++) {
        if (lines[k].hide === true) {
            continue;
        }
        if (lines[k].top < startArea) {
            continue;
        }
        if (lines[k].top > endArea) {
            break;
        }
        if ($(lines[k]).parent().length == 0) {
            if (lastItem !== undefined) {
                $(lastItem).after(lines[k]);
                $(lastNumberItem).after(numbers[k]);
            } else {
                lineView.prepend(lines[k]);
                numberView.prepend(numbers[k]);
            }
        }
        lastItem = lines[k];
        lastNumberItem = numbers[k];
    }
}


function initJavaView(code) {
    function aClick(e) {
        return $(e.target).closest("a").map((i, element) => {
            if (e.ctrlKey || macKeys.cmdKey) {
                let href = $(element).attr("href");
                if (href.startsWith("#")) {
                    // 跳转至元素内部定义
                    findDeclaration(code, href).each((index, item) => {
                        gotoItem(code, $(item));//  跳转至定义
                        //focusRelationItem(code, $(item));// 聚焦关联元素
                    });
                    e.stopPropagation();
                    e.preventDefault(); // A 连接失效
                    return true;
                } else if (code.openFile) {
                    // 打开外部文件
                    code.openFile.call(element, code);
                } else {
                    return false;
                }
            }
            e.preventDefault(); // A 连接失效，阻止默认事件
            return false;
        }).get()[0];
    }


    // 代码元素点击
    function elementClick(e) {
        let $target = $(e.target);
        // 移除基它元素的focus 样式
        $target.each((i, e) => focusRelationItem(code, $(e)));
    }

    function aMsousemove(e) {
        if (e.ctrlKey || macKeys.cmdKey) {
            $(e.target).closest("a").addClass("active");
        }
    };

    function aMouseout(e) {
        $(e.target).closest("a").removeClass("active");

    };

    code.$lineView.mousemove(function (e) {
        aMsousemove(e);
    });
    code.$lineView.mouseout(function (e) {
        aMouseout(e);
    });


    code.$lineView.click(function (e) {
        if (aClick(e)) {
            return;
        }
        elementClick(e);
    });
    // ======end
}


var BREAK_LINE_REGEXP = /\r\n|\r|\n/g;

function getLinesCount(text) {
    return (text.match(BREAK_LINE_REGEXP) || []).length + 1;
}

function format(format, args) {
    return format.replace(/\{(\d+)\}/g, function (m, n) {
        return args[n] ? args[n] : m;
    });
}


var id_number = 0;
var blockStack = new Array();

// 替换 {}()[] 包裹符号
function replaceBlockSymbol(codeView) {
    // 找到所有 包含{}()[]的文本
    var regExp = new RegExp("[\[\(\{\\]\)\}]")

    let finds = ['{', '}', '[', ']', '(', ')'];
    let formatText = ":contains('{0}'):not(.hljs-comment):not(.hljs-string)"
    for (let i = 0; i < finds.length; i++) {
        finds[i] = format(formatText, finds[i]);
    }
    finds.push(".hljs-comment:contains('/*')")
    finds.push(".hljs-comment:contains('*/')")

    // 重置
    id_number = 0;
    blockStack = new Array();

    codeView.find(finds.join(","))
        .contents()
        .filter(function () {
            return this.nodeType === 3
                && (regExp.test(this.nodeValue) ||
                    this.nodeValue.indexOf("/*") > -1 ||
                    this.nodeValue.indexOf("*/") > -1);
        }).replaceWith(function () {
        //堆栈
        if (this.nodeValue.startsWith("/**")) {
            return this.nodeValue.replace("/**", wrap("comment", true))
                .replace("*/", wrap("comment", false));
        }
        return this.nodeValue
            .replace(/\(/g, wrap("little", true))
            .replace(/\)/g, wrap("little", false))
            .replace(/\[/g, wrap("middle", true))
            .replace(/\]/g, wrap("middle", false))
            .replace(/\{/g, wrap("big", true))
            .replace(/\}/g, wrap("big", false))
            .replace("/*", wrap("comment", true))
            .replace("*/", wrap("comment", false));
    });

    function wrap(type, isBegin) {
        return function (symbol, i, e) {
            let id;
            let fold = new String(); // 折叠按钮
            if (isBegin) {
                id_number++;
                blockStack.push(id_number);
                id = id_number;
                if (symbol === "{" || (e.startsWith("/*") && !e.endsWith("*/"))) { //单行注释不添加折叠按钮
                    // onclick='$(this).toggleClass("minus plus")'

                    fold = "<i class='ui icon minus square outline fitted  link fold' ></i>"
                }
            } else {
                id = blockStack.pop() * -1;
            }
            return format("<span class='block {0}' id={1}_block>{2}{3}</span>", [type, id, symbol, fold]);
        };
    }
}

/**
 * 折叠代码块
 * @param block（必填），折叠按钮
 * @param expand 是否展开，如果为空将根据现有状态进行切换。
 */
function doFoldLine(code, blockFold, expand) {
    // minus 展开 plus 折叠状态
    if (expand === undefined) {
        expand = !blockFold.hasClass("minus"); // 切换状态
    } else if (expand === blockFold.hasClass("minus")) {
        return;
    }

    // 0. 获取所有行集
    var lines = code.$lines;
    var numbers = code.$numbers;

    let startLine, endLine;
    // 1.1.找到开始行
    startLine = blockFold.closest(".line");
    // 1.2.找到结束行
    let block_number = parseInt(blockFold.parent()[0].id) * -1;
    endLine = lines.find("#" + block_number + "_block").closest(".line");
    // endLine = lines.eq(endLine[0].index - 1);// 保留最后一行
    // 3.隐藏/显示范围内的行
    lines.slice(startLine[0].index + 1, endLine[0].index + 1).each(function () {
        if (!expand && this.hide !== true) {// 折叠
            this.hide = true;
            this.hide_trigger = blockFold[0];
        } else if (expand && this.hide_trigger === blockFold[0]) { // 展开,（由谁折叠就谁来展开）
            this.hide = false;
        }
    });

    // 4、重新计算行位置
    let hideHeight = endLine[0].top - startLine[0].top;
    for (let i = endLine[0].index + 1; i < lines.length; i++) {
        if (expand) {
            lines[i].top += hideHeight;
        } else {
            lines[i].top -= hideHeight;
        }
        $(lines[i]).css("top", lines[i].top);
        $(numbers[i]).css("top", lines[i].top);
    }

    // 5、重新计算行面版行高
    code.$lineView.css("height", (lines.last()[0].top + dynamic_lineHeight) + "px");
    code.$numberView.css("height", (lines.last()[0].top + dynamic_lineHeight) + "px");

    refresh_Dynamic_view(code);
    // 6.切换折叠图标
    blockFold.toggleClass("minus plus");
    // 7.添加/移除折叠标记
    if (expand) {
        blockFold.next(".fold.tag").remove();
    } else {
        blockFold.after("<span class='fold tag'><span>" + endLine.text().trim() + "</span></span>");
    }
}


/**
 * 0: hide:如果其子属性为空则隐藏 折叠按钮
 * 1: 类型标签值 local\file\parameter
 * 2: 属性名称
 * 3: 属性类别及ID
 * 4: 值类型
 * 5: 值描述
 * @type {string}
 */
var template_variable_item = '<div class="item">\n' +
    '        <i class=" right triangle icon {0}"></i>\n' +
    '        <label class="ui  circular type label {1}"></label>\n' +
    '        <span class="key">{2}</span>=<span class="type">{3}</span ><span class="value {4}">{5}</span>\n' +
    '           \n' +
    '</div>';

function loadVariable($list, url) {
    $list.empty();
    fetch(url).then(function (response) {
        return response.json();
    }).then(function (value) {
        value['@refs'] = new Array();
        setRef(value, value);
        value = filterRef(value, url);// 基于锚点过滤值
        // 遍历其属性
        for (let scope of ["field", "parameter", "local"]) {
            if (!(scope in value)) {
                continue;
            }
            let keys = Object.getOwnPropertyNames(value[scope]);
            keys.filter(k => !k.startsWith("@")) //  '@'开头为描述属性
                .forEach(key => {
                    $list.append(buildVariableTableItem(scope, key, value[scope][key], value));
                })
        }
        $list[0].data = value;
    }).then(function () { // 添加折叠事件
        /* $list.find(".right.triangle.icon").click(function () {
             $(this).nextAll(".list").first().toggle();
         });*/
    });
}

function setRef(root, data) {
    if ('@id' in data) {
        let id = data['@id'];
        root['@refs'][id] = data;
    }
    $.each(data, function (key, value) {
        if (value instanceof Object) {
            setRef(root, value);
        }
    });
}

//基于锚点 表达取值
function filterRef(value, url) {
    let a = document.createElement('a');
    a.href = url;
    let anchor = a.hash.trim().replace('#', ''); //读取锚点
    if (anchor === "") {
        return value;
    }
    let root = value;
    let express = anchor.split(".");
    for (let exp of express) {
        if (!(exp in value)) {
            console.error("错误的变量表达示："+anchor);
            return null;
        }
        value = value[exp];
        if (('@ref' in value) && value['@ref']) {
            value = root['@refs'][value['@ref']];
        }
    }
    let result = {"@refs": root['@refs'], local: {var: value}};

    return result;
}


//构建变量列表
function buildVariableTableItem(scope, name, item, root) {
    let fold = " ", type = " ", valueType = " ", value = " ";
    valueType = typeof item; //4 值类型
    if (item instanceof Array) {
        type = "{Array@}"; //3 属性类型
        value = "size=" + item.length;
    } else if (item instanceof Object) {
        if (('@ref' in item) && item['@ref']) {
            /* value = "id=" + item['@ref'];
             valueType = "ref";//{引用@12}
             type = format("{{0}}", ["引用"]); //3 属性类型*/
            let refValue = root['@refs'][item['@ref']];
            return buildVariableTableItem(scope, name, refValue, root)
        } else {
            let simpleType = item['@type'].substring(item['@type'].lastIndexOf(".") + 1);
            if ('@id' in item) {
                type = format("{{0}@{1}}", [simpleType, item['@id']]); //3 属性类型
            } else {
                type = format("{{0}}", [simpleType]); //3 属性类型
            }
        }
        // 数组类型 显示数量
        if ('@items' in item) {
            value = "size=" + item['@items'].length;
        }
    } else {
        fold = "hide";    //0 是否显示折叠图标
        value = item != null ? item.toString() : "null";//5 值描述
    }

    let itemHtml = format(template_variable_item, [fold, scope, name, type, valueType, value]);
    let $item = $(itemHtml);


    $item[0].data = item;
    $item.find(".triangle.icon").click(function () {
        if ($(this).nextAll(".list").length == 0) { // 构建子元素
            buildVariableChildren($item, root);
        }
        $(this).nextAll(".list").first().toggle();
        $(this).toggleClass('down right');
    });
    return $item;
}

function buildVariableChildren($item, root) {
    // 构建对像属性
    let data = $item[0].data;
    var itemChild = $('<div class="list" style="display: none"></div>');

    // 构建对像属性
    if (('@ref' in data) && data['@ref']) {
        let id = data['@ref'];
        data = root['@refs'][id];//获取引用
    }
    if ('@keys' in data) { // map 构建
        for (let i = 0; i < data['@keys'].length; i++) {
            let entry = {"@type": "Map.Entry", "key": data['@keys'][i], "value": data['@items'][i]};
            itemChild.append(buildVariableTableItem("key", "[" + i + "]", entry, root))
        }

    } else if ('@items' in data) { // 数组类型
        for (let i = 0; i < data['@items'].length; i++) {
            itemChild.append(buildVariableTableItem("hide", "[" + i + "]", data['@items'][i], root))
        }
    } else if (data instanceof Array) {
        for (let i = 0; i < data.length; i++) {
            itemChild.append(buildVariableTableItem("hide", "[" + i + "]", data[i], root))
        }
    } else {
        let keys = Object.getOwnPropertyNames(data);
        keys.filter(k => !k.startsWith("@"))
            .forEach(k => {
                itemChild.append(buildVariableTableItem("field", k, data[k], root))
            });
    }
    if (itemChild.children().length == 0) {
        itemChild.append("<span class='empty object'>没有任何属性</span>");
    }
    $item.append(itemChild);
}


