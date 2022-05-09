/*
 * Copyright (c) 2015 Ricoh Company, Ltd. All rights reserved.
 *
 * SnmpCommunityNameDisplayText.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.printersearchsettings

import android.content.Context
import android.widget.LinearLayout
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartprint.R
import android.widget.TextView
import android.os.Parcelable
import android.os.Parcel
import android.os.Parcelable.Creator
import android.util.AttributeSet
import androidx.preference.PreferenceManager

class SnmpCommunityNameDisplayText : LinearLayout {
    private var _communityName: String? = AppConstants.PREF_DEFAULT_SNMP_COMMUNITY_NAME

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        val view = inflate(context, R.layout.printersearchsettings_community_name_display, this)
        _communityName = PreferenceManager.getDefaultSharedPreferences(context).getString(
            AppConstants.PREF_KEY_SNMP_COMMUNITY_NAME,
            AppConstants.PREF_DEFAULT_SNMP_COMMUNITY_NAME
        )
        val textView = view.findViewById<TextView>(R.id.snmpCommunityName)
        textView.text = _communityName
    }

    public override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.communityName = _communityName
        return savedState
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        _communityName = state.communityName
    }

    internal class SavedState : BaseSavedState {
        var communityName: String? = null

        constructor(superState: Parcelable?) : super(superState)
        private constructor(`in`: Parcel) : super(`in`) {
            communityName = `in`.readString()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeString(communityName)
        }

        companion object {
            //required field that makes Parcelables from a Parcel
            @JvmField
            val CREATOR: Creator<SavedState?> = object : Creator<SavedState?> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}