package it.unipi.dsmt.DTO;

import it.unipi.dsmt.util.SessionManagement;

public class FriendDTO {
    private String username1;

    private boolean isLogged;

    private final SessionManagement session = SessionManagement.getInstance();

    public FriendDTO(){}

    public FriendDTO(String username1) {
        this.username1 = username1;
    }

    public String getUsername1() {
        return username1;
    }

    public void setUsername1(String username1) {
        this.username1 = username1;
    }

    public boolean getLogged(){
        return isLogged;
    }

    public void setLogged(String Username){
        this.isLogged = session.isUserLogged(Username);
    }
}
