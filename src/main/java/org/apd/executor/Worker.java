package org.apd.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import org.apd.storage.EntryResult;
import org.apd.storage.SharedDatabase;

public abstract class Worker extends Thread{
    protected int id;
    protected SharedDatabase db;
    protected List<EntryResult> out = new ArrayList<>();
    protected ArrayBlockingQueue<StorageTask>  tasks;

    public Worker(int id, SharedDatabase db, ArrayBlockingQueue<StorageTask> tasks) {
        this.id = id;
        this.db = db;
        this.tasks = tasks;
    }

    public List<EntryResult> output() {
        return out;
    }
}
