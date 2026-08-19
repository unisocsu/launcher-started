package kernel.unisocsu.kosherlauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    private HomeGridView homeGrid;
    private final List<HomeItem> items = new ArrayList<HomeItem>();
    private PackageManager packageManager;
    private LauncherStorage launcherStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        packageManager = getPackageManager();
        launcherStorage = new LauncherStorage(this);

        createInterface();
        loadApplications();
        populateGrid();
    }

    private void createInterface() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(245, 245, 245));

        TextView title = new TextView(this);
        title.setText("KosherLauncher");
        title.setTextSize(22);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(24, 0, 24, 0);

        root.addView(
                title,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        80
                )
        );

        homeGrid = new HomeGridView(this);
        homeGrid.setColumns(4);

        homeGrid.setOnItemLongPressedListener(
                new HomeGridView.OnItemLongPressedListener() {
                    @Override
                    public void onItemLongPressed(int index) {
                        showItemMenu(index);
                    }
                }
        );

        homeGrid.setOnMoveFinishedListener(
                new HomeGridView.OnMoveFinishedListener() {
                    @Override
                    public void onMoveFinished(int fromIndex, int toIndex) {
                        handleMove(fromIndex, toIndex);
                    }
                }
        );

        root.addView(
                homeGrid,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        setContentView(root);
    }

    private void loadApplications() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> results = packageManager.queryIntentActivities(intent, 0);
        List<AppInfo> apps = new ArrayList<AppInfo>();

        for (ResolveInfo info : results) {
            if (info.activityInfo == null) {
                continue;
            }

            AppInfo app = new AppInfo(
                    info.loadLabel(packageManager).toString(),
                    info.activityInfo.applicationInfo.packageName,
                    info.loadIcon(packageManager)
            );
            apps.add(app);
        }

        // Sort all apps alphabetically by default
        Collections.sort(
                apps,
                new Comparator<AppInfo>() {
                    @Override
                    public int compare(AppInfo a, AppInfo b) {
                        return a.getLabel().compareToIgnoreCase(b.getLabel());
                    }
                }
        );

        // Map package names to AppInfo to quickly look them up
        Map<String, AppInfo> appMap = new HashMap<String, AppInfo>();
        for (AppInfo app : apps) {
            appMap.put(app.getPackageName(), app);
        }

        items.clear();

        // Restore saved order
        List<LauncherStorage.SavedItem> savedOrder = launcherStorage.loadOrder();
        if (savedOrder != null && !savedOrder.isEmpty()) {
            for (LauncherStorage.SavedItem saved : savedOrder) {
                if (saved.type == LauncherStorage.SavedItem.Type.APP) {
                    AppInfo app = appMap.remove(saved.packageName);
                    if (app != null) {
                        items.add(new HomeItem(app));
                    }
                } else if (saved.type == LauncherStorage.SavedItem.Type.FOLDER) {
                    HomeFolder folder = new HomeFolder(saved.folderName);
                    for (String pkg : saved.folderApps) {
                        AppInfo app = appMap.remove(pkg);
                        if (app != null) {
                            folder.addApp(app);
                        }
                    }
                    if (!folder.getApps().isEmpty()) {
                        items.add(new HomeItem(folder));
                    }
                }
            }
        }

        // Add remaining/newly installed apps that weren't in the saved order
        for (AppInfo app : apps) {
            if (appMap.containsKey(app.getPackageName())) {
                items.add(new HomeItem(app));
            }
        }
    }

    private void populateGrid() {
        homeGrid.clearItems();

        for (int i = 0; i < items.size(); i++) {
            final int position = i;
            View view = createItemView(items.get(i));

            view.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openItem(position);
                        }
                    }
            );

            homeGrid.addItem(view);
        }

        if (!items.isEmpty()) {
            homeGrid.select(0);
        }
    }

    private View createFolderIconView(HomeFolder folder) {
        LinearLayout folderIconContainer = new LinearLayout(this);
        folderIconContainer.setOrientation(LinearLayout.VERTICAL);
        folderIconContainer.setGravity(Gravity.CENTER);

        // Semi-transparent rounded folder background
        GradientDrawable folderBg = new GradientDrawable();
        folderBg.setColor(Color.argb(35, 0, 0, 0));
        folderBg.setStroke(2, Color.rgb(200, 200, 200));
        folderBg.setCornerRadius(16);
        folderIconContainer.setBackground(folderBg);

        int padding = 6;
        folderIconContainer.setPadding(padding, padding, padding, padding);

        // A 2x2 grid of miniature app icons
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setGravity(Gravity.CENTER);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setGravity(Gravity.CENTER);

        List<AppInfo> apps = folder.getApps();

        for (int i = 0; i < 4; i++) {
            ImageView miniIcon = new ImageView(this);
            miniIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            LinearLayout.LayoutParams miniParams = new LinearLayout.LayoutParams(22, 22);
            miniParams.setMargins(2, 2, 2, 2);

            if (i < apps.size()) {
                miniIcon.setImageDrawable(apps.get(i).getIcon());
            } else {
                miniIcon.setImageDrawable(null);
            }

            if (i < 2) {
                row1.addView(miniIcon, miniParams);
            } else {
                row2.addView(miniIcon, miniParams);
            }
        }

        folderIconContainer.addView(row1, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        folderIconContainer.addView(row2, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        return folderIconContainer;
    }

    private View createItemView(HomeItem item) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(8, 8, 8, 8);

        View iconView;
        TextView label = new TextView(this);
        label.setTextSize(14);
        label.setTextColor(Color.BLACK);
        label.setGravity(Gravity.CENTER);
        label.setSingleLine(true);

        if (item.isApp()) {
            AppInfo app = item.getAppInfo();
            ImageView icon = new ImageView(this);
            icon.setImageDrawable(app.getIcon());
            iconView = icon;
            label.setText(app.getLabel());
        } else {
            iconView = createFolderIconView(item.getFolder());
            label.setText(item.getFolder().getName());
        }

        root.addView(
                iconView,
                new LinearLayout.LayoutParams(
                        64,
                        64
                )
        );

        root.addView(
                label,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        40
                )
        );

        return root;
    }

    private void openItem(int index) {
        if (index < 0 || index >= items.size()) {
            return;
        }

        HomeItem item = items.get(index);
        if (item.isApp()) {
            launchApplication(item.getAppInfo());
        } else {
            openFolder(item.getFolder());
        }
    }

    private void launchApplication(AppInfo app) {
        Intent intent = packageManager.getLaunchIntentForPackage(app.getPackageName());
        if (intent == null) {
            return;
        }
        startActivity(intent);
    }

    private void openFolder(HomeFolder folder) {
        StringBuilder text = new StringBuilder();
        for (AppInfo app : folder.getApps()) {
            text.append(app.getLabel()).append("\n");
        }

        new AlertDialog.Builder(this)
                .setTitle(folder.getName())
                .setMessage(text.toString())
                .setPositiveButton("סגור", null)
                .show();
    }

    private void showItemMenu(final int index) {
        if (index < 0 || index >= items.size()) {
            return;
        }

        final HomeItem item = items.get(index);
        if (!item.isApp()) {
            return;
        }

        String[] options = {
                "העברה",
                "ביטול"
        };

        new AlertDialog.Builder(this)
                .setTitle(item.getAppInfo().getLabel())
                .setItems(
                        options,
                        (dialog, which) -> {
                            if (which == 0) {
                                homeGrid.select(index);
                                homeGrid.startMove();
                            }
                        }
                )
                .show();
    }

    private void handleMove(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex >= items.size()) {
            return;
        }
        if (toIndex < 0 || toIndex >= items.size()) {
            return;
        }
        if (fromIndex == toIndex) {
            return;
        }

        HomeItem source = items.get(fromIndex);
        HomeItem target = items.get(toIndex);

        if (source.isApp() && target.isApp()) {
            showMoveDialog(fromIndex, toIndex);
            return;
        }

        Collections.swap(items, fromIndex, toIndex);
        populateGrid();
        homeGrid.select(toIndex);

        // Save order when swapped without dialog
        launcherStorage.saveOrder(items);
    }

    private void showMoveDialog(final int fromIndex, final int toIndex) {
        HomeItem source = items.get(fromIndex);
        HomeItem target = items.get(toIndex);

        String sourceName = source.getAppInfo().getLabel();
        String targetName = target.getAppInfo().getLabel();

        String[] options = {
                "החלף מיקומים",
                "מזג לתיקייה",
                "ביטול"
        };

        new AlertDialog.Builder(this)
                .setTitle("העברת " + sourceName)
                .setMessage("היעד: " + targetName)
                .setItems(
                        options,
                        (dialog, which) -> {
                            if (which == 0) {
                                swapItems(fromIndex, toIndex);
                            } else if (which == 1) {
                                mergeIntoFolder(fromIndex, toIndex);
                            }
                        }
                )
                .show();
    }

    private void swapItems(int first, int second) {
        Collections.swap(items, first, second);
        populateGrid();
        homeGrid.select(second);

        // Save order
        launcherStorage.saveOrder(items);
    }

    private void mergeIntoFolder(int sourceIndex, int targetIndex) {
        HomeItem source = items.get(sourceIndex);
        HomeItem target = items.get(targetIndex);

        if (!source.isApp() || !target.isApp()) {
            return;
        }

        HomeFolder folder = new HomeFolder(
                source.getAppInfo().getLabel() + " + " + target.getAppInfo().getLabel()
        );

        folder.addApp(target.getAppInfo());
        folder.addApp(source.getAppInfo());

        HomeItem folderItem = new HomeItem(folder);

        int newIndex = Math.min(sourceIndex, targetIndex);

        items.remove(Math.max(sourceIndex, targetIndex));
        items.remove(Math.min(sourceIndex, targetIndex));

        items.add(newIndex, folderItem);

        populateGrid();
        homeGrid.select(newIndex);

        // Save order
        launcherStorage.saveOrder(items);
    }
}