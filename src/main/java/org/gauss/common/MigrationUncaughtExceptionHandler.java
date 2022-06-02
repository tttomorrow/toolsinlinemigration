package org.gauss.common;

import org.slf4j.Logger;

/**
 * @author saxisuer
 * @Description cache thread exception when thread run in thread pool executor
 * @date 2022/4/22
 * @email sheng.pu@enmotech.com
 * @COMPANY ENMOTECH
 */
public class MigrationUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final Logger logger;

    public MigrationUncaughtExceptionHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        String msg = String.format("getException from thread: %s,exceptionName:%s", t.getName(), e.getMessage());
        logger.error(msg, e);
    }
}
