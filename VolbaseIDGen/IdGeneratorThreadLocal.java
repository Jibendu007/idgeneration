package com.volantetech.services.engine.util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Monikandan on 01/10/21.
 */

public class IdGeneratorThreadLocal {

    private static final ThreadLocal<ArrayList> threadLocalIdGenerate = ThreadLocal.withInitial(ArrayList::new);
    private IdGeneratorThreadLocal() {
    }

    public static ArrayList getThreadLocalIdGenerate() {
        return threadLocalIdGenerate.get();
    }

    public static void setThreadLocalIdGenerate(HashMap threadLocalMap) {
        threadLocalIdGenerate.get().add(threadLocalMap);
    }


}
