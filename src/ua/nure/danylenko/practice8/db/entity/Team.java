package ua.nure.danylenko.practice8.db.entity;

import java.util.Objects;

public class Team {
    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {

        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()){
            return false;
        }
        Team team=(Team)obj;
        return Objects.equals(this.name, team.name);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public static Team createTeam(String name){
        Team team= new Team();
        team.setId(0);
        team.setName(name);
        return team;
    }
}
