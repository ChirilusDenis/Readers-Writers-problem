package org.apd.executor;

import org.apd.storage.EntryResult;
import org.apd.storage.SharedDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/* DO NOT MODIFY THE METHODS SIGNATURES */
public class TaskExecutor {
    private final SharedDatabase sharedDatabase;
    public static volatile boolean stop;

    public TaskExecutor(int storageSize, int blockSize, long readDuration, long writeDuration) {
        sharedDatabase = new SharedDatabase(storageSize, blockSize, readDuration, writeDuration);
    }

    public List<EntryResult> ExecuteWork(int numberOfThreads, List<StorageTask> tasks, LockType lockType) {
        /* IMPLEMENT HERE THE THREAD POOL, ASSIGN THE TASKS AND RETURN THE RESULTS */
        List<EntryResult> out = new ArrayList<>();

        ArrayBlockingQueue<StorageTask> ts = new ArrayBlockingQueue<>(tasks.size());
        Worker[] pool = new Worker[numberOfThreads];
        stop = false;

        switch (lockType) {
            case ReaderPreferred:
                for(int i = 0; i < numberOfThreads; i++) {
                    pool[i] = new ReaderPrefferedThread(i, sharedDatabase, ts);
                }
                ReaderPrefferedThread.init();
                break;

            case WriterPreferred1:
                return out;
            case WriterPreferred2:
                return out;
        }

        for(Worker thread : pool) thread.start();

        for(StorageTask task : tasks) ts.add(task);
//        ts.addAll(tasks);

        while(!ts.isEmpty()) {}
        stop = true;

        for(Worker thread : pool) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        for(Worker thread : pool) out.addAll(thread.output());

//        Thread[] trd_pool = new Thread[numberOfThreads];
////        sync = Collections.synchronizedList(tasks);
//        out = Collections.synchronizedList(new ArrayList<>());
////        out = new ArrayList<>();
////        sync = tasks;
//        next_task = new AtomicInteger(0);
//
//        for(int i = 0; i < numberOfThreads; i++) {
//            trd_pool[i] = new Worker(i, lockType, sharedDatabase, tasks);
//            trd_pool[i].start();
//        }
//
////        for (int i = 0; i < numberOfThreads; i++) {
////            trd_pool[i].start();
////        }
//
//        for(int i = 0; i < numberOfThreads ;i++) {
//            try {
//                trd_pool[i].join();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        for(int i = 0; i < numberOfThreads; i++) {
//            out.addAll(((Worker)trd_pool[i]).output());
//        }

        return out;
    }

    public List<EntryResult> ExecuteWorkSerial(List<StorageTask> tasks) {
        var results = tasks.stream().map(task -> {
            try {
                if (task.isWrite()) {
                    return sharedDatabase.addData(task.index(), task.data());
                } else {
                    return sharedDatabase.getData(task.index());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).toList();

        return results.stream().toList();
    }
}
