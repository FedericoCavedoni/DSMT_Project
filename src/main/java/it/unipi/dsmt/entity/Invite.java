package it.unipi.dsmt.entity;

import it.unipi.dsmt.DTO.InviteDTO;
import it.unipi.dsmt.util.Costant;

public class Invite {
    String id;
    String player1;
    String player2;
    String userInvite;
    Costant.PlayerRole role1;
    Costant.PlayerRole role2;

    public Invite(InviteDTO invite){
        this.id = invite.getGameId();
        this.userInvite = invite.getUserInvite();
        this.player1 = invite.getPlayer1();
        this.player2 = invite.getPlayer2();
        this.role1 = invite.getRole1();
        this.role2 = invite.getRole2();
    }

    public String getId(){
        return id;
    }

    public String getPlayer1() {return player1;}
    public String getPlayer2() {return player2;}
    public String getUserInvite() {return userInvite;}
    public Costant.PlayerRole getRole1() { return role1;}
    public Costant.PlayerRole getRole2() { return role2;}

    public void setPlayer1(String username){
        player1 = username;
    }
    public void setPlayer2(String username){
        player2 = username;
    }

};