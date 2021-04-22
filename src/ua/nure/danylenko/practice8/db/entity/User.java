package ua.nure.danylenko.practice8.db.entity;

import java.util.Objects;

public class User {
    private int id;

    private String login;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Override
    public String toString() {
        return login;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()){
            return false;
        }
        User user=(User)obj;
        return Objects.equals(this.login, user.login);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public static User createUser(String login){
        User user= new User();
        user.setId(0);
        user.setLogin(login);
        return user;
    }
}
