
    var wsUri = "ws://" + window.location.hostname +":" + window.location.port + "/messageSocket";

     websocket = new WebSocket(wsUri);
     websocket.onopen = function(evt) { onOpen(evt) };
     websocket.onclose = function(evt) { onClose(evt) };
     websocket.onmessage = function(evt) { onMessage(evt) };
     websocket.onerror = function(evt) { onError(evt) };

      function onOpen(evt)
      {
         writeToDiv("Connected");
      }

      function onClose(evt)
      {
        writeToDiv("Disconnected");
      }

      function onMessage(evt)
      {
         writeToDiv(evt.data);
      }

      function onError(evt)
      {
        writeToDiv("Error: " + evt.data);
      }

      function doSend(message)
      {
         //writeToDiv("Sent: " + message);
         websocket.send(message);
      }

      function writeToDiv(message)
      {
          var div = document.getElementById('uploadresult');
          div.innerHTML = div.innerHTML + "<p>" + message + "</p>";
      }
