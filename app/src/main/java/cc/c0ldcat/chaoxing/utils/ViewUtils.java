package cc.c0ldcat.chaoxing.utils;

import android.view.View;
import android.widget.ListView;

public class ViewUtils {
    public static View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    public static View getPositionByView(View view, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        for (int i = firstListItemPosition; i <= lastListItemPosition; i++) {
            if (listView.getChildAt(i) == view)
                return view;
        }

        return null;
    }
}
