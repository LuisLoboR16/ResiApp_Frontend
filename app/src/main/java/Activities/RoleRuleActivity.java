package Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

public abstract class RoleRuleActivity extends AppCompatActivity {

    protected String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadUserRoleFromPreferences();
    }

    private void loadUserRoleFromPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        userRole = prefs.getString("user_role", "Guess");
    }

    protected boolean isAdmin() {
        return "Admin".equalsIgnoreCase(userRole);
    }

    protected boolean isResident() {
        return "Resident".equalsIgnoreCase(userRole);
    }

    protected boolean hasRole(String... roles) {
        for (String r : roles) {
            if (r.equalsIgnoreCase(userRole)) return true;
        }
        return false;
    }

    protected void roleRules(View rootView) {
        if (rootView == null) return;

        if (rootView.getTag() != null) {
            String tag = rootView.getTag().toString();
            if (tag.equalsIgnoreCase("adminOnly") && !isAdmin()) {
                rootView.setVisibility(View.GONE);
            } else if (tag.equalsIgnoreCase("residentOnly") && !isResident()) {
                rootView.setVisibility(View.GONE);
            }
        }

        if (rootView instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) rootView;
            for (int i = 0; i < group.getChildCount(); i++) {
                roleRules(group.getChildAt(i));
            }
        }
    }
}
