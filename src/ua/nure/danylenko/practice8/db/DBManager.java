package ua.nure.danylenko.practice8.db;

import ua.nure.danylenko.practice8.db.entity.Team;
import ua.nure.danylenko.practice8.db.entity.User;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DBManager {


    private static final Logger LOGGER = Logger.getLogger(DBManager.class.getName());
    //
    private static DBManager instance;
    public static synchronized DBManager getInstance() {
        if (instance == null) {
            instance = new DBManager();
        }
        return instance;
    }
    private DBManager() {    }
    //

    private static final String URL;
    static{
        Properties prop = new Properties();
        String temp="";
        try (InputStream input = new FileInputStream("app.properties")) {
            prop.load(input);
            temp=prop.getProperty("connection.url");
            LOGGER.fine("app.properties loaded");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed loading app.properties: ", e);


        }
            URL=temp;
    }
    private static final String SQL_FIND_ALL_USERS = "SELECT * FROM users";
    private static final String SQL_FIND_ALL_TEAMS = "SELECT * FROM teams";
    private static final String SQL_INSERT_USER = "INSERT INTO users VALUES (DEFAULT, ?)";
    private static final String SQL_INSERT_TEAM = "INSERT INTO teams VALUES (DEFAULT, ?)";
    private static final String SQL_GET_USER = "SELECT id, login FROM users WHERE login=?";
    private static final String SQL_GET_TEAM = "SELECT id, name FROM teams WHERE name=?";
    private static final String SQL_INSERT_TEAM_FOR_USER = "INSERT INTO users_teams VALUES (?, ?)";
    private static final String SQL_GET_USER_TEAMS = "SELECT * FROM teams WHERE teams.id " +
            "= ANY (SELECT team_id FROM users_teams WHERE user_id=?)";
    private static final String SQL_DELETE_TEAM = "DELETE FROM teams WHERE name=?";
    private static final String SQL_UPDATE_TEAM = "UPDATE teams SET name=? WHERE id=?";

    public List<User> findAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();

        try(Connection con = DriverManager.getConnection(URL);
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(SQL_FIND_ALL_USERS)) {
            while (rs.next()) {
                users.add(extractUser(rs));
            }
        }
        users.sort(Comparator.comparingInt(User::getId));
        return users;
    }

    public List<Team> findAllTeams() throws SQLException {
        List<Team> teams = new ArrayList<>();

        try(Connection con = DriverManager.getConnection(URL);
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(SQL_FIND_ALL_TEAMS)) {
            while (rs.next()) {
                teams.add(extractTeam(rs));
            }
        }
        return teams;
    }

    // id = 0
    public void insertUser(User user) throws DBException {

        ResultSet rs = null;
        try(Connection connection = DriverManager.getConnection(URL);
            PreparedStatement pstmt =
                    connection.prepareStatement(SQL_INSERT_USER,
                            Statement.RETURN_GENERATED_KEYS)){
            int k = 1;
            pstmt.setString(k, user.getLogin());

            if (pstmt.executeUpdate() > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    user.setId(id);
                }
            }
        }catch (SQLException e){
            LOGGER.log(Level.SEVERE, "DB insertUser() problem", e);
            throw new DBException("DB insertUser() problem",e);
        } finally {
            if(rs!=null){
                try {
                    rs.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "DB insertUser() -> rs.close() problem", e);
                }
            }
        }
    }

    public void insertTeam(Team team) throws DBException {
        ResultSet rs = null;
        try(Connection connection = DriverManager.getConnection(URL);
            PreparedStatement pstmt =
                    connection.prepareStatement(SQL_INSERT_TEAM,
                            Statement.RETURN_GENERATED_KEYS)){
            int k = 1;
            pstmt.setString(k, team.getName());
            if (pstmt.executeUpdate() > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    team.setId(id);
                }
            }
        }catch (SQLException e){
            LOGGER.log(Level.SEVERE, "DB insertTeam() problem ", e);
            throw new DBException("DB insertTeam() problem",e);

        } finally {
            if(rs!=null){
                try {
                    rs.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "DB insertTeam() -> rs.close() problem ", e);
                }
            }
        }
    }

    public User getUser(String login) throws SQLException {
        User user = null;
        ResultSet rs =null;
        try(Connection con = DriverManager.getConnection(URL);
            PreparedStatement pstmt =
                    con.prepareStatement(SQL_GET_USER)) {

            pstmt.setString(1, login);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                user = extractUser(rs);
            }
        }catch (SQLException e){
            LOGGER.log(Level.SEVERE, "getUser() SQLException ", e);
        } finally {
            if(rs!=null){
                rs.close();
            }
        }
        return user;
    }

    public Team getTeam(String name) throws SQLException {
        Team team = null;
        ResultSet rs =null;
        try(Connection con = DriverManager.getConnection(URL);
            PreparedStatement pstmt =
                    con.prepareStatement(SQL_GET_TEAM)) {

            pstmt.setString(1, name);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                team = extractTeam(rs);
            }
        }catch (SQLException e){
            LOGGER.log(Level.SEVERE, "getTeam() SQLException ", e);
        }finally {
            if(rs!=null){
                rs.close();
            }
        }
        return team;
    }

    public boolean setTeamsForUser(User user, Team ... teams) throws SQLException {

        Connection con=null;
        try{
            con=DriverManager.getConnection(URL);
            con.setAutoCommit(false);
            con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            for (Team t:teams) {
                setTeamForUserCheck(con,user,t);
            }
            con.commit();
        } catch (SQLException ex) {
            if(con!=null) {
                LOGGER.log(Level.SEVERE, "Cannot add user in two teams in method setTeamsForUser(): ", ex);
                con.rollback();
            }
        }finally {
            if(con!=null){
                con.close();
            }
        }
        return true;
    }

    private static boolean setTeamForUserCheck(Connection con, User user, Team team) throws SQLException {
        try{
            setTeamForUser(con,user,team);
        }catch (DBException ex){
            LOGGER.log(Level.SEVERE, "Failed commit in method setTeamForUserCheck: ", ex);
            con.rollback();
            con.close();
            return false;
        }
        return true;
    }

    private static void setTeamForUser(Connection con, User user, Team team) throws DBException{

        try(PreparedStatement pstmt =
                    con.prepareStatement(SQL_INSERT_TEAM_FOR_USER)){
            int k = 1;
            pstmt.setInt(k++, user.getId());
            pstmt.setInt(k, team.getId());
            pstmt.executeUpdate();

        }catch (SQLException e){
            LOGGER.log(Level.SEVERE, "Failed in method setTeamForUser(): ", e);
            throw new DBException("DB setTeamForUser() problem",e);

        }
    }

    public List<Team> getUserTeams(User user) throws DBException {
        List<Team> teams= new ArrayList<>();
        ResultSet rs=null;
        try(Connection con= DriverManager.getConnection(URL);
            PreparedStatement pstmt =con.prepareStatement(SQL_GET_USER_TEAMS)) {

                pstmt.setInt(1, user.getId());
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    teams.add(extractTeam(rs));
                }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed in method getUserTeams(): ", ex);
            throw new DBException("DB getUserTeams() problem", ex);
        }finally {
            if(rs!=null){
                try {
                    rs.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Failed in method getUserTeams() -> rs.close(): ", e);
                }
            }
        }
        return teams;

    }

    private static User extractUser(ResultSet rs) throws SQLException {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setLogin(rs.getString("login"));
            return user;
        }

    private static Team extractTeam(ResultSet rs) throws SQLException {
            Team team = new Team();
            team.setId(rs.getInt("id"));
            team.setName(rs.getString("name"));
            return team;
        }

    public boolean deleteTeam(Team team) throws SQLException {
        boolean result;

        try(Connection con = DriverManager.getConnection(URL);
        PreparedStatement pstmt =
                con.prepareStatement(SQL_DELETE_TEAM)) {
            int k = 1;
            pstmt.setString(k, team.getName());
            result = pstmt.executeUpdate() > 0;
        }
        return result;
    }

    public boolean updateTeam(Team team) throws SQLException {
        boolean result;

        try(Connection con = DriverManager.getConnection(URL);
            PreparedStatement pstmt =
                    con.prepareStatement(SQL_UPDATE_TEAM)) {
            int k = 1;
            pstmt.setString(k++, team.getName());
            pstmt.setInt(k, team.getId());

            result = pstmt.executeUpdate() > 0;
        }
        return result;
    }
}
