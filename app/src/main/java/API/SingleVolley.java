package API;

import android.content.Context;

import androidx.annotation.NonNull;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class SingleVolley {
    private static SingleVolley volleyInstance = null;
    private final RequestQueue requestQueues;

    private SingleVolley(Context context) {
        requestQueues = Volley.newRequestQueue(context);
    }

    public static SingleVolley getInstance(Context context) {
        if (volleyInstance == null) {
            volleyInstance = new SingleVolley(context);
        }
        return volleyInstance;
    }

    public RequestQueue getRequestQueue() {
        return requestQueues;
    }

    @NonNull
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}