package io.mdevlab.unconnectify.alarm;

import android.content.Context;

import com.evernote.android.job.JobManager;

import java.util.List;

import io.mdevlab.unconnectify.data.AlarmSqlHelper;
import io.mdevlab.unconnectify.jobs.ConnectivityJobManager;
import io.mdevlab.unconnectify.notification.AlarmNotificationManager;
import io.mdevlab.unconnectify.utils.AlarmUtils;
import io.mdevlab.unconnectify.utils.Connection;

/**
 * Created by mdevlab on 2/10/17.
 */

public class AlarmManager {

    private static Context mContext;
    private static AlarmManager instance = null;
    private static AlarmSqlHelper alarmSqlHelper = null;

    private AlarmManager(Context context) {
        this.mContext = context;
        alarmSqlHelper = new AlarmSqlHelper(context);
    }

    public static AlarmManager getInstance(Context context) {
        if (instance == null)
            instance = new AlarmManager(context);
        return instance;
    }

    /**
     * creating an alarm consists of:
     * - Saving the alarm in the local database
     * - Launching whatever necessary jobs
     *
     * @param alarm
     */
    public long createAlarm(PreciseConnectivityAlarm alarm) {

        // Saving the alarm to the local database using the sql helper
        long alarmId = alarmSqlHelper.createAlarm(alarm);

        // Launching alarm job
        alarm.setAlarmId((int) alarmId);
        createAlarmJob(alarm);
        return alarmId;
    }

    /**
     * This method is for deleting an alarm from the database
     *
     * @param alarmId id of the alarm
     * @return true if deleted false otherways
     */
    public Boolean clearAlarm(int alarmId) {

        // Cancel the job assigned to the alarm being deleted
        cancelAlarmJob(alarmSqlHelper.getAlarmById(alarmId));

        // Deleting the alarm from the local database using the sql helper
        int lines = alarmSqlHelper.deleteAlarm(alarmId);

        if (lines > 0) {
            return true;
        }

        return false;
    }

    /**
     * Method that sets the first job for an alarm right after its creation
     * It's also called to update an alarm's job after this alarm has been modified
     * So it starts by canceling the previous job first before creating a new one
     *
     * @param alarm: The newly created alarm
     */
    private void createAlarmJob(PreciseConnectivityAlarm alarm) {
        // Cancel previous job assigned to alarm if it exists
        if (alarm.getJobId() != -1)
            JobManager.instance().cancel(alarm.getJobId());

        ConnectivityJobManager.buildJobRequest(alarm, AlarmUtils.getStringFromConnection(AlarmUtils.getFirstConnection(alarm)),
                false,
                alarm.getExecuteTimeInMils());

        alarmSqlHelper.updateAlarmJob(alarm.getAlarmId(), alarm.getJobId());
    }

    /**
     * Method that cancels an alarm's job
     *
     * @param alarm: Alarm of which we want to cancel the job
     */
    private void cancelAlarmJob(PreciseConnectivityAlarm alarm) {

        // If the alarm has a job, cancel it
        if (alarm.getJobId() != -1)
            JobManager.instance().cancel(alarm.getJobId());
    }

    /**
     * Method that updates an alarm's state (on/off)
     *
     * @param alarm:    Alarm object whose state is being changed
     * @param isActive: New state of the alarm, either on (true) or off (false)
     */
    public void updateAlarmState(PreciseConnectivityAlarm alarm, boolean isActive) {

        // Update the state of the alarm in the dababase
        alarmSqlHelper.updateAlarmCurrentState(alarm.getAlarmId(), isActive);

        // Update the value of 'isActive'
        alarm.setActive(isActive);

        // If alarm is now active, create its job
        if (isActive)
            createAlarmJob(alarm);

            // Else, cancel its current running job
        else
            cancelAlarmJob(alarm);
    }

    /**
     * Method that updates the alarm's execution time and duration
     *
     * @param alarm:       The alarm being updated
     * @param executionTime: New execution time to be assigned to the alarm being updated
     * @param alarmDuration: New duration to be assigned to the alarm being updated
     */
    public void updateAlarm(PreciseConnectivityAlarm alarm, long executionTime, long alarmDuration) {
        alarm.setExecuteTimeInMils(executionTime);
        alarm.setDuration(alarmDuration);
        alarmSqlHelper.updateAlarm(alarm.getAlarmId(), executionTime, alarmDuration);
        createAlarmJob(alarmSqlHelper.getAlarmById(alarm.getAlarmId()));
    }

    /**
     * Method that updates the alarm's connections
     *
     * @param alarmId:            Id of the alarm being updated
     * @param selectedConnection: Selected/unselected connection to be attributed to the alarm being updated
     * @param isActive:           State of the selected connection
     */
    public void updateAlarmConnection(int alarmId, Connection selectedConnection, boolean isActive) {
        alarmSqlHelper.updateAlarmConnection(alarmId, selectedConnection, isActive);
        createAlarmJob(alarmSqlHelper.getAlarmById(alarmId));
    }

    /**
     * Method that updates the alarm's days
     *
     * @param alarmId:     Id of the alarm being updated
     * @param selectedDay: Selected/unselected day to be attributed to the alarm being updated
     * @param isActive:    State of the selected day
     */
    public void updateAlarmDay(int alarmId, int selectedDay, boolean isActive) {
        alarmSqlHelper.updateAlarmDay(alarmId, selectedDay, isActive);
        createAlarmJob(alarmSqlHelper.getAlarmById(alarmId));
    }

    /**
     * Method that handles conflicts between alarms
     * This method checks whether its argument, a precise alarm is
     * in conflict with another alarm that already exists
     * A conflict between two alarms is when all these conditions are met:
     * - both alarms run in the same day (or more than one day)
     * - At least one connection model is handled by both alarms
     * - The period of "work" of one alarm intersect with that of the other
     *
     * @param alarm: Alarm we want to check if in conflict with other alarms
     * @return: Id of the conflicting alarm's id if a conflict is found, -1 otherwise
     */
    public int handleAlarmConflicts(PreciseConnectivityAlarm alarm, Connection connection) {
        if (alarmSqlHelper != null) {

            // Get a list of all precise active alarms from the database
            List<PreciseConnectivityAlarm> activeAlarms = alarmSqlHelper.readAllAlarms(null, null);

            /**
             * Loop on the elements of the alarm
             * The alarm passed on as an argument is compared with each element of the list
             * If a conflict is found, the conflicting alarm's Id is returned
             */
            for (PreciseConnectivityAlarm activeAlarm : activeAlarms) {

                // If the two alarms are in conflict, return the conflicting alarm's Id
                if (alarm.inConflictWithAlarm(activeAlarm, connection) != -1)
                    return activeAlarm.getAlarmId();
            }
        }

        // At this point there aren't any conflicts, -1 is returned
        return -1;
    }

    public void handleNotification() {
        AlarmNotificationManager.triggerNotification(mContext);
    }
}
