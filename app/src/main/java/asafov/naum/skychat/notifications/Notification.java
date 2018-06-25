package asafov.naum.skychat.notifications;

import java.util.List;

import asafov.naum.skychat.User;

/**
 * Created by user on 02/06/2018.
 * Notification class have id and messageId of notification
 */

public class Notification {
    private int notificationId;
    private String messageId;
    private User requestedUser;

    public Notification(int notificationId,String messageId){
        this.notificationId = notificationId;
        this.messageId = messageId;
    }
    public Notification(int notificationId, User requestedUser){
        this.notificationId = notificationId;
        this.requestedUser = requestedUser;
    }


    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public User getRequestedUser() {
        return requestedUser;
    }

    public void setRequestedUser(User requestedUser) {
        this.requestedUser = requestedUser;
    }
}
