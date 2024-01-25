package it.unipi.dsmt.controller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.dsmt.DAO.FriendDAO;
import it.unipi.dsmt.DAO.UserDAO;
import it.unipi.dsmt.controller.UserControllerInterface;
import it.unipi.dsmt.util.Costant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import it.unipi.dsmt.DTO.*;
import it.unipi.dsmt.util.SessionManagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

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


    class PlayersWaiting{
        int numPlayers = 0;
        int numGuessers = 0;
        ArrayList<String> usernamePlayers = new ArrayList<>();
        ArrayList<String> usernameGuesser = new ArrayList<>();
        PlayersWaiting(int numPlayers, int numGuessers, ArrayList<String> usernamePlayers, ArrayList<String> usernameGuesser){
            this.numPlayers = numPlayers;
            this.numGuessers = numGuessers;
            this.usernamePlayers = usernamePlayers;
            this.usernameGuesser = usernameGuesser;
        }
        PlayersWaiting(int numPlayers, int numGuessers){
            this.numPlayers = numPlayers;
            this.numGuessers = numGuessers;
            this.usernamePlayers = new ArrayList<>();
            this.usernameGuesser = new ArrayList<>();
        }
    }


    HashMap<String, it.unipi.dsmt.controller.impl.UserController.PlayersWaiting> gameMap = new HashMap<>();

    private static final AtomicLong counter = new AtomicLong(0);

    public static String generateUniqueId() {
        // Ottieni il timestamp attuale in millisecondi
        long timestamp = System.currentTimeMillis();

        // Ottieni il valore corrente del contatore
        long count = counter.getAndIncrement();

        // Combina timestamp e contatore per creare l'ID univoco
        String uniqueId = timestamp + "-" + count;

        return uniqueId;
    }


    int playerCounter = 0;
    int guesserCounter = 0;
    Vector<String> uniqueIdPlayer = new Vector<>();
    Vector<String> uniqueIdGuesser = new Vector<>();
    PlayersWaiting globalPlayersWaiting = new PlayersWaiting(0,0);

    boolean firstPlayer = true;
    //boolean rejectedInvitation = false;
    @Async
    @PostMapping("/game")
    @Override
    public ResponseEntity<String> game(@RequestBody GameRequestDTO request){
        // logica attesa 3 player
        // Idea: --> HashMap sul gameId, role
        // quando raggiunge 2 player e 1 guesser il gioco è pronto e si manda la risposta
        // PROBLEMA MULTITHREADING?????!!!!!

        System.out.println("request: "+request.getGameId()+" "+request.getRole()+ " " + request.getUsernamePlayer());

        PlayersWaiting playersWaiting;

        String matchId = "";


        if(request.getGameId() == ""){
            if(globalPlayersWaiting.numPlayers % 2 == 0 && request.getRole() == Costant.PlayerRole.Player && firstPlayer){
                matchId = generateUniqueId();
                uniqueIdPlayer.add(matchId);
                uniqueIdGuesser.add(matchId);
                globalPlayersWaiting.numPlayers++;
            }
            else if (!firstPlayer && request.getRole() == Costant.PlayerRole.Player){
                firstPlayer = true;
                matchId = uniqueIdPlayer.firstElement();
                globalPlayersWaiting.numPlayers++;
            }
            else if(request.getRole() == Costant.PlayerRole.Player){
                matchId = uniqueIdPlayer.firstElement();
                uniqueIdPlayer.removeElementAt(0);
                globalPlayersWaiting.numPlayers++;
            }
            else if(request.getRole() == Costant.PlayerRole.Guesser && !uniqueIdGuesser.isEmpty()){
                matchId = uniqueIdGuesser.firstElement();
                uniqueIdGuesser.removeElementAt(0);
                globalPlayersWaiting.numGuessers++;
            }
            else if(request.getRole() == Costant.PlayerRole.Guesser && uniqueIdGuesser.isEmpty()){
                matchId = generateUniqueId();
                uniqueIdPlayer.add(matchId);
                uniqueIdGuesser.add(matchId);
                firstPlayer = false;
                globalPlayersWaiting.numGuessers++;
            }
            request.setGameId(matchId);
        }

        System.out.println(request.getUsernamePlayer());
        System.out.println(request.getGameId());

        synchronized(gameMap) {
            playersWaiting = gameMap.get(request.getGameId());
            if (playersWaiting == null)
                playersWaiting = new PlayersWaiting(0, 0);
        }
        synchronized(playersWaiting) {
            if (request.getRole() == Costant.PlayerRole.Player) {
                playersWaiting.numPlayers++;
                playersWaiting.usernamePlayers.add(request.getUsernamePlayer());
            } else {
                playersWaiting.numGuessers++;
                playersWaiting.usernameGuesser.add(request.getUsernamePlayer());
            }
        }
        synchronized (gameMap) {
            System.out.println("players: " + playersWaiting.numPlayers + " guessers: " + playersWaiting.numGuessers);
            gameMap.put(request.getGameId(), playersWaiting);
        }
        synchronized (playersWaiting){
            if(playersWaiting.numPlayers > 2 || playersWaiting.numGuessers > 1)
                playersWaiting.notifyAll();
            else if(playersWaiting.numPlayers == 2 && playersWaiting.numGuessers == 1)
                playersWaiting.notifyAll();
            else if(playersWaiting.numPlayers < 2 || playersWaiting.numGuessers < 1)
                try {
                    playersWaiting.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
        }

        System.out.println();
        /*if(rejectedInvitation){
            rejectedInvitation = false;
            gameMap.put(request.getGameId(), new it.unipi.dsmt.controller.impl.UserController.PlayersWaiting(0, 0));
            return new ResponseEntity<>("Invitation rejected, game not started", HttpStatus.BAD_REQUEST);
        }*/
        synchronized (gameMap) {
            if (playersWaiting.numPlayers > 2 || playersWaiting.numGuessers > 1) {
                // error handling
                gameMap.put(request.getGameId(), new PlayersWaiting(0, 0));
                return new ResponseEntity<>("Error occurred, game can't start", HttpStatus.BAD_REQUEST);
            }

            // ritorna risposta quando ci sono 3 player
            if (playersWaiting.numPlayers == 2 && playersWaiting.numGuessers == 1) {
                String jsonResult = "{\"player1\":\"" + playersWaiting.usernamePlayers.get(0) +
                        "\",\"player2\":\"" + playersWaiting.usernamePlayers.get(1) +
                        "\",\"guesser\":\"" + playersWaiting.usernameGuesser.get(0) + "\"}";
                gameMap.put(request.getGameId(), new PlayersWaiting(0, 0));
                return new ResponseEntity<>(jsonResult, HttpStatus.OK);
            }
        }

        return new ResponseEntity<>("Error occurred, game can't start", HttpStatus.BAD_REQUEST);
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

    class invite{
        String id;
        String player1;
        String player2;
        String userInvite;
        Costant.PlayerRole role1;
        Costant.PlayerRole role2;

        invite(InviteDTO invite){
            this.id = invite.getGameId();
            this.userInvite = invite.getUserInvite();
            this.player1 = invite.getPlayer1();
            this.player2 = invite.getPlayer2();
            this.role1 = invite.getRole1();
            this.role2 = invite.getRole2();
        }

        String getId(){
            return id;
        }

        String getPlayer1() {return player1;}
        String getPlayer2() {return player2;}
        String getUserInvite() {return userInvite;}
        Costant.PlayerRole getRole1() { return role1;}
        Costant.PlayerRole getRole2() { return role2;}

        void setPlayer1(String username){
            player1 = username;
        }
        void setPlayer2(String username){
            player2 = username;
        }

    };

    Vector<invite> invites = new Vector<>();
    @Async
    @PostMapping("/inviteFriend")
    @Override
    public ResponseEntity<String> inviteFriend(@RequestBody InviteDTO request){
        invites.add(new invite(request));
        return new ResponseEntity<>("correct invite", HttpStatus.OK);
    }

    @Async
    @PostMapping("/checkInvite")
    @Override
    public ResponseEntity<String> checkInvite(@RequestBody String username){
        for(invite invitation : invites){
            if(username.equals(invitation.player1)){
                String jsonResult = "{\"id\":\"" + invitation.getId() +
                        "\",\"role\":\"" + invitation.getRole1() + "\",\"userInvite\":\"" + invitation.getUserInvite() + "\"}";
                invitation.setPlayer1("");
                if(invitation.getPlayer1() == "" && invitation.getPlayer2() == "")
                    invites.remove(invitation);
                return new ResponseEntity<>(jsonResult, HttpStatus.OK);
            }
            if(username.equals(invitation.player2)){
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
        //rejectedInvitation = true;
        synchronized (playersWaiting){
            playersWaiting.notifyAll();
        }
        return new ResponseEntity<>("Invitation declined succesfuly", HttpStatus.OK);
    }
}




