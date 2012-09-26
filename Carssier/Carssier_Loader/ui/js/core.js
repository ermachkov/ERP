var uiCore = null;
var orderPanel = null;
var currentX = -1;
var currentY = -1;
var javaReflector = "ru.sibek.core.JSHandler";
var ws;
var mozilla = navigator.userAgent.indexOf("Firefox") !=-1 ? true : false;

jQuery(document).ready(function(){
    getUICore();
    
    if (mozilla){
        document.addEventListener("contextmenu", uiCore.showContextMenuMoz, true);
    }
    
    $(document).mousemove(function(e){
        currentX = e.pageX;
        currentY = e.pageY;
    }); 
    
    $(document).click(function(e){
        uiCore.globalClick(e);
    });
    
    $(document).keyup(function(e){
        uiCore.globalKeyup(e);
    });
    
    uiCore.initWebSocket();
    
});

function getUICore() {
    if (uiCore == null) {
        uiCore = new UICore();
        uiCore.init();
    }

    return uiCore;
}

function UICore() {
    
    var selectedRightPanel = "";
    var isSwitchToEdit = false;
    var isFirstClick = false;
    var selectionStart = 0;
    var lastSelectedWorkPanel = "";
    var loginPanel = "ru.sibek.core.LoginHandler";
    var isContextMenuShowing = false;
    var isExplorerEditableMode = false;
    var isInitTabs = false;
    var isTabAction = false;
    var isRightClick = false;
    var isProgressBarEnabled = false;
    var operationButton = "Оформить";
    var lastSelectedTab = "";
    var subOperationButton = "";
    var isEditorInit = false;
    var isWsFirstStart = true;
    var isRightPanelOpen = false;
    var mainFrame = "";
    
    this.initWebSocket = function(){
        console.log("ws://"+window.location.hostname+":1573");
        ws = new WebSocket("ws://"+window.location.hostname+":1573");
        ws.onopen = function () {
            ws.send("{command:register, sess:" + sess + "}");
            if(isWsFirstStart){
                isWsFirstStart = false;
                uiCore.initSplash(sess);
            }
        }
        
        ws.onmessage = function(message){
            getUICore().showProgressBar(0);
            if(message.data == ""){
                return;
            }
        
            eval(message.data);
        }
        
        ws.onclose = function(){
            getUICore().initWebSocket();
        }
    }
    
    this.showSplashPanel = function(html){
        mainFrame = $("body").html();
        $("body").html(html);
        
        var value = window.innerHeight + "px";
        $("#dataBaseHandlerPanel").css("height", value);
    }
    
    this.restoreBody = function(){
        $("body").html(mainFrame);
    }
    
    this.setSplashProgressBar = function(percent){
        var w = (600 / 100) * percent;
        $("#progressBarDB").css("width", w + "px");
    }
    
    this.initSplash = function(sess){
        var json = {
            "identificator":sess, 
            "eventType":"push",
            "action":"showSplashScreen"
        };
        javaEventAsync($.toJSON(json));
    }

    this.getSelectedWorkPanel = function(){
        return lastSelectedWorkPanel;
    }
    
    this.getElementCssAttributeValue = function(elementId, cssAttrName){
        var json = {
            value:$("#" + elementId).css(cssAttrName)
        };
        return $.toJSON(json);
    }
    
    this.setExplorerEditableMode = function(mode){
        isExplorerEditableMode = mode;
    }
    
    this.refreshWorkPanel = function(workPanelName, model){
        if(workPanelName == lastSelectedWorkPanel){
            getUICore().setWorkPanel(model);
        }
    }
    
    this.refreshRightPanel = function(workPanelName, model){
        if(workPanelName == lastSelectedWorkPanel){
            getUICore().setRightPanel(model);
        }
    }
    
    this.init = function(){
        $("body").click(function(event){
            if(isContextMenuShowing){
                $("#contextMenu").slideUp("fast", function(){});
            }
        });
        $("#printFrame").hide();
        uiCore.resize();
        uiCore.setComponetsEventHandler();
    }
    
    this.globalKeyup = function(e){
        if(event.which == 37){
        //
        }
        
        if(event.which == 39){
        //alert($("*:focus").attr("identificator"));
        }
    }
    
    this.resize = function(){
        var value = (window.innerHeight - 150) + "px";
        $("#main").css("height", value);
    }
    
    this.initLogin = function(identificator){
        $("body").html(mainFrame);
        var json = {
            "identificator":identificator, 
            "eventType":"push",
            "action":"showLoginPanel"
        };
        javaEventAsync($.toJSON(json));
    }
    
    this.showLoginPanel = function(panel){
        var title = "<span style='font-size:80%;'>Войти</span>";
        var isButtonOkPressed = false;
        $("body").append(panel);
        
        $(".btnLoginOk").click(function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click"
            };
            javaEventAsync($.toJSON(json));
            
            $(".loginPanel").dialog("close");
            $(".loginPanel").remove();
        });
        
        $(".btnLoginCancel").click(function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click"
            };
            javaEventAsync($.toJSON(json));
        });
        
        $(".loginPanel").dialog({
            autoOpen: false,
            title: title,
            modal: true,
            width: 380,
            height: 280,
            open: function(event, ui) {
                $(".passwordTextField").focus(); 
            },
            beforeClose: function(event, ui){
                return isButtonOkPressed;
            }
        });
        
        $(".passwordTextField").keyup(function(event){
            if(event.which == 13){
                var json = {
                    "identificator":"" + $(".btnLoginOk").attr("identificator"), 
                    "eventType":"click"
                };
                javaEventAsync($.toJSON(json));
            
                $(".loginPanel").dialog("close");
                $(".loginPanel").remove();
            
            } else {
                json = {
                    "identificator":"" + $(this).attr("identificator"), 
                    "eventType":"keyup",
                    "value":"" + $(this).val()
                };
               
                javaEventAsync($.toJSON(json));
            }
        });
        
        $(".comboBox").change(function (){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"change",
                "data":"" + $(this).val(),
                "selectedValue":"" + $(this).val(),
                "selectedIndex":"" + $(this)[0].selectedIndex
            };
               
            javaEventAsync($.toJSON(json));
        });
        
        $(".loginPanel").dialog("open");
    }
    
    this.setIndicator = function(indicator){
        $("#appInfoPanel").html(indicator);
    }
    
    this.globalClick = function(e){
        // hide node text editor
        if($(".explorerTextEdit").length > 0){
            var p = $(".explorerTextEdit").position();
            $('body').find(".treeLeaf").removeClass("ui-selected");
            if($(".explorerTextEdit").length == 0){
                $(this).addClass("ui-selected");
            }
            var width = $(".explorerTextEdit").width();
            var height = $(".explorerTextEdit").height();
        
            var isOutSide = true;
            var hmax = p.top + height;
            var wmax = p.left + width;
            if(e.pageY > p.top && e.pageY < hmax && e.pageX > p.left && e.pageX < wmax){
                isOutSide = false;
            }
        
            if(isOutSide){
                var text = $(".explorerTextEdit").val();
                $(".explorerTextEdit").parent().text(text);
                $(".explorerTextEdit").remove();
            }
        }
        
        if(isContextMenuShowing){
            console.log("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
            isContextMenuShowing = false;
            
            var v = $("body").find("#wpLeftPanel");
            if(v.length > 0){
                $("#workPanel").selectable("destroy");
                $("#wpLeftPanel").selectable();
            
            } else {
                $("#workPanel").selectable();
            }
        }
    }
    
    this.showConfirmPanel = function(title, message){
        $(".confirmPanel").remove();
        $("body").append(message);
        
        $(".confirmPanel").find(".btnConfirm").click(function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click"
            };
            javaEventAsync($.toJSON(json));
            
            $(".confirmPanel").dialog("close");
            $(".confirmPanel").remove();
        });
        
        $(".confirmPanel").dialog({
            autoOpen: false,
            title: title,
            modal: true,
            show: 'slide'
        });
        
        $(".confirmPanel").dialog("open");
    }
    
    this.updateRemoveOrderPanel = function(panel){
        $(".returnInfoPanel").html(panel);
    }
    
    this.setTextFieldValue = function(identificator, value){
        $("body").find(".textField").each(function(index){
            if($(this).attr("identificator") == identificator){
                $(this).val(value);
                return;
            }
        });
    }
    
    this.setTextFieldEnable = function(identificator, isEnabled){
        $("body").find(".textField").each(function(index){
            if($(this).attr("identificator") == identificator){
                if(isEnabled){
                    $(this).removeAttr('disabled');
                } else {
                    $(this).attr('disabled', true);
                }
                return;
            }
        });
    }
    
    this.showRemoveOrderPanel = function(panel){
        $("body").append(panel);
        
        $(".removeOrderPanel").find(".returnTable").each(function(index){
            var row = 0;
            $(this).find("tr").each(function(index){
                if(row % 2){
                    $(this).css("background-color", "#c4d6f9");
                } else {
                    $(this).css("background-color", "#FFFFFF");
                }
                
                row++;
            });
        });
        
        $(".removeOrderPanel").find("button").click(function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click"
            };
            javaEventAsync($.toJSON(json));
            
            $(".removeOrderPanel").dialog("close");
            $(".removeOrderPanel").remove();
        });
        
        $(".checkBox").click(function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click",
                "checked":$(this).is(':checked')
            };
            
            javaEventAsync($.toJSON(json));
        });
        
        $(".radioButton").click(function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click",
                "checked":$(this).is(':checked')
            };
            
            javaEventAsync($.toJSON(json));
        });
        
        $(".textField").keyup(function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"keyup",
                "value":"" + $(this).val()
            };
               
            javaEventAsync($.toJSON(json));
        });
        
        var _height = parseInt(window.innerHeight * .8);
        var _width = parseInt(window.innerWidth * .5);
        //alert(_width);
        $(".removeOrderPanel").dialog({
            autoOpen: false,
            //height: 600,
            width: _width,
            title: "<span style='font-size:85%;'>Удаление заказа5555</span>",
            modal: true
        });
        
        $(".removeOrderPanel").dialog("open");
    }
    
    this.hidePopupPanel = function(){
        $(".popupPanel").dialog("close");
        $(".popupPanel").remove();
    }
    
    this.showPopupPanel = function(title, message, _width, _height){
        $(".popupPanel").remove();
        $("body").append(message);
        
        $(".popupPanel").find("button").click(function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click"
            };
            javaEventAsync($.toJSON(json));
            
            $(".popupPanel").dialog("close");
            $(".popupPanel").remove();
        });
        
        if(_width > 0 && _height > 0){
            $(".popupPanel").dialog({
                autoOpen: false,
                height: _height,
                width: _width,
                title: title,
                modal: true,
                closeOnEscape: true
            });    
            
        } else if(_width > 0 && _height < 0){
            $(".popupPanel").dialog({
                width: _width,
                autoOpen: false,
                title: title,
                modal: true,
                closeOnEscape: true
            });
            
        } else if(_width < 0 && _height > 0){
            $(".popupPanel").dialog({
                height: _height,
                autoOpen: false,
                title: title,
                modal: true,
                closeOnEscape: true
            });
            
        } else {
            $(".popupPanel").dialog({
                autoOpen: false,
                title: title,
                modal: true,
                closeOnEscape: true
            });
        }
        

        $(".popupPanel").find(".textField").keyup(function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"keyup",
                "value":"" + $(this).val()
            };
               
            javaEventAsync($.toJSON(json));
        });
        
        $(".popupPanel").find(".radioButton").click(function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click",
                "checked":$(this).is(':checked')
            };
            
            javaEventAsync($.toJSON(json));
        });
        
        $(".popupPanel").find(".imageIcon").click(function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click",
                "src":$(this).attr("src")
            };
            
            javaEventAsync($.toJSON(json));
        });
        
        $(".popupPanel").dialog({
            show: 'slide'
        });
        
        $(".popupPanel").dialog("open");
    }
    
    this.getSelectedRightPanel = function(){
        return selectedRightPanel;
    }
    
    this.showProgressBar = function(value){
        $("#progressbar").progressbar({
            value: value
        });
    }
    
    this.showContextMenuMoz = function(e){
        var x = e.clientX;
        var y = e.clientY;
        var el = document.elementFromPoint(x, y);
        
        if(el.id == "workPanel"){
            $("#contextMenuContainer").html("<div id='contextMenu'></div>");
            $("#contextMenu").hide();
            $("#contextMenu").css("top", y + "px");
            $("#contextMenu").css("left", x + "px");
            
            var rb = $("#ribbon_bottom_panel_modules").find(".ribbon_bottom_button_selected");
            var json = {
                "identificator":"" + $(rb).attr("identificator"), 
                "eventType":"rightclick"
            };
            javaEventAsync($.toJSON(json));
            
        } else {
            $("#contextMenuContainer").html("<div id='contextMenu'></div>");
            $("#contextMenu").hide();
            $("#contextMenu").css("top", y + "px");
            $("#contextMenu").css("left", x + "px");
        
            var t;
            if(el.tagName == "IMG"){
                t = el.parentNode;
            } else {
                t = el;
            }
                    
            if(t.tagName == "TD"){
                json = {
                    "identificator":"" + t.getAttribute("identificator"), 
                    "eventType":"rightclick",
                    "className":"" + t.getAttribute("className"),
                    "row":"" + t.getAttribute("row"),
                    "column":"" + t.getAttribute("column"),
                    "value":"" + t.getAttribute("value")
                };
                        
            } else {
                json = {
                    "identificator":"" + t.getAttribute("identificator"), 
                    "eventType":"rightclick",
                    "className":"" + t.getAttribute("className"),
                    "dbid":"" + t.getAttribute("dbid")
                };
            }
                    
            javaEventAsync($.toJSON(json));
        }

        e.preventDefault();
        return false;
    }
    
    this.showContextMenu = function(){
        if(mozilla){
            return false;
        }
        
        isRightClick = true;
        var evt = document.createEvent("MouseEvents");
        
        var el = document.elementFromPoint(event.clientX, event.clientY);
        var t;
        if(el.tagName == "IMG"){
            t = el.parentNode;
        } else if(el.tagName == "SPAN"){
            t = el.parentNode;
        } else {
            t = el;
        }
        
        var isMultiSelect = false;
        var isNew = true;
        var className = "" + t.getAttribute("className");
        var dbid = "" + t.getAttribute("dbid");
        if(className == "org.ubo.tree.TreeLeafBasic" || className == "org.ubo.tree.TreeFolderBasic"){
            $("body").find(".treeFolder").each(function(index){
                if(("" + $(this).attr("class")).indexOf("ui-selected") != -1){
                    isMultiSelect = true;
                    
                    if(className == "org.ubo.tree.TreeFolderBasic" && ("" + $(this).attr("dbid")) == dbid){
                        isNew = false;
                    }
                }
            });
            
            $("body").find(".treeLeaf").each(function(index){
                if(("" + $(this).attr("class")).indexOf("ui-selected") != -1){
                    isMultiSelect = true;
                    
                    if(className == "org.ubo.tree.TreeLeafBasic" && ("" + $(this).attr("dbid")) == dbid){
                        isNew = false;
                    }
                }
            });
        }
        
        evt.initMouseEvent("click", true, true, window,
            1, event.screenX, event.screenY,
            event.clientX, event.clientY,
            false, false, false, false, 0, null);

        var x = event.clientX;
        var y = event.clientY;
        if(el.id == "workPanel"){
            el.dispatchEvent(evt);// uldyrwch
            $("#contextMenuContainer").html("<div id='contextMenu'></div>");
            $("#contextMenu").hide();
            $("#contextMenu").css("top", y + "px");
            $("#contextMenu").css("left", x + "px");
            
            var rb = $("#ribbon_bottom_panel_modules").find(".ribbon_bottom_button_selected");
            var json = {
                "identificator":"" + $(rb).attr("identificator"), 
                "eventType":"rightclick"
            };
            javaEventAsync($.toJSON(json));
            
        } else if(el.id == "wpLeftPanel"){    
            el.dispatchEvent(evt);
            $("#contextMenuContainer").html("<div id='contextMenu'></div>");
            $("#contextMenu").hide();
            $("#contextMenu").css("top", y + "px");
            $("#contextMenu").css("left", x + "px");
            
            rb = $("#ribbon_bottom_panel_modules").find(".ribbon_bottom_button_selected");
            json = {
                "identificator":"" + $(rb).attr("identificator"), 
                "eventType":"rightclick"
            };
            javaEventAsync($.toJSON(json));
            
        } else {
            console.log("isMultiSelect = " + isMultiSelect + ", isNew = " + isNew);
            if(!isMultiSelect){
                $(t).addClass("ui-selected");
                
            } else if(isMultiSelect && isNew){
                $("body").find(".ui-selected").each(function(index){
                    $(this).removeClass("ui-selected");
                });
                $(t).addClass("ui-selected");
            }
            
            $("#contextMenuContainer").html("<div id='contextMenu'></div>");
            $("#contextMenu").hide();
            $("#contextMenu").css("top", y + "px");
            $("#contextMenu").css("left", x + "px");
            
            if(t.tagName == "TD"){
                json = {
                    "identificator":"" + t.getAttribute("identificator"), 
                    "eventType":"rightclick",
                    "className":"" + t.getAttribute("className"),
                    "row":"" + t.getAttribute("row"),
                    "column":"" + t.getAttribute("column"),
                    "value":"" + t.getAttribute("value")
                };
            
            } else {
                json = {
                    "identificator":"" + t.getAttribute("identificator"), 
                    "eventType":"rightclick",
                    "className":"" + t.getAttribute("className"),
                    "dbid":"" + t.getAttribute("dbid")
                };
            }
            
            javaEventAsync($.toJSON(json));
        }
        
        isRightClick = false;
        return false;
    }
    
    this.setContextMenu = function(html){
        if(html == ""){
            return;
        }
        
        $("#contextMenu").html(html);
        $("#contextMenu").slideDown("fast", function(){});
        isContextMenuShowing = true;
        var v = $("body").find("#wpLeftPanel");
        if(v.length > 0){
            $("#workPanel").selectable("destroy");
            $("#wpLeftPanel").selectable("destroy");
            
        } else {
            $("#workPanel").selectable("destroy");
        }
        
        $(".contextMenuItem").click(function(event){
            $("#contextMenu").slideUp("fast", function(){});
            isContextMenuShowing = false;
            
            var v = $("body").find("#wpLeftPanel");
            if(v.length > 0){
                $("#workPanel").selectable("destroy");
                $("#wpLeftPanel").selectable();
            
            } else {
                $("#workPanel").selectable();
            }
        
            var identificator = "" + $(this).attr("identificator");
            var action = $(this).attr("action");
            var data = null;
            if(action != "undefined"){
                try{
                    data = eval(action);
                } catch (e){
                    alert("setContextMenu: " + e);
                }
            }
            
            var json;
            if(data == null){
                json = {
                    "identificator":identificator
                };
            } else {
                json = {
                    "identificator":identificator,
                    "data":data
                };
            }
            
            javaEventAsync($.toJSON(json));
            
        });
    }

    this.setTopButtonsModel = function(model) {
        $("#ribbon_top_panel").html(model);
        $("#ribbon_top_panel").hide();
        $("#ribbon_top_panel").show("slide", {
            direction: "up"
        }, function(){});
        
        uiCore.initTabs();
    }
    
    this.setOperationButton = function(obTable){
        var t = $(".ribbon_bottom_button_selected .ribbon_bottom_button_text").text();
        $("#ribbon_operation_panel").html(obTable);
        if($("#ribbon_bottom_panel_modules").find("td").length <= 1){
            $("#ribbon_operation_panel").html("");
            return;
        }
        
        var isRepaint = true;
        $(".ribbon_bottom_button_text").each(function(index){
            if($(this).text() == t){
                isRepaint = false;
            }
        });
        
        uiCore.operationPanelHandler();
        
    }
    
    this.operationPanelHandler = function(){
        if(isInitTabs){
            isInitTabs = false;
            var findOperationButton;
            $('body').find(".ribbon_bottom_button_text").each(function(index, el){
                if(el.textContent == operationButton){
                    findOperationButton = el.parentNode;
                }
            });
                
            $(findOperationButton).click();
        }
            
        if(isTabAction){
            isTabAction = false;
            $('body').find(".ribbon_bottom_button_text").each(function(index, el){
                if(el.textContent == operationButton){
                    findOperationButton = el.parentNode;
                }
            });
            
            $(findOperationButton).click();
            
            $(".tabSelected").removeClass().addClass("tab");
            $("#tabContainer").find(".tabText").each(function(index){
                if($(this).text() == operationButton){
                    $(this).parent().removeClass().addClass("tabSelected");
                }
            });
        }
    }
    
    this.hideRightPanel = function(){
        var p = $("#workPanel").find("#wpLeftPanel");
        if(p.length > 0){
            $("#workPanel").html($('#wpLeftPanel').html());
            uiCore.workPanelHandler();
            isRightPanelOpen = false;
        }
    }
    
    this.restoreSwitchToEdit = function(){
        isSwitchToEdit = false;
        isFirstClick = false;
        $(".explorerText").removeClass("readyToEdit");
    }
    
    this.enableFirstClick = function(){
        isFirstClick = true;
    }
    
    this.setWorkPanel = function(html){
        uiCore.doublePanelManager(html, "left");
        uiCore.hideLockPanel();
    }
    
    this.workPanelHandler = function(){
        if($('body').find('.leftMacTable').length > 0){
            $("#workPanel").css("-webkit-user-select", "none");
            $("#wpLefPanel").css("-webkit-user-select", "none");
            
            var row = 0;
            $(".leftMacTable").find("tr").each(function(index, element){
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
            
            $(".leftMacTable").find("tr").click(function(event){
                var row = 0;
                $(".leftMacTable").find("tr").each(function(index){
                    if(row % 2){
                        $(this).css("background-color", "#c4d6f9");
                    } else {
                        $(this).css("background-color", "#FFFFFF");
                    }
                
                    row++;
                });
                
                $(this).css("background-color", "#7a8bac");
               
                if($(this).attr("row")){
                    var tabl = $(this).parents(".leftMacTable")[0];
                    var json = {
                        "identificator":$(tabl).attr("identificator"),
                        "eventType":"click",
                        "row":$(this).attr("row")
                    };
                    
                    if(!isRightClick){
                        javaEventAsync($.toJSON(json));
                    }
                }
                
            });
            
            $(".macTableRemoveButton").button({
                icons: {
                    primary: "ui-icon-trash"
                },
                text: false
            });
            $(".macTableRemoveButton").hide();
        }

        if(isExplorerEditableMode){
            $(".explorerText").dblclick(function(event){
                if($(this).parent().attr("class").indexOf("treeFolder", 0) == -1){
                    return;
                }
            
                var json = {
                    "identificator":$(this).parent().attr("identificator"), 
                    "eventType":"dblclick",
                    "className":$(this).parent().attr("className"),
                    "dbid":$(this).parent().attr("dbid")
                };
            
                javaEventAsync($.toJSON(json));
            });
        
            $(".explorerText").click(function(event){
                event.stopPropagation();
                $(this).parent().removeClass("ui-selected");
                if(!isSwitchToEdit){
                    isSwitchToEdit = true;
                    setTimeout("getUICore().restoreSwitchToEdit()", 1500);
                    setTimeout("getUICore().enableFirstClick()", 500);
                    $(this).addClass("readyToEdit");
                    return;
                }
            
                if(!isFirstClick){
                    return;
                }
            
                $("body").find(".ui-selected").each(function(index){
                    $(this).removeClass("ui-selected");
                });
                $('body').find(".explorerTextEdit").each(function(index){
                    var text = $(this).val();
                    $(this).parent().text(text);
                });
                
                var text = $(this).text();
                $(this).removeClass("ui-selected");
                $(this).text("");
                $(this).append("<input class='explorerTextEdit' style='width:96%;' "
                    +"type='text' value='" + text + "'/>").keypress(
                    function(event){
                        if ( event.which == 13 ) {
                            var json = {
                                "identificator":$(this).parent().attr("identificator"), 
                                "className":$(this).parent().attr("className"),
                                "dbid":$(this).parent().attr("dbid"),
                                "eventType":"keypress",
                                "data":"" + $(".explorerTextEdit").val()
                            };
                            javaEventAsync($.toJSON(json));
                        }
                    });
            });
        }
        
        $(".treeFolder").draggable({
            revert: true, 
            opacity: 0.5, 
            helper: "clone",
            scroll: false,
            appendTo: 'body',
            start: function(e, ui){
                var selectedCount = 0;
                $("body").find(".treeLeaf").each(function(index){
                    if($(this).attr("class").indexOf("ui-selected") != -1){
                        selectedCount++;
                    }
                });
                $("body").find(".treeFolder").each(function(index){
                    if($(this).attr("class").indexOf("ui-selected") != -1){
                        selectedCount++;
                    }
                });
                
                if(selectedCount <= 1){
                    $(".treeFolder").removeClass("ui-selected");
                    $(".treeLeaf").removeClass("ui-selected");
                }
                if($(this).attr("class").indexOf("ui-selected") == -1){
                    $(this).addClass("ui-selected");
                }
                
                var count = 0;
                $('.treeFolder').each(function(){
                    if($(this).attr("class").indexOf("ui-selected") != -1){
                        var p = $(this).offset();
                        var img = $(this).find("img").attr("src");
                        $('body').append("<div class='animateIcon' "
                            +"style='position:absolute;z-index:5000;top:" 
                            + p.top + "px;left:" + p.left + "px;'>"
                            +"<img src='" + img + "'/></div>");
                        count++;
                    }
                });
                
                $('.treeLeaf').each(function(){
                    if($(this).attr("class").indexOf("ui-selected") != -1){
                        var p = $(this).offset();
                        var img = $(this).find("img").attr("src");
                        $('body').append("<div class='animateIcon' "
                            +"style='position:absolute;z-index:5000;top:" 
                            + p.top + "px;left:" + p.left + "px;'>"
                            +"<img src='" + img + "'/></div>");
                        count++;
                    }
                });
                $(".animateIcon").last().detach("div");
                
                if(count > 1){
                    $(ui.helper).find('img').detach();
                    $(ui.helper).find('div').detach();
                    $(ui.helper).prepend("<img src='img/dragdrop/multidrag.png'/>");
                    
                    $(".animateIcon").animate(
                    {
                        left:e.pageX + "px", 
                        top:e.pageY + "px"
                    },
                    {
                        duration:600,
                        step:function(now, fx){
                            if(fx.prop == "left"){
                                fx.end = currentX - 32;
                            }
                            
                            if(fx.prop == "top"){
                                fx.end = currentY - 32;
                            }
                        },
                        complete: function() {
                            $(this).hide();
                            $(this).remove();
                        }
                    } 
                    );
                }
            }
        });
        
        $(".treeLeaf").draggable({
            revert: true, 
            opacity: 0.5, 
            helper: "clone",
            scroll: false,
            appendTo: 'body',
            start: function(e, ui){
                var selectedCount = 0;
                $("body").find(".treeLeaf").each(function(index){
                    if($(this).attr("class").indexOf("ui-selected") != -1){
                        selectedCount++;
                    }
                });
                $("body").find(".treeFolder").each(function(index){
                    if($(this).attr("class").indexOf("ui-selected") != -1){
                        selectedCount++;
                    }
                });
                
                if(selectedCount <= 1){
                    $(".treeFolder").removeClass("ui-selected");
                    $(".treeLeaf").removeClass("ui-selected");
                }
                if($(this).attr("class").indexOf("ui-selected") == -1){
                    $(this).addClass("ui-selected");
                }
                
                var count = 0;
                $('.treeFolder').each(function(){
                    if($(this).attr("class").indexOf("ui-selected") != -1){
                        var p = $(this).offset();
                        var img = $(this).find("img").attr("src");
                        $('body').append("<div class='animateIcon' "
                            +"style='position:absolute;z-index:5000;top:" 
                            + p.top + "px;left:" + p.left + "px;'>"
                            +"<img src='" + img + "'/></div>");
                        count++;
                    }
                });
                
                $('.treeLeaf').each(function(){
                    if($(this).attr("class").indexOf("ui-selected") != -1){
                        var p = $(this).offset();
                        var img = $(this).find("img").attr("src");
                        $('body').append("<div class='animateIcon' "
                            +"style='position:absolute;z-index:5000;top:" 
                            + p.top + "px;left:" + p.left + "px;'>"
                            +"<img src='" + img + "'/></div>");
                        count++;
                    }
                });
                $(".animateIcon").last().detach("div");
                
                if(count > 1){
                    $(ui.helper).find('img').detach();
                    $(ui.helper).find('div').detach();
                    $(ui.helper).prepend("<img src='img/dragdrop/multidrag.png'/>");
                    
                    $(".animateIcon").animate(
                    {
                        left:e.pageX + "px", 
                        top:e.pageY + "px"
                    },
                    {
                        duration:600,
                        step:function(now, fx){
                            if(fx.prop == "left"){
                                fx.end = currentX - 32;
                            }
                            
                            if(fx.prop == "top"){
                                fx.end = currentY - 32;
                            }
                        },
                        complete: function() {
                            $(this).hide();
                            $(this).remove();
                        }
                    } 
                    );
                }
            }
        });
        
        if(isExplorerEditableMode){
            $(".treeFolder").droppable({
                activate: function(event, ui) {
                },
                drop: function( event, ui ) {
                    if(confirm("Переместить выделение?")){
                        $(".treeFolder").draggable( "option", "revert", false );
                        $(".treeLeaf").draggable( "option", "revert", false );
                        var json = {
                            "identificator":$(this).attr("identificator"), 
                            "className":$(this).attr("className"),
                            "dbid":$(this).attr("dbid"),
                            "eventType":"drop",
                            "data":uiCore.getAllSelectedNodes()
                        };
                        javaEventAsync($.toJSON(json));
                    }
                }
            });
        }
        
        $(".treeFolder").click(function(event){
            if(isExplorerEditableMode){
                if(event.srcElement.className == "explorerText"){
                    return;
                }
            }
            
            if(event.ctrlKey){
                $(this).removeClass("ui-selected").addClass("ui-selected");
                return;
            }
            
            $("body").find(".ui-selected").each(function(index){
                $(this).removeClass("ui-selected");
            });
            if($(".explorerTextEdit").length == 0){
                $(this).addClass("ui-selected");
            }
            
            if(!isRightClick){
                var identificator = "" + $(this).attr("identificator");
                var json = {
                    "identificator":identificator, 
                    "eventType":"click",
                    "className":$(this).attr("className"),
                    "dbid":$(this).attr("dbid")
                };
                javaEventAsync($.toJSON(json));
                
            } else {
                isRightClick = false;
            }
            
        });
        
        var v = $("body").find("#wpLeftPanel");
        if(v.length > 0){
            $("#workPanel").selectable("destroy");
            $("#wpLeftPanel").selectable();
            
        } else {
            $("#workPanel").selectable();
        }
        
        if($('body').find('.leftMacTable').length > 0){
            $("#workPanel").selectable("destroy");
            $("#wpLeftPanel").selectable("destroy"); 
        }
        
        $(".treeLeaf").click(function(event){
            if(isExplorerEditableMode){
                if(event.srcElement.className.indexOf("explorerText") != -1){
                    return;
                }
            }
            
            if(event.ctrlKey){
                $(this).removeClass("ui-selected").addClass("ui-selected");
                return;
            }
            
            $("body").find(".ui-selected").each(function(index){
                $(this).removeClass("ui-selected");
            });
            if($(".explorerTextEdit").length == 0){
                $(this).addClass("ui-selected");
            }

            if($('body').find("#wpRightPanel").length == 0){
                var rb = $("#ribbon_bottom_panel_modules").find(".ribbon_bottom_button_selected");
                var identificator = "" + $(rb).attr("identificator");
                var json = {
                    "identificator":identificator, 
                    "eventType":"push",
                    "action":"showRightPanel",
                    "data":uiCore.getAllSelectedNodes()
                };
                javaEventAsync($.toJSON(json));
                
            } else {
                var p = $(this).offset();
                var img = $(this).find("img").attr("src");
                $('body').append("<div class='animateIcon' "
                    +"style='position:absolute;z-index:5000;top:" 
                    + p.top + "px;left:" + p.left + "px;'>"
                    +"<img src='" + img + "'/></div>");
                $(".animateIcon").css("opacity", "0.5");
            
                var leftDst = $("#wpRightPanel").offset().left + ($("#wpRightPanel").width() / 2);
                var topDst = $("#wpRightPanel").offset().top + ($("#wpRightPanel").height() / 2);
                $(".animateIcon").animate(
                {
                    left: leftDst + "px", 
                    top: topDst + "px"
                },
                {
                    duration:800,
                    step:function(now, fx){
                    //
                    },
                    complete: function() {
                        $(this).fadeTo('slow', 0, function() {
                            $(this).remove();
                            
                            var json = {
                                "identificator":"" + $(".searchOrderDescription").attr("identificator"), 
                                "eventType":"change",
                                "data":"" + $(".searchOrderDescription").val()
                            };
                
                            javaEventAsync($.toJSON(json));
                            
                            json = {
                                "identificator":"" + $(".rightPanel").attr("identificator"), 
                                "eventType":"drop",
                                "data":uiCore.getAllSelectedNodes()
                            };
                            javaEventAsync($.toJSON(json));
                            
                        });
                    }
                });
            }
        });
        
        $(".calendar").datepicker();
        $(".calendar").datepicker($.datepicker.regional["ru"]);
        
        $(".treeFolder").css("overflow", "hidden");
        $(".treeLeaf").css("overflow", "hidden");
        $(".explorerText").css("overflow", "hidden");  
        
        if($("body").find("#scrollMacTable").length > 0){
            var p, len;
            if($("body").find("#wpLeftPanel").length > 0){
                p = $("#wpLeftPanel").parent("div");
                len = parseInt($("#scrollMacTable").width() - $(p).width());
                len = len * 2;
                $(p).scrollLeft(len);
                
            } else {
                p = $("#scrollMacTable").parent("div");
                len = parseInt($("#scrollMacTable").width() - $(p).width());
                len = len * 2;
                $(p).scrollLeft(len);
            }
        }
    }
    
    this.setRightPanel = function(rightPanel, sizeLeft){
        if(("" + sizeLeft) == "undefined"){
            sizeLeft = parseInt(window.innerWidth / 2);
        } else {
            sizeLeft = parseInt(window.innerWidth * sizeLeft);
        }
        
        uiCore.doublePanelManager(rightPanel, "right", sizeLeft);
        //Скрыть
        var text = "<div class='vertical_first'>\u0421</div>";
        text += "<div class='vertical'>\u043a</div>";
        text += "<div class='vertical'>\u0440</div>";
        text += "<div class='vertical'>\u044b</div>";
        text += "<div class='vertical'>\u0442</div>";
        text += "<div class='vertical'>\u044c</div>";
        $(".rightPanelButton").html(text);
        
        // order panel smart chooser caller
        //        $(".label").click(function(event){
        //            var json = {
        //                "identificator":"" + $(this).attr("identificator"), 
        //                "eventType":"click"
        //            };
        //            javaEventAsync($.toJSON(json));
        //        });
        
        var row = 0;
        $(".macTable").find("tr").each(function(index){
            if(row % 2){
                $(this).css("background-color", "#c4d6f9");
            } else {
                $(this).css("background-color", "#FFFFFF");
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
                            if(row % 2){
                                $(this).css("background-color", "#c4d6f9");
                            } else {
                                $(this).css("background-color", "#FFFFFF");
                            }
                            row++;
                        });
                        $(this).css("background-color", "#7a8bac");
                    }
                }
            });
                
            row++;
        });
        
        $(".macTableRemoveButton").button({
            icons: {
                primary: "ui-icon-trash"
            },
            text: false
        });
        $(".macTableRemoveButton").hide();
        
        //        $(".tblCheckBoxRemove").click(function(event){
        //            $("#wpRightPanel").find(".macTableRemoveButton").show("fast");
        //            
        //            var json = {
        //                "identificator":"" + $(this).attr("identificator"), 
        //                "eventType":"click",
        //                "checked":$(this).is(':checked')
        //            };
        //            
        //            javaEventAsync($.toJSON(json));
        //        });
        
        //        $(".macTableCellEditor").keyup(function(event){
        //            if ( event.which == 13 ) {
        //                var json = {
        //                    "identificator":"" + $(this).attr("identificator"), 
        //                    "eventType":"stopCellEditing",
        //                    "row":$(this).attr("row"),
        //                    "column":$(this).attr("column"),
        //                    "value":"" + $(this).val()
        //                };
        //               
        //                javaEventAsync($.toJSON(json));
        //                
        //            } else {
        //                json = {
        //                    "identificator":"" + $(this).attr("identificator"), 
        //                    "eventType":"keypress",
        //                    "row":$(this).attr("row"),
        //                    "column":$(this).attr("column"),
        //                    "value":"" + $(this).val()
        //                };
        //               
        //                javaEventAsync($.toJSON(json));
        //            }
        //        });
        //        
        //        $(".macTableCellEditor").blur(function(){
        //            var json = {
        //                "identificator":"" + $(this).attr("identificator"), 
        //                "eventType":"stopCellEditing",
        //                "row":$(this).attr("row"),
        //                "column":$(this).attr("column"),
        //                "value":"" + $(this).val()
        //            };
        //               
        //            javaEventAsync($.toJSON(json));
        //        });
        
        $(".cboTotalDiscount").change(function (){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"change",
                "selectedValue":"" + $(this).val(),
                "selectedIndex":"" + $(this)[0].selectedIndex
            };
               
            javaEventAsync($.toJSON(json));
        });
        
        // if droppable
        $("#wpRightPanel").droppable({
            activate: function(event, ui){
                $(".treeFolder").draggable( "option", "revert", true);
                $(".treeLeaf").draggable( "option", "revert", true);
            },
            drop: function( event, ui ) {
                var json = {
                    "identificator":"" + $(".searchOrderDescription").attr("identificator"), 
                    "eventType":"change",
                    "data":"" + $(".searchOrderDescription").val()
                }
                javaEventAsync($.toJSON(json));
                
                json = {
                    "identificator":"" + $(".rightPanel").attr("identificator"), 
                    "eventType":"drop",
                    "data":uiCore.getAllSelectedNodes()
                };
                javaEventAsync($.toJSON(json));
                
                $(".treeFolder").draggable( "option", "revert", false );
                $(".treeLeaf").draggable( "option", "revert", false );
            }
        });
        
        if($("#wpRightPanel").find(".tabbedPane").length > 0){
            $(".tabbedPane").find("li").css("font-size", "80%");
            $(".tabbedPane").tabs();
        
            $(".txtIncomeCash").focus();
            $(".txtIncomeCash")[0].setSelectionRange(selectionStart, selectionStart);
        
            $(".txtIncomeCash").keyup(function(event){
                selectionStart = $(".txtIncomeCash")[0].selectionStart;
                var json;
                if ( event.which == 13 ) {
                    json = {
                        "identificator":"" + $(this).attr("identificator"), 
                        "eventType":"keyup",
                        "value":"" + $(this).val(),
                        "extra":"KEY_ENTER"
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
        }
    }
    
    this.setSelectTab = function(index){
        var $tabs = $(".tabbedPane").tabs();
        $tabs.tabs('select', index);
    }
    
    this.setComponetsEventHandler = function(){
        $("body").on("click", ".ribbon_top_button", function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click",
                "name":$(this).attr("topButtonName")
            };
            
            $(".ribbon_top_button_selected").removeClass().addClass("ribbon_top_button");
            $(this).removeClass().addClass("ribbon_top_button_selected");
            
            javaEventAsync($.toJSON(json));
        });
        
        $("body").on("click", ".ribbon_top_button_selected", function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click",
                "name":$(this).attr("topButtonName")
            };
            
            javaEventAsync($.toJSON(json));
        });
        
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
        
        $("body").on("keyup", ".searchCustomersField", function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"keyup",
                "value":"" + $(this).val()
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
        
        $("body").on("keyup", ".searchOrderDescription", function(){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"keyup",
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
        
        $("body").on("click", ".button", function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click"
            };
            javaEventAsync($.toJSON(json));
        });
        
        $("body").on("click", ".macTableNavigatorButton", function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click"
            };
            javaEventAsync($.toJSON(json));
        });
        
        $("body").on("click", ".btnRulesAllSelector", function(event){
            getUICore().selectAllModules();
        });
        
        $("body").on("click", ".spinnerButton", function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click"
            };
            javaEventAsync($.toJSON(json));
        });
        
        $("body").on("click", ".subOperationButton", function(event){
            var action = $(this).attr("action");
            var data = null;
            if(action != "undefined"){
                try{
                    data = eval(action);
                } catch (e){
                    alert(e);
                }
            }
            
            var identificator = "" + $(this).attr("identificator");
            var json;
            if(data == null){
                json = {
                    "identificator":identificator
                };
            } else {
                json = {
                    "identificator":identificator,
                    "data":data
                };
            }
            
            javaEventAsync($.toJSON(json));
        });
              
        $("body").on("click", ".tabText", function(event){ 
            lastSelectedTab = $(".tabSelected").text();
            var selOperationButton = "";
            $("body").find(".ribbon_bottom_button_selected").each(function(index){
                selOperationButton = $(this).find(".ribbon_bottom_button_text").text();
            });
            
            if($(this).text() == selOperationButton){
                return;
            }
            
            var topButton = $(this).parent().attr("topButton");
            operationButton = $(this).parent().attr("operationButton");
            var findTopButton;
            $("#ribbonTopPanel").find("td").each(function(index, td){
                if(td.textContent == topButton){
                    findTopButton = td;
                }
            });
            
            isTabAction = true;
            $(findTopButton).click();
        });
        
        $("body").on("hover", ".tabCloseImage", function(){
            var image = $(this).find("img").attr('src');
            if(image.indexOf("red") != -1){
                $(this).find("img").attr('src', "img/tab/close_icon_gray.png");
            } else {
                $(this).find("img").attr('src', "img/tab/close_icon_red.png");
            }
            
        });
            
        $("body").on("click", ".tabCloseImage", function(event){
            $(this).parent().remove();
            var f = $("#tabContainer").find(".tab").length;
            f += $("#tabContainer").find(".tabSelected").length;
            if(f == 0){
                $("#workPanel").fadeOut("slow", function(){
                    $("#workPanel").html("");
                });
                
                $(".ribbon_bottom_button_selected").removeClass().addClass("ribbon_bottom_button");
                
            } else if(f == 1){
                $(".tabText").click();
                
            } else {
                var tabForClick = null;
                $("#tabContainer").find(".tabText").each(function(index){
                    if($(this).text() == lastSelectedTab){
                        tabForClick = $(this);
                    }
                });
                
                if(tabForClick != null){
                    tabForClick.click();
                } else {
                    var tabs = $("#tabContainer").find(".tabText");
                    $(tabs[tabs.length - 1]).click();
                }
            }
        });
        
        $("body").on("click", ".ribbon_bottom_button", function(event){
            $("#tabContainer").find(".tabText").each(function(index){
                if($(this).parent().hasClass("tabSelected")){
                    lastSelectedTab = $(this).text();
                }
            });
            
            $(".ribbon_bottom_button_selected").removeClass().addClass("ribbon_bottom_button");
            
            $(this).removeClass().addClass('ribbon_bottom_button_selected');
            
            $("#workPanel").html("");
            //uiCore.showLockPanel();
            var identificator = "" + $(this).attr("identificator");
            var json = {
                "identificator":identificator, 
                "eventType":"click"
            };
            javaEventAsync($.toJSON(json));
                
            uiCore.setRightPanelButton($(this).attr("rightPanels"));
            uiCore.tabHandler();
            
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
    
    this.selectAllModules = function(){
        $('body').find(".checkBox").each(function(index, ui){
            if($(this).attr("name") == "ruleBlockCheckBox"){
                getUICore().showStatusInfo(1, "" + index + ", " + ui + ", " + $(this));
                $(this).click();
            }
        });
    }
    
    this.setAllTableCheckBoxSelected = function(tableId, isSelected){
        $("body").find("#"+tableId).find("tr").each(function(index){
            $(this).find(".tblCheckBoxRemove").each(function(index){
                $(this).prop("checked", isSelected);
                var json = {
                    "identificator":"" + $(this).attr("identificator"), 
                    "eventType":"click",
                    "checked":isSelected
                };
            
                javaEventAsync($.toJSON(json));
            
                    
            });
        });
    }
    
    this.setRightPanelButton = function(jsonPanels){
        var buttons = "";
        var s = jsonPanels.replace("{", "");
        s = s.replace("}", "");
        var arr = s.split(",");
        for(var i = 0; i < arr.length; i++){
            var v = arr[i].split(":");
            var t = v[0].split("");
            var text = "";
            for(var j = 0; j < t.length; j++){
                if(t[j] == " "){
                    t[j] = "&nbsp;";
                }
                
                if(j == 0){
                    text += "<div class='vertical_first'>"+t[j]+"</div>";
                } else {
                    text += "<div class='vertical'>"+t[j]+"</div>";
                }
                
            }
            buttons += "<div class='rightPanelButton' text='"+v[0]+"' identificator='"+v[1]+"'>"
            + text
            +"</div>";
        }
        
        $(".rightPanelButtonsConatainer").html(buttons);
        
        $(".rightPanelButton").click(function(event){
            var p = $("#workPanel").find("#wpLeftPanel");
            var isRightPanel = false;
            if(p.length > 0){
                isRightPanel = true;
            }
            
            if(isRightPanel){
                var identificator = $(this).attr("identificator");
                    
                var val = $("#wpLeftPanel").html();
                $("#wpRightPanel").hide("slide", {
                    direction:"right"
                }, 
                function(){
                    $("#workPanel").html(val);
                    uiCore.workPanelHandler();
                    
                    json = {
                        "identificator":"" + identificator, 
                        "eventType":"click",
                        "action":"hideRightPanel"
                    };
                    javaEventAsync($.toJSON(json));
                });
                
                $("body").find(".rightPanelButton").each(function(index){
                    if($(this).attr("identificator") == identificator){
                        var t = $(this).attr("text").split("");
                        var text = "";
                        for(var j = 0; j < t.length; j++){
                            if(t[j] == " "){
                                t[j] = "&nbsp;";
                            }
                
                            if(j == 0){
                                text += "<div class='vertical_first'>"+t[j]+"</div>";
                            } else {
                                text += "<div class='vertical'>"+t[j]+"</div>";
                            }
                        }
                        $(this).html(text);
                    }
                });
                
            } else {
                var json = {
                    "identificator":"" + $(this).attr("identificator"), 
                    "eventType":"click",
                    "action":"showRightPanel"
                };
                javaEventAsync($.toJSON(json));
                
                var _identificator = $(this).attr("identificator");
                $("body").find(".rightPanelButton").each(function(index){
                    if($(this).attr("identificator") == _identificator){
                        //Скрыть
                        var text = "<div class='vertical_first'>\u0421</div>";
                        text += "<div class='vertical'>\u043a</div>";
                        text += "<div class='vertical'>\u0440</div>";
                        text += "<div class='vertical'>\u044b</div>";
                        text += "<div class='vertical'>\u0442</div>";
                        text += "<div class='vertical'>\u044c</div>";
                        $(this).html(text);
                    }
                });
            }
        });
    }
    
    this.isRightPanelOpen = function(){
        return "{isRightPanelOpen:"+isRightPanelOpen+"}";
    }
    
    // panel = [left | right]
    this.doublePanelManager = function(model, panel, sizeLeft){
        var p = $("#workPanel").find("#wpLeftPanel");
        var isRightPanel = false;
        if(p.length > 0){
            isRightPanel = true;
        }
        isRightPanelOpen = isRightPanel;
        
        if(panel == "left"){
            if(isRightPanel){
                $("#wpLeftPanel").fadeOut("fast", function(){
                    $("#wpLeftPanel").html(model);
                    $("#wpLeftPanel").fadeIn("fast", function(){
                        uiCore.workPanelHandler();
                    });
                });
                
            } else {
                if(lastSelectedWorkPanel != uiCore.getSelectedRibbonButtons()[1]){
                    lastSelectedWorkPanel = uiCore.getSelectedRibbonButtons()[1]
                    $("#workPanel").html(model);
                    //                    $("#workPanel").hide();
                    //                    $("#workPanel").show("slide", {
                    //                        direction: "up"
                    //                    }, function(){
                    uiCore.workPanelHandler();
                //                    });
                    
                } else {
                    $("#workPanel").fadeOut("fast", function(){
                        $("#workPanel").html(model);
                        $("#workPanel").fadeIn("fast", function(){
                            uiCore.workPanelHandler();
                        });
                    });
                }
            }
        }
        
        if(panel == "right"){
            if(isRightPanel){
                $("#wpRightPanel").html(model);
                
            } else{
                var explorer = $("#workPanel").html();
                var panels = "<div class='wpSplitPane'>"
                +"<div style='overflow: auto;'>"
                +"<div id='wpLeftPanel'>"+explorer+"</div>"
                +"</div>"
                +"<div>"
                +"<div id='wpRightPanel'>"+model+"</div>"
                +"</div>"
                +"</div>";
            
                $("#workPanel").html(panels);
                $("#wpRightPanel").hide();
                $("#wpRightPanel").show("slide", {
                    direction: "right"
                }, 
                function(){
                    //alert("Callback!");
                    });
                                
                $(".wpSplitPane").splitter({
                    "sizeLeft": sizeLeft
                });
                uiCore.workPanelHandler();
            }
        }
    }
    
    /**
     * set position split bar
     * pos - should be between (0 ... 1)
     */
    this.moveSplitBar = function(pos){
        var p = parseInt(window.innerWidth  * pos);
        $(".wpSplitPane").trigger("resize", [ p ]);
    }
    
    this.setPanelContextMenu = function(html){
        $("#contextMenu").html(html);
        $("#contextMenu").show("fast");
        alert(html);
    }
    
    this.decorateSubButtonsTable = function(){
        var count = $(".subButtonsTable").find("td").length;
        var max = count / 2;
        var i = 0;
        $(".subButtonsTable").find("td").each(function(){
            if(i != count -1 && i != max - 1){
                $(this).css("border-right", "1px dotted white");
            }
            
            if(i < max){
                $(this).css("border-bottom", "1px dotted white");
            }
            i++;
        });
    }
    
    this.setSubOperationButton = function(html){
        if(html == "<div></div>"){
            subOperationButton = "";
        }
        
        subOperationButton = html;
        $(".subButtonsPanel").remove();
        $(".arrow").removeClass().addClass("arrowInactive");
        $(".vline").removeClass().addClass("vlineInactive");
        $(".arrow-left").removeClass().addClass("arrow-leftInactive");
        $(".arrow-right").removeClass().addClass("arrow-leftInactive");
        
        $("#ribbon_operation_panel").find("td").each(function(){
            if($(this).hasClass("ribbon_bottom_button_selected")){
                if($(this).next().hasClass("arrowInactive")){
                    $(this).next().removeClass().addClass("arrow");
                    $(this).next().html("<div class='vline'></div><div class='arrow-left'></div>");
                    $(this).next().after("<td class='subButtonsPanel' valign='middle'></td>");   
                }
            }
        });
    }
    
    this.getAllSelectedNodes = function(){
        var data = new Array();
        var i = 0;
        $('body').find('*[className]').each(function(index){
            if($(this).attr("class").indexOf("ui-selected") != -1){
                data[i] = {
                    "className":$(this).attr("className").toString(), 
                    "dbid":$(this).attr("dbid")
                };
                i++;
            }
        });
        
        return data;
    }
    
    this.requestAllSelectedNodes = function(eventType, identificator){
        var json = {
            "identificator": identificator, 
            "eventType": eventType,
            "data": uiCore.getAllSelectedNodes()
        };
        javaEventAsync($.toJSON(json));
    }
    
    this.confirmAction = function(message, evalCode){
        var data = null;
        if(confirm(message.toString())){
            if(evalCode != null && evalCode != ""){
                data = eval(evalCode.toString());
            }
        }
        
        return data;
    }
    
    this.selectedTreeLeaf = function(className, dbid){
        $("body").find(".treeLeaf").each(function(index){
            if($(this).attr("className") == className && $(this).attr("dbid") == dbid){
                $(this).click();
            }
        });
    }
    
    this.setOrderDescription = function(jsonString){
        var json = jQuery.parseJSON(jsonString);
        $(".searchOrderDescription").autocomplete({
            minLength: 3,
            
            select: function(event, ui) {
                var json = {
                    "identificator":"" + $(this).attr("identificator"), 
                    "eventType":"change",
                    "data":"" + ui.item.value
                };
                
                javaEventAsync($.toJSON(json));
            }
        });
        
        $(".searchOrderDescription").autocomplete("option", "source", json.data);
    }
    
    this.setCustomersSelector = function(jsonString){
        var json = jQuery.parseJSON(jsonString);
        $(".searchCustomersField").autocomplete({
            minLength: 1,
            
            focus: function( event, ui ) {
                $(this).val( ui.item.label );
                return false;
            },
            
            select: function(event, ui) {
                $(this).val( ui.item.label );
                var json = {
                    "identificator":"" + $(this).attr("identificator"), 
                    "eventType":"change",
                    "value":"" + ui.item.label,
                    "extra":"" + ui.item.value
                };
                
                javaEventAsync($.toJSON(json));
                return false;
            }
        });
        
        //[ { label: "Choice1", value: "value1" }, ... ]
        var vals = new Array();
        var s = "";
        for(var i = 0; i < json.data.length; i++){
            var name = json.data[i]["name"];
            s += name + ", ";
            var id = "" + json.data[i]["id"];
            vals[i] = {
                label:name, 
                value:id
            };
        }
        
        $(".searchCustomersField").autocomplete("option", "source", vals);
    }
    
    this.tabHandler = function(){
        var rb = uiCore.getSelectedRibbonButtons();
        $(".tabSelected").removeClass().addClass("tab");
        
        var newTab = "<div class='tabSelected' topButton='"+rb[0]+"' operationButton='"+rb[1]+"'>"
        +"<span class='tabText'>"+rb[1]+"</span>"
        +"<span class='tabCloseImage'><img src='img/tab/close_icon_gray.png'></span>"
        +"</div>";
    
        var isPresent = false;
        $("body").find(".tab").each(function(index, div){
            if($(div).attr("topButton") == rb[0] && $(div).attr("operationButton") == rb[1]){
                isPresent = true;
            }
        });
        
        $("body").find(".tabSelected").each(function(index, div){
            if($(div).attr("topButton") == rb[0] && $(div).attr("operationButton") == rb[1]){
                isPresent = true;
            }
        });
            
        if(!isPresent){
            $("#tabContainer").append(newTab);
            localStorage.setItem("topButton", rb[0]);
            localStorage.setItem("operationButton", rb[1]);
        } else {
            $("#tabContainer").find(".tabText").each(function(){
                if($(this).text() == rb[1]){
                    $(this).parent().removeClass().addClass("tabSelected");
                }
            });
        }
    }
    
    this.initTabs = function(){
        var topButton = "Заказы";//localStorage.getItem("topButton");
        var operationButton = "Оформить";//localStorage.getItem("operationButton");
        
        //alert(topButton + " " + operationButton);
        
        var findTopButton;
        $("#ribbonTopPanel").find("td").each(function(index, td){
            if(td.textContent == topButton){
                findTopButton = td;
            }
        });
        isInitTabs = true;
        $(findTopButton).click();
    }
    
    this.switchModule = function(topButton, operationButton){
        var findTopButton;
        $("#ribbonTopPanel").find("td").each(function(index, td){
            if(td.textContent == topButton){
                findTopButton = td;
            }
        });
        $(findTopButton).click();
            
        var findOperationButton;
        $('body').find(".ribbon_bottom_button_text").each(function(index, el){
            if(el.textContent == operationButton){
                findOperationButton = el.parentNode;
            }
        });
        $(findOperationButton).click();
    }
    
    this.showStatusInfo = function(sectionNumber, msg){
        var s = "#statusInfoPanel_" + sectionNumber;
        $(s).html(msg);
    }
    
    this.setWorkPanelBusy = function(isBusy){
        var busyPanel = "<div style='position:absolute; z-index:1000; left:0px; "
        +"top:0px;width:100%;height:100%;' id='fullScreenBusyPanel'>"
        +"<table width='100%' height='100%'><tr><td align='center' valign='middle'>"
        +"<img src='img/progress/wait_circle.gif'/></td></tr></table></div>";
        
        if(isBusy){
            $("body").append(busyPanel);
        } else {
            $("body").find("#fullScreenBusyPanel").remove();
        }
    }
    
    this.setViewSwitcher = function(viewSwitcher){
        $("#viewSwitcher").html(viewSwitcher);
        $("#viewIcon").click(function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click",
                "value":$(this).attr("panel")
            };
            javaEventAsync($.toJSON(json));
            
        });
        
        $("#viewTable").click(function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click",
                "value":$(this).attr("panel")
            };
            javaEventAsync($.toJSON(json));
        });
        
        $("#viewMac").click(function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click",
                "value":$(this).attr("panel")
            };
            javaEventAsync($.toJSON(json));
        });
        
        $("#view").buttonset();
    }
    
    /**
     * id of root panel,
     * panel - html model for panel,
     * direction - slide direction (left, right, up, down)
     */
    this.setSliderPanel = function(id, panel, direction){
        $("#"+id).hide();
        $("#"+id).html(panel);
        
        $("#"+id).show("slide", {
            direction: direction
        }, 
        function(){
            // paint mactable
            if($("#"+id).find(".macTable").length > 0){
                var row = 0;
                $("#"+id).find(".macTable").each(function(index){
                    $(this).find("tr").each(function(index, element){
                        if($(element).attr("row")){
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
                                        if(row % 2){
                                            $(this).css("background-color", "#c4d6f9");
                                        } else {
                                            $(this).css("background-color", "#FFFFFF");
                                        }
                                        row++;
                                    });
                                    $(this).css("background-color", "#7a8bac");
                                }
                            }
                        });
                    });
                });
                
                getUICore().macTableButtonsView();
            }
        });
    }
    
    this.setNavigatorPanel = function(buttons){
        var t = "<table width='100%' cellpadding='0' cellspacing='0'>"
        +"<tr><td>"+buttons+"</td>"
        +"<td width='120' id='viewSwitcher'></td>"
        +"</tr></table>";
        //$("#navigationPanel").html(buttons);
        $("#navigationPanel").html(t);
        $("#navigationPanel").show();
        
        $(".navigatorButton").button({
            icons: {
                primary: "ui-icon-folder-collapsed"
            }
        });
        
        $('.navigatorButton').last().button({
            icons: {
                primary: "ui-icon-folder-open"
            }
        });
        
        $('.navigatorButton').first().button({
            icons: {
                primary: "ui-icon-home"
            },
            text:false
        });
        
        $(".navigatorButton").click(function(event){
            var identificator = "" + $(this).attr("identificator");
            var json = {
                "identificator":identificator, 
                "eventType":"click",
                "className":$(this).attr("className"),
                "dbid":$(this).attr("dbid")
            };
            javaEventAsync($.toJSON(json));
        });
    }
    
    this.getSelectedRibbonButtons = function(){
        var topButtonName = "";
        var operationButtonName = "";
        
        var tdElements = $("#ribbonTopPanel").find("td");
        for(var i = 0; i < tdElements.length; i++){
            var cls = $("#ribbonTopPanel").find("td")[i].className;
            if(cls == "ribbon_top_button_selected"){
                topButtonName = $("#ribbonTopPanel").find("td")[i].textContent;
                break;
            }
        }
        
        var val = $(".ribbon_bottom_button_selected").find(".ribbon_bottom_button_text");
        if(val.length > 0){
            operationButtonName = val[0].textContent;
        }
        
        return new Array(topButtonName, operationButtonName);
    }
    
    this.setSelectedRibbonButtons = function(topButtonName, operationButtonName){
        alert(topButtonName + ", " + operationButtonName);
        var tdElements = $("#ribbonTopPanel").find("td");
        for(var i = 0; i < tdElements.length; i++){
            $("#ribbonTopPanel").find("td")[i].className = "ribbon_top_button";
            if(topButtonName == $("#ribbonTopPanel").find("td")[i].textContent){
                //$("#debugPanel").html($("#debugPanel").html() + "<br/>FIND!!!");
                $("#ribbonTopPanel").find("td")[i].className = "ribbon_top_button_selected";
            }
        }
        
        $(".ribbon_bottom_button_selected").removeClass().addClass("ribbon_bottom_button");
        var b = $(".ribbon_bottom_button").find(".ribbon_bottom_button_text");
        for(i = 0; i < b.length; i++){
            //$("#debugPanel").html($("#debugPanel").html() + "<br/> ? = " + b[i].textContent);
            if(b[i].textContent == operationButtonName){
                $(b[i].parentNode).removeClass().addClass("ribbon_bottom_button_selected");
            }
        }
    }
    
    this.showLoggerPanel = function(){
        $("#loggerPanel").css("z-index", 500);
        $("#loggerPanel").css("visibility", "visible");
        $("#loggerPanel").show();
    }
    
    this.hideLoggerPanel = function(){
        $("#loggerPanel").css("z-index", 0);
        $("#loggerPanel").hide();
        $("#loggerPanel").css("visibility", "hidden");
    }
    
    this.setLoggerInfo = function(msg){
        $("#loggerInfo").html(msg);
    }
    
    this.showLockPanel = function(){
        $("#lockPanel").css("z-index", 400);
        $("#lockPanel").css("visibility", "visible");
        $("#lockPanel").show();
    }
    
    this.hideLockPanel = function(){
        $("#lockProgressBar").html("<img src=\"img/progress/wait_circle.gif\"/>");
        $("#lockPanel").css("z-index", 0);
        $("#lockPanel").css("visibility", "hidden");
        $("#lockPanel").hide();
    }
    
    this.setLockPanelProgressBar = function(val){
        $("#lockProgressBar").html(val);
    }
    
    this.hideNavigationPanel = function(){
        $("#navigationPanel").hide();
    //$("#navigationPanel").html("");
    //        $("#navigationPanel").show("slide", {
    //            direction: "down"
    //        }, function(){});
    }
    
    this.print = function(contentToPrint){
        var html = "<html>"
        +"<head>"
        +"<meta charset='utf-8'/>"
        +"<link type='text/css' href='css/ui-darkness/jquery-ui-1.8.16.custom.css' rel='stylesheet' />"
        +"<link href='css/core.css' rel='stylesheet' type='text/css'>"
        +"</head>"
        +"<body>"
        +contentToPrint
        +"</body>"
        +"</html>";
        $('#printFrame').contents().find('html').html(html);
        window.frames["printFrame"].focus();
        window.frames["printFrame"].print();
    }
    
    this.cancelLogin = function(){
        //<br><span style='font-size:xx-small'>[Press F5 button for restart]</span>
        $('body').html("<b>Bye!</b>");
    }
    
    this.setCashBox = function(val){
        $(".cashBox").html(val);
    }
    
    this.showFileChooser = function(){
        $('body').file().choose(function(e, input) {
            alert(input.val()); //alerts the chosen filename. 
        });
    //$('body').append("<input type='file' name='file' id='file' />");
    //document.getElementById("file").click();
    }
    
    this.showImageChooser = function(chooserPanel){
        $("#imageChooser").remove();
        $("body").append(chooserPanel);
        
        $("#imageChooser").dialog({
            autoOpen: false,
            height: $("#imageChooser").attr("panelHeight"),
            width: $("#imageChooser").attr("panelWidth"),
            modal: true,
            closeOnEscape: true
        });   
            
        $("#imageChooser").find(".imageIcon").click(function(event){
            var json = {
                "identificator":"" + $(this).attr("identificator"), 
                "eventType":"click",
                "src":$(this).attr("src")
            };
            
            javaEventAsync($.toJSON(json));
            $("#imageChooser").remove();
            
        });
        
        $("#imageChooser").dialog({
            show: 'slide'
        });
        
        $("#imageChooser").dialog("open");
    }
    
    this.hideImageChooser = function(){
        $("#imageChooser").remove();
    }
    
    this.dbDumpUpload = function(){
        var uploader = $("#btnDumpUpload").upload({
            name: 'file',
            action: '',
            enctype: 'multipart/form-data',
            params: {},
            autoSubmit: true,
            onSubmit: function() {},
            onComplete: function(response) {
                var json = {
                    "identificator":"ru.sibek.db.service.DataBaseServicePanel", 
                    "eventType":"fileUploaded",
                    "response":response
                };
                javaEventAsync($.toJSON(json));
            },
            onSelect: function() {
                var json = {
                    "identificator":"ru.sibek.db.service.DataBaseServicePanel", 
                    "eventType":"fileSelected",
                    "filename":uploader.filename()
                };
                javaEventAsync($.toJSON(json));
            }
        });        
        return false;
    }
    
    this.webCameraSlideShow = function(elementId, imageURL){
        var img = new Image();
        img.onload = function(){  
            $("#" + elementId).find("img").each(function(index){
                $(this).prop("src", img.src);
                console.log(img);
            });
        };  
        img.src = imageURL;
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
                            if(row % 2){
                                $(this).css("background-color", "#c4d6f9");
                            } else {
                                $(this).css("background-color", "#ffffff");
                            }
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
                                    if(row % 2){
                                        $(this).css("background-color", "#c4d6f9");
                                    } else {
                                        $(this).css("background-color", "#FFFFFF");
                                    }
                                    row++;
                                });
                                $(this).css("background-color", "#7a8bac");
                            }
                        }
                    });
                });
            });
            
            getUICore().macTableButtonsView();
        }
    }
    
    this.macTableButtonsView = function(){
        $(".macTableRemoveButton").button({
            icons: {
                primary: "ui-icon-trash"
            },
            text: false
        });
        $(".macTableRemoveButton").hide();
            
        $(".macTableEditButton").button({
            icons: {
                primary: "ui-icon-pencil"
            },
            text: false
        });
            
        $(".macTableAllRowCheckedButton").button({
            icons: {
                primary: "ui-icon-check"
            },
            text: false
        });
            
        $(".macTableAllRowUncheckedButton").button({
            icons: {
                primary: "ui-icon-close"
            },
            text: false
        });
    }
    
    this.refreshAgentsTable = function(elementId, html){
        $(elementId).html(html);
        var row = 0;
        $(".leftMacTable").find("tr").each(function(index, element){
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
            
        $(".leftMacTable").find("tr").click(function(event){
            var row = 0;
            $(".leftMacTable").find("tr").each(function(index){
                if(row % 2){
                    $(this).css("background-color", "#c4d6f9");
                } else {
                    $(this).css("background-color", "#FFFFFF");
                }
                
                row++;
            });
                
            $(this).css("background-color", "#7a8bac");
               
            if($(this).attr("row")){
                var tabl = $(this).parents(".leftMacTable")[0];
                var json = {
                    "identificator":$(tabl).attr("identificator"),
                    "eventType":"click",
                    "row":$(this).attr("row")
                };
                    
                if(!isRightClick){
                    javaEventAsync($.toJSON(json));
                }
            }
                
        });
    }
    
    this.setEditorContent = function(content){
        $("#editor").html(content);
    }
    
    this.setWorkPanelSelectable = function(isSelectable){
        if(isSelectable){
            $("#workPanel").selectable();
        } else {
            $("#workPanel").selectable("destroy");
        }
            
    }
    
    this.showEditor = function(html){
        $("#workPanel").selectable("destroy");
        $("#workPanel").html(html);
        
        $("#editor").tinymce({
            // Location of TinyMCE script
            script_url : 'js/tiny_mce/tiny_mce.js',

            // General options
            theme : "advanced",
            plugins : "pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras,template",

            // Theme options
            theme_advanced_buttons1 : "save,newdocument,|,bold,italic,underline,strikethrough,|,justifyleft,justifycenter,justifyright,justifyfull,styleselect,formatselect,fontselect,fontsizeselect",
            theme_advanced_buttons2 : "cut,copy,paste,pastetext,pasteword,|,search,replace,|,bullist,numlist,|,outdent,indent,blockquote,|,undo,redo,|,link,unlink,anchor,image,cleanup,help,code,|,insertdate,inserttime,preview,|,forecolor,backcolor",
            theme_advanced_buttons3 : "tablecontrols,|,hr,removeformat,visualaid,|,sub,sup,|,charmap,emotions,iespell,media,advhr,|,print,|,ltr,rtl,|,fullscreen",
            theme_advanced_buttons4 : "insertlayer,moveforward,movebackward,absolute,|,styleprops,|,cite,abbr,acronym,del,ins,attribs,|,visualchars,nonbreaking,template,pagebreak",
            theme_advanced_toolbar_location : "top",
            theme_advanced_toolbar_align : "left",
            theme_advanced_statusbar_location : "bottom",
            theme_advanced_resizing : true,
            external_image_list_url : "js/image_list.js"
        });   
    }
    
    this.savePage = function(){
        var json = {
            "identificator":"" + $("#editor").attr("identificator"), 
            "eventType":"save",
            "value":$("#editor").html()
        };
        javaEventAsync($.toJSON(json));
        return false;
    }
    
    this.setImageBrowser = function(html){
        $("#workPanel").selectable("destroy");
        $("#workPanel").html(html);
        $(".imageView").click(function(event){
            $(".imageView").removeClass("ui-selected");
            $(this).addClass("ui-selected");
            var json = {
                "identificator":"ru.granplat.plugin.editor.EditorPanel", 
                "eventType":"selectImage",
                "image":"" + $(this).attr("file")
            };
            javaEventAsync($.toJSON(json));
        });
        
    }
    
    this.imageUpload = function(){
        $("#btnImageUpload").upload({
            name: 'file',
            action: '',
            enctype: 'multipart/form-data',
            params: {},
            autoSubmit: true,
            onSubmit: function() {},
            onComplete: function(response) {
                var json = {
                    "identificator":"ru.granplat.plugin.editor.EditorPanel", 
                    "eventType":"fileUploaded",
                    "response":response
                };
                javaEventAsync($.toJSON(json));
            },
            onSelect: function() {
            //                var json = {
            //                    "identificator":"ru.sibek.db.service.DataBaseServicePanel", 
            //                    "eventType":"fileSelected",
            //                    "filename":uploader.filename()
            //                };
            //                javaEventAsync($.toJSON(json));
            }
        });        
        return false;
    }
    
    this.setProgressBarEnabled = function(enabled){
        isProgressBarEnabled = enabled;
    }
    
    this.isProgressBarEnabled = function(){
        return isProgressBarEnabled;
    }
    
    this.showFilterPanel = function(model, identificator){
        model = "<div align='right'>"
        +"<button id='hideFilterPanel' style='font-size:70%;font-weight:900'>^</button>"
        +"</div>" + model;
        $("#" + identificator).html(model);
        $("#" + identificator).hide();
        $("#" + identificator).show("fast");
        
        $("#hideFilterPanel").click(function(){
            $("#" + identificator).hide();
            var json = {
                "identificator":identificator, 
                "eventType":"click",
                "value":"hide"
            };
            javaEventAsync($.toJSON(json));
        });
    }
    
    this.hideFilterPanel = function(identificator){
        $("#" + identificator).hide();
    }
    
    this.refreshVideoFrame = function(pictureName, videoFrameId){
        var img = new Image();
        img.src = "http://"+window.location.host+"/img/stream/last/"+pictureName+"?q=" + new Date().getTime();
        img.onload = function(){
            document.getElementById(videoFrameId).src = img.src;
        };
    }
}

function debug(message){
//toJava(javaReflector, "debug", message);
}

function toJava(className, methodName, param){
    var p = encodeURIComponent(param);
    ws.send("{command:execute, sess:"+sess+", reflector:"+javaReflector+", method:"+methodName+", parameters:"+p+"}");
}

function javaEventAsync(json){
    var jsonEncode = encodeURIComponent(json);
    if(getUICore().isProgressBarEnabled()){
        getUICore().showProgressBar(100);
    }
    
    console.log(new Date() + ", javaEventAsync: command:execute, sess:"+sess+", reflector:"+javaReflector+", method:sendEvent, parameters:"+json);
    ws.send("{command:execute, sess:"+sess+", reflector:"+javaReflector+", method:sendEvent, parameters:"+jsonEncode+"}");
}

function ajaxFileUpload(){

    $.ajaxFileUpload({
        url:'/upload.do',
        secureuri:false,
        fileElementId:'fileToUpload',
        dataType: 'json',
        beforeSend:function(){
            alert("beforeSend");
        },
        complete:function(){
            alert("complete");
        },				
        success: function (data, status){
            alert(data + ", " + status);
            if(typeof(data.error) != 'undefined'){
                if(data.error != ''){
                    alert(data.error);
                }else{
                    alert(data.msg);
                }
            }
        },
        error: function (data, status, e){
            alert(e);
        }
    })
		
    return false;
}

function callback(identificator, jsEval){
    console.log("callback: " + identificator + ", " + jsEval);
    var val = eval(jsEval);
    var json = {
        "identificator":identificator,
        "value":val
    };
                    
    javaEventAsync($.toJSON(json));
}
