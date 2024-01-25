package it.unipi.dsmt.DAO;

// browse matches
// insert match

import it.unipi.dsmt.DTO.MatchDTO;
import it.unipi.dsmt.DTO.PageDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MatchDAO extends  BaseDAO{

    public PageDTO<MatchDTO> browseGames(String username){
        PageDTO<MatchDTO> pageDTO = new PageDTO<>();
        List<MatchDTO> entries = new ArrayList<>();
        String browseMatchSQL = "select * from intesa_vincente.match where user1 = ? OR user2 = ? OR user3 = ?";
        String browseMatchAdmin = "select * from intesa_vincente.match ";

        if(username == null){
            try (Connection connection = getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(browseMatchAdmin, PreparedStatement.RETURN_GENERATED_KEYS)) {

                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()){
                    MatchDTO matchDTO = new MatchDTO();
                    matchDTO.setId(resultSet.getInt(1));
                    matchDTO.setUser1(resultSet.getString(2));
                    matchDTO.setUser2(resultSet.getString(3));
                    matchDTO.setUser3(resultSet.getString(4));
                    matchDTO.setScore(resultSet.getInt(5));
                    matchDTO.setTimestamp(resultSet.getTimestamp(6));
                    entries.add(matchDTO);
                }

                pageDTO.setEntries(entries);
                pageDTO.setCounter(entries.size());
            } catch (SQLException e){
                e.printStackTrace();
                return null;
            }
        }
        else{
            try (Connection connection = getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(browseMatchSQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, username);
                preparedStatement.setString(3, username);

                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()){
                    MatchDTO matchDTO = new MatchDTO();
                    matchDTO.setId(resultSet.getInt(1));
                    matchDTO.setUser1(resultSet.getString(2));
                    matchDTO.setUser2(resultSet.getString(3));
                    matchDTO.setUser3(resultSet.getString(4));
                    matchDTO.setScore(resultSet.getInt(5));
                    matchDTO.setTimestamp(resultSet.getTimestamp(6));
                    entries.add(matchDTO);
                }

                pageDTO.setEntries(entries);
                pageDTO.setCounter(entries.size());
            } catch (SQLException e){
                e.printStackTrace();
                return null;
            }
        }

        return pageDTO;
    }

    public boolean insert(MatchDTO match) {
        String insertQuery = "INSERT INTO intesa_vincente.match" +
                "(User1, User2, User3, Score, Timestamp) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, match.getUser1());
            preparedStatement.setString(2, match.getUser2());
            preparedStatement.setString(3, match.getUser3());
            preparedStatement.setInt(4, match.getScore());
            preparedStatement.setTimestamp(5, match.getTimestamp());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                // Recupera l'ID generato automaticamente
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        match.setId(generatedId);
                    }
                }
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

}
