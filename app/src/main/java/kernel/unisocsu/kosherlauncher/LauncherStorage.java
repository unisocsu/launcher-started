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

    public static class SavedItem {
        public enum Type {
            APP,
            FOLDER
        }
        public Type type;
        public String packageName; // used if type == APP
        public String folderName;  // used if type == FOLDER
        public List<String> folderApps; // used if type == FOLDER
    }

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

            } else if (item.isFolder()) {

                HomeFolder folder = item.getFolder();
                String folderName = folder.getName();
                
                // Escape delimiters in the folder name to avoid parsing bugs
                folderName = folderName.replace("|", " ").replace(":", " ");

                value.append("FOLDER:");
                value.append(folderName);
                value.append(":");

                List<AppInfo> folderApps = folder.getApps();
                for (int i = 0; i < folderApps.size(); i++) {
                    value.append(folderApps.get(i).getPackageName());
                    if (i < folderApps.size() - 1) {
                        value.append(",");
                    }
                }
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

    public List<SavedItem> loadOrder() {

        String value =
                preferences.getString(
                        ORDER,
                        ""
                );

        List<SavedItem> result =
                new ArrayList<SavedItem>();

        if (value.length() == 0) {
            return result;
        }

        String[] entries =
                value.split("\\|");

        for (String entry : entries) {

            if (entry.trim().isEmpty()) {
                continue;
            }

            if (entry.startsWith("APP:")) {

                SavedItem item = new SavedItem();
                item.type = SavedItem.Type.APP;
                item.packageName = entry.substring(4);
                result.add(item);

            } else if (entry.startsWith("FOLDER:")) {

                String rest = entry.substring(7);
                int firstColon = rest.indexOf(':');
                if (firstColon != -1) {
                    String folderName = rest.substring(0, firstColon);
                    String appsString = rest.substring(firstColon + 1);

                    SavedItem item = new SavedItem();
                    item.type = SavedItem.Type.FOLDER;
                    item.folderName = folderName;
                    item.folderApps = new ArrayList<String>();

                    String[] appPkgs = appsString.split(",");
                    for (String pkg : appPkgs) {
                        if (!pkg.trim().isEmpty()) {
                            item.folderApps.add(pkg);
                        }
                    }
                    result.add(item);
                }
            }
        }

        return result;
    }
}