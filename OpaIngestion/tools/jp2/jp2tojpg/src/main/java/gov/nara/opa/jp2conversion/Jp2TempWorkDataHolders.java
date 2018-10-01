package gov.nara.opa.jp2conversion;

import java.io.File;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Jp2TempWorkDataHolders {
    public static final Queue<File> WORK_QUEUE = new ConcurrentLinkedQueue<File>();
    public static final AtomicInteger NO_OF_THREADS = new AtomicInteger(0);
    public static final AtomicInteger NO_OF_FILES_CONVERTED = new AtomicInteger(0);
    public static final AtomicInteger NO_OF_FILES_PROCESSED = new AtomicInteger(0);
    public static int NO_OF_FILES_READ = 0;
    public static long START_TIME = (new Date()).getTime();
}
