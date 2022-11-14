// 拖动调整 大小
function initBasicUi() {
    // 初始化数据展示提示框
    $("#data-button").popup({
        on: "click",
        onShow: function (item) {
            let $list = $(this).find(".root.list");
            loadVariable($list, item.url);
        },
        onVisible: function () {
            let maxHeight = document.documentElement.clientHeight - parseInt($(this).css('top')) - 20;
            $(this).css('max-height', maxHeight + 'px');
        }
    });
    //点击画布隐藏提示按钮
    $("#map_body").click(function(){  $("#data-button").hide();   });
    initSrcArea();
}

function loadChart(url) {
    // 装载流程图
    fetch(url).then(function (response) {
        return response.text();
    }).then(function (html) { // 创建SVG
        $("#map_body").append(html);
        let $svg = $("#map_body").children("svg");
        $svg.attr('width', "100%");
        $svg.attr('height', "100%");
        return $svg;
    }).then(function ($svg) {  // 初始画布
        initPanZoom($svg); // 初始画布
        initListener($svg); // 初始事件
        return $svg;
    }).then(function ($svg) {
        $svg.find("[data-src]").css('cursor', 'pointer');// 修改鼠标样式
        $svg.find("[data-type]").each(function () {
            $(this).addClass($(this).attr('data-type'));//添加默认class
        })
    });
}

function initListener($svg) {
    $svg.find("[data-src]").click(function () { // 装载源码
        let srcUrl = $(this).attr('data-src');
        let a = document.createElement('a');
        a.href = srcUrl;
        let className = a.pathname.substring(a.pathname.lastIndexOf("/") + 1);
        let data = {url: srcUrl, className: className};
        loadSrc(data);//点击打开源码
    })
    // 鼠标移动后显示 数据按钮
    $svg.find("[data-data]").mouseover(function () {
        let dataUrl = $(this).attr('data-data');// 数据模型URL
        let offset = $(this).children('rect')[0].getBoundingClientRect(); //获取元素位置
        $('#data-button').css('left', offset.left + offset.width);
        $('#data-button').css('top', offset.top);
        $('#data-button').show();
        $('#data-button')[0].url = dataUrl;

    });

    // 弹出帽泡提示框
    $svg.find("[data-desc]").each(function (item) {
        let desc = $(this).attr('data-desc');
        if (desc.trim() == "") {
            return;
        }
        let target= $(this).children('rect');
        desc="<pre>"+desc+"<s/pre>";
        $(this).popup({
            variation: "inverted",
            target: $(this).children('rect,path'),
            hoverable:true,
            html: desc
        });
    });
    //鼠标移动后显示 数据按钮  基于帽泡提示框架
    /* $svg.find("[data-data]").each(function (item) {
        $(this).popup({
            // on:"click",
             position : 'top right',
             variation:"basic tips",
             target:$(this).children('rect'),
             hoverable:true,
            // popup:$("#data-button"),
             html  : '<i class="info circle link red icon " onclick=""> </i>'
         });
     })*/


    $svg.find("[data-script]").click(function () {
        let script = $(this).attr('data-script');
        let lines = script.split('\n');
        let cmds=new Array();
        for (let i in lines) {
            if (lines[i].trim() === "") {
                continue;
            }
            let cmd = lines[i].split(/\s+/);
            let name = cmd[0];
            let params = cmd.slice(1);
            for (let key in params) {
                params[key] = "'" + params[key] + "'"
            }
            cmds.push(format("animation.{0}({1})", [name, params.join(",")]))
        }

        let run=function(index){
            eval(cmds[index]);
            index++;
            if (index < cmds.length) {
                setTimeout(() => run(index), 500);
            }
        }
        run(0);
    });
}

// 初始化：SVG画布，已支持缩放
function initPanZoom($svg) {
    /*
    TODO 关于配置参数 在慢慢研究
               https://github.com/jillix/svg.pan-zoom.js/
     */
    /*$svg.svgPanZoom({
        animationTime: 0, // time in milliseconds to use as default for animations. Set 0 to remove the animation
        zoomFactor: 0.05, // how much to zoom-in or zoom-out
        maxZoom: 3, //maximum zoom in, must be a number bigger than 1
        panFactor: 50, // how much to move the viewBox when calling .panDirection() methods
        initialViewBox: { // the initial viewBox, if null or undefined will try to use the viewBox set in the svg tag. Also accepts string in the format "X Y Width Height"
            x: 0, // the top-left corner X coordinate
            y: -100, // the top-left corner Y coordinate
            width: "1000", // the width of the viewBox
            height: "1000", // the height of the viewBox
        },
        limits: {
            x: -15000,
            y: -15000,
            x2: 11500,
            y2: 11500
        }
    }).zoomIn();*/

    SVG($svg[0])
        .size('100%', '100%')
        .panZoom({ zoomMin: 0.5, zoomMax: 20,oneFingerPan:true });
    return $svg;
}

// 初始化：拖动调整源码区大小
function initSrcArea() {
    var resize = $("#src_frame>.divide")[0];
    var box = $("#src_frame")[0];
    resize.onmousedown = function (e) {
        let startX = e.clientX;
        box.initWdith = box.clientWidth;
        document.onmousemove = function (e) {
            let moveLength = startX - e.clientX;
            $(box).css('width', (box.initWdith + moveLength) + "px");
        }
        document.onmouseup = function (evt) {
            document.onmousemove = null;
            document.onmouseup = null;
            resize.releaseCapture && resize.releaseCapture();
        }
        resize.setCapture && resize.setCapture();
        return false;
    }
}


function loadSrc(data) {
    $("#src_frame>.label>span").text(data.className);
    $("#src_frame>.label>a").attr("href", data.url);
    let href = document.createElement('a');
    href.href = data.url;
    href.search = "pretty"
    fetch(href.href).then(function (response) {
        return response.text();
    }).then(value => {
        $("#src_frame>.code").empty();
        let $codeView = $("#src_frame>.code").code({
            src: value,
            language: "java",
            escape: false// 特殊字符转义
        });
        // 堆栈行
        $("#src_frame").show();
        if (data.line != undefined) {
            let lines = data.line.split(",");
            // 选中行
            for (let i = 0; i < lines.length; i++) {
                selectLine($codeView[0].code, lines[i]);
            }
            // 跳转至行
            if (lines.length > 0) {
                gotoLine($codeView[0].code, lines[0]);
            }
        } else { // 跳转到描点定义
            let hash = href.hash !== "" ? href.hash : "#";
            findDeclaration($codeView[0].code, hash).each((index, item) => {
                gotoItem($codeView[0].code, $(item));//  跳转至定义
            });
        }

    });
}

function closeSrcArea() {
    $("#src_frame").hide();
    $("#src_frame>.code").empty();
}


function format(format, args) {
    return format.replace(/\{(\d+)\}/g, function (m, n) {
        return args[n] ? args[n] : m;
    });
}


// 在iframe 嵌入模式下，网页全屏展开
function doFullScreen() {
    window.open(
        window.location.href,
        "_blank",
        "menubar=no,toolbar=no,status=no,location=no,scrollbars=yes"
    )
}
