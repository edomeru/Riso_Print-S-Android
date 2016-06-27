/*
 * Copyright (c) 2015 Ricoh Company, Ltd. All rights reserved.
 *
 * SnmpCommunityNameDisplayText.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.printersearchsettings;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartprint.R;

public class SnmpCommunityNameDisplayText extends LinearLayout {

    private String communityName = AppConstants.PREF_DEFAULT_SNMP_COMMUNITY_NAME;

    public SnmpCommunityNameDisplayText(Context context) {
        super(context);
        init(context);
    }

    public SnmpCommunityNameDisplayText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SnmpCommunityNameDisplayText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public SnmpCommunityNameDisplayText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        View view = View.inflate(context, R.layout.printersearchsettings_community_name_display, this);

        this.communityName = PreferenceManager.getDefaultSharedPreferences(context).getString(AppConstants.PREF_KEY_SNMP_COMMUNITY_NAME, AppConstants.PREF_DEFAULT_SNMP_COMMUNITY_NAME);

        TextView textView = (TextView) view.findViewById(R.id.snmpCommunityName);
        textView.setText(this.communityName);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);

        savedState.communityName = this.communityName;

        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if(!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState)state;
        super.onRestoreInstanceState(savedState.getSuperState());

        this.communityName = savedState.communityName;
    }

    static class SavedState extends BaseSavedState {
        String communityName;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.communityName = in.readString();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(this.communityName);
        }

        //required field that makes Parcelables from a Parcel
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
