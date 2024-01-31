package it.unipi.dsmt.service;

import it.unipi.dsmt.DTO.GameRequestDTO;
import it.unipi.dsmt.entity.PlayersWaiting;
import it.unipi.dsmt.util.Costant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

public class UserService {
    Vector<String> uniqueIdPlayer = new Vector<>();
    Vector<String> uniqueIdGuesser = new Vector<>();
    PlayersWaiting globalPlayersWaiting = new PlayersWaiting(0,0);
    private static final AtomicLong counter = new AtomicLong(0);
    boolean firstPlayer = false;
    public static String generateUniqueId() {
        // Ottieni il timestamp attuale in millisecondi
        long timestamp = System.currentTimeMillis();

        // Ottieni il valore corrente del contatore
        long count = counter.getAndIncrement();

        // Combina timestamp e contatore per creare l'ID univoco
        String uniqueId = timestamp + "-" + count;

        return uniqueId;
    }
    public int handleGame(GameRequestDTO request, HashMap<String, PlayersWaiting> gameMap){
        PlayersWaiting playersWaiting;

        String matchId = "";

        if(request.getGameId() == ""){
            System.out.println(uniqueIdGuesser);
            System.out.println(uniqueIdPlayer);
            if(globalPlayersWaiting.getNumPlayers() % 2 == 0 && request.getRole() == Costant.PlayerRole.Player && uniqueIdPlayer.isEmpty()){
                System.out.println("non entro qui");
                matchId = generateUniqueId();
                uniqueIdPlayer.add(matchId);
                firstPlayer = true;
                uniqueIdGuesser.add(matchId);
                globalPlayersWaiting.setNumPlayers(globalPlayersWaiting.getNumPlayers()+1);
                System.out.println(globalPlayersWaiting.getNumPlayers());
            }
            else if (!firstPlayer && request.getRole() == Costant.PlayerRole.Player && !uniqueIdPlayer.isEmpty()){
                System.out.println("entro qui");
                firstPlayer = true;
                matchId = uniqueIdPlayer.firstElement();
                globalPlayersWaiting.setNumPlayers(globalPlayersWaiting.getNumPlayers()+1);
            }
            else if(firstPlayer && request.getRole() == Costant.PlayerRole.Player){
                System.out.println("entro pippo");
                firstPlayer = false;
                matchId = uniqueIdPlayer.firstElement();
                uniqueIdPlayer.removeElementAt(0);
                globalPlayersWaiting.setNumPlayers(globalPlayersWaiting.getNumPlayers()+1);
            }
            else if(request.getRole() == Costant.PlayerRole.Guesser && !uniqueIdGuesser.isEmpty()){
                matchId = uniqueIdGuesser.firstElement();
                uniqueIdGuesser.removeElementAt(0);
                globalPlayersWaiting.setNumGuessers(globalPlayersWaiting.getNumGuessers()+1);
            }
            else if(request.getRole() == Costant.PlayerRole.Guesser && uniqueIdGuesser.isEmpty()){
                matchId = generateUniqueId();
                uniqueIdPlayer.add(matchId);
                firstPlayer = false;
                globalPlayersWaiting.setNumGuessers(globalPlayersWaiting.getNumGuessers()+1);
            }
            request.setGameId(matchId);
        }

        System.out.println(request.getUsernamePlayer());
        System.out.println(request.getGameId());

        synchronized(gameMap) {
            playersWaiting = gameMap.get(request.getGameId());
            if (playersWaiting == null || (playersWaiting.getNumGuessers() >= 1 && playersWaiting.getNumPlayers() >= 2))
                playersWaiting = new PlayersWaiting(0, 0);
            if(playersWaiting.isInvitationDeclined()){
                playersWaiting.setInvitationDeclined(false);
                return -1;
            }

        }
        synchronized(playersWaiting) {
            if (request.getRole() == Costant.PlayerRole.Player) {
                playersWaiting.setNumPlayers(playersWaiting.getNumPlayers()+1);
                playersWaiting.getUsernamePlayers().add(request.getUsernamePlayer());
                System.out.println("add" + playersWaiting.getUsernamePlayers());
            } else {
                playersWaiting.setNumGuessers(playersWaiting.getNumGuessers()+1);
                playersWaiting.getUsernameGuesser().add(request.getUsernamePlayer());
            }
        }
        synchronized (gameMap) {
            System.out.println("players: " + playersWaiting.getNumPlayers() + " guessers: " + playersWaiting.getNumGuessers());
            gameMap.put(request.getGameId(), playersWaiting);
        }
        synchronized (playersWaiting){
            if(playersWaiting.getNumPlayers() > 2 || playersWaiting.getNumGuessers() > 1)
                playersWaiting.notifyAll();
            else if(playersWaiting.getNumPlayers() == 2 && playersWaiting.getNumGuessers() == 1)
                playersWaiting.notifyAll();
            else if(playersWaiting.getNumPlayers() < 2 || playersWaiting.getNumGuessers() < 1 || !playersWaiting.isInvitationDeclined())
                try {
                    playersWaiting.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
        }

        System.out.println();
        synchronized (gameMap) {
            if (playersWaiting.getNumPlayers() > 2 || playersWaiting.getNumGuessers() > 1)
                return 0;
            // ritorna risposta quando ci sono 3 player
            if (playersWaiting.getNumPlayers() == 2 && playersWaiting.getNumGuessers() == 1)
                return 1;
        }
        return -1;
    }
}
