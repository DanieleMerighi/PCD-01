package pcd.pooolTaskOriented.util;

import java.util.List;
import java.util.stream.IntStream;

public final class IntRange {

    private IntRange() {}

    public static List<Integer> until(int endExclusive) {
        return IntStream.range(0, endExclusive).boxed().toList();
    }

    public static List<Integer> withStep(int start, int endExclusive, int step) {
        return IntStream.range(start, endExclusive).filter(i -> (i - start) % step == 0).boxed().toList();
    }

}
