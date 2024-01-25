package it.unipi.dsmt.DTO;

public class FriendSearchDTO {
    private String usernameToSearch;

    public FriendSearchDTO(){}

    public FriendSearchDTO(String usernameToSearch) {
        this.usernameToSearch = usernameToSearch;
    }

    public String getUsernameToSearch() {
        return usernameToSearch;
    }

    public void setUsernameToSearch(String usernameToSearch) {
        this.usernameToSearch = usernameToSearch;
    }
}
