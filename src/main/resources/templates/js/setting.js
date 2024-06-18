$(function () {

    const e = JSON.parse(document.getElementById("data").innerText);


    new Vue({
        el: '#vue-div',
        data: {
            changyan: e,
            version: e.version,
        },
        methods: {
            val: function (val) {
                return val;
            }
        }
    })

    // 初始化 bootstrap-switch
    $("#commentEmailNotify-switch").bootstrapSwitch('state', e.commentEmailNotify === 'on');
    $("#commentEmailNotifyVal").attr("value", e.commentEmailNotify);
    $("#status-switch").bootstrapSwitch('state', e.status === 'on');
    $("#statusVal").attr("value", e.status);

    // 绑定 switchChange 事件
    $('#commentEmailNotify-switch').on('switchChange.bootstrapSwitch', function (event, state) {
        $("#commentEmailNotifyVal").attr("value", state ? "on" : "off");
    });

    $('#status-switch').on('switchChange.bootstrapSwitch', function (event, state) {
        $("#statusVal").attr("value", state ? "on" : "off");
    });

    $(".btn-info").click(function () {
        const formId = "ajax" + $(this).attr("id");
        $.post('update', $("#" + formId).serialize(), function (data) {
            if (data.success || data.status === 200) {
                $.gritter.add({
                    title: '  操作成功...',
                    class_name: 'gritter-success' + (!$('#gritter-light').get(0).checked ? ' gritter-light' : ''),
                });
            } else {
                $.gritter.add({
                    title: '  发生了一些异常...',
                    class_name: 'gritter-error' + (!$('#gritter-light').get(0).checked ? ' gritter-light' : ''),
                });
            }
        });
    });
});