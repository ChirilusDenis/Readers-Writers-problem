package org.apd.executor;

import org.apd.storage.SharedDatabase;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

public class ReaderPrefferedThread extends Worker{

    public ReaderPrefferedThread(int id, SharedDatabase db, ArrayBlockingQueue<StorageTask> tasks) {
        super(id, db, tasks);
    }
    private static Semaphore[] readWriteAt = new Semaphore[20];

    private static int readers[] = new int[20];
    private static final String[] mutex = new String[20];

    public static void init() {
        for (int i = 0; i < 20; i++) {
            readWriteAt[i] = new Semaphore(1);
            readers[i]  = 0;
            mutex[i] = String.valueOf(i);
        }
    }

    @Override
    public void run() {
        while (!TaskExecutor.stop) {
            StorageTask task = tasks.poll();
            if(task == null) {
                continue;
            }

            if(task.isWrite()) {
                try {readWriteAt[task.index()].acquire();}
                catch (InterruptedException e) {throw new RuntimeException(e);}

                out.add(db.addData(task.index(), task.data()));

                readWriteAt[task.index()].release();

            } else {
                synchronized (mutex[task.index()]) {
                    readers[task.index()]++;
                    if (readers[task.index()] == 1) {
                        try {
                            readWriteAt[task.index()].acquire();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                out.add(db.getData(task.index()));

                synchronized (mutex[task.index()]) {
                    readers[task.index()]--;
                    if (readers[task.index()] == 0) readWriteAt[task.index()].release();
                }
            }
        }
    }
}
