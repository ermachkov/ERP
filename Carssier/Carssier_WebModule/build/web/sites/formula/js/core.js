var uiCore = null;
var ws = null;

jQuery(document).ready(function(){
    getUICore();
});

function getUICore() {
    if (uiCore == null) {
        uiCore = new UICore();
        uiCore.setComponetsEventHandler();
        uiCore.initWebSocket();
    }

    return uiCore;
}

function javaEventAsync(json){
    var v = ("" + json).replace("{", "{sess:\"" + sess + "\", ");
    console.log(v);
    var jsonEncode = encodeURIComponent(v);
    $.ajax({
        url: "/carssier/formula?action=event&json=" + jsonEncode,
        success: function(data) {
        //eval(data);
        }
    });
}

function UICore() {
    
    this.initWebSocket = function(){
        ws = new WebSocket("ws://"+window.location.hostname+":8080/carssier/websocket");
        ws.onopen = function () {
            //alert("sess:" + sess);
            ws.send("{command:\"register\", sess:\"" + sess + "\"}");
        //uiCore.initLogin();
        }
        
        ws.onmessage = function(message){
            //getUICore().showProgressBar(0);
            if(message.data == ""){
                return;
            }
        
            if(message.data == "stub"){
                return;
            }
        
            if(message.data == "Accepted"){
                return;
            }
        
            eval(message.data);
        }
        
        ws.onclose = function(){
            //alert("WebSocket close");
            getUICore().initWebSocket();
        }
        
        websocket.onerror = function(evt) { 
            alert(evt) 
        };
    }
    
    this.setUserPanel = function(panel){
        $("#userPanel").html(panel);
    }
    
    this.resize = function(){
        var value = window.innerHeight + "px";
        $("#main").css("height", value);
        $("#verticalBorder").css("height", value);
    }
    
    this.setWaitPanelEnabled = function(isEnable){
        if(isEnable){
            $("#waitPanel").remove();
            $("body").append("<div id='waitPanel'>"
                + "<table width='100%' height='100%'>"
                + "<tr><td valign='middle' align='center'>"
                +"<img src='img/progress/wait_circle.gif'/>"
                + "</td></tr>"
                + "</table>"
                +"</div>");
        } else {
            $("#waitPanel").remove();
        }
    }
    
    this.hideToolTip = function(){
        $("#toolTipPanel").remove();
    }
    
    this.showToolTip = function(message){
        $("#toolTipPanel").remove();
        $("body").append("<div id='toolTipPanel'>"
            +"<table width='100%' height='100%;'>"
            +"<tr><td valign='middle' align='center'>"
            +message
            +"</td></tr>"
            +"</table>"
            +"</div>");
    }
    
    this.setComponetsEventHandler = function(){
        var json;
        
        $("body").on("click", ".menu", function(event){
            json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click"
            };
            
            javaEventAsync($.toJSON(json));
        });
        
        $("body").on("click", ".checkBox", function(event){
            json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click",
                "checked":$(this).is(':checked')
            };
            
            javaEventAsync($.toJSON(json));
        });
        
        $("body").on("click", ".radioButton", function(event){
            json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click",
                "checked":$(this).is(':checked')
            };
            
            javaEventAsync($.toJSON(json));
        });
        
        $("body").on("keyup", ".textField", function(event){
            if ( event.which == 13 ) {
                var json = {
                    "identificator":"" + $(this).attr("identificator"), 
                    "eventType":"stopEditing"
                };
                
            } else {
                json = {
                    "identificator":"" + $(this).attr("identificator"), 
                    "eventType":"keyup",
                    "value":"" + $(this).val()
                };
            }
               
            javaEventAsync($.toJSON(json));
        });
        
        $("body").on("keyup", ".passwordTextField", function(event){
            json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"keyup",
                "value":"" + $(this).val()
            };
               
            javaEventAsync($.toJSON(json));
        });
        
        $("body").on("click", ".textField", function(){
            json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click",
                "value":"" + $(this).val()
            };
               
            javaEventAsync($.toJSON(json));
        });
        
        $("body").on("change", ".comboBox", function (){
            json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"change",
                "selectedValue":"" + $(this).val(),
                "selectedIndex":"" + $(this)[0].selectedIndex
            };
               
            javaEventAsync($.toJSON(json));
        });
        
        $("body").on("change", ".calendar", function (){
            json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"change",
                "date":"" + $(this).val()
            };
               
            javaEventAsync($.toJSON(json));
        });
        
        $("body").on("click", ".button", function(event){
            json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click"
            };
            javaEventAsync($.toJSON(json));
        });
        
        $("body").on("click", ".macTableNavigatorButton", function(event){
            json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click"
            };
            javaEventAsync($.toJSON(json));
        });
        
        $("body").on("click", ".spinnerButton", function(event){
            json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click"
            };
            javaEventAsync($.toJSON(json));
        });
        
        $("body").on("click", ".macTableRemoveButton", function(event){
            json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click"
            };
            javaEventAsync($.toJSON(json));
        });
    };
    
    this.setWorkPanel = function(html){
        this.hideToolTip();
        $("#workPanel").html(html);
        uiCore.macTableMatrason(".macTable");
        uiCore.macTableMatrason(".macTableBasket");
        
        $(".macTable").find("tr").click(function(event){
            uiCore.macTableMatrason(".macTable");
            $(this).css("background-color", "#7a8bac");
               
            if($(this).attr("row")){
                var tabl = $(this).parents(".macTable")[0];
                var json = {
                    "identificator":$(tabl).attr("identificator"),
                    "eventType":"click",
                    "row":$(this).attr("row")
                };
                    
                javaEventAsync($.toJSON(json));
            }
                
        });
    }
    
    this.showAlert = function(alert){
        alert(alert);
    }
    
    this.refreshBasketPanel = function(panel){
        $("#basketPanel").html(panel);
        uiCore.macTableMatrason(".macTableBasket");
        
        $(".macTableRemoveButton").button({
            icons: {
                primary: "ui-icon-trash"
            },
            text: false
        });
        $(".macTableRemoveButton").hide();
        
        $(".tblCheckBoxRemove").click(function(event){
            $("#basketPanel").find(".macTableRemoveButton").show("fast");
            
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click",
                "checked":$(this).is(':checked')
            };
            
            javaEventAsync($.toJSON(json));
        });
        
        $(".macTableCellEditor").keyup(function(event){
            if ( event.which == 13 ) {
                var json = {
                    "identificator":"" + $(this).attr("identificator"), 
                    "eventType":"stopCellEditing",
                    "row":$(this).attr("row"),
                    "column":$(this).attr("column"),
                    "value":"" + $(this).val()
                };
               
                javaEventAsync($.toJSON(json));
                
            } else {
                json = {
                    "identificator":"" + $(this).attr("identificator"), 
                    "eventType":"keypress",
                    "row":$(this).attr("row"),
                    "column":$(this).attr("column"),
                    "value":"" + $(this).val()
                };
               
                javaEventAsync($.toJSON(json));
            }
        });
        
        $(".macTableCellEditor").blur(function(){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"stopCellEditing",
                "row":$(this).attr("row"),
                "column":$(this).attr("column"),
                "value":"" + $(this).val()
            };
               
            javaEventAsync($.toJSON(json));
        });
    }
    
    this.macTableMatrason = function(macTableClass){
        var row = 0;
        $(macTableClass).find("tr").each(function(index, element){
            if($(element).attr("row")){
                //alert($(this).attr("sel"));
                if($(this).attr("sel") == "selected"){
                    $(this).css("background-color", "#7a8bac");
                } else {
                    if(row % 2){
                        $(this).css("background-color", "#c4d6f9");
                    } else {
                        $(this).css("background-color", "#ffffff");
                    }
                }               
                row++;
            }  
        });
    }   
}

