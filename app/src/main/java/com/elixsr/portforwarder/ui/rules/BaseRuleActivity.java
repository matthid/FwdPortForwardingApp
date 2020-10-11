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

package com.elixsr.portforwarder.ui.rules;

import android.content.Intent;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.List;

import com.elixsr.portforwarder.R;
import com.elixsr.portforwarder.exceptions.RuleValidationException;
import com.elixsr.portforwarder.models.RuleModel;
import com.elixsr.portforwarder.ui.BaseActivity;
import com.elixsr.portforwarder.ui.MainActivity;
import com.elixsr.portforwarder.util.InterfaceHelper;
import com.elixsr.portforwarder.util.NetworkHelper;
import com.elixsr.portforwarder.validators.RuleModelValidator;

/**
 * The BaseRuleActivity class  provides logic for subclasses which utilise the shared Rule detail
 * layout.
 * <p>
 * This class provides functionality to set up the core GUI components, and provides shared logic
 * for the validation of user input.
 * <p>
 * This class also provides a function to generate a {@link String} {@link List} of Network
 * Interfaces available on the device.
 *
 * @author Niall McShane
 */
public abstract class BaseRuleActivity extends BaseActivity {

    protected static final String ACTION_SAVE = "Save";
    protected static final String CATEGORY_RULES = "Rules";

    private static final String TAG = "BaseRuleActivity";

    protected Spinner protocolSpinner;
    protected Spinner fromInterfaceSpinner;
    protected ArrayAdapter<String> fromSpinnerAdapter;
    protected ArrayAdapter<CharSequence> protocolAdapter;

    private static final String INVALID_PORT_ERROR_MESSAGE = "Please enter a value greater than or equal to %s and less than or equal to %s";


    /**
     * Generate a user interface for all shared activities that use the {@link com.elixsr
     * .portforwarder.R.layout.rule_detail_view} layout.
     * <p>
     * This will pre-populate the {@link Spinner} Objects.
     */
    protected void constructDetailUi() {

        // Set up protocol spinner/dropdown
        protocolSpinner = (Spinner) findViewById(R.id.protocol_spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        protocolAdapter = ArrayAdapter.createFromResource(this,
                R.array.rule_protocol_array, R.layout.my_spinner);

        // Specify the layout to use when the list of choices appears
        protocolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the protocolAdapter to the spinner
        protocolSpinner.setAdapter(protocolAdapter);

        // Generate interfaces
        List<String> interfaces = null;
        try {
            interfaces = generateInterfaceList();


        } catch (SocketException e) {
            Log.i(TAG, "Error generating Interface list", e);

            // Show toast and move to main screen
            Toast.makeText(this, "Problem locating network interfaces. Please refer to 'help' to " +
                            "assist with troubleshooting.",
                    Toast.LENGTH_LONG).show();
            Intent mainActivityIntent = new Intent(this, MainActivity.class);
            startActivity(mainActivityIntent);
            return;
        }

        // Check to ensure we have some interface to show!
        if (interfaces == null || interfaces.isEmpty()) {
            Toast.makeText(this, "Could not locate any network interfaces. Please refer to 'help'" +
                            " to assist with troubleshooting.",
                    Toast.LENGTH_LONG).show();
            Intent mainActivityIntent = new Intent(this, MainActivity.class);
            startActivity(mainActivityIntent);
            return;
        }


        // Set up protocol spinner/dropdown
        fromInterfaceSpinner = (Spinner) findViewById(R.id.from_interface_spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        fromSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.my_spinner, interfaces);

        // Specify the layout to use when the list of choices appears
        fromSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the protocolAdapter to the spinner
        fromInterfaceSpinner.setAdapter(fromSpinnerAdapter);

    }

    /**
     * Returns a list of all Network interfaces located on the device.
     *
     * @return a String list containing the name of the network interfaces on the device.
     * @throws SocketException
     */
    public List<String> generateInterfaceList() throws SocketException {

        return InterfaceHelper.generateInterfaceNamesList();
    }

    /**
     * Constructs a {@link RuleModel} object based on the data held inside the shared layout.
     * <p>
     * This method will also check to ensure that all inputs are valid, and will show the user an
     * error message if the user has not entered valid data.
     *
     * @return a {@link RuleModel} object as a result of the users input.
     */
    public RuleModel generateNewRule(long ruleModelId) {

        // Create the blank rule object
        RuleModel ruleModel = new RuleModel();
        ruleModel.setId(ruleModelId);

        /*
            Protocol
         */
        Spinner protocolSpinner = (Spinner) findViewById(R.id.protocol_spinner);
        String selectedProtocol = protocolSpinner.getSelectedItem().toString();

        // Determine the protocol
        switch (selectedProtocol) {
            case NetworkHelper.TCP:
                ruleModel.setIsTcp(true);
                break;
            case NetworkHelper.UDP:
                ruleModel.setIsUdp(true);
                break;
            // If BOTH, or default - same thing I assume
            case NetworkHelper.BOTH:
            default:
                ruleModel.setIsTcp(true);
                ruleModel.setIsUdp(true);
                break;
        }

        /*
            Rule Name
         */
        TextInputEditText ruleNameText = (TextInputEditText) findViewById(R.id.new_rule_name);
        TextInputLayout ruleNameTextInputLayout = (TextInputLayout) findViewById(R.id.new_rule_name_input_layout);

        try {
            if (RuleModelValidator.validateRuleName(ruleNameText.getText().toString())) {
                // If everything is correct, set the name
                ruleModel.setName(ruleNameText.getText().toString());
                ruleNameTextInputLayout.setErrorEnabled(false);
            }
        } catch (RuleValidationException e) {
            ruleNameTextInputLayout.setErrorEnabled(true);
            // Alternate error style above line
            // ruleNameText.setError(e.getMessage());
            ruleNameTextInputLayout.setError(getString(R.string.text_input_error_enter_name_text));
            Log.w(TAG, "No rule name was included");
        }

        /*
            From port
         */
        TextInputEditText fromPortMinText = (TextInputEditText) findViewById(R.id.new_rule_from_port_min);

        // Validate the input, and show error message if wrong
        try {
            if (RuleModelValidator.validateRuleFromPort(fromPortMinText.getText().toString())) {
                ruleModel.setFromPortMin(Integer.valueOf(fromPortMinText.getText().toString()));
            }
        } catch (RuleValidationException e) {
            fromPortMinText.setError(e.getMessage());
        }

        TextInputEditText fromPortMaxText = (TextInputEditText) findViewById(R.id.new_rule_from_port_max);
        if (fromPortMaxText == null || fromPortMaxText.length() <= 0 || "0".equals(fromPortMaxText.getText())) {
            ruleModel.setFromPortMax(0);
        } else {
            try {
                if (RuleModelValidator.validateRuleFromPort(fromPortMaxText.getText().toString())) {
                    int maxPort = Integer.valueOf(fromPortMaxText.getText().toString());
                    if (maxPort < ruleModel.getFromPortMin()){
                        fromPortMaxText.setError("maxport should be greater or equal to minport");
                    } else {
                        ruleModel.setFromPortMax(maxPort);
                    }
                }
            } catch (RuleValidationException e) {
                fromPortMaxText.setError(e.getMessage());
            }
        }

        /*
            Target
         */
        String targetIpAddress = null;
        int targetPort = 0;

        /*
            Target IP Address
         */
        TextInputEditText targetIpAddressText = (TextInputEditText) findViewById(R.id.new_rule_target_ip_address);

        // Validate the input, and show error message if wrong
        try {
            if (RuleModelValidator.validateRuleTargetIpAddress(targetIpAddressText.getText().toString())) {
                targetIpAddress = targetIpAddressText.getText().toString();
            }
        } catch (RuleValidationException e) {
            targetIpAddressText.setError(e.getMessage());
        }

        /*
            Target port
         */
        TextInputEditText targetPortText = (TextInputEditText) findViewById(R.id.new_rule_target_port_min);

        // Validate the input, and show error message if wrong
        try {
            if (RuleModelValidator.validateRuleTargetPort(targetPortText.getText().toString())) {
                targetPort = Integer.valueOf(targetPortText.getText().toString());
            }
        } catch (RuleValidationException e) {
            targetPortText.setError(e.getMessage());
        }

        if (targetIpAddress != null && targetIpAddress.length() > 0 && targetPort >= 0) {
            // Create a InetSocketAddress object using data
            //InetSocketAddress target = new InetSocketAddress(targetIpAddress, targetPort);
            ruleModel.setTargetIp(targetIpAddress);
            ruleModel.setTargetPortMin(targetPort);
        } else {
            Log.w(TAG, "Could not create Target InetSocketAddress Object");
        }

        Spinner fromInterfaceSpinner = (Spinner) findViewById(R.id.from_interface_spinner);
        String selectedFromInterface = fromInterfaceSpinner.getSelectedItem().toString();
        ruleModel.setFromInterfaceName(selectedFromInterface);

        return ruleModel;
    }


}
