package asafov.naum.skychat.functions;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import asafov.naum.skychat.User;
import asafov.naum.skychat.chat.Chat;

/**
 * Created by Naum Asafov on 01/04/2018.
 * Static methods for Application
 */

public final class Functions {
    public static final String MYTAG = "MYTAG";

    /**
     * Static method for creating folder get string path of folder
     * Create folder in path
     */
    public static void createFolder(String path){
        File child = new File(path);//Создание папки
        child.mkdirs();
    }

    /**
     * Create Bitmap in folder
     */
    public static void createBitmapInFolder(File file,Bitmap bitmap){
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);//ставим компресс изобр в поток
            out.flush();//flush записывает
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Check if user send request or received
    public static boolean checkIfUserRequest(List<User> myRequest,User userSearchRes){
        for (User user : myRequest){
            if (user.getNickname().equals(userSearchRes.getNickname())){
                return true;
            }
        }
        return false;
    }
    //Get user by email or nickname
    public static User getUserByEmailOrNick(String str,List<User> usersList){
        for (User user : usersList){
            if (user.getEmail().equals(str) || user.getNickname().equals(str)){
                return user;
            }
        }
        return null;
    }

    //Set profile picture in imageview
    public static void setProfilePuctureInImgView(String path, ImageView profileImage) {
        File imageFile;
        imageFile = new File(path);
        String imagePath = imageFile.getPath();
        // Получить bitmap из файла.
        Bitmap image = BitmapFactory.decodeFile(imagePath);
        // Показать изображение пользователю.
        profileImage.setImageBitmap(image);
    }
    //Get profile picture in imageview
    public static Bitmap getProfilePuctureInImgView(String path) {
        File imageFile;
        imageFile = new File(path);
        String imagePath = imageFile.getPath();
        // Получить bitmap из файла.
        Bitmap image = BitmapFactory.decodeFile(imagePath);
        // Показать изображение пользователю.
        return image;
    }
    //Create chat when user add friend
    public static Chat createChat(User curUser,User userTwo){
        //Create Chat
        String chatId = curUser.getNickname() + userTwo.getNickname();
        List<User> chatUsers = new ArrayList<>();
        chatUsers.add(curUser);
        chatUsers.add(userTwo);
        Chat chat = new Chat(chatUsers,chatId);
        return chat;
    }
}
