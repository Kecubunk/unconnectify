package io.mdevlab.unconnectify.notification;

/**
 * Created by mdevlab on 2/12/17.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import java.util.List;

import io.mdevlab.unconnectify.MainActivity;
import io.mdevlab.unconnectify.R;
import io.mdevlab.unconnectify.alarm.PreciseConnectivityAlarm;
import io.mdevlab.unconnectify.data.AlarmSqlHelper;
import io.mdevlab.unconnectify.utils.Connection;
import io.mdevlab.unconnectify.utils.DateUtils;

/**
 * This is tne main class for launching a notification
 * The notification will inform the user about the next alarm
 * when the user click on the notification he will be redirected to the main activity of unconnectify app
 * the next alarm is extracted from the db
 */
public class AlarmNotificationManager {
    private static final int NOTIFICATIONID = 001;

    /**
     * This static method is the trigger for notifications to inform the user what will happen next
     * what are the connections that wil be on/off
     */
    public static void triggerNotification(Context context) {

        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //Get Needed data
        AlarmSqlHelper alarmSqlHelper = new AlarmSqlHelper(context);
        PreciseConnectivityAlarm preciseConnectivityAlarm = alarmSqlHelper.readNextAlarm();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        //force the verification of the returned Alarm
        if (preciseConnectivityAlarm != null) {
            //Set the text
            String notifiactionText = buildNotificationString(preciseConnectivityAlarm);

            //Get the Notification Builder


            //Set the intent
            Intent intent = new Intent(context, MainActivity.class);

            // The pending intent to be attached  to the notifiaction
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            context,
                            0,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            //Customize the Notification
            mBuilder.setContentTitle("Unconnectify")
                    .setContentIntent(resultPendingIntent)
                    .setContentText(notifiactionText)
                    .setSmallIcon(R.mipmap.ic_launcher)
                /*
                    * Sets the big view "big text" style and supplies the
                    * text (the user's reminder message) that will be displayed
                    * in the detail area of the expanded notification.
                    * These calls are ignored by the support library for
                    * pre-4.1 devices.
                */
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(notifiactionText));


            // Builds the notification and issues it.
            mNotifyMgr.notify(NOTIFICATIONID, mBuilder.build());
        }else{
            //Remove The notification if we get an empty result no Next alarm
            mNotifyMgr.cancel(NOTIFICATIONID);
        }

    }

    /**
     * This function return the built String from the next Alarm Object
     *
     * @param preciseConnectivityAlarm the object from which we get the String
     * @return the built String
     * return example Next -Turning ON/OFF WIFI|BLUETOOTH|HOTSPOT at Monday 18:00
     */
    private static String buildNotificationString(PreciseConnectivityAlarm preciseConnectivityAlarm) {

        List<Connection> connectionList = preciseConnectivityAlarm.getConnections();
        long executionTime = preciseConnectivityAlarm.getExecuteTimeInMils();
        Boolean currentStatus = preciseConnectivityAlarm.getCurrentState();

        //Build the string using String Builder to avoid Instansiation of a new String each time
        //String is immutable
        StringBuilder result = new StringBuilder("Next - Turning ");
        if (currentStatus) {
            result.append( " ON ");
        } else
            result.append(" OFF ");

        for (Connection connection : connectionList) {
            result.append(connection ).append(" | ");
        }

        //Date  with the given format using timeinMillistoDate function from DateUtils
        String returnedDate = DateUtils.timeinMillistoDate(preciseConnectivityAlarm.getStartTime() + executionTime, DateUtils.NOTIFICATION_DATE_FORMAT);

        //set the final result
        result.append(" at ").append( returnedDate);

        return result.toString();
    }
}
