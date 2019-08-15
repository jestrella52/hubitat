/*
	SECURIFI KEY FOB - https://community.hubitat.com/t/securifi-key-fob
	Kevin Tierney
	
	manufacturer : Sercomm Corp.
	idAsInt : 8
	inClusters : 0000,0003,0500
	endpointId : 08
	profileId : 0104
	application : 33
	outClusters : 0003,0501
	initialized : true
	model : SZ-KFB01
	stage : 4
 	
	

	*/

metadata {
    definition (name: "Securifi Key Fob", namespace: "hubitat", author: "Kevin Tierney") {
        capability "PushableButton"
        capability "Configuration"
        fingerprint profileId: "0104", inClusters: "0000,0003,0500", outClusters: "0003,0501", manufacturer: "Sercomm Corp.", model: "SZ-KFB01", deviceJoinName: "Securifi Key Fob"
    }
preferences {
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
        input name: "txtEnable", type: "bool", title: "Enable descriptionText logging", defaultValue: true
    }
}

def logsOff(){
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable",[value:"false",type:"bool"])
}

def updated(){
    log.info "Updated"
    log.warn "Description logging is: ${txtEnable == true}"
    if (logEnable) runIn(1800,logsOff)
}

def parse(String description) {
    
	if (description?.startsWith('enroll request')) {
        log.debug "Enroll Request: ${description}"
       // List cmds = enrollResponse()
       // log.debug "enroll response: ${cmds}"
        //def result = cmds?.collect { new hubitat.device.HubAction(it) }
       // log.debug "res: ${result}"
       // return result    
    } else if (description?.startsWith('catchall:')) {
        def msg = zigbee.parse(description)
        log.debug "Catchall: ${msg}"
        //buttonPush(msg.data[0])
    } else {
        log.debug "parse description: ${description}"
    }    
    

    
}




def configure() {
    log.info "Configuring"
    runIn(1800,logsOff)
    def cmds = [
		//Taken from ST Driver
		"zcl global write 0x500 0x10 0xf0 {${device.zigbeeId}}", "delay 200",
		"send 0x${device.deviceNetworkId} ${endpointId} 1", "delay 1500",
		"zdo bind 0x${device.deviceNetworkId} ${endpointId} 0x01 0x0501 {${device.zigbeeId}} {}", "delay 500",
		"zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 1 {${device.zigbeeId}} {}"

    ]
    return cmds
}

def enrollResponse() {
    log.debug "Sending enroll response"
    [            
    "raw 0x500 {01 23 00 00 00}", "delay 200",
    "send 0x${device.deviceNetworkId} ${endpointId} 1"        
    ]
}

def installed(){}



