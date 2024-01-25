package it.unipi.dsmt.DTO;

import it.unipi.dsmt.util.Costant;

public class GameRequestDTO {
    private String gameId;
    private Costant.PlayerRole role;

    public String getUsernamePlayer() {
        return usernamePlayer;
    }

    public void setUsernamePlayer(String usernamePlayer) {
        this.usernamePlayer = usernamePlayer;
    }

    private String usernamePlayer;

    public GameRequestDTO(){}

    public GameRequestDTO(String gameId, Costant.PlayerRole role, String usernamePlayer) {
        this.gameId = gameId;
        this.role = role;
        this.usernamePlayer = usernamePlayer;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public Costant.PlayerRole getRole() {
        return role;
    }

    public void setRole(Costant.PlayerRole role) {
        this.role = role;
    }
}
