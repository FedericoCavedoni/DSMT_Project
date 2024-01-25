package it.unipi.dsmt.util;
import org.springframework.stereotype.Component;
import java.util.ArrayList;

@Component
public class SessionManagement {

    private static SessionManagement session = null;

    private ArrayList<String> userLogged = new ArrayList<>();

    private SessionManagement() {}

    public static SessionManagement getInstance() {
        if(session == null) {
            session = new SessionManagement();
        }
        return session;
    }

    public void setLogUser(String username) {
        if(session == null) {
            throw new RuntimeException("Session is not active.");
        } else {

            userLogged.add(username);
        }
    }

    public boolean isUserLogged(String Username) {
        if(session == null) {
            throw new RuntimeException("Session is not active.");
        } else {
            return userLogged.contains(Username);
        }
    }

    public boolean logoutUser(String Username){
        if(isUserLogged(Username)){
            userLogged.remove(Username);
            return true;
        }
        return false;
    }


}
