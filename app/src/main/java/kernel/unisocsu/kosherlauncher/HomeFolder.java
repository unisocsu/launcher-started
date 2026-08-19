package kernel.unisocsu.kosherlauncher;

import java.util.ArrayList;
import java.util.List;

public class HomeFolder {

    private String name;
    private final List<AppInfo> apps;

    public HomeFolder(String name) {
        this.name = name;
        this.apps = new ArrayList<AppInfo>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AppInfo> getApps() {
        return apps;
    }

    public void addApp(AppInfo app) {
        if (app != null) {
            apps.add(app);
        }
    }

    public void removeApp(AppInfo app) {
        if (app != null) {
            apps.remove(app);
        }
    }
}