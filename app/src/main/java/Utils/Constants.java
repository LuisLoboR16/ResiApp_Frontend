package Utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Constants {
    public static final String LOG_TAG = "ResiApp" ;
    public static final String URL = "http://10.0.2.2:5069/api/v1/";
    public static final String AUTH_LOGIN_ENDPOINT = "Auth/login";
    public static final String AUTH_LOGOUT_ENDPOINT = "Auth/logout";
    public static final String AUTH_VALIDATE_TOKEN_ENDPOINT = "Auth/validate-token";
    public static final String USERS_ENDPOINT = "Users";
    public static final String FORGOT_PASSWORD_ENDPOINT = "Auth/FORGOT-PASSWORD";
    public static final String FIND_BY_USER_ID_ENDPOINT = USERS_ENDPOINT + "/findUserById/";
    public static final String SPACES_ENDPOINT = "Spaces";
    public static final String SPACE_RULES_ENDPOINT = "SpaceRules";
    public static final String REVIEWS_ENDPOINT = "Reviews";
    public static final String RESERVATIONS_ENDPOINT = "Reservations";
    public static final String EMAIL_ENDPOINT = "Email";
    public static final String FIND_RESERVATIONS_BY_ID = RESERVATIONS_ENDPOINT + "/findReservationsByUser/";
    public static final String FIND_RESERVATIONS_BY_AVAILABLE_SLOTS = RESERVATIONS_ENDPOINT + "/findReservationsByAvailableSlots?spaceId=";
    public static final String SEND_EMAIL = EMAIL_ENDPOINT + "/send-email";
    public static final String SLOT_MINUTES_SLOTS = "&slotMinutes=60";
    public static final String DATE_SLOTS = "&date=";
    public static final String ADMIN_EMAIL = "notificationsresiapp@gmail.com";
    public static final String DATE_FORMAT_LONG = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm";
    public static final String DATE_INVERTED_FORMAT = "yyyy-MM-dd";
    public static final String DATE_FORMAT_SHORTEST = "hh:mm a";
    public static final SimpleDateFormat DATE_FORMAT_CUSTOM =  new SimpleDateFormat("dd MMMM yy", Locale.ENGLISH);
    public static final SimpleDateFormat DATE_FORMAT_HOURS_CUSTOM =  new SimpleDateFormat("HH:mm", Locale.ENGLISH);
    public static final String REGEX_EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    public static final String SUBJECT_RESERVATIONS = "Your reservation for spaceName has been confirmed – ResiApp";
    public static final String BODY_RESERVATIONS = "<html> <body style='font-family: Arial, sans-serif; line-height: 1.6;'> <p>Hello <strong> currentUser.getResidentName()</strong>,</p> <p>Your reservation has been <strong>successfully confirmed</strong>.</p> <p> <strong>Space:</strong>  spaceName <br> <strong>Start time:</strong> startTime <br> <strong>End time:</strong> endTime <br> <br> Thanks you for using our app.<br> <strong>ResiApp team.</strong></p> </body> </html>";
}