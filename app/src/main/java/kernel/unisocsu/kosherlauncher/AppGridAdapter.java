package kernel.unisocsu.kosherlauncher;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class AppGridAdapter extends BaseAdapter {

    private final Context context;
    private final List<AppInfo> apps;

    public AppGridAdapter(
            Context context,
            List<AppInfo> apps) {

        this.context = context;
        this.apps = apps;
    }

    @Override
    public int getCount() {
        return apps.size();
    }

    @Override
    public Object getItem(int position) {
        return apps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(
            int position,
            View convertView,
            ViewGroup parent) {

        AppInfo app = apps.get(position);

        LinearLayout root =
                new LinearLayout(context);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setGravity(Gravity.CENTER);

        root.setPadding(
                8,
                8,
                8,
                8
        );

        ImageView icon =
                new ImageView(context);

        Drawable drawable =
                app.getIcon();

        icon.setImageDrawable(drawable);

        root.addView(
                icon,
                new LinearLayout.LayoutParams(
                        64,
                        64
                )
        );

        TextView label =
                new TextView(context);

        label.setText(
                app.getLabel()
        );

        label.setTextColor(
                Color.BLACK
        );

        label.setTextSize(14);

        label.setGravity(
                Gravity.CENTER
        );

        label.setSingleLine(true);

        root.addView(
                label,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        40
                )
        );

        return root;
    }
}