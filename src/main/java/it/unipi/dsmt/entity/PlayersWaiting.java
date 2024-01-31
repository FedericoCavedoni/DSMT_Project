package it.unipi.dsmt.entity;

import java.util.ArrayList;

public class PlayersWaiting{
    int numPlayers = 0;
    int numGuessers = 0;
    ArrayList<String> usernamePlayers = new ArrayList<>();
    ArrayList<String> usernameGuesser = new ArrayList<>();
    boolean invitationDeclined = false;
    public PlayersWaiting(int numPlayers, int numGuessers, ArrayList<String> usernamePlayers, ArrayList<String> usernameGuesser){
        this.numPlayers = numPlayers;
        this.numGuessers = numGuessers;
        this.usernamePlayers = usernamePlayers;
        this.usernameGuesser = usernameGuesser;
    }
    public PlayersWaiting(int numPlayers, int numGuessers){
        this.numPlayers = numPlayers;
        this.numGuessers = numGuessers;
        this.usernamePlayers = new ArrayList<>();
        this.usernameGuesser = new ArrayList<>();
        this.invitationDeclined = false;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public void setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
    }

    public int getNumGuessers() {
        return numGuessers;
    }

    public void setNumGuessers(int numGuessers) {
        this.numGuessers = numGuessers;
    }

    public ArrayList<String> getUsernamePlayers() {
        return usernamePlayers;
    }

    public void setUsernamePlayers(ArrayList<String> usernamePlayers) {
        this.usernamePlayers = usernamePlayers;
    }

    public ArrayList<String> getUsernameGuesser() {
        return usernameGuesser;
    }

    public void setUsernameGuesser(ArrayList<String> usernameGuesser) {
        this.usernameGuesser = usernameGuesser;
    }

    public boolean isInvitationDeclined() {
        return invitationDeclined;
    }

    public void setInvitationDeclined(boolean invitationDeclined) {
        this.invitationDeclined = invitationDeclined;
    }
}