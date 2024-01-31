package it.unipi.dsmt.controller;

import it.unipi.dsmt.DTO.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface UserControllerInterface {
    ResponseEntity<String> signUp(UserDTO UserSignUp);
    ResponseEntity<String> viewFriends(ViewFriendsRequestDTO request);
    ResponseEntity<String> removeFriend(FriendRequestDTO request);
    ResponseEntity<String> addFriend(FriendRequestDTO request);
    ResponseEntity<String> game(@RequestBody GameRequestDTO request);
    ResponseEntity<String> globalSearch(FriendSearchDTO request);
    ResponseEntity<String> inviteFriend(@RequestBody InviteDTO request);
    ResponseEntity<String> checkInvite(@RequestBody String username);
    ResponseEntity<String> declineInvitation(@RequestBody String username);
}