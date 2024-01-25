package it.unipi.dsmt.controller;


import it.unipi.dsmt.DTO.*;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface UserControllerInterface {
     ResponseEntity<String> signUp(UserDTO UserSignUp);
     ResponseEntity<String> viewFriends(ViewFriendsRequestDTO request);
     ResponseEntity<String> removeFriend(FriendRequestDTO request);
     ResponseEntity<String> addFriend(FriendRequestDTO request);
     ResponseEntity<String> game(@RequestBody GameRequestDTO request);
     ResponseEntity<String> globalSearch(FriendSearchDTO request);

    @Async
    @PostMapping("/inviteFriend")
    ResponseEntity<String> inviteFriend(@RequestBody InviteDTO request);

    @Async
    @PostMapping("/checkInvite")
    ResponseEntity<String> checkInvite(@RequestBody String username);

    @Async
    @PostMapping("/declineInvitation")
    ResponseEntity<String> declineInvitation(@RequestBody String username);
}

