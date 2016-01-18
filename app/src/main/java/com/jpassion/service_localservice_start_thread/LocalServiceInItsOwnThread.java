/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jpassion.service_localservice_start_thread;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.ConditionVariable;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import java.util.Calendar;
import java.util.GregorianCalendar;


/**
 * This is an example of service that will update its status bar balloon every 5
 * minutes for a minute.
 * 
 */
public class LocalServiceInItsOwnThread extends Service {

	// Use a layout id for a unique identifier
	private static int MOOD_NOTIFICATIONS = R.layout.status_bar_notifications;

	// variable which controls the notification thread
	private ConditionVariable mCondition;
	
	private NotificationManager mNotificationManager;

	// Create Runnable object
	private Runnable mTask = new Runnable() {
		public void run() {


				showNotification(R.drawable.stat_happy);
				//show the time again after five minutes
				mCondition.block(300*1000);
				showNotification(R.drawable.stat_happy);
				//show the time again after five minutes
				mCondition.block(300*1000);
				showNotification(R.drawable.stat_happy);

		}

	};

	@Override
	public void onCreate() {
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		/// Start up the thread
		Thread notifyingThread = new Thread(
				null,
				mTask,
		        "NotifyingService");
		mCondition = new ConditionVariable(false);
		notifyingThread.start();
	}
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
		mNotificationManager.cancel(MOOD_NOTIFICATIONS);
		// Stop the thread from generating further notifications
		mCondition.open();

	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private void showNotification(int moodId) {

		Calendar calendario = new GregorianCalendar();
		int hora, minutos, segundos;
		hora =calendario.get(Calendar.HOUR_OF_DAY);
		minutos = calendario.get(Calendar.MINUTE);
		segundos = calendario.get(Calendar.SECOND);
		String textContext = String.valueOf(hora)+":"+String.valueOf(minutos)+":"+String.valueOf(segundos);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setContentText(textContext)
				.setSmallIcon(moodId);


		Intent resultIntent = new Intent(this, ServiceLauncher.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(ServiceLauncher.class);
		stackBuilder.addNextIntent(resultIntent);


		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);


		mBuilder.setContentIntent(resultPendingIntent);


		mNotificationManager.notify(MOOD_NOTIFICATIONS, mBuilder.build());
	}

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new Binder() {
		@Override
		protected boolean onTransact(int code, Parcel data, Parcel reply,
		        int flags) throws RemoteException {
			return super.onTransact(code, data, reply, flags);
		}
	};

}
