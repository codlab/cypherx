package eu.codlab.cypherx.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;

public class Bytes {
    public static byte[] removeTrailingZeroes(@Nullable byte[] bytes) {
        int index = -1;

        byte[] to_return = null;

        if (bytes != null) {
            to_return = Arrays.copyOf(bytes, firstZero(bytes));
        }
        return to_return;
    }

    private static int firstZero(@NonNull byte[] bytes) {
        int index = bytes.length;
        while (bytes[index - 1] == 0) index--;
        return index;
    }
}
