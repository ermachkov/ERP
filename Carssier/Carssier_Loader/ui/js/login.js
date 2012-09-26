var login = null;
var ws;

jQuery(document).ready(function(){
    login = getLogin();
});

function getLogin(){
    if(login == null){
        login = new Login();
        login.init();
        login.initWebSocket();
    }
    
    return login;
}

function Login(){
    
    this.initWebSocket = function(){
        ws = new WebSocket("ws://localhost:1573");
        ws.onopen = function () {
            ws.send("{command:register, sess:" + sess + "}");
        }
        
        ws.onmessage = function(message){
            if(message.data == ""){
                return;
            }
        
            eval(message.data);
        }
        
        ws.onclose = function(){
            getLogin().initWebSocket();
        }
    }
    
    this.init = function(){
        $("#btnLogin").button().click(function(event){
            login.doLogin();
        });
    }
    
    this.doLogin = function(){
        var json = {login:$("#txtLogin").val(), password:$("#txtPassword").val()};
        alert($.toJSON(json));
    }
}


