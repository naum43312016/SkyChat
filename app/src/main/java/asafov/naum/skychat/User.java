package asafov.naum.skychat;

import java.io.Serializable;

/**
 * Created by Naum Asafov on 11/03/2018.
 */

public class User implements Serializable{
    private String userId;
    private String email;
    private String nickname;
    private String profileImg;

    public User(){}

    public User(String userId,String email){
        this.userId = userId;
        this.email = email;
    }
    public User(String userId,String email,String nickname){
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getProfileImg() {
        return profileImg;
    }
}
