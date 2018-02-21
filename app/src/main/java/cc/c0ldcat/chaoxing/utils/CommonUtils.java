package cc.c0ldcat.chaoxing.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommonUtils {
    public static List<Integer> range(int start, int end) {
        List<Integer> result = new ArrayList<>();

        for (int i = start; i <= end; i++)
            result.add(i);

        return result;
    }

    public static <E> List<List<E>> cartesianProduct(List<E> ...ass) {
        return cartesianProduct(Arrays.asList(ass));
    }

    public static <E> List<List<E>> cartesianProduct(List<E> as, int num) {
        List<List<E>> result = null;
        for (int i = 0; i < num; i++)
            result = cartesianProduct(result, as);
        return result;
    }

    public static <E> List<List<E>> cartesianProduct(List<List<E>> ass) {
        List<List<E>> result = null;
        for (List<E> as: ass)
            result = cartesianProduct(result, as);
        return result;
    }

    public static <E> List<List<E>> cartesianProduct(List<List<E>> ass, List<E> bs) {
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

    public static String exceptionStacktraceToString(Exception e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        e.printStackTrace(ps);
        ps.close();
        return baos.toString();
    }
}
