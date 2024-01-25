package it.unipi.dsmt.controller;

import it.unipi.dsmt.DTO.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface GenericControllerInterface {

     ResponseEntity<ResponseRequest> login(UserAccessRequest requestUser);
     ResponseEntity<String> isLogged(String Username);
     ResponseEntity<String> logout(String Username);
     ResponseEntity<String> browseGames(String Username);
     ResponseEntity<String> insertMatch(@RequestBody MatchDTO match);
}
