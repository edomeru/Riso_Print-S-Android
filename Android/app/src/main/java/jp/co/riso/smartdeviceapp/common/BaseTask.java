/*
 * Copyright (c) 2021 RISO, Inc. All rights reserved.
 *
 * BaseTask.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.common;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public abstract class BaseTask<T, R> {
    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private FutureTask<Void> mFuture = null;
    private CountDownLatch mLatch = null;
    private Boolean mCancelled = false;

    @SuppressWarnings("unchecked")     // R is defined in BaseTask implementation
    private ArrayList<R> mResult = new ArrayList();

    public Boolean isCancelled() {
        return mCancelled;
    }

    public void cancel(Boolean mayInterruptIfRunning) {
        mFuture.cancel(mayInterruptIfRunning);
        mCancelled = true;
    }

    @SuppressWarnings("unchecked")     // T is defined in BaseTask implementation
    public void execute(final T... params) {
        mFuture = new FutureTask<>(() -> {
            mLatch = new CountDownLatch(1);
            new Thread(() -> preExecute()).start();
            mLatch.await();

            mLatch = new CountDownLatch(1);
            new Thread(() -> executeInBackground(params)).start();
            mLatch.await();

            mLatch = null;
            new Thread(() -> onPostExecute(mResult.get(0))).start();
            return null;
        });
        executor.execute(mFuture);
    }

    private void preExecute() {
        onPreExecute();
        mLatch.countDown();
    }

    @SuppressWarnings("unchecked")     // T is defined in BaseTask implementation
    private void executeInBackground(T... params) {
        R result = doInBackground(params);
        mResult.add(result);
        mLatch.countDown();
    }

    protected void onPreExecute() {}

    @SuppressWarnings("unchecked")     // T is defined in BaseTask implementation
    protected abstract R doInBackground(T... params);

    protected void onPostExecute(R result) {}
}
