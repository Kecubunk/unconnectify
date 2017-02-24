package io.mdevlab.unconnectify.adapter;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.chauthai.swipereveallayout.SwipeRevealLayout;

import java.util.Calendar;

import io.mdevlab.unconnectify.MainActivity;
import io.mdevlab.unconnectify.R;
import io.mdevlab.unconnectify.alarm.AlarmManager;
import io.mdevlab.unconnectify.alarm.PreciseConnectivityAlarm;
import io.mdevlab.unconnectify.fragment.TimePickerFragment;
import io.mdevlab.unconnectify.utils.Connection;
import io.mdevlab.unconnectify.utils.Constants;
import io.mdevlab.unconnectify.utils.DateUtils;
import io.mdevlab.unconnectify.utils.DialogUtils;

/**
 * Created by mdevlab on 2/12/17.
 */

public class AlarmViewHolder extends RecyclerView.ViewHolder implements TimePickerDialog.OnTimeSetListener {

    private PreciseConnectivityAlarm mAlarm;
    private Context mContext;
    private int mPosition;

    private boolean hasChosenStartTime = true;

    SwipeRevealLayout mSwipeRevealLayout;

    View mSwitchedOffAlarmCover;
    View mSwitchOnOff;
    ToggleButton mSwitchOnOffToggle;
    ImageView mDeleteAlarm;

    View mContainer;

    TextView mStartTime;
    TextView mTimesSeparator;
    TextView mEndTime;

    ToggleButton mWifi;
    ToggleButton mHotspot;
    ToggleButton mBluetooth;

    ToggleButton mSunday;
    ToggleButton mMonday;
    ToggleButton mTuesday;
    ToggleButton mWednesday;
    ToggleButton mThursday;
    ToggleButton mFriday;
    ToggleButton mSaturday;

    public AlarmViewHolder(View itemView, final Context context) {
        super(itemView);
        mContext = context;

        mSwipeRevealLayout = (SwipeRevealLayout) itemView.findViewById(R.id.swipeRevealLayout);

        // Switched off alarm opaque cover
        mSwitchedOffAlarmCover = itemView.findViewById(R.id.switched_off_alarm);

        // Main view container
        mContainer = itemView.findViewById(R.id.container);

        // Switch alarm on/off
        mSwitchOnOff = itemView.findViewById(R.id.switch_alarm_on_off);
        mSwitchOnOffToggle = (ToggleButton) itemView.findViewById(R.id.switch_alarm_on_off_toggle);
        mSwitchOnOffToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mAlarm != null) {
                    /**
                     * If it's checked, it means "switch alarm on" is written
                     * in this case the alarm is off, which means the cover should be visible
                     *
                     * If on the other hand it's unchecked, "switch alarm off" is displayed
                     * In this case the alarm is on and thus the cover should be 'gone'
                     */
                    mSwitchedOffAlarmCover.setVisibility(isChecked ? View.VISIBLE : View.GONE);

                    // If the alarm view is masked, it shouldn't be clickable
                    if (mSwitchedOffAlarmCover.getVisibility() == View.GONE)
                        disableAlarmViewOnClick();

                    AlarmManager.getInstance(mContext).updateAlarmState(mAlarm, !isChecked);
                }
            }
        });

        // Delete the alarm, it's onClickListener is in the Adapter class
        mDeleteAlarm = (ImageView) itemView.findViewById(R.id.delete_alarm_button);

        // start time
        mStartTime = (TextView) itemView.findViewById(R.id.start_time);
        mStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hasChosenStartTime = true;
                showTimePicker();
            }
        });

        // Times separator
        mTimesSeparator = (TextView) itemView.findViewById(R.id.times_separator);

        // End time
        mEndTime = (TextView) itemView.findViewById(R.id.end_time);
        mEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hasChosenStartTime = false;
                showTimePicker();
            }
        });

        mWifi = (ToggleButton) itemView.findViewById(R.id.wifi);
        mWifi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mAlarm != null) {
                    AlarmManager.getInstance(mContext).updateAlarmConnection(mAlarm.getAlarmId(), Connection.WIFI, isChecked);
                }
            }
        });

        mHotspot = (ToggleButton) itemView.findViewById(R.id.hotspot);
        mHotspot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mAlarm != null) {
                    AlarmManager.getInstance(mContext).updateAlarmConnection(mAlarm.getAlarmId(), Connection.HOTSPOT, isChecked);
                } else {
                    mHotspot.setChecked(false);
                }
            }
        });

        mBluetooth = (ToggleButton) itemView.findViewById(R.id.bluetooth);
        mBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mAlarm != null) {
                    AlarmManager.getInstance(mContext).updateAlarmConnection(mAlarm.getAlarmId(), Connection.BLUETOOTH, isChecked);
                }
            }
        });

        mSunday = (ToggleButton) itemView.findViewById(R.id.sunday);
        mSunday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mAlarm != null) {
                    AlarmManager.getInstance(mContext).updateAlarmDay(mAlarm.getAlarmId(), Calendar.SUNDAY, isChecked);
                    changeOpacity(mSunday, isChecked);
                }
            }
        });

        mMonday = (ToggleButton) itemView.findViewById(R.id.monday);
        mMonday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mAlarm != null) {
                    AlarmManager.getInstance(mContext).updateAlarmDay(mAlarm.getAlarmId(), Calendar.MONDAY, isChecked);
                    changeOpacity(mMonday, isChecked);
                }
            }
        });

        mTuesday = (ToggleButton) itemView.findViewById(R.id.tuesday);
        mTuesday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mAlarm != null) {
                    AlarmManager.getInstance(mContext).updateAlarmDay(mAlarm.getAlarmId(), Calendar.TUESDAY, isChecked);
                    changeOpacity(mTuesday, isChecked);
                }
            }
        });

        mWednesday = (ToggleButton) itemView.findViewById(R.id.wednesday);
        mWednesday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mAlarm != null) {
                    AlarmManager.getInstance(mContext).updateAlarmDay(mAlarm.getAlarmId(), Calendar.WEDNESDAY, isChecked);
                    changeOpacity(mWednesday, isChecked);
                }
            }
        });

        mThursday = (ToggleButton) itemView.findViewById(R.id.thursday);
        mThursday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mAlarm != null) {
                    AlarmManager.getInstance(mContext).updateAlarmDay(mAlarm.getAlarmId(), Calendar.THURSDAY, isChecked);
                    changeOpacity(mThursday, isChecked);
                }
            }
        });

        mFriday = (ToggleButton) itemView.findViewById(R.id.friday);
        mFriday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mAlarm != null) {
                    AlarmManager.getInstance(mContext).updateAlarmDay(mAlarm.getAlarmId(), Calendar.FRIDAY, isChecked);
                    changeOpacity(mFriday, isChecked);
                }
            }
        });

        mSaturday = (ToggleButton) itemView.findViewById(R.id.saturday);
        mSaturday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mAlarm != null) {
                    AlarmManager.getInstance(mContext).updateAlarmDay(mAlarm.getAlarmId(), Calendar.SATURDAY, isChecked);
                    changeOpacity(mSaturday, isChecked);
                }
            }
        });
    }

    /**
     * Method that displays a time picker interface
     */
    private void showTimePicker() {
        TimePickerFragment timePickerFragment = new TimePickerFragment();

        /**
         * If the user has chosen to set the alarm's end time, a boolean indicating
         * this is sent to the time picker fragment. The alarm's position in the
         * recyclerView is also sent
         * - The boolean variable is used in order to add a "deactivate" button in the
         * time picker dialog
         * - The position of the alarm is used in order to disable the end time textView
         * if the user decides to deactivate the alarm
         */
        if (!hasChosenStartTime) {
            Bundle endTimeBundle = new Bundle();
            endTimeBundle.putBoolean(Constants.END_TIME_BUNDLE_KEY, true);
            endTimeBundle.putInt(Constants.ALARM_POSITION, mPosition);
            timePickerFragment.setArguments(endTimeBundle);
        }

        timePickerFragment.setListener(this);
        timePickerFragment.show(((MainActivity) mContext).getSupportFragmentManager(), "time picker");
    }

    /**
     * Method that's called once the time is set in the timePickerDialog
     *
     * @param view
     * @param hourOfDay
     * @param minute:   Chosen hour in the timePickerDialog
     */
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        /**
         * If 'minute' is between 0 and 9 it only has one digit,
         * so a '0' is added to the beginning of it
         */
        String s_minute = (0 <= minute && minute <= 9) ? "0" + minute : String.valueOf(minute);

        /**
         * By default, we suppose that the start time is being updated
         * So the textView to update once the time has been chosen by the user
         * is the startTime textView
         */
        TextView viewToUpdate = mStartTime;

        // New execution time
        long newExecutionTime = DateUtils.getLongFromTime(hourOfDay + ":" + s_minute);

        // Duration hasn't changed
        long newDuration = mAlarm.getDuration();

        // If the end time is being updated
        if (!hasChosenStartTime) {

            // Update end time and separator opacity
            enableEndTime();

            // The textView to update is the endTime
            viewToUpdate = mEndTime;

            /**
             * duration = endTime - executionTime
             * endTime's value is in newExecutionTime, so:
             * duration = newExecutionTime - executionTime
             */
            newDuration = newExecutionTime - mAlarm.getExecuteTimeInMils();

            // Execution time hasn't changed
            newExecutionTime = mAlarm.getExecuteTimeInMils();
        }

        // Update the UI
        viewToUpdate.setText(hourOfDay + ":" + s_minute);

        // Update the alarm in the database and the alarm's job
        AlarmManager.getInstance(mContext).updateAlarm(mAlarm, newExecutionTime, newDuration);
    }

    /**
     * Method that sets the opacity of a toggle button depending on a boolean
     * value 'isChecked', which disignates whether or not the toggle button is checked.
     *
     * @param toggleButton
     * @param isChecked
     */
    private void changeOpacity(View toggleButton, boolean isChecked) {
        float opacity = 0.5f;
        if (isChecked)
            opacity = 1f;
        toggleButton.setAlpha(opacity);
    }

    /**
     * Method that sets the current alarm being handled which is also related to the current
     * viewHolder object
     *
     * @param mAlarm
     */
    public void setAlarm(PreciseConnectivityAlarm mAlarm) {
        this.mAlarm = mAlarm;
    }

    /**
     * Method that updates an alarm's UI once the end time has been set
     * It basically makes the opacity of mEndTime and the separator 1
     */
    public void enableEndTime() {
        if (mEndTime != null) {
            mEndTime.setAlpha(1f);
            mTimesSeparator.setAlpha(1f);
        }
    }

    /**
     * Method that updates an alarm's UI once the end time has been deactivated
     * It also
     */
    public void disableEndTime() {
        if (mEndTime != null) {
            mEndTime.setAlpha(0.5f);
            mTimesSeparator.setAlpha(0.5f);

            if (mAlarm != null)
                AlarmManager.getInstance(mContext).updateAlarm(mAlarm,
                        mAlarm.getExecuteTimeInMils(),
                        0);
        }
    }

    /**
     * Method that sets the position of the current alarm
     *
     * @param mPosition
     */
    public void setPosition(int mPosition) {
        this.mPosition = mPosition;
    }

    /**
     * Method that disables any click events on an alarm view's elements
     * This is used when the alarm is set to off
     * The user will have to reset the alarm to 'on' in order to modify it
     */
    public void disableAlarmViewOnClick() {
        if (mSwitchedOffAlarmCover != null)
            mSwitchedOffAlarmCover.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });
    }
}
