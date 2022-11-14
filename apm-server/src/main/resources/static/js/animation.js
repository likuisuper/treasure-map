var animation = window.animation || {};


animation.shine = function (element, last) {
    if (last === undefined|isNaN(last)) {
        last = 1000;
    } else if (last.endsWith('s')) {
        last = parseInt(last) * 1000;
    } else {
        last = parseInt(last);
    }
    element=element.replaceAll('#','#cell-');//
    $(element).addClass('shine');
    setTimeout(function () {
        $(element).removeClass('shine');
    }, last);
}

animation.show = function (element) {
    element=element.replaceAll('#','#cell-');//
    $(element).addClass('show');
}