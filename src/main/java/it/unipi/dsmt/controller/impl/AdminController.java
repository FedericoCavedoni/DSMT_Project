package it.unipi.dsmt.controller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.dsmt.DAO.MatchDAO;
import it.unipi.dsmt.DAO.UserDAO;
import it.unipi.dsmt.DTO.*;

import it.unipi.dsmt.controller.AdminControllerInterface;
import it.unipi.dsmt.util.SessionManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class AdminController implements AdminControllerInterface {

    private UserDAO userDAO = new UserDAO();

    @PostMapping("/viewUsers")
    @Override
    public ResponseEntity<String> viewUsers(@RequestBody ViewUsersRequestDTO request) {
        PageDTO<UserDTO> pageDTO = userDAO.viewUsers(request.getPage());
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String jsonResult = objectMapper.writeValueAsString(pageDTO);
            return new ResponseEntity<>(jsonResult, HttpStatus.OK);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Errore durante la serializzazione in JSON", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/removeUser")
    @Override
    public ResponseEntity<String> removeUser(@RequestBody String username) {
        if(!userDAO.removeUser(username)){
            return new ResponseEntity<>("Error during the operation", HttpStatus.BAD_REQUEST);
        }

        if(!userDAO.removeUserFromFriends(username)){
            return new ResponseEntity<>("Error during the operation", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>("User '"+username+"' removed", HttpStatus.OK);
    }

    @PostMapping("/browseGamesAdmin")
    @Override
    public ResponseEntity<String> browseGames() {
        MatchDAO matchDAO = new MatchDAO();
        ObjectMapper objectMapper = new ObjectMapper();

        PageDTO<MatchDTO> pageDTO = matchDAO.browseGames(null);

        try {
            String jsonResult = objectMapper.writeValueAsString(pageDTO);
            return new ResponseEntity<>(jsonResult, HttpStatus.OK);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Errore durante la serializzazione in JSON", HttpStatus.BAD_REQUEST);
        }
    }

}

