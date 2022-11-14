var template_stack = '\n' +
    '<div class=" item phase top aligned" phaseId="{0}">\n' +
    '    <div class="ui top attached compact stack segment">\n' +
    '        <div class="ui  top  attached large label">\n' +
    '                        {1}\n' +
    '                        <i  class="delete  icon" style="float: right;"></i>\n' +
    '                    </div>' +
    '        <table class="ui single line very basic stack   table">\n' +
    '        </table>\n' +
    '    </div>\n' +
    '</div>';

var template_stack_item = ' <tr tid="{0}">\n' +
    '                            <td >\n' +
    '                               {1}:{2}, {3}\n' +
    '                                <span class="comment">{4}</span>\n' +
    '                                 {5}\n' +
    '                                <i class="ui icon link  grey hidden code"></i>\n' +
    '                            </td>\n' +
    '                        </tr>';

/**
 * 0: hide:如果其子属性为空则隐藏
 * 1: 类型标签值 local\file\parameter
 * 2: 属性名称
 * 3: 属性类别及ID
 * 4: 值类型
 * 5: 值描述
 * @type {string}
 */
var template_variable_item = '<div class="item">\n' +
    '        <i class="right triangle link icon {0}"></i>\n' +
    '        <a class="ui  circular type label {1}"></a>\n' +
    '        <span class="key">{2}</span>=<span class="type">{3}</span ><span class="value {4}">{5}</span>\n' +
    '           \n' +
    '</div>';

function openStack(option) {
    if (option.url !== undefined) {
        fetch(url).then(function (response) {
            return response.json();
        }).then(function (value) {
            buildPhaseTable(value.phase);
        });
    } else if (option.content !== undefined) {
        let value= JSON.parse(option.content);
        buildPhaseTable(value.phase);
    }else {
        console.error("错误的参数，url或content至少有一个不为空")
    }

}

// 构建阶段表格
function buildPhaseTable(phase) {
    // 构建表格行
    var items = phase.items;
    var phaseUi = $(format(template_stack, [phase.id, phase.name]));
    var tableUi = phaseUi.find("table");
    for (let i = 0; i <items.length ; i++) {
        tableUi.append(buildPhaseTableRow(items[i]));
    }
    $("#centerContent .container>.list").append(phaseUi);
    phaseUi[0].data = phase;

    // 初始化事件
    tableUi.find("tr").click(function () {
        // 点击展开分支
        if (this.data.branchPhase !== undefined) {
            openBranchPhase(this);
        }
        // 刷新状态
        refreshStatus(this);// 基于点击当前行 刷新状态
    });

    tableUi.find("tr").mouseenter(function () {
        $(this).find(".icon.hidden").css('visibility', 'visible');
    });
    tableUi.find("tr").mouseleave(function () {
        $(this).find(".icon.hidden").css('visibility', 'hidden');
    });
    tableUi.find("tr .icon.code").click(function () {
        var item = $(this).parentsUntil(".stack.table", "tr")[0].data;
        loadSrc(item);
        loadVariable(item);
    });

    // 关闭 阶段项
    phaseUi.find(".top.label .delete.icon").click(function () {
        $(this).parentsUntil("#centerContent .container>.list", ".item").remove();
    })
    return phaseUi[0];
}

function loadSrc(item) {
    $("#layout_bottom .title.label span").text(format("{0}#{1}", [item.className, item.methodName]));
    fetch(item.src+"?pretty").then(function (response) {
        return response.text();
    }).then(value => {
        $(".src").empty();
        let $codeView = $(".src").code({
            src: value,
            language: "java",
            escape: false// 特殊字符转义
        });
        // 选中行
        selectLine($codeView[0].code,item.lineNumber);
        // 跳转至指定行
        gotoLine($codeView[0].code,item.lineNumber);
        showSrcArea();
    });

   /* fetch(item.codeUrl).then(function (response) {
        return response.json();
    }).then(function (value) {
        buildCodeArea(value);
        // 高亮选中当前行
        var line = item.stackElement.lineNumber;
        var lime_tr = $('#layout_bottom .src pre .hljs-ln-line.hljs-ln-n')
            .filter('[data-line-number="' + line + '"]')
            .parentsUntil("pre", "tr");
        lime_tr.addClass("selected");
        gotoItem(lime_tr, $("#layout_bottom .src"));
    });*/
}

function loadVariable(item) {
    if (item.variables === undefined || item.variables == null) {
        return;
    }
    var rootList = $("#layout_bottom .variable>.list");
    rootList.empty();
    fetch(item.variables).then(function (response) {
        return response.json();
    }).then(function (value) {
        // 遍历其属性
        for (let scope of ["field", "parameter", "local"]) {
            let keys = Object.getOwnPropertyNames(value[scope]);
            keys.filter(k => !k.startsWith("@")) //  '@'开头为描述属性
                .forEach(key => {
                    rootList.append(buildVariableItem(scope, key, value[scope][key]));
                })
        }
    }).then(function () { // 添加折叠事件
        $(".variable .right.triangle.icon").click(function () {
            $(this).nextAll(".list").first().toggle();
        });

        // --------初始化提示框
        $(".variable .label.field").popup({
            on: "click",
            content: "属性（field）",
            variation: "inverted tiny"
        })
        $(".variable .label.local").popup({
            on: "click",
            content: "局部变量（local）",
            variation: "inverted tiny"
        })
        $(".variable .label.parameter").popup({
            on: "click",
            content: "方法参数（param）",
            variation: "inverted tiny"
        })
    });
}


function buildVariableItem(scope, name, item) {
    let fold = " ", type = " ", valueType = " ", value = " ";
    valueType = typeof item; //4 值类型
    if (item instanceof Array) {
        type = "{Array@}"; //3 属性类型
        value = "size=" + item.length;
    } else if (item instanceof Object) {
        if (('@ref' in item) && item['@ref']) {
            fold = "hide";
            value = "id=" + item['@ref'];
            valueType = "ref";//{引用@12}
            type = format("{{0}}", ["引用"]); //3 属性类型
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
    let itemUI = $(itemHtml);


    // 构建对像属性
    if (item instanceof Object) {
        if (('@ref' in item) && item['@ref']) { // 引用对像 不构建属性
            return itemUI;
        }

        var itemChild = $('<div class="list" style="display: none"></div>');
        if ('@keys' in item) { // map 构建
            for (let i = 0; i < item['@keys'].length; i++) {
                let entry = {"@type": "Map.Entry", "key": item['@keys'][i], "value": item['@items'][i]};
                itemChild.append(buildVariableItem("key", "[" + i + "]", entry))
            }

        } else if ('@items' in item) { // 数组类型
            for (let i = 0; i < item['@items'].length; i++) {
                itemChild.append(buildVariableItem("hide", "[" + i + "]", item['@items'][i]))
            }
        } else if (item instanceof Array) {
            for (let i = 0; i < item.length; i++) {
                itemChild.append(buildVariableItem("hide", "[" + i + "]", item[i]))
            }
        } else {
            let keys = Object.getOwnPropertyNames(item);
            keys.filter(k => !k.startsWith("@"))
                .forEach(k => {
                    itemChild.append(buildVariableItem("field", k, item[k]))
                });
        }
        if (itemChild.children().length == 0) {
            itemChild.append("<span class='empty object'>没有任何属性</span>");
        }
        itemUI.append(itemChild);
    }

    return itemUI;
}

// 构建代码区
function buildCodeArea(value) {
    var src = value.src.replace(/</g, "&lt;").replace(/>/g, "&gt;");// 转义特殊符号
    $("#layout_bottom .src pre").html(src);
    $("#layout_bottom .title.label span").text(format("{0}#{1}", [value.class, value.method]));

    $('#layout_bottom .src pre').each(function (i, block) {
        hljs.highlightBlock(block);// 高亮显示
        block.innerHTML = hljs.lineNumbersValue(block.innerHTML); // 添加行号
        // 修改行号
        var lineNumbers = $(block).find('.hljs-ln-line.hljs-ln-n');
        var firstNumber = value.firstNumber;
        for (var j = 0; j < lineNumbers.length; j++) {
            $(lineNumbers[j]).attr('data-line-number', firstNumber++);
        }
    });
    showSrcArea();
}

/**
 * 表格行状态：
 1、选中行 (全局唯一）
 获得：通过点击行触发，同时包含激活状态
 失去：点击任一表格行意其它行
 2、激活行(在阶段表格中唯一）
 获得：点击触发当前行，或子级中的任意行
 失去：点击同级表格或父级表格中的其它行
 表格状态：
 获得：点击其它表格中的无关行（非父类行）

 * @param selectedRow
 */
function refreshStatus(selectedRow) {
    $(".stack.table tr.selected").removeClass("selected");
    $(".stack.table tr.active").removeClass("active");
    $("#centerContent .phase.item .segment").addClass("disabled");

    // 选中当前行
    $(selectedRow).addClass("selected");
    // 激活分支phase
    $("#centerContent .phase")
        .filter((index, item) => item.parentRow === selectedRow)
        .children(".segment").removeClass("disabled");

    // 激活所有父类行及父面版
    let row = selectedRow;
    while (row !== undefined) {
        $(row).addClass("active");
        $(row).closest("div.phase>.segment").removeClass("disabled");
        row = $(row).closest("div.phase")[0].parentRow;
    }
}

// 构建堆栈表格行
function buildPhaseTableRow(item) {
    var method = item.methodName;
    var line = item.lineNumber;
    var fullClassName = item.className;
    var className = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
    var comment = " ";
    var icons = " ";
    if (item.comment != null) {
        comment = item.comment;
    }
    if (item.branchPhase !== undefined) {
        icons = '<i class="ui icon  caret right"></i>';
    }
    var tr = $(format(template_stack_item, [item.id, method, line, className, comment, icons]));
    tr[0].data = item;
    return tr;

}

// 打开分支阶段
/**
 *
 * @param phaseRow
 */
function openBranchPhase(phaseRow) {
    // 判断当前阶段是否已经展开
    if ($("#centerContent .phase").is((index, item) => item.parentRow === phaseRow)) {
        return;
    }
    let phaseUi = buildPhaseTable(phaseRow.data.branchPhase);
    // 设置展开阶段的父类行
    phaseUi.parentRow = phaseRow;
}

/**
 * 将阶段 与阶段 项进行互相关联
 * phase.items -- 阶段下所有的堆栈项
 * item.phase  -- 堆栈项对应的阶段
 * item.branchPhase --  堆栈项关联的分支阶段
 * @param phases
 * @param items
 */
function joinPhaseItems(phases, items) {
    // 到序遍历
    for (var i = items.length - 1; i >= 0; i--) {
        var id = items[i].id;
        var pid = id.substring(0, id.lastIndexOf("."));
        phases.forEach(function (phase) {
            if (phase.id == pid) {// 属于当前阶段
                items[i].phase = phase;
                if (phase.items === undefined) {
                    phase.items = new Array();
                }
                phase.items.push(items[i]);
            }
            if (phase.id == id) { // 分支阶段
                items[i].branchPhase = phase;
            }
        });

    }
}

// 打开源码区
function showSrcArea() {
    if ($("#layout_bottom").is(":visible")) {
        return;
    }
    $("#layout_bottom").show();
    $("#layout_center_resize").show();
    $("#layout_center_resize").css("bottom", "300px");
    $("#layout_center").css("bottom", "300px");
    $("#layout_bottom").css("height", "300px");
}

// 找开源码区
function closeSrcArea() {
    $("#layout_bottom").hide();
    $("#layout_center_resize").hide();
    $("#layout_center").css("bottom", "0px");
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

