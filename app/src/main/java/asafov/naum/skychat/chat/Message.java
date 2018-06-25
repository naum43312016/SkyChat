package asafov.naum.skychat.chat;

import android.util.Log;

import java.io.Serializable;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;

import asafov.naum.skychat.User;

/**
 * Created by user on 08/04/2018.
 */

public class Message implements Serializable{
    private User senderUser;
    private User destinationUser;
    private Map<String,String> messageTime;
    private String tag;
    private String message;
    private String fileName;
    private String fileStoragePath;
    private String fileOnUserStoragePath;
    private int randomId;
    private long fileSize;
    private boolean read;
    private boolean downloaded;
    private String secretKey;
    private String messageId;

    public Message(){}
    public Message(String message){
        this.message = message;
    }
    public Message(User senderUser,User destinationUser,Map<String,String> messageTime){
        this.senderUser = senderUser;
        this.destinationUser = destinationUser;
        this.message = null;
        this.messageId = UUID.randomUUID().toString();
        this.randomId = new Random().nextInt(100);
    }

    public Message(User senderUser,User destinationUser,String message,Map<String,String> messageTime,String tag){
        this.senderUser = senderUser;
        this.destinationUser = destinationUser;
        this.message = message;
        this.messageTime = messageTime;
        this.tag = tag;
        this.messageId = UUID.randomUUID().toString();
        this.randomId = new Random().nextInt(100);
    }


    public Message(User senderUser,User destinationUser,String message,String fileStoragePath,long fileSize,Map<String,String> messageTime,String tag){
        this.senderUser = senderUser;
        this.destinationUser = destinationUser;
        this.message = message;
        this.fileStoragePath = fileStoragePath;
        this.fileSize = fileSize;
        this.messageTime = messageTime;
        this.tag = tag;
        this.messageId = UUID.randomUUID().toString();
        this.randomId = new Random().nextInt(100);
    }

    //Constructor for audio microfone file and image file
    public Message(User senderUser,User destinationUser,Map<String,String> messageTime,String tag,String fileName){
        this.senderUser = senderUser;
        this.destinationUser = destinationUser;
        this.tag = tag;
        this.fileName = fileName;
        this.message = "";
        this.messageId = UUID.randomUUID().toString();
        this.messageTime = messageTime;
        this.randomId = new Random().nextInt(100);
    }

    public Message(User senderUser,User destinationUser,String fileStoragePath,long fileSize,Map<String,String> messageTime){
        this.senderUser = senderUser;
        this.destinationUser = destinationUser;
        this.fileStoragePath = fileStoragePath;
        this.fileSize = fileSize;
        this.message = "";
        this.messageId = UUID.randomUUID().toString();
        this.randomId = new Random().nextInt(100);
    }

    public User getSenderUser() {
        return senderUser;
    }

    public User getDestinationUser() {
        return destinationUser;
    }


    public String getFileStoragePath() {
        return fileStoragePath;
    }

    public void setFileStoragePath(String fileStoragePath) {
        this.fileStoragePath = fileStoragePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }


    public String getMessage() {
        return message;
    }

    public String getFileOnUserStoragePath() {
        return fileOnUserStoragePath;
    }

    public void setFileOnUserStoragePath(String fileOnUserStoragePath) {
        this.fileOnUserStoragePath = fileOnUserStoragePath;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public boolean getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public void setMessageTime(Map<String,String> messageTime){this.messageTime = messageTime;}
    public Map<String,String> getMessageTime(){return this.messageTime;}

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public int getRandomId() {
        return randomId;
    }

    public void setRandomId(int randomId) {
        this.randomId = randomId;
    }
}