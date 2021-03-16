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
    private ArrayList<R> mResult = new ArrayList();
    private Boolean mCancelled = false;

    public Boolean isCancelled() {
        return mCancelled;
    }

    public void cancel(Boolean mayInterruptIfRunning) {
        mFuture.cancel(mayInterruptIfRunning);
        mCancelled = true;
    }

    public void execute(final T... params) {
        mFuture = new FutureTask<>(new Callable<Void>() {
            @Override
            public Void call() throws InterruptedException {
                mLatch = new CountDownLatch(1);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        preExecute();
                    }
                }).start();
                mLatch.await();

                mLatch = new CountDownLatch(1);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        executeInBackground(params);
                    }
                }).start();
                mLatch.await();

                mLatch = null;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        onPostExecute(mResult.get(0));
                    }
                }).start();
                return null;
            }
        });
        executor.execute(mFuture);
    }

    private void preExecute() {
        onPreExecute();
        mLatch.countDown();
    }

    private void executeInBackground(T... params) {
        R result = doInBackground(params);
        mResult.add(result);
        mLatch.countDown();
    }

    protected void onPreExecute() {}
    protected abstract R doInBackground(T... params);
    protected void onPostExecute(R result) {}
}
