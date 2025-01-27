package pro.belbix.ethparser.utils;

import java.util.function.BiConsumer;

public class LoopUtils {

    private static final int LOOP_BATCH = 5000;

    public static void handleLoop(Integer from, Integer to, BiConsumer<Integer, Integer> handler) {
        while (true) {
            Integer end = null;
            if (to != null) {
                end = from + LOOP_BATCH;
            }
            handler.accept(from, end);
            from = end;
            if (to != null) {
                if (end > to) {
                    break;
                }
            } else {
                break;
            }
        }
    }

}
