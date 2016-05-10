package com.icedcap.remoteviewsdemo;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Author: doushuqi
 * Date: 16-4-28
 * Email: shuqi.dou@singuloid.com
 * LastUpdateTime:
 * LastUpdateBy:
 */
public class MusicWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "MusicWidgetProvider";
    private static final String SHAREPREFERENCE_NAME = "music";
    private static final String SHAREPREFERENCE_KEY_PLAYING = "isplaying";
    private static final String SHAREPREFERENCE_KEY_LOVE = "love";
    private static final String SHAREPREFERENCE_KEY_PROGRESS = "progress";
    private static final String ACTION_MUSIC_PLAY = "com.icedcap.sample.MUSIC_PLAY";
    private static final String ACTION_MUSIC_PAUSE = "com.icedcap.sample.MUSIC_PAUSE";
    private static final String ACTION_MUSIC_LOVE = "com.icedcap.sample.MUSIC_LOVE";
    private MyTask mMyTask;


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        final String action = intent.getAction();
        Log.i(TAG, "action = " + action);
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.music_widget);

        if (action.equals(ACTION_MUSIC_PLAY)) {
            emulatePlayMusic(context, remoteViews);
        } else if (action.equals(ACTION_MUSIC_PAUSE)) {
            emulatePauseMusic(context, remoteViews);
        } else if (action.equals(ACTION_MUSIC_LOVE)) {
            musicLove(context, remoteViews);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            appWidgetManager.updateAppWidget(new ComponentName(context, MusicWidgetProvider.class), remoteViews);
        }


    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.i(TAG, "------onUpdate----------");
        final int totalWidgetIds = appWidgetIds.length;
        Log.i(TAG, "totalWidgetIds = " + totalWidgetIds);
        for (int i = 0; i < totalWidgetIds; i++) {
            updateWidget(context, appWidgetManager, appWidgetIds[i]);
        }

    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    private void updateWidget(Context context, AppWidgetManager manager, int viewId) {
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.music_widget);
        final boolean isPlaying = context.getSharedPreferences(SHAREPREFERENCE_NAME, Context.MODE_PRIVATE).getBoolean(SHAREPREFERENCE_KEY_PLAYING, false);
        Intent click = new Intent();
//        click.setAction(isPlaying ? ACTION_MUSIC_PAUSE : ACTION_MUSIC_PLAY);
        if (!isPlaying) {
            click.setAction(ACTION_MUSIC_PLAY);
            remoteViews.setOnClickPendingIntent(R.id.music_play_pause, PendingIntent.getBroadcast(context, 0, click, 0));
        }

        click.setAction(ACTION_MUSIC_LOVE);
        remoteViews.setOnClickPendingIntent(R.id.music_love, PendingIntent.getBroadcast(context, 0, click, 0));
        manager.updateAppWidget(viewId, remoteViews);
    }


    private void emulatePlayMusic(Context c, RemoteViews remoteViews) {
        mMyTask = new MyTask(c, remoteViews);
        mMyTask.execute();
    }

    private void emulatePauseMusic(Context c, RemoteViews r) {
        writeIsplayingToSharePreference(c, false);
        if (null != mMyTask && !mMyTask.isCancelled()) {
            mMyTask.cancel(true);
        }
        r.setImageViewResource(R.id.music_play_pause, R.drawable.desk2_play);
        r.setProgressBar(R.id.music_progress, 100,
                c.getSharedPreferences(SHAREPREFERENCE_NAME, Context.MODE_PRIVATE)
                        .getInt(SHAREPREFERENCE_KEY_PROGRESS, 0), false);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(c);
        appWidgetManager.updateAppWidget(new ComponentName(c, MusicWidgetProvider.class), r);
    }

    private void musicLove(Context c, RemoteViews remoteViews) {
        boolean isLove = !c.getSharedPreferences(SHAREPREFERENCE_NAME, Context.MODE_PRIVATE).getBoolean(SHAREPREFERENCE_KEY_LOVE, false);
        remoteViews.setImageViewResource(R.id.music_love, isLove ? R.drawable.desk_btn_loved : R.drawable.desk_love);
        writeLoveToSharePreference(c, isLove);
    }

    private void writeLoveToSharePreference(Context c, boolean love) {
        SharedPreferences.Editor editor = c.getSharedPreferences(SHAREPREFERENCE_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(SHAREPREFERENCE_KEY_LOVE, love);
        editor.apply();
    }

    private void writeIsplayingToSharePreference(Context c, boolean play) {
        SharedPreferences.Editor editor = c.getSharedPreferences(SHAREPREFERENCE_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(SHAREPREFERENCE_KEY_PLAYING, play);
        editor.apply();
    }

    private void writeProgressToSharePreference(Context c, int progress) {
        SharedPreferences.Editor editor = c.getSharedPreferences(SHAREPREFERENCE_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(SHAREPREFERENCE_KEY_PROGRESS, progress);
        editor.apply();
    }

    class MyTask extends AsyncTask<Void, Integer, Integer> {
        RemoteViews mRemoteViews;
        Context mContext;
        int mProgress;

        public MyTask(Context c, RemoteViews remoteViews) {
            mRemoteViews = remoteViews;
            mContext = c;
            mProgress = c.getSharedPreferences(SHAREPREFERENCE_NAME, Context.MODE_PRIVATE).getInt(SHAREPREFERENCE_KEY_PROGRESS, 0);
        }

        @Override
        protected void onPreExecute() {
            writeIsplayingToSharePreference(mContext, true);
            mRemoteViews.setImageViewResource(R.id.music_play_pause, R.drawable.desk2_pause);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            while (mProgress <= 100) {
                try {
                    Thread.sleep(30);
                    publishProgress(mProgress);
                    mProgress += 1;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return 100;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mRemoteViews.setProgressBar(R.id.music_progress, 100, values[0], false);

            writeProgressToSharePreference(mContext, values[0]);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
            appWidgetManager.updateAppWidget(new ComponentName(mContext, MusicWidgetProvider.class), mRemoteViews);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            writeIsplayingToSharePreference(mContext, false);
            mRemoteViews.setProgressBar(R.id.music_progress, 100, 0, false);
            mRemoteViews.setImageViewResource(R.id.music_play_pause, R.drawable.desk2_play);
            writeProgressToSharePreference(mContext, 0);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
            appWidgetManager.updateAppWidget(new ComponentName(mContext, MusicWidgetProvider.class), mRemoteViews);

        }
    }
}
