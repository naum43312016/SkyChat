package asafov.naum.skychat.chat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import asafov.naum.skychat.User;

/**
 * Created by user on 09/04/2018.
 */

public class Chat implements Serializable{

    private List<Message> messages;
    private List<User> users;
    private String secretKey;
    private String chatId;

    public Chat(){}
    public Chat(List<Message> messages, List<User> users) {
        this.messages = messages;
        this.users = users;
    }

    public Chat(List<User> users,String chatId) {
        this.users = users;
        this.chatId = chatId;
        messages = new ArrayList<>();
    }
    public Chat(List<User> users) {
        this.users = users;
        this.messages = new ArrayList<>();
    }

    public Chat(List<Message> messages, List<User> users, String secretKey) {
        this.messages = messages;
        this.users = users;
        this.secretKey = secretKey;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getChatId() {
        return chatId;
    }
}
