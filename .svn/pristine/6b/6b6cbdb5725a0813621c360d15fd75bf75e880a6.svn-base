package ua.nure.danylenko.practice8;

import ua.nure.danylenko.practice8.db.entity.User;
import ua.nure.danylenko.practice8.db.entity.Team;
import ua.nure.danylenko.practice8.db.DBManager;
import ua.nure.danylenko.practice8.db.DBException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.LogManager;


public class Demo {

    private static void printList(List<?> list) {
        list.forEach(System.out::println);

    }

    public static void main(String[] args) throws SQLException, DBException{
        try {
            LogManager.getLogManager().readConfiguration(
                    Demo.class.getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            System.err.println("Could not setup logger configuration: " + e);
        }

        // users  ==> [ivanov]
        // teams  ==> [teamA]

        DBManager dbManager = DBManager.getInstance();

        // Part 1
        dbManager.insertUser(User.createUser("petrov"));
        dbManager.insertUser(User.createUser("obama"));
        printList(dbManager.findAllUsers());
        // users  ==> [ivanov, petrov, obama]

        System.out.println("===========================");

        // Part 2
        dbManager.insertTeam(Team.createTeam("teamB"));
        dbManager.insertTeam(Team.createTeam("teamC"));

        printList(dbManager.findAllTeams());
        // teams ==> [teamA, teamB, teamC]
        System.out.println("===========================");

        // Part 3
        User userPetrov = dbManager.getUser("petrov");
        User userIvanov = dbManager.getUser("ivanov");
        User userObama = dbManager.getUser("obama");

        Team teamA = dbManager.getTeam("teamA");
        Team teamB = dbManager.getTeam("teamB");
        Team teamC = dbManager.getTeam("teamC");
        //Team teamD = dbManager.getTeam("teamD")

        // method setTeamsForUser must implement transaction!
        dbManager.setTeamsForUser(userIvanov, teamA);
        dbManager.setTeamsForUser(userPetrov, teamA, teamB);
        dbManager.setTeamsForUser(userObama, teamA, teamB, teamC);
        //dbManager.setTeamsForUser(userObama, teamA, teamB, teamC, teamD)

        for (User user : dbManager.findAllUsers()) {
            printList(dbManager.getUserTeams(user));
            System.out.println("~~~~~");
        }
        // teamA
        // teamA teamB
        // teamA teamB teamC

        System.out.println("===========================");

        // Part 4

        // on delete cascade!
        System.out.println(dbManager.deleteTeam(teamA));

        // Part 5
        teamC.setName("teamX");
        System.out.println(dbManager.updateTeam(teamC));
        printList(dbManager.findAllTeams());
        // teams ==> [teamB, teamX]
    }
}