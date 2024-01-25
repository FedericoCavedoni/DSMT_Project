package it.unipi.dsmt.controller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.dsmt.DAO.MatchDAO;
import it.unipi.dsmt.DAO.UserDAO;
import it.unipi.dsmt.controller.GenericControllerInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import it.unipi.dsmt.DTO.*;
import it.unipi.dsmt.util.SessionManagement;

@RestController
@SessionAttributes("userlog")
public class GenericController implements GenericControllerInterface {
    @Autowired
    SessionManagement session;

    private UserDAO userDao = new UserDAO();


    @PostMapping("/login")
    @Override
    public ResponseEntity<ResponseRequest> login(@RequestBody UserAccessRequest requestUser) {
        session = SessionManagement.getInstance();
        ResponseRequest responsereq;

        try {
            UserDTO user = userDao.login(requestUser.getUsername(), requestUser.getPassword());
            if (user == null) {
                responsereq = new ResponseRequest("Wrong Username or Password inserted");
                return new ResponseEntity<>(responsereq, HttpStatus.BAD_REQUEST);
            }

            session.setLogUser(user.getUsername());
        } catch (Exception e) {
            responsereq = new ResponseRequest(e.getMessage());
            return new ResponseEntity<>(responsereq, HttpStatus.UNAUTHORIZED);
        }

        ResponseRequest response = new ResponseRequest("Login success");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/isLogged")
    @Override
    public ResponseEntity<String> isLogged(@RequestBody String Username) {
        session = SessionManagement.getInstance();
        if (!session.isUserLogged(Username))
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        else
            return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @PostMapping("/logout")
    @Override
    public ResponseEntity<String> logout(@RequestBody String Username) {
        session = SessionManagement.getInstance();
        if(!session.isUserLogged(Username)){
            ResponseRequest response = new ResponseRequest("Logout failed");
            return new ResponseEntity<>(response.getAnswer(), HttpStatus.FORBIDDEN);
        }
        session.logoutUser(Username);
        ResponseRequest response = new ResponseRequest("Logout success");
        return new ResponseEntity<>(response.getAnswer(), HttpStatus.OK);
    }

    @PostMapping("/browseGames")
    @Override
    public ResponseEntity<String> browseGames(@RequestBody String username) {
        MatchDAO matchDAO = new MatchDAO();
        ObjectMapper objectMapper = new ObjectMapper();

        PageDTO<MatchDTO> pageDTO = matchDAO.browseGames(username);

        try {
            String jsonResult = objectMapper.writeValueAsString(pageDTO);
            return new ResponseEntity<>(jsonResult, HttpStatus.OK);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Errore durante la serializzazione in JSON", HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/insertMatch")
    @Override
    public ResponseEntity<String> insertMatch(@RequestBody MatchDTO match) {
        MatchDAO matchDAO = new MatchDAO();

        try {
            if (!matchDAO.insert(match)) {
                return new ResponseEntity<>("Error during the insert", HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>("Inserted correctly", HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("Server error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}