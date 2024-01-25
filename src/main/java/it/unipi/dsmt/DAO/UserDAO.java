package it.unipi.dsmt.DAO;

import it.unipi.dsmt.DTO.UserDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import it.unipi.dsmt.DTO.PageDTO;
import org.springframework.web.bind.annotation.RequestBody;

public class UserDAO extends BaseDAO {

    public int signup(UserDTO user) {
        String checkExistingUserSQL = "SELECT * FROM intesa_vincente.user WHERE Username = ?";
        String registerUserSQL = "INSERT INTO intesa_vincente.user" +
                "(Username, Name, Surname, Password)" +
                "VALUES" +
                "(?,?,?,?);";

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);

            // Check if the username already exists
            try (PreparedStatement checkStatement = connection.prepareStatement(checkExistingUserSQL)) {
                checkStatement.setString(1, user.getUsername());

                try (ResultSet resultSet = checkStatement.executeQuery()) {
                    if (resultSet.next()) {
                        // Username already exists, return an error
                        return 0;
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                return -1;
            }


            // Insert the new user if the username doesn't exist
            try (PreparedStatement preparedStatement = connection.prepareStatement(registerUserSQL)) {
                preparedStatement.setString(1, user.getUsername());
                preparedStatement.setString(2, user.getFirstName());
                preparedStatement.setString(3, user.getLastName());
                preparedStatement.setString(4, user.getPassword());


                if (preparedStatement.executeUpdate() == 0) {
                    connection.rollback();
                    return -1;
                }

                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                ex.printStackTrace();
                return -1;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return -1;
        }

        return 1;
    }


    public UserDTO login(String username, String password) {
        String loginQuery = "SELECT * FROM intesa_vincente.user WHERE username = ? AND password = ?";

        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(loginQuery)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    UserDTO userDTO = new UserDTO();
                    userDTO.setUsername(resultSet.getString("username"));
                    userDTO.setFirstName(resultSet.getString("name"));
                    userDTO.setLastName(resultSet.getString("surname"));
                    return userDTO;
                } else {
                    return null;
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public boolean removeUser(String username) {
        String removeQuery = "DELETE FROM intesa_vincente.user WHERE username = ?";

        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(removeQuery)) {

            preparedStatement.setString(1, username);

            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public UserDTO globalSearch(String username) {
        String searchQuery = "SELECT * FROM intesa_vincente.user WHERE username = ?";

        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(searchQuery)) {

            preparedStatement.setString(1, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    UserDTO userDTO = new UserDTO();
                    userDTO.setUsername(resultSet.getString("username"));
                    userDTO.setFirstName(resultSet.getString("name"));
                    userDTO.setLastName(resultSet.getString("surname"));
                    return userDTO;
                } else {
                    return null;
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public PageDTO<UserDTO> viewUsers(int page) {
        PageDTO<UserDTO> pageDTO = new PageDTO<>();
        List<UserDTO> entries = new ArrayList<>();
        String browseUsersSQL = "SELECT Username, Name, Surname FROM intesa_vincente.user";
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(browseUsersSQL)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                UserDTO userDTO = new UserDTO();
                userDTO.setUsername(resultSet.getString("Username"));
                userDTO.setFirstName(resultSet.getString("Name"));
                userDTO.setLastName(resultSet.getString("Surname"));
                entries.add(userDTO);
            }

            pageDTO.setEntries(entries);
            pageDTO.setCounter(entries.size());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return pageDTO;
    }

    public boolean removeUserFromFriends(@RequestBody String username) {
        String removeFriendsQuery = "DELETE FROM intesa_vincente.friends WHERE username1 = ? OR username2 = ?";

        try (Connection connection = getConnection(); PreparedStatement removeFriendsStatement = connection.prepareStatement(removeFriendsQuery)) {

            removeFriendsStatement.setString(1, username);
            removeFriendsStatement.setString(2, username);
            removeFriendsStatement.executeUpdate();

            removeFriendsStatement.executeUpdate();
            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }


}

