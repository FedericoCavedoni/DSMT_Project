package it.unipi.dsmt.DAO;


// view Friends
// remove Friend
// insert friend

import it.unipi.dsmt.DTO.FriendDTO;
import it.unipi.dsmt.DTO.PageDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FriendDAO extends BaseDAO{

    public PageDTO<FriendDTO> viewFriends(String username, int page){
        PageDTO<FriendDTO> pageDTO = new PageDTO<>();
        List<FriendDTO> entries = new ArrayList<>();
        String browseMatchSQL = "select case when username1 = ? then username2 else username1 end as other_username" +
                " from intesa_vincente.friends where username1 = ? OR username2 = ? ";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(browseMatchSQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, username);
            preparedStatement.setString(3, username);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                FriendDTO friendDTO = new FriendDTO();
                friendDTO.setUsername1(resultSet.getString(1));
                friendDTO.setLogged(friendDTO.getUsername1());
                entries.add(friendDTO);
            }

            pageDTO.setEntries(entries);
            pageDTO.setCounter(entries.size());
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }

        return pageDTO;
    }

    public boolean addFriend(String username1, String username2) {
        String insertQuery = "INSERT INTO intesa_vincente.friends " +
                "(Username1, Username2) " +
                "VALUES (?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, username1);
            preparedStatement.setString(2, username2);

            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean removeFriend(String username1, String username2) {
        String removeQuery = "DELETE FROM intesa_vincente.friends " +
                "WHERE (Username1 = ? AND Username2 = ?) OR (Username1 = ? AND Username2 = ?)";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(removeQuery)) {
            preparedStatement.setString(1, username1);
            preparedStatement.setString(2, username2);
            preparedStatement.setString(3, username2);
            preparedStatement.setString(4, username1);

            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}