package it.unipi.dsmt.controller;

import it.unipi.dsmt.DTO.*;
import org.springframework.http.ResponseEntity;

public interface AdminControllerInterface {

    ResponseEntity<String> viewUsers(ViewUsersRequestDTO request);

    ResponseEntity<String> removeUser(String username);

    ResponseEntity<String> browseGames();

}