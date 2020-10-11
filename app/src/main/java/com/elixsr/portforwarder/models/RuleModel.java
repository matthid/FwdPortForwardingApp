/*
 * Fwd: the port forwarding app
 * Copyright (C) 2016  Elixsr Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.elixsr.portforwarder.models;

import android.util.Log;

import java.io.Serializable;
import java.net.InetSocketAddress;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;

import com.elixsr.portforwarder.util.RuleHelper;

/**
 * The {@link RuleModel} class represents a Forwarding Rule.
 *
 * @author Niall McShane
 */
public class RuleModel implements Serializable {

    private static final String TAG = "RuleModel";

    @Expose(serialize = false, deserialize = false)
    private long id;

    @Expose
    private boolean isTcp;

    @Expose
    private boolean isUdp;

    @Expose
    private String name;

    //TODO: create a class? - worth the effort?
    private String fromInterfaceName;

    @Expose
    private int fromPortMin;
    @Expose
    private int fromPortMax;

    private boolean isEnabled = true;

    @Expose
    private int targetPortMin;

    @Expose
    private String targetIp;

    // Null constructor - for object building
    public RuleModel() {

    }

    public RuleModel(boolean isTcp, boolean isUdp, String name, String fromInterfaceName, int fromPortMin, int fromPortMax, String targetIp, int targetPortMin) {
        this.isTcp = isTcp;
        this.isUdp = isUdp;
        this.name = name;
        this.fromInterfaceName = fromInterfaceName;
        this.fromPortMin = fromPortMin;
        this.fromPortMax = fromPortMax;
        this.targetIp = targetIp;
        this.targetPortMin = targetPortMin;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isTcp() {
        return isTcp;
    }

    public void setIsTcp(boolean isTcp) {
        this.isTcp = isTcp;
    }

    public boolean isUdp() {
        return isUdp;
    }

    public void setIsUdp(boolean isUdp) {
        this.isUdp = isUdp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFromInterfaceName() {
        return fromInterfaceName;
    }

    public void setFromInterfaceName(String fromInterfaceName) {
        this.fromInterfaceName = fromInterfaceName;
    }

    public int getFromPortMin() {
        return fromPortMin;
    }

    public void setFromPortMin(int fromPort) {
        this.fromPortMin = fromPort;
    }

    public int getFromPortMax() {
        return fromPortMax;
    }

    public void setFromPortMax(int fromPort) {
        this.fromPortMax = fromPort;
    }

    public InetSocketAddress getTarget(int portOffset) {
        return new InetSocketAddress(targetIp, targetPortMin + portOffset);
    }

    public void setTargetIp(String targetIp) {
        this.targetIp = targetIp;
    }
    public void setTargetPortMin(int targetPortMin) {
        this.targetPortMin = targetPortMin;
    }

    public String protocolToString() {
        return RuleHelper.getRuleProtocolFromModel(this);
    }

    /**
     * Return a string of the target IPv4 address
     *
     * @return the IPv4 address as a String
     */
    public String getTargetIp() {
        return this.targetIp;
    }

    /**
     * Return the target port as an integer
     *
     * @return the target port integer.
     */
    public int getTargetPortMin() {
        return this.targetPortMin;
    }

    /**
     * Validate all data held within the model.
     * <p/>
     * Validation rules: <ul> <li>Name should not be null & greater than 0 characters</li>
     * <li>Either TCP or UDP should be true</li> <li>From Interface should not be null & greater
     * than 0 characters</li> <li>From port should be greater than minimum port and smaller than
     * max</li> <li>Target port should be greater than minimum port and smaller than max </li
     * <li>Target IP address should not be null & greater than 0 characters</li> </ul>
     *
     * @return true if valid, false if not valid.
     */
    public boolean isValid() {

        // Ensure the rule has a name
        if (name == null || name.length() <= 0) {
            return false;
        }

        // It must either be one or the other, or even both
        if (!isTcp && !isUdp) {
            return false;
        }

        if (fromInterfaceName == null || fromInterfaceName.length() <= 0) {
            return false;
        }

        if (fromPortMin < RuleHelper.MIN_PORT_VALUE || fromPortMin > RuleHelper.MAX_PORT_VALUE) {
            return false;
        }

        if (fromPortMax != 0 && (fromPortMin > fromPortMax || fromPortMax > RuleHelper.MAX_PORT_VALUE)) {
            return false;
        }

        try {
            // Ensure that the value is greater than the minimum, and smaller than max
            if (getTargetPortMin() <= 0 || getTargetPortMin() < RuleHelper.TARGET_MIN_PORT || getTargetPortMin() > RuleHelper.MAX_PORT_VALUE) {
                return false;
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "Target object was null.", e);
            return false;
        }


        // The new rule activity should take care of IP address validation
        if (getTargetIp() == null || name.length() <= 0) {
            return false;
        }

        return true;

    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
