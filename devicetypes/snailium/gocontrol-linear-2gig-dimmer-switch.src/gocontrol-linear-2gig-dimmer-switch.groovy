/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 ****
 * Added functionality and controls to allow setting scenes to be controlled by zwave scene controllers.
 * The scene ID and Level are entered in to the preferences. The scene is then set by sending the command a standard tile.
 */
metadata {
	definition (name: "GoControl/Linear/2gig Dimmer Switch", namespace: "snailium", author: "snailium") {
		capability "Switch Level"
		capability "Actuator"
		capability "Indicator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Health Check"
        
 // *************************Scene definition***********************************************************      
        command "configScene"
        command "reportScene"
 

    attribute "dataScene", "string"
    attribute "setScene", "enum", ["Set_Scene", "Setting_Scene"]
        
        
// *************************************************************************************        
  
		//zw:L type:1104 mfr:014F prod:4457 model:3034 ver:5.41 zwv:3.42 lib:06 cc:26,2B,2C,27,73,70,86,72
		fingerprint mfr:"014F", prod:"4457", model:"3034", deviceJoinName: "WD500Z Z-Wave Wall Dimmer"  // http://www.pepper1.net/zwavedb/device/482, http://products.z-wavealliance.org/products/1032
        fingerprint mfr:"014F", prod:"4457", model:"3331", deviceJoinName: "WD1000Z Z-Wave Wall Dimmer" // http://www.pepper1.net/zwavedb/device/483
	}

	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"
		status "09%": "command: 2003, payload: 09"
		status "10%": "command: 2003, payload: 0A"
		status "33%": "command: 2003, payload: 21"
		status "66%": "command: 2003, payload: 42"
		status "99%": "command: 2003, payload: 63"

		// reply messages
		reply "2001FF,delay 5000,2602": "command: 2603, payload: FF"
		reply "200100,delay 5000,2602": "command: 2603, payload: 00"
		reply "200119,delay 5000,2602": "command: 2603, payload: 19"
		reply "200132,delay 5000,2602": "command: 2603, payload: 32"
		reply "20014B,delay 5000,2602": "command: 2603, payload: 4B"
		reply "200163,delay 5000,2602": "command: 2603, payload: 63"
	}

	preferences {
    	input "dimStartLevel",    "enum", title: "Dim Start Level", description: "Start dimming from...", required: false, options:["preset": "Device's preset value", "current": "Current level"], defaultValue: "current"
		input "ledIndicator",     "enum", title: "LED Indicator", description: "Turn LED indicator... ", required: false, options:["on": "When On", "off": "When Off"], defaultValue: "off"
        input "switchInversion",  "bool", title: "Invert Switch", description: "Change the top of the switch to OFF and the bottom to ON", required: false, defaultValue: false
        input "groupTwoEnable",   "bool", title: "Enable Shade Group 2", required: false, defaultValue: false
        input "groupThreeEnable", "bool", title: "Enable Shade Group 3", required: false, defaultValue: false
        input "ledFlicker",       "enum", title: "LED Transmission Indication", description:"Flicker LED when...", required: false, options:["none": "Not flicker", "entire": "Flicker entire time of transmitting", "second": "Flicker for only 1 second"], defaultValue: "second"
        
        //input "isAssociate",       "bool", title: "Associate to another Z-Wave device", required: false, defaultValue: false
		//input "associateTo",       "string", title: "The master device ID that this dimmer is associated to", required: false
		//input "associateBehavior", "enum", title: "Master device behavior to trigger this dimmer", required: false, options:["doubleTap": "Double tap master device to trigger this dimmer", "tripleTap": "Tap master device three times to trigger this dimmer"], defaultValue: "doubleTap"

/****************************Scene Program inputs - put in preferences******************************************************/

		input "sceneNum", "number", title: "Scene Id to add (0-255)", required: false
        input "sceneLevel", "number", title: "Scene Brightness (0-100) (0 zero will disable the scene)", required: false


/******************************************************************************************************************************/

}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
    			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
		}
        
		standardTile("indicator", "device.indicatorStatus", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
			state "when off", action:"indicator.indicatorWhenOn", icon:"st.indicators.lit-when-off"
			state "when on", action:"indicator.indicatorNever", icon:"st.indicators.lit-when-on"
		}



        
/****************************Scene Program Controls - put in tiles*******************************************************/

        standardTile("dataScene", "device.dataScene", width: 3, height: 1, inactiveLabel: false, decoration: "flat") {
        	state "default", label: '${currentValue}', action:"reportScene"        	

    		}
       standardTile("setScene", "device.setScene", width: 2, height: 1, inactiveLabel: false, decoration: "flat") {
        	state "Set_Scene", label: '${name}', action:"configScene", nextState: "Setting_Scene"      	
			state "Setting_Scene", label: '${name}' //, nextState: "Set_Scene"
    		}
        
       //add  "dataScene" and "setScene" to the details([]) line.
/******************************************************************************************************************************/

		standardTile("refresh", "device.switch", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
		valueTile("level", "device.level", width:2, height: 1, inactiveLabel: false, decoration: "flat") {
			state "level", label:'Current level:\n${currentValue} %', unit:"%", backgroundColor:"#ffffff"
		}  

		main(["switch"])
		details(["switch", "level", "indicator", "refresh","setScene", "dataScene"])
        

	}
}

def updated(){

	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    
    switch (dimStartLevel) {
    	case "preset":
        	setDimStart(false)
            break
        case "current":
        	setDimStart(true)
            break
    }
    
	switch (ledIndicator) {
        case "on":
            indicatorWhenOn()
            break
        case "off":
            indicatorWhenOff()
            break
        case "never":
            indicatorNever()
            break
        default:
            indicatorWhenOn()
            break
    }
    
    invertSwitch(switchInversion)
    
    setGroupEnable(groupTwoEnable, groupThreeEnable)
    
    switch (ledFlicker) {
    	case "none":
        	setLedFlicker(false)
            break
        case "entire":
        	setLedFlicker(true, true)
            break
        case "second":
        	setLedFlicker(true, false)
            break
    }
    
    if(isAssociate) {
		switch(associateBehavior) {
			case "doubleTap":
				setAssociation(associateTo, 2)
                break
			case "tripleTap":
				setAssociation(associateTo, 3)
                break
		}
	} else {
    	removeAssociation()
    }

}

def parse(String description) {
	log.debug "parse from linear wd500 >> zwave.parse($description)"
	def result = null
	if (description != "updated") {
		log.debug "parse() >> zwave.parse($description)"
		def cmd = zwave.parse(description, [0x20: 1, 0x26: 1, 0x70: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	if (result?.name == 'hail' && hubFirmwareLessThan("000.011.00602")) {
		result = [result, response(zwave.basicV1.basicGet())]
		log.debug "Was hailed: requesting state update"
	} else {
		log.debug "Parse returned ${result?.descriptionText}"
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd) {
	dimmerEvents(cmd)
}

private dimmerEvents(physicalgraph.zwave.Command cmd) {
	def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value)]
	if (cmd.value && cmd.value <= 100) {
		result << createEvent(name: "level", value: cmd.value, unit: "%")
	}
	return result
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	log.debug "ConfigurationReport $cmd"
	def value = "when off"
	if (cmd.configurationValue[0] == 1) {value = "when on"}
	if (cmd.configurationValue[0] == 2) {value = "never"}
	createEvent([name: "indicatorStatus", value: value])
}

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
	createEvent([name: "hail", value: "hail", descriptionText: "Switch button was pressed", displayed: false])
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
	log.debug "manufacturerName: ${cmd.manufacturerName}"
	log.debug "productId:        ${cmd.productId}"
	log.debug "productTypeId:    ${cmd.productTypeId}"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)
	updateDataValue("manufacturer", cmd.manufacturerName)
	createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd) {
	[createEvent(name:"switch", value:"on"), response(zwave.switchMultilevelV1.switchMultilevelGet().format())]
}

/****************************Scene Program Controls - parse handling******************************************************/
def zwaveEvent(physicalgraph.zwave.commands.sceneactuatorconfv1.SceneActuatorConfReport cmd) {
	log.debug "SceneActuatorConfReport $cmd"
	def Scene = cmd.sceneId
    def Level = cmd.level
	createEvent([name: "dataScene", value: "SceneId: $Scene, level: $Level"])
    }
/******************************************************************************************************************************/



def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
    def linkText = device.label ?: device.name
    [linkText: linkText, descriptionText: "$linkText: $cmd", displayed: false]
	[:]
}

def on() {
	delayBetween([
			zwave.basicV1.basicSet(value: 0xFF).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],5000)
}

def off() {
	delayBetween([
			zwave.basicV1.basicSet(value: 0x00).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],5000)
}

def setLevel(value) {
	log.debug "setLevel >> value: $value"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	if (level > 0) {
		sendEvent(name: "switch", value: "on")
	} else {
		sendEvent(name: "switch", value: "off")
	}
	sendEvent(name: "level", value: level, unit: "%")
	delayBetween ([zwave.basicV1.basicSet(value: level).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
}

def setLevel(value, duration) {
	log.debug "setLevel >> value: $value, duration: $duration"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
	def getStatusDelay = duration < 128 ? (duration*1000)+2000 : (Math.round(duration / 60)*60*1000)+2000
	delayBetween ([zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration).format(),
				   zwave.switchMultilevelV1.switchMultilevelGet().format()], getStatusDelay)
}

def poll() {
	zwave.switchMultilevelV1.switchMultilevelGet().format()
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	refresh()
}

def refresh() {
	log.debug "refresh() is called"
	def commands = []
	commands << zwave.switchMultilevelV1.switchMultilevelGet().format()
	if (getDataValue("MSR") == null) {
		commands << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	}
	delayBetween(commands,100)
}

void indicatorWhenOn() {
	sendEvent(name: "indicatorStatus", value: "when on", displayed: false)
	sendHubCommand(new physicalgraph.device.HubAction(zwave.configurationV1.configurationSet(parameterNumber: 3, configurationValue: [0], size: 1).format()))
}

void indicatorWhenOff() {
	sendEvent(name: "indicatorStatus", value: "when off", displayed: false)
	sendHubCommand(new physicalgraph.device.HubAction(zwave.configurationV1.configurationSet(parameterNumber: 3, configurationValue: [1], size: 1).format()))
}

void indicatorNever() {
	sendEvent(name: "indicatorStatus", value: "never", displayed: false)
	sendHubCommand(new physicalgraph.device.HubAction(zwave.configurationV1.configurationSet(parameterNumber: 3, configurationValue: [2], size: 1).format()))
}

def invertSwitch(invert=true) {
    def devName = device.label ?: device.name
	def configVal = invert ? 1 : 0
	def cmd = zwave.configurationV1.configurationSet(parameterNumber: 4, configurationValue: [configVal], size: 1).format()
	log.trace "$devName: invertSwitch($invert): $cmd"
    return cmd
}

def setDimStart(current=true) {
    def devName = device.label ?: device.name
	def configVal = current ? 1 : 0
	def cmd = zwave.configurationV1.configurationSet(parameterNumber: 1, configurationValue: [configVal], size: 1).format()
	log.trace "$devName: setDimStart($current): $cmd"
    return cmd
}

def setGroupEnable(groupTwoEnable=false, groupThreeEnable=false) {
    def devName = device.label ?: device.name
	def grp2en  = groupTwoEnable ? 1 : 0
    def grp3en  = groupThreeEnable ? 1 : 0
	def cmds    = []
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 14, configurationValue: [grp2en], size: 1).format()
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 15, configurationValue: [grp3en], size: 1).format()
	log.trace "$devName: setGroupEnable($groupTwoEnable, $groupThreeEnable): $cmds"
    delayBetween(cmds, 100)
}

def setLedFlicker(enable=true, entire=false) {
	invertSwitch
}

/**************************** Scene Program Controls - Commands  ******************************************************/

def configScene() {
	delayBetween([
    zwave.sceneActuatorConfV1.sceneActuatorConfSet(sceneId:sceneNum, level:sceneLevel, dimmingDuration:0xFF, override:1).format(),
    zwave.sceneActuatorConfV1.sceneActuatorConfGet(sceneId:sceneNum).format(),
    sendEvent([name: "setScene", value: "Set_Scene"])
	], 1000)
}

def reportScene() {
	delayBetween([
    zwave.sceneActuatorConfV1.sceneActuatorConfGet(sceneId:sceneNum).format()
	], 1000)
}


/******************************************************************************************************************************/

def setAssociation(devId, groupId) {
    def devName = device.label ?: device.name
    def nodeId  = []
    def cmds    = []
    
    nodeId << Integer.parseInt(devId, 16)
    cmds << zwave.associationV1.associationRemove(groupingIdentifier:groupId).format()
    cmds << zwave.associationV1.associationSet(groupingIdentifier:groupId, nodeId:nodeId).format()
    cmds << zwave.associationV1.associationGet(groupingIdentifier:groupId).format()
    cmds << zwave.associationV1.associationGroupingsGet().format() 

	log.trace "$devName: setAssociation(${devId}=${nodeId}, $groupId): $cmds"
    delayBetween(cmds, 100)
}

def removeAssociation() {
    def devName = device.label ?: device.name
    def cmds    = []
    
    cmds << zwave.associationV1.associationRemove(groupingIdentifier:2).format()
    cmds << zwave.associationV1.associationRemove(groupingIdentifier:3).format()

	log.trace "$devName: removeAssociation(): $cmds"
    delayBetween(cmds, 100)
}
