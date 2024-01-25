var socket;

function initializeSocket(){
    socket = new WebSocket("ws://localhost:8090/erlServer");
    // Socket opened
    socket.addEventListener("open", (event) => {
        console.log("Connessione WebSocket aperta:", event);
        //socket.send("Ciao, sono il client!");
    });

    socket.onclose = closeWebSocket;
    socket.onmessage = handleWebSocketMessage;
    socket.onerror = handleWebSocketError;
}

function closeWebSocket(){
    console.log("Connessione WebSocket chiusa");
}

function handleWebSocketError(){
    console.error("Errore WebSocket:");
}

function handleWebSocketMessage(event){
    console.log("Dati ricevuti dal server:", event.data);

    // formato messaggio type, msg
    var messaggioJSON = JSON.parse(event.data);
    console.log("Messaggio decodificato:", messaggioJSON);

    // Ora puoi accedere alle proprietà del tuo oggetto JSON
    var type = messaggioJSON.type;
    var msg = messaggioJSON.msg;

    // Fai qualcosa in base al tipo di messaggio ricevuto
    switch (type) {
        // Ricezione parola iniziale
        case "wordToGuess":
            // far apparire parola sopra il tabellone
            break;
        // Ricezione parola durante gioco
        case "word":
            // far apparire parola dentro il tabellone
            break;
        // Ricezione tentativo indovinare
        case "guessWord":
            // far apparire parola sotto il tabellone
            break;
        // Ricezione risultato tentativo indovinare
        case "guessResult":
            // fare display del risultato
            if(msg === "OK")
                points += 1
            if(msg === "NO")
                points -= 1
    }
}

function sendSocketMessage(mess){
    socket.send(JSON.stringify(mess));
}