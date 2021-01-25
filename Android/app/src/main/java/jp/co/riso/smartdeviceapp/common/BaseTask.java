package jp.co.riso.smartdeviceapp.common;

import android.app.Activity;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import jp.co.riso.smartdeviceapp.SmartDeviceApp;

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
        final Activity activity = SmartDeviceApp.getActivity();

        mFuture = new FutureTask<>(new Callable<Void>() {
            @Override
            public Void call() throws InterruptedException {
                mLatch = new CountDownLatch(1);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        preExecute();
                    }
                });
                mLatch.await();

                mLatch = new CountDownLatch(1);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        R result = executeInBackground(params);
                        mResult.add(result);
                    }
                });
                mLatch.await();

                mLatch = null;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onPostExecute(mResult.get(0));
                    }
                });
                return null;
            }
        });
        executor.execute(mFuture);
    }

    private void preExecute() {
        onPreExecute();
        mLatch.countDown();
    }

    private R executeInBackground(T... params) {
        R result = doInBackground(params);
        mLatch.countDown();
        return result;
    }

    protected void onPreExecute() {}
    protected abstract R doInBackground(T... params);
    protected void onPostExecute(R result) {}
}
