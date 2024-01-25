$(document).ready(function () {
  document.getElementById('login_btn').onclick = function (e) {
    let uname = document.getElementById('uname_login').value
    let psw = document.getElementById('psw_login').value

    if(uname === "admin" && psw === "admin"){
      location.href = "../templates/admin.html"
      sessionStorage.setItem("userLog","admin");
    }

    let requestUser = {
      username : uname,
      password : psw
    };

    $.ajax({
      url : "http://localhost:5050/login",
      data : JSON.stringify(requestUser),
      type : "POST",
      dataType: "json",
      contentType: 'application/json',
      success: function () {
        sessionStorage.setItem("userLog",uname);
        sessionStorage.setItem("gameId","");
        location.href = "../templates/playerMainPage.html"
      },
      error: function(xhr) {
        let response = JSON.parse(xhr.responseText)
        alert(response.answer)
      }
    })
  }
});