package com.moon.util.compute.core;

import com.moon.lang.ref.IntAccessor;
import com.moon.util.compute.RunnerSettings;

import static com.moon.lang.ThrowUtil.noInstanceError;
import static com.moon.util.compute.core.Constants.*;

/**
 * @author benshaoye
 */
final class ParseOpposite {
    private ParseOpposite() {
        noInstanceError();
    }

    final static AsRunner parse(char[] chars, IntAccessor indexer, int len, RunnerSettings settings) {
        AsRunner handler, linked;
        int curr = ParseUtil.nextVal(chars, indexer, len);
        switch (curr) {
            case YUAN_L:
                handler = ParseGetter.parseYuan(chars, indexer, len, settings);
                break;
            case FANG_L:
                handler = ParseGetter.parseFang(chars, indexer, len, settings);
                break;
            case HUA_L:
                handler = ParseCurly.parse(chars, indexer, len, settings);
                break;
            case CALLER:
                handler = ParseCall.parse(chars, indexer, len, settings);
                break;
            default:
                if (ParseUtil.isNum(curr)) {
                    handler = ParseConst.parseNum(chars, indexer, len, curr);
                } else if (ParseUtil.isVar(curr)) {
                    handler = ParseGetter.parseVar(chars, indexer, len, curr);
                    ParseUtil.assertFalse(handler.isConst(), chars, indexer);
                } else {
                    handler = ParseUtil.throwErr(chars, indexer);
                }
                break;
        }
        ParseUtil.assertTrue(handler.isValuer(), chars, indexer);
        linked = ParseGetter.tryParseLinked(chars, indexer, len, settings, handler);
        return linked.isConst()
            ? DataConst.getOpposite((DataConst) linked)
            : new GetOpposite(linked);
    }
}
