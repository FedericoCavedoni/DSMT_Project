package it.unipi.dsmt.DTO;

import it.unipi.dsmt.util.Costant;

public class InviteDTO {
    private String gameId;
    private String player1;
    private String player2;
    private String userInvite;
    private Costant.PlayerRole role1;
    private Costant.PlayerRole role2;
    private Costant.PlayerRole role3;

    public InviteDTO(){}

    public InviteDTO(String gameId, String player1, String player2, String userInvite, Costant.PlayerRole role1, Costant.PlayerRole role2, Costant.PlayerRole role3) {
        this.gameId = gameId;
        this.player1 = player1;
        this.player2 = player2;
        this.userInvite = userInvite;

        this.role1 = role1;
        this.role2 = role2;
    }

    public String getGameId() {
        return gameId;
    }
    public String getPlayer1() {
        return player1;
    }
    public String getPlayer2() {
        return player2;
    }
    public String getUserInvite() {
        return userInvite;
    }
    public Costant.PlayerRole getRole1() {
        return role1;
    }
    public Costant.PlayerRole getRole2() {
        return role2;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
}