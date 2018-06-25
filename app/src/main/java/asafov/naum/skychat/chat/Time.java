package asafov.naum.skychat.chat;

import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 * Created by user on 30/04/2018.
 */

public abstract class Time implements Serializable{
    public static final SimpleDateFormat FORMAT_YEAR = new SimpleDateFormat("yyyy");
    public static final SimpleDateFormat FORMAT_MONTH = new SimpleDateFormat("MM");
    public static final SimpleDateFormat FORMAT_DAY = new SimpleDateFormat("dd");
    public static final SimpleDateFormat FORMAT_HOUR = new SimpleDateFormat("kk");
    public static final SimpleDateFormat FORMAT_MINUTE = new SimpleDateFormat("mm");
    public static final SimpleDateFormat FORMAT_SECOND = new SimpleDateFormat("ss");
}
