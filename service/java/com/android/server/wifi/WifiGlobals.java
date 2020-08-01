/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.server.wifi;

import android.content.Context;

import com.android.wifi.resources.R;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.ThreadSafe;


/** Global wifi service in-memory state that is not persisted. */
@ThreadSafe
public class WifiGlobals {

    /**
     * Maximum allowable interval in milliseconds between polling for RSSI and linkspeed
     * information. This is also used as the polling interval for WifiTrafficPoller, which updates
     * its data activity on every CMD_RSSI_POLL.
     */
    private static final int MAXIMUM_POLL_RSSI_INTERVAL_MSECS = 6000;

    private final Context mContext;

    private final AtomicInteger mPollRssiIntervalMillis = new AtomicInteger(-1);
    private final AtomicBoolean mIpReachabilityDisconnectEnabled = new AtomicBoolean(true);
    private final AtomicBoolean mIsBluetoothConnected = new AtomicBoolean(false);

    public WifiGlobals(Context context) {
        mContext = context;
    }

    /** Get the interval between RSSI polls, in milliseconds. */
    public int getPollRssiIntervalMillis() {
        int interval = mPollRssiIntervalMillis.get();
        if (interval > 0) {
            return interval;
        } else {
            return Math.min(
                    mContext.getResources()
                            .getInteger(R.integer.config_wifiPollRssiIntervalMilliseconds),
                    MAXIMUM_POLL_RSSI_INTERVAL_MSECS);
        }
    }

    /** Set the interval between RSSI polls, in milliseconds. */
    public void setPollRssiIntervalMillis(int newPollIntervalMillis) {
        mPollRssiIntervalMillis.set(newPollIntervalMillis);
    }

    /** Returns whether CMD_IP_REACHABILITY_LOST events should trigger disconnects. */
    public boolean getIpReachabilityDisconnectEnabled() {
        return mIpReachabilityDisconnectEnabled.get();
    }

    /** Sets whether CMD_IP_REACHABILITY_LOST events should trigger disconnects. */
    public void setIpReachabilityDisconnectEnabled(boolean enabled) {
        mIpReachabilityDisconnectEnabled.set(enabled);
    }

    /** Set whether bluetooth is enabled. */
    public void setBluetoothEnabled(boolean isEnabled) {
        // If BT was connected and then turned off, there is no CONNECTION_STATE_CHANGE message.
        // So set mIsBluetoothConnected to false if we get a bluetooth disable while connected.
        // But otherwise, Bluetooth being turned on doesn't mean that we're connected.
        if (!isEnabled) {
            mIsBluetoothConnected.set(false);
        }
    }

    /** Set whether bluetooth is connected. */
    public void setBluetoothConnected(boolean isConnected) {
        mIsBluetoothConnected.set(isConnected);
    }

    /** Get whether bluetooth is connected */
    public boolean isBluetoothConnected() {
        return mIsBluetoothConnected.get();
    }

    /**
     * Helper method to check if Connected MAC Randomization is supported - onDown events are
     * skipped if this feature is enabled (b/72459123).
     *
     * @return boolean true if Connected MAC randomization is supported, false otherwise
     */
    public boolean isConnectedMacRandomizationEnabled() {
        return mContext.getResources().getBoolean(
                R.bool.config_wifi_connected_mac_randomization_supported);
    }
}