$(function(){
    var changyan = new Vue({
        el : '#vue-div',
        data : {
            changyan:{},
            version: '',
        },
        mounted : function(){
            $.get("info",function(e){
                $("#commentEmailNotify-switch").bootstrapSwitch('state', e.commentEmailNotify == 'on');
                $("#commentEmailNotify-switch").attr("value",e.commentEmailNotify);
                $("#status-switch").bootstrapSwitch('state', e.status  == 'on');
                $("#status-switch").attr("value",e.status );
                changyan.$set(changyan,'changyan',e);
                changyan.$set(changyan,'version','v'+e.version);
            })
        },
        methods: {
            val:function(val){
                return val;
            }
        }
    })

    $('#commentEmailNotify-switch').on('switchChange.bootstrapSwitch', function (event, state) {
        $("#commentEmailNotifyVal").attr("value",state?"on":"off");
    });
    $('#status-switch').on('switchChange.bootstrapSwitch', function (event, state) {
        $("#statusVal").attr("value",state?"on":"off");
    });

    $(".btn-info").click(function(){
        var formId="ajax"+$(this).attr("id");
        $.post('update',$("#"+formId).serialize(),function(data){
            if(data.success || data.status==200){
                $.gritter.add({
                    title: '  操作成功...',
                    class_name: 'gritter-success' + (!$('#gritter-light').get(0).checked ? ' gritter-light' : ''),
                });
            }else{
                $.gritter.add({
                    title: '  发生了一些异常...',
                    class_name: 'gritter-error' + (!$('#gritter-light').get(0).checked ? ' gritter-light' : ''),
                });
            }
        });
    });

    bootbox.setDefaults({locale:"zh_CN"});
    $("#refresh-btn").click(function(){
        bootbox.confirm("会重新获取评论数据", function(result){
            if(result){
                $.get('refresh',function(data){
                    $.gritter.add({
                        title: '刷新数据成功',
                        class_name: 'gritter-success' + (!$('#gritter-light').get(0).checked ? ' gritter-light' : ''),
                    });
                });
            }
        });
    });
});