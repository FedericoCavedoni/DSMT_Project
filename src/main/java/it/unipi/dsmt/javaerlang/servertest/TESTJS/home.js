var socket;
//first home page load
$(document).ready(function () {
    socket = new WebSocket("ws://localhost:8090/erlServer");

    // Gestisci l'apertura della connessione
    socket.addEventListener("open", (event) => {
        console.log("Connessione aperta:", event);
    });

    socket.onmessage = receive;
    
    
    // Puoi inviare messaggi al server qui
    //socket.send("Ciao, sono il client!");
  //generate_business();
});

function send(){
  socket.send(JSON.stringify("prova"));
}

function receive(event){
  // parse of the JSON object received and execute
  var message = JSON.parse(event.data);
  alert(message);
}

//next arrow button
document.getElementById('next').onclick = function () {
  send();
};

