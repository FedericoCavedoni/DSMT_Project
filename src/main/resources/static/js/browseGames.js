let user_logged = sessionStorage.getItem("userLog")
document.addEventListener("DOMContentLoaded", function () {
    checkLogIn();

    generate_games();

    document.getElementById('logout').onclick = function () {
        logout();
    };

    document.getElementById('home').onclick = function () {
        location.href = "../templates/playerMainPage.html";
    }
});

function logout(){
    if(!confirm("Are you sure you want to log out?")) return;

    $.ajax({
        url: "http://localhost:5050/logout",
        type: "POST",
        data: user_logged,
        dataType: "text",
        contentType: 'application/json',
        success: function () {
            sessionStorage.removeItem("userLog");
            location.href = "../templates/login.html";
        },
        error: function (xhr) {
            alert(xhr.responseText);
        }
    });
}
function checkLogIn(){
    if(!sessionStorage.getItem("userLog")){
        alert("User not logged!")
        location.href = "../templates/home.html"
    }
}

function generate_games() {
    document.getElementById('games_list').innerHTML = "";
    let username = sessionStorage.getItem("userLog")

    $.ajax({
        url: "http://localhost:5050/browseGames",
        data: username,
        type: "POST",
        contentType: 'application/json',
        success: function(data) {
            var jsonData = JSON.parse(data);

            var entries = jsonData.entries;

            if (entries) {
                entries.forEach(function (entry) {
                    let user1 = entry.user1;
                    let user2 = entry.user2;
                    let user3 = entry.user3;
                    let score = entry.score;
                    let timestamp = new Date(entry.timestamp).toLocaleString();

                    let row = document.createElement('div');
                    row.classList.add('game-entry');
                    //row.style.backgroundColor = '#8FBC8F'; cambiare colore cose selezionate
                    row.innerHTML = `
                        <p><strong>User1:</strong> ${user1}</p>
                        <p><strong>User2:</strong> ${user2}</p>
                        <p><strong>User3:</strong> ${user3}</p>
                        <p><strong>Score:</strong> ${score}</p>
                        <p><strong>Timestamp:</strong> ${timestamp}</p>
                    `;

                    document.getElementById('games_list').appendChild(row);
                });
            } else {
                console.error('La proprietà "entries" è mancante o non è definita nella risposta JSON.');
            }
        },
        error: function (xhr) {
            alert(xhr.responseText);
        }
    });
}