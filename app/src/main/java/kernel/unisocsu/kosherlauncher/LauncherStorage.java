package kernel.unisocsu.kosherlauncher;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class LauncherStorage {

    private static final String PREFS =
            "kosher_launcher";

    private static final String ORDER =
            "home_order";

    private final SharedPreferences preferences;

    public LauncherStorage(Context context) {

        preferences =
                context.getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                );
    }

    public void saveOrder(
            List<HomeItem> items) {

        StringBuilder value =
                new StringBuilder();

        for (HomeItem item : items) {

            if (item.isApp()) {

                value.append("APP:");
                value.append(
                        item.getAppInfo()
                                .getPackageName()
                );

            } else {

                /*
                 * כרגע נשמור את התיקיות בשלב הבא.
                 */
                value.append("EMPTY");
            }

            value.append("|");
        }

        preferences.edit()
                .putString(
                        ORDER,
                        value.toString()
                )
                .apply();
    }

    public List<String> loadOrder() {

        String value =
                preferences.getString(
                        ORDER,
                        ""
                );

        List<String> result =
                new ArrayList<String>();

        if (value.length() == 0) {
            return result;
        }

        String[] entries =
                value.split("\\|");

        for (String entry : entries) {

            if (entry.startsWith("APP:")) {

                result.add(
                        entry.substring(4)
                );
            }
        }

        return result;
    }
}