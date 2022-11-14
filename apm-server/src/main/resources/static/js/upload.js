

function uploadFile(fileObj, url, resultNotify, progress) {
    // 计算md5 值
    doMd5(fileObj, function (md5) {
        // FormData 对象
        var form = new FormData();
        form.append("md5", md5)
        form.append("file", fileObj);
        // XMLHttpRequest 对象
        var xhr = new XMLHttpRequest();
        xhr.open("post", url, true);
        xhr.onload =resultNotify;
        xhr.upload.addEventListener("progress", progress, false);
        xhr.send(form);
    });
}


function doMd5(file, call) {
    var blobSlice = File.prototype.slice || File.prototype.mozSlice || File.prototype.webkitSlice;
    var chunkSize = 2097152; // read in chunks of 2MB
    var chunks = Math.ceil(file.size / chunkSize);
    var currentChunk = 0;
    var spark = new SparkMD5.ArrayBuffer();
    var fileReader = new FileReader();
    var begin = new Date().getTime();
    fileReader.onload = function (e) {
        //  log.innerHTML+="\nread chunk number "+parseInt(currentChunk+1)+" of "+chunks;
        spark.append(e.target.result); // append array buffer
        currentChunk++;
        if (currentChunk < chunks) {
            loadData();
        } else {
            var md5Val = spark.end();
            console.log("md5 值:" + md5Val + " 用时:" + (new Date().getTime() - begin));
            call(md5Val);
        }
    };
    fileReader.onerror = function (ev) {
        console.log("md5计算异常:" + ev);
    };

    function loadData() {
        var start = currentChunk * chunkSize,
            end = ((start + chunkSize) >= file.size) ? file.size : start + chunkSize;
        fileReader.readAsArrayBuffer(blobSlice.call(file, start, end));
    };
    loadData();
}
