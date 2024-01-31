package it.unipi.dsmt.controller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.dsmt.DAO.FriendDAO;
import it.unipi.dsmt.DAO.UserDAO;
import it.unipi.dsmt.controller.UserControllerInterface;
import it.unipi.dsmt.util.Costant;
import it.unipi.dsmt.entity.PlayersWaiting;
import it.unipi.dsmt.entity.Invite;
import it.unipi.dsmt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import it.unipi.dsmt.DTO.*;
import it.unipi.dsmt.util.SessionManagement;

import java.util.HashMap;
import java.util.Vector;

@RestController
public class UserController implements UserControllerInterface {
    @Autowired
    SessionManagement session;

    private UserDAO userDao = new UserDAO();

    @PostMapping("/signup")
    @Override
    public ResponseEntity<String> signUp(@RequestBody UserDTO UserSignUp) {

        UserDTO user = new UserDTO(UserSignUp.getFirstName(), UserSignUp.getLastName(), UserSignUp.getUsername(), UserSignUp.getPassword());
        int control = userDao.signup(user);

        if (control == 1){
            session = SessionManagement.getInstance();
            session.setLogUser(user.getUsername());

            return new ResponseEntity<>("Signup success", HttpStatus.OK);
        }
        else if(control == 0) return new ResponseEntity<>("Username already used", HttpStatus.BAD_REQUEST);

        else return new ResponseEntity<>("User not inserted", HttpStatus.BAD_REQUEST);

    }

    @PostMapping("/viewFriends")
    @Override
    public ResponseEntity<String> viewFriends(@RequestBody ViewFriendsRequestDTO request) {
        FriendDAO friendDAO = new FriendDAO();
        PageDTO<FriendDTO> pageDTO = friendDAO.viewFriends(request.getUsername(), request.getPage());
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String jsonResult = objectMapper.writeValueAsString(pageDTO);
            return new ResponseEntity<>(jsonResult, HttpStatus.OK);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Errore durante la serializzazione in JSON", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/removeFriend")
    @Override
    public ResponseEntity<String> removeFriend(@RequestBody FriendRequestDTO request) {
        FriendDAO friendDAO = new FriendDAO();
        if(friendDAO.removeFriend(request.getUsername(), request.getUsernameFriend()))
            return new ResponseEntity<>(request.getUsernameFriend() + " removed as friend", HttpStatus.OK);
        else
            return new ResponseEntity<>("Error occurred, friend not removed", HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/addFriend")
    @Override
    public ResponseEntity<String> addFriend(@RequestBody FriendRequestDTO request) {
        FriendDAO friendDAO = new FriendDAO();
        if(friendDAO.addFriend(request.getUsername(), request.getUsernameFriend()))
            return new ResponseEntity<>(request.getUsernameFriend()+" added as a friend", HttpStatus.OK);
        else
            return new ResponseEntity<>("Error occurred, friend not added", HttpStatus.BAD_REQUEST);
    }

    HashMap<String, PlayersWaiting> gameMap = new HashMap<>();
    UserService userService = new UserService();
    @Async
    @PostMapping("/game")
    @Override
    public ResponseEntity<String> game(@RequestBody GameRequestDTO request){
        System.out.println("request: "+request.getGameId()+" "+request.getRole()+ " " + request.getUsernamePlayer());

        int res = userService.handleGame(request, gameMap);
        if(res == 0)
            return new ResponseEntity<>("Error occurred, game can't start", HttpStatus.BAD_REQUEST);
        if(res == 1){
            PlayersWaiting playersWaiting = gameMap.get(request.getGameId());
            System.out.println(request.getGameId());
            System.out.println(playersWaiting.getUsernamePlayers());
            System.out.println(playersWaiting.getUsernameGuesser());
            String jsonResult = "{\"player1\":\"" + playersWaiting.getUsernamePlayers().get(0) +
                    "\",\"player2\":\"" + playersWaiting.getUsernamePlayers().get(1) +
                    "\",\"guesser\":\"" + playersWaiting.getUsernameGuesser().get(0) + "\"}";
            return new ResponseEntity<>(jsonResult, HttpStatus.OK);
        }
        return new ResponseEntity<>("Invitation refused", HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/globalSearch")
    @Override
    public ResponseEntity<String> globalSearch(@RequestBody FriendSearchDTO request) {
        UserDAO userDAO = new UserDAO();
        UserDTO userDTO = userDAO.globalSearch(request.getUsernameToSearch());

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String jsonResult = objectMapper.writeValueAsString(userDTO);
            return new ResponseEntity<>(jsonResult, HttpStatus.OK);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Errore durante la serializzazione in JSON", HttpStatus.BAD_REQUEST);
        }
    }

    Vector<Invite> invites = new Vector<>();
    @Async
    @PostMapping("/inviteFriend")
    @Override
    public ResponseEntity<String> inviteFriend(@RequestBody InviteDTO request){
        invites.add(new Invite(request));
        synchronized (gameMap) {
            PlayersWaiting playersWaiting = new PlayersWaiting(0, 0);
            gameMap.put(request.getGameId(), playersWaiting);
        }
        return new ResponseEntity<>("correct invite", HttpStatus.OK);
    }

    @Async
    @PostMapping("/checkInvite")
    @Override
    public ResponseEntity<String> checkInvite(@RequestBody String username){
        for(Invite invitation : invites){
            if(username.equals(invitation.getPlayer1())){
                String jsonResult = "{\"id\":\"" + invitation.getId() +
                        "\",\"role\":\"" + invitation.getRole1() + "\",\"userInvite\":\"" + invitation.getUserInvite() + "\"}";
                invitation.setPlayer1("");
                if(invitation.getPlayer1() == "" && invitation.getPlayer2() == "")
                    invites.remove(invitation);
                return new ResponseEntity<>(jsonResult, HttpStatus.OK);
            }
            if(username.equals(invitation.getPlayer2())){
                String jsonResult = "{\"id\":\"" + invitation.getId() +
                        "\",\"role\":\"" + invitation.getRole2() + "\",\"userInvite\":\"" + invitation.getUserInvite() + "\"}";
                invitation.setPlayer2("");
                if(invitation.getPlayer1() == "" && invitation.getPlayer2() == "")
                    invites.remove(invitation);
                return new ResponseEntity<>(jsonResult, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>("", HttpStatus.NO_CONTENT);
    }

    @Async
    @PostMapping("/declineInvitation")
    @Override
    public ResponseEntity<String> declineInvitation(@RequestBody String gameId) {
        PlayersWaiting playersWaiting = gameMap.get(gameId);
        synchronized (playersWaiting){
            if(playersWaiting.getNumPlayers() + playersWaiting.getNumGuessers() == 1)
                playersWaiting.setInvitationDeclined(true);
            playersWaiting.notifyAll();
        }
        return new ResponseEntity<>("Invitation declined succesfuly", HttpStatus.OK);
    }
}




