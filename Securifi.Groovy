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
 	
	dev:1882019-08-14 09:26:22.263 pm debugCatchall: SmartShield(text: null, manufacturerId: 0x0000, direction: 0x00, data: [0xc9, 0xfe, 0xa6, 0x9b, 0xe6, 0x4f, 0x04, 0x00, 0x6f, 0x0d, 0x00, 0x00], number: null, isManufacturerSpecific: false, messageType: 0x00, senderShortId: 0xa6fe, isClusterSpecific: false, sourceEndpoint: 0x00, profileId: 0x0000, command: 0x00, clusterId: 0x0013, destinationEndpoint: 0x00, options: 0x0040)
dev:1882019-08-14 09:26:14.404 pm debugEnroll Request: enroll request endpoint 0x08 : data 0x0115
dev:1882019-08-14 09:26:12.904 pm debugEnroll Request: enroll request endpoint 0x08 : data 0x0115
dev:1882019-08-14 09:26:11.412 pm debugEnroll Request: enroll request endpoint 0x08 : data 0x0115
dev:1882019-08-14 09:26:09.920 pm debugEnroll Request: enroll request endpoint 0x08 : data 0x0115
dev:1882019-08-14 09:26:08.430 pm debugEnroll Request: enroll request endpoint 0x08 : data 0x0115
dev:1882019-08-14 09:26:06.942 pm debugEnroll Request: enroll request endpoint 0x08 : data 0x0115
dev:1882019-08-14 09:26:06.878 pm debugCatchall: SmartShield(text: null, manufacturerId: 0x0000, direction: 0x00, data: [0xa2, 0x00, 0xfe, 0xa6, 0x01, 0x08], number: null, isManufacturerSpecific: false, messageType: 0x00, senderShortId: 0xa6fe, isClusterSpecific: false, sourceEndpoint: 0x00, profileId: 0x0000, command: 0x00, clusterId: 0x8005, destinationEndpoint: 0x00, options: 0x0040)
dev:1882019-08-14 09:26:05.436 pm debugEnroll Request: enroll request endpoint 0x08 : data 0x0115
dev:1882019-08-14 09:26:03.957 pm debugEnroll Request: enroll request endpoint 0x08 : data 0x0115
dev:1882019-08-14 09:26:02.464 pm debugEnroll Request: enroll request endpoint 0x08 : data 0x0115
dev:1882019-08-14 09:26:02.228 pm debugparse description: read attr - raw: A6FE0800001A05004208535A2D4B46423031, dni: A6FE, endpoint: 08, cluster: 0000, size: 1A, attrId: 0005, encoding: 42, command: 01, value: 08535A2D4B46423031
dev:1882019-08-14 09:26:02.074 pm debugparse description: read attr - raw: A6FE0800001A05004208535A2D4B46423031, dni: A6FE, endpoint: 08, cluster: 0000, size: 1A, attrId: 0005, encoding: 42, command: 01, value: 08535A2D4B46423031
dev:1882019-08-14 09:26:01.936 pm debugparse description: read attr - raw: A6FE0800001A05004208535A2D4B46423031, dni: A6FE, endpoint: 08, cluster: 0000, size: 1A, attrId: 0005, encoding: 42, command: 01, value: 08535A2D4B46423031
dev:1882019-08-14 09:26:01.879 pm debugparse description: read attr - raw: A6FE0800001A05004208535A2D4B46423031, dni: A6FE, endpoint: 08, cluster: 0000, size: 1A, attrId: 0005, encoding: 42, command: 01, value: 08535A2D4B46423031
dev:1882019-08-14 09:26:00.961 pm debugEnroll Request: enroll request endpoint 0x08 : data 0x0115
dev:1882019-08-14 09:26:00.837 pm debugparse description: read attr - raw: A6FE0800001A05004208535A2D4B46423031, dni: A6FE, endpoint: 08, cluster: 0000, size: 1A, attrId: 0005, encoding: 42, command: 01, value: 08535A2D4B46423031
dev:1882019-08-14 09:26:00.824 pm infoConfiguring

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



