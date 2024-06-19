$(function () {

    const e = JSON.parse(document.getElementById("data").innerText);
    if (e.commentEmailNotify === 'on') {
        document.getElementById("commentEmailNotify").setAttribute('checked', "checked");
    }
    if (e.status === 'on') {
        document.getElementById("status").setAttribute('checked', "checked");
    }

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

    $(".checkbox").map((e) => {
        $($(".checkbox")[e]).on('click', (event) => {
            if (event.target.checked) {
                console.info(event);
                document.getElementById(event.target.id + "Val").value = 'on';
            } else {
                document.getElementById(event.target.id + "Val").value = 'off';
            }
        });
    });

    $(".btn-primary").click(function () {
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