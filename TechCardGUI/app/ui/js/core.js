var uiCore = null;
var orderPanel = null;
var currentX = -1;
var currentY = -1;
var ws;
var counter=0;
var javaReflector = "ru.sibek.techcard.ui.JSHandler";

jQuery(document).ready(function(){
    getUICore();
    uiCore.initWebSocket();
    uiCore.init();
    uiCore.loadPanel();
    
});

function getUICore() {
    if (uiCore == null) {
        uiCore = new UICore();
    }

    return uiCore;
}

function UICore() {
    
    var isWsFirstStart = true;
    
    this.initWebSocket = function(){
        //ws = new WebSocket("ws://localhost:10081");
        ws = new WebSocket("ws://"+window.location.hostname+":1573");
        ws.onopen = function () {
            ws.send("{command:register, sess:" + sess + "}");
            if(isWsFirstStart){
                isWsFirstStart = false;
            //uiCore.initLogin();
            }
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
            getUICore().initWebSocket();
        }
    }
    
    this.init = function(){
        uiCore.setComponetsEventHandler();
    }
    
    this.loadPanel = function(){
        if (counter<1){
        var json = {
            "identificator":"ru.sibek.techcard.ui.ComponentsView", 
            "eventType":"load"
        };    
        javaEventAsync($.toJSON(json));
        counter++;
        }
    }
    
    this.setWorkPanel = function(html){
        $("#bodyPanel").html(html);
        

        
    //$(".calendar").datepicker();
    //$(".calendar").datepicker($.datepicker.regional["ru"]);
    }
         this.setTabHeader = function(html){
        $(".nav-tabs").append(html);}
    
    this.setTechCardModal = function(html){
        
        $("#make-tech-card .modal-body").html(html);}
    this.setFormCmb = function(html){
        
        $("#cbo-panel").html(html);}
    
    this.setFormModel = function(html){
        
        $("#form-panel").html(html);}
    
    
       this.setTabBody = function(html){
          if ($("div").is(".tab")==false){
        $(".tab-content").append(html);
    }
}
     
    this.setAddDeviceButton = function(html){
     
        $("#make-device .modal-footer").html(html);}
    
  
    
       this.setAddOperationRow = function(html){
          // alert('7979797');
        $("#operationsTable tbody").append(html);
    }
        
        this.setAddTechCardButton = function(html){
        $("#make-tech-card .modal-footer").html(html);
        

        
    //$(".calendar").datepicker();
    //$(".calendar").datepicker($.datepicker.regional["ru"]);
    }   
    this.setComponetsEventHandler = function(){
        $("body").on("click", ".checkBox", function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click",
                "checked":$(this).is(':checked')
            };
            
            javaEventAsync($.toJSON(json));
        });
        

        
        $("body").on("click", ".radioButton", function(event){
            var json = {
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
      
        $("body").on("click", ".marshrut-card", function(){
            var json = {
                "identificator":"ru.sibek.techcard.ui.ComponentsView",
                "id_mk":$(this).attr('id_mk'),
                "content":$(this).attr('class'),
                "text":$(this).parent().prev().text(),//attr("class"),
                "eventType":"tab-card-link"
            };
               
            javaEventAsync($.toJSON(json));
        });
        $("body").on("click", ".textField", function(){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click",
                "value":"" + $(this).val()
            };
               
            javaEventAsync($.toJSON(json));
        });
        
        $("body").on("change", ".comboBox", function (){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"change",
                "selectedValue":"" + $(this).val(),
                "selectedIndex":"" + $(this)[0].selectedIndex
            };
               
            javaEventAsync($.toJSON(json));
        });
        
        $("body").on("change", ".calendar", function (){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"change",
                "date":"" + $(this).val()
            };
               
            javaEventAsync($.toJSON(json));
        });

        $("body").on("click", ".btn", function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click"
            };
            javaEventAsync($.toJSON(json));
        });
        
         $("body").on("click", ".add-operation", function(event){
             if($("#radio1").attr("checked")=="checked") {                 
            // alert('111');
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click"
            };
            javaEventAsync($.toJSON(json));
        } else {
            if($("#radio2").attr("checked")=="checked") {
            //alert('222  '+$("#oper-num").val());
            var num=jQuery.trim($("#oper-num").val());
            if(num!="" && isNaN(num) == false){
               var temp=0;
                $("tbody span[id^='numberinput_']").each(function(i,elem) 
                { 
                  if(num==$(elem).text()) {alert("ОЛОЛО Операция с таким номером уже есть");temp++}
                });  // выбор всех div с атрибутом title начинающихся с my  // выбор всех div с атрибутом title начинающихся с my
                if(temp==0)
                {
                    json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click",
                "number":""+num
                };
                javaEventAsync($.toJSON(json));
                } 
            } 
            } else alert('NaN');
            
        }
            
             
        });
        
        $("body").on("click", ".saveDevice", function(event){
            if ($("#deviceNumber").val()=="" || $("#deviceName").val()=="") alert('Поле не должно быть пустым');
            else{
                var json = {
                    "identificator":"" + $(this).attr("identificator"), 
                    "eventType":"click111",
                    "deviceNumber":""+ $("#deviceNumber").val(),
                    "deviceName":""+ $("#deviceName").val()
                };
                javaEventAsync($.toJSON(json));
            }
        });
        
        $("body").on("click", ".saveTechCard", function(event){
           var operation=new Array();
           var operations="";
           var value="\"\"";
           var text="\"\"";
           $("#operationsTable tr").each(function(i,elem)
           {
                          
            $(elem).find(" [id*='input_']").each(function(j,elem2) 
            {
                //alert(i+"__"+j);
                value="\"\"";
                if ($(elem2).val()!="") value= $(elem2).val();
                if ($(elem2).text()!="") text=$(elem2).text();
                if(this.nodeName=="SPAN") operation[j]='"'+$(elem2).attr("name")+'"'+":"+text;
                else operation[j]='"'+$(elem2).attr("name")+'"'+":"+value;//alert (i+"__"+$(elem2).val());
            });
              if ((operation.toString()!="")) operations+="{"+operation.toString() +"}!!!"; 
             //alert(operation);
           });
           operations=operations.substring(0, operations.length - 3);
           operations=operations+";";
           //alert(operations);									
var oper={
"identificator":"" + $(this).attr("identificator"),
"deviceid":$(this).attr("device_id_bd"),
"firmname":$('#input_0').val(),
"number1":$('#input_1').val(),
"number2":$('#input_2').val(),
"partname":$('#input_3').val(),
"matname":$('#input_4').val(),
"kod":$('#input_5').val(),
"ev":$('#input_6').val(),
"md":$('#input_7').val(),
"en":$('#input_8').val(),
"nrash":$('#input_9').val(),
"kim":$('#input_10').val(),
"kodzagotovki":$('#input_11').val(),
"profile-size":$('#input_12').val(),
"mz":$('#input_13').val(),
"kd":$('#input_14').val(),
    "contents":
        [
        operations
        ]
};            
//$("#form-panel").append("<textarea>"+oper.contents[0].toString()+"</textarea>");
                javaEventAsync($.toJSON(oper));
                //javaEventAsync($.toJSON(cart));
           // }
        });
        
        $("body").on("click", ".macTableNavigatorButton", function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click"
            };
            javaEventAsync($.toJSON(json));
        });
        
        $("body").on("click", ".spinnerButton", function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click"
            };
            javaEventAsync($.toJSON(json));
        });
        
        $("body").on("click", ".arrow", function(event){
            if($(".arrow").find(".arrow-left").length > 0){
                $(".arrow-left").removeClass().addClass("arrow-right");
                $(".subButtonsPanel").html("<div class='subp'>" + subOperationButton + "</div>");
                
            } else {
                $(".subButtonsPanel").html("");
                $(".arrow-right").removeClass().addClass("arrow-left");
            }
        });
        
        $("body").on("click", ".macTableRemoveButton", function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click"
            };
            javaEventAsync($.toJSON(json));
        });
        
        $("body").on("click", ".macTableEditButton", function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click"
            };
            javaEventAsync($.toJSON(json));
        });
        
        $("body").on("click", ".macTableAllRowCheckedButton", function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click"
            };
            javaEventAsync($.toJSON(json));
        });
      /*  $("body").on("click", ".tab-link", function(event){
             var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click"
            };
            javaEventAsync($.toJSON(json));
        });*/
        $("body").on("click", ".device", function(event){
          // $(".nav-tabs").append('<li><a href=#tab'+$(this).attr('id_bd')+' class="tab-link" content="'+$(this).attr('class')+'" device_id_bd="'+$(this).attr('id_bd')+'" data-toggle="tab">'+$(this).text()+'<button class="close">×</button></a></li>');
           //$("#bodyPanel").append('<div class="tab-pane" id="tab3"><p>3-я секция.</p></div>')
 var json = {   //"identificator":"" + $(this).attr("identificator"), 
                "identificator":"ru.sibek.techcard.ui.ComponentsView",
                "id_bd":$(this).attr('id_bd'),
                "content":$(this).attr('class'),
                "text":$(this).parent().parent().next().attr("value"),//text(),
                "eventType":"tab-link"
            };
            javaEventAsync($.toJSON(json));
    });
        $("body").on("click", ".close", function(event){
           if ($(this).parent().parent().attr('class')=="active")
               {
            $(this).parent().parent().prev().addClass("active");
            //alert($(this).parent().attr("href"));
            $($(this).parent().attr("href")).remove();
            $($(this).parent().parent().prev().find("a").attr("href")).addClass("active");
           $(this).parent().parent().remove();
           //var r="'"+$(this).parent().attr("href")+"'"; 
          // $(r).remove();
           
       } else {
           $($(this).parent().attr("href")).remove();
           // $($(this).parent().parent().prev().find("a").attr("href")).addClass("active");
           $(this).parent().parent().remove();
       }
        });
       
        $("body").on("click", ".operationicons", function(event){
            if ($(this).attr("action")){
              
                
                   //$(this).parent("tr").attr("id").val();  'span:first-child'
                var json = {
                    "identificator":"" + $(this).attr("identificator"), 
                    "eventType":"click",
                    "action":$(this).attr("action"),
                    "element": $(this).parent().parent().parent().find("td:first").find("a:first").attr("id_bd"),//$(".device").attr("id_bd"),
                    "rownumber": $(this).parent().parent().parent().attr("row")//$(".devicerow").attr("row")
                };
                javaEventAsync($.toJSON(json));

            }
        });
        
        $("body").on("click", ".macTableAllRowUncheckedButton", function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click"
            };
            javaEventAsync($.toJSON(json));
        });
        
        $("body").on("click", ".tblCheckBoxRemove", function(event){
            $("#wpRightPanel").find(".macTableRemoveButton").show("fast");
            
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click",
                "checked":$(this).is(':checked')
            };
            
            javaEventAsync($.toJSON(json));
        });
        
        $("body").on("keyup", ".macTableCellEditor", function(){
            //$(".macTableCellEditor").keyup(function(event){
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
        
        $("body").on("blur", ".macTableCellEditor", function(){
            //$(".macTableCellEditor").blur(function(){
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
    
    this.refreshElement = function(elementId, html){
        $("#" + elementId).html(html);
        
        // paint mactable
        if($("#"+elementId).find(".macTable").length > 0){
            var row = 0;
            $("#"+elementId).find(".macTable").each(function(index){
                $(this).find("tr").each(function(index, element){
                    if($(element).attr("row")){
                        if($(this).attr("sel") == "selected"){
                            $(this).css("background-color", "#7a8bac");
                            
                        } else {
                        /* if(row % 2){
                                $(this).css("background-color", "#c4d6f9");
                            } else {
                                $(this).css("background-color", "#ffffff");
                            }*/
                        }               
                        row++;
                    }
                    
                    $(this).click(function(event){
                        if($(this).attr("row")){
                            var tabl = $(this).parents(".macTable")[0];
                            var json = {
                                "identificator":$(tabl).attr("identificator"),
                                "eventType":"click",
                                "row":$(this).attr("row")
                            };
                    
                            if(!isRightClick){
                                javaEventAsync($.toJSON(json));
                        
                                var row = 0;
                                $(".macTable").find("tr").each(function(index){
                                    /*if(row % 2){
                                        $(this).css("background-color", "#c4d6f9");
                                    } else {
                                        $(this).css("background-color", "#FFFFFF");
                                    }*/
                                    row++;
                                });
                            // $(this).css("background-color", "#7a8bac");
                            }
                        }
                    });
                });
            });
            
        //getUICore().macTableButtonsView();
        }
    }
}

function javaEventAsync(json){
    var jsonEncode = encodeURIComponent(json);
    ws.send("{command:execute, sess:"+sess+", reflector:"+javaReflector+", method:sendEvent, parameters:"+jsonEncode+"}");
}


