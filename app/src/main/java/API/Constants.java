package API;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Constants {
    public static final String LOG_TAG = "ResiApp" ;
    public static final String URL = "http://10.0.2.2:5069/api/v1/";
    public static final String AUTH_LOGIN_ENDPOINT = "Auth/login";
    public static final String USERS_ENDPOINT = "Users";
    public static final String FORGOT_PASSWORD_ENDPOINT = "Auth/FORGOT-PASSWORD";
    public static final String FIND_BY_USER_ID_ENDPOINT = USERS_ENDPOINT + "/findUserById/";
    public static final String SPACES_ENDPOINT = "Spaces";
    public static final String SPACE_RULES_ENDPOINT = "SpaceRules";
    public static final String REVIEWS_ENDPOINT = "Reviews";
    public static final String RESERVATIONS_ENDPOINT = "Reservations";
    public static final String FIND_RESERVATIONS_BY_AVAILABLE_SLOTS = RESERVATIONS_ENDPOINT + "/findReservationsByAvailableSlots?spaceId=";
    public static final String SLOT_MINUTES_SLOTS = "&slotMinutes=60";
    public static final String DATE_SLOTS = "&date=";
    public static final String ADMIN_EMAIL = "Admin@Resiapp.es";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE dd/MM/yyyy HH:mm", Locale.ENGLISH);
    public static final String DATE_FORMAT_LONG = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm";
    public static final String DATE_INVERTED_FORMAT = "yyyy-MM-dd";
    public static final String DATE_FORMAT_SHORTEST = "hh:mm a";
}