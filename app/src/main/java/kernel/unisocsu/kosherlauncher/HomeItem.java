package kernel.unisocsu.kosherlauncher;

public class HomeItem {

    public enum Type {
        APP,
        FOLDER
    }

    private Type type;
    private AppInfo appInfo;
    private HomeFolder folder;

    public HomeItem(AppInfo appInfo) {
        this.type = Type.APP;
        this.appInfo = appInfo;
    }

    public HomeItem(HomeFolder folder) {
        this.type = Type.FOLDER;
        this.folder = folder;
    }

    public Type getType() {
        return type;
    }

    public AppInfo getAppInfo() {
        return appInfo;
    }

    public HomeFolder getFolder() {
        return folder;
    }

    public boolean isApp() {
        return type == Type.APP;
    }

    public boolean isFolder() {
        return type == Type.FOLDER;
    }
}