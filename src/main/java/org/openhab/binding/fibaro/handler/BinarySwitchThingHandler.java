/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fibaro.handler;

import static org.openhab.binding.fibaro.FibaroBindingConstants.SWITCH;

import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.fibaro.config.BinarySwitchConfiguration;
import org.openhab.binding.fibaro.config.FibaroBridgeConfiguration;
import org.openhab.binding.fibaro.internal.model.json.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BinarySwitchThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Johan Williams - Initial contribution
 */
public class BinarySwitchThingHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(BinarySwitchThingHandler.class);

    public BinarySwitchThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Add some more error handling here (if item has no bridge for example)
        FibaroBridgeHandler bridge = (FibaroBridgeHandler) getBridge().getHandler();
        FibaroBridgeConfiguration bridgeConfig = bridge.getConfigAs(FibaroBridgeConfiguration.class);
        BinarySwitchConfiguration config = getConfigAs(BinarySwitchConfiguration.class);

        if (channelUID.getId().equals(SWITCH)) {
            String baseUrl = "http://" + bridgeConfig.ipAddress + "/api/devices/";
            try {
                if (command.equals(OnOffType.ON)) {
                    ApiResponse apiResponse = bridge.callFibaroApi(HttpMethod.POST,
                            baseUrl + config.id + "/action/turnOn", "", ApiResponse.class);
                    logger.debug(apiResponse.toString());
                } else if (command.equals(OnOffType.OFF)) {
                    ApiResponse apiResponse = bridge.callFibaroApi(HttpMethod.POST,
                            baseUrl + config.id + "/action/turnOff", "", ApiResponse.class);
                    logger.debug(apiResponse.toString());

                }
            } catch (Exception e) {
                logger.debug("Failed to handle command SWITCH " + e.getMessage());
            }

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }

    }

    @Override
    public void initialize() {
        logger.debug("Initializing the binary switch handler");
        super.initialize();

        BinarySwitchConfiguration config = getConfigAs(BinarySwitchConfiguration.class);
        logger.debug("config id = {}", config.id);

        boolean validConfig = true;
        String errorMsg = null;

        if (config.id < 1) {
            errorMsg = BinarySwitchConfiguration.ID + "' must be larget than 0";
            validConfig = false;
        }
        // TODO: Call the fibaro API to verify that this id exists and the device is of correct type. This should
        // preferably be done in the refresh to simultaneously get the channel values

        if (validConfig) {
            // TODO: startAutomaticRefresh();
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        }
    }

}
