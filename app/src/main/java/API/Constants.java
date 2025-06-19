package API;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Constants {
    public static final String LOG_TAG = "ResiApp" ;
    public static final String URL = "http://10.0.2.2:5069/api/v1/";
    public static final String AUTH_LOGIN = "Auth/login";
    public static final String USERS_ENDPOINT = "Users";
    public static final String FIND_BY_USER_ID_ENDPOINT = USERS_ENDPOINT + "/findUserById/";
    public static final String SPACES_ENDPOINT = "Spaces";
    public static final String SPACE_RULES_ENDPOINT = "SpaceRules";
    public static final String REVIEWS_ENDPOINT = "Reviews";
    public static final String RESERVATIONS_ENDPOINT = "Reservations";
    public static final String ADMIN_EMAIL = "Admin@Resiapp.es";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE dd/MM/yyyy HH:mm", Locale.ENGLISH);
}