package it.unipi.dsmt.DTO;

import java.sql.Timestamp;

public class MatchDTO {
    private int id;
    private String user1;
    private String user2;
    private String user3;
    private int score;
    private Timestamp timestamp;

    public MatchDTO(){}

    public MatchDTO(int id, String user1, String user2, String user3, int score, Timestamp timestamp) {
        this.id = id;
        this.user1 = user1;
        this.user2 = user2;
        this.user3 = user3;
        this.score = score;
        this.timestamp = timestamp;
    }

    public MatchDTO(String user1, String user2, String user3, int score, Timestamp timestamp) {
        this.user1 = user1;
        this.user2 = user2;
        this.user3 = user3;
        this.score = score;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUser1() {
        return user1;
    }

    public void setUser1(String user1) {
        this.user1 = user1;
    }

    public String getUser2() {
        return user2;
    }

    public void setUser2(String user2) {
        this.user2 = user2;
    }

    public String getUser3() {
        return user3;
    }

    public void setUser3(String user3) {
        this.user3 = user3;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
