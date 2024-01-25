package it.unipi.dsmt.DTO;

public class FriendRequestDTO {
    private String username;
    private String usernameFriend;

    public FriendRequestDTO(String username, String usernameFriend) {
        this.username = username;
        this.usernameFriend = usernameFriend;
    }

    public String getUsername() {
        return username;
    }

    public String getUsernameFriend() {
        return usernameFriend;
    }
}
