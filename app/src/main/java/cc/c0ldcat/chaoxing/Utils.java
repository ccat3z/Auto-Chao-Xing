package cc.c0ldcat.chaoxing;

import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {
    static public void main(String[] arg) {
        for (List<Boolean> res: cartesianProduct(new ArrayList<List<Boolean>>() {{
            add(Arrays.asList(true, false));
            add(Arrays.asList(true, false));
        }})) {
            System.out.println(res.toString());
        }
    }

    static public List<Integer> range(int start, int end) {
        List<Integer> result = new ArrayList<>();

        for (int i = start; i <= end; i++)
            result.add(i);

        return result;
    }

    static public <E> List<List<E>> cartesianProduct(List<E> ...ass) {
        return cartesianProduct(Arrays.asList(ass));
    }

    static public <E> List<List<E>> cartesianProduct(List<List<E>> ass) {
        List<List<E>> result = null;
        for (List<E> as: ass)
            result = cartesianProduct(result, as);
        return result;
    }

    static public <E> List<List<E>> cartesianProduct(List<List<E>> ass, List<E> bs) {
        if (ass == null) {
            ass = new ArrayList<List<E>>() {{
               add(new ArrayList<E>());
            }};
        }

        List<List<E>> allResult = new ArrayList<>();
        for (final List<E> as: ass) {
            for (final E b: bs) {
                allResult.add(new ArrayList<E>() {{
                    addAll(as);
                    add(b);
                }});
            }
        }
        return allResult;
    }

    static public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
}
}
