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
	
	def cluster = description.split(",").find {it.split(":")[0].trim() == "cluster"}?.split(":")[1].trim()
	def attrId = description.split(",").find {it.split(":")[0].trim() == "attrId"}?.split(":")[1].trim()
	def encoding = Integer.parseInt(description.split(",").find {it.split(":")[0].trim() == "encoding"}?.split(":")[1].trim(), 16)
	def valueHex = description.split(",").find {it.split(":")[0].trim() == "value"}?.split(":")[1].trim()
	
	if (logEnable) log.debug "Parsing message: ${description}"
	if (logEnable) log.debug "Cluster: ${cluster}"
	if (logEnable) log.debug "Attrid: ${attrId}"
	if (logEnable) log.debug "encoding: ${encoding}"
	if (logEnable) log.debug "Message payload: ${valueHex}"
	
	
	/* PARSE - ST Driver
	
		if (description?.startsWith('enroll request')) {        
			List cmds = enrollResponse()
			log.debug "enroll response: ${cmds}"
			def result = cmds?.collect { new hubitat.device.HubAction(it) }
			return result    
		} else if (description?.startsWith('catchall:')) {
			def msg = zigbee.parse(description)
			log.debug msg
			buttonPush(msg.data[0])
		} else {
			log.debug "parse description: $description"
		}    
	*/
    
}

def buttonPush(button){
    //Button Numbering vs positioning is slightly counterintuitive
    //Bottom Left Button (Unlock) = 0 and goes counterclockwise
    //Securifi Numbering - 0 = Unlock, 1 = * (only used to join), 2 = Home, 3 = Lock
    //For ST App Purposes 1=Lock, 2=Home, 3=Unlock , 4 = * (only used to join)
    def name = null
    if (button == 0) {
        //Unlock - ST Button 3
        name = "3"
        def currentST = device.currentState("button3")?.value
        log.debug "Unlock button Pushed"           
    } else if (button == 2) {
    	//Home - ST Button 2
        name = "2"
        def currentST = device.currentState("button2")?.value
        log.debug "Home button pushed"        
    } else if (button == 3) {
        //Lock ST Button 1
        name = "1"
     	def currentST = device.currentState("button")?.value
        log.debug "Lock Button pushed"         
    } 

    def result = createEvent(name: "button", value: "pushed", data: [buttonNumber: name], descriptionText: "$device.displayName button $name was pushed", isStateChange: true)
    log.debug "Parse returned ${result?.descriptionText}"
    return result
}



def configure() {
    log.warn "configure..."
    runIn(1800,logsOff)
    def cmds = [
		//Taken from ST Driver
		"zcl global write 0x500 0x10 0xf0 {${device.zigbeeId}}", "delay 200",
		"send 0x${device.deviceNetworkId} ${endpointId} 1", "delay 1500",
		"zdo bind 0x${device.deviceNetworkId} ${endpointId} 0x01 0x0501 {${device.zigbeeId}} {}", "delay 500",
		"zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 1 {${device.zigbeeId}} {}"

    ] + refresh()
    return cmds
}



def installed() {
}

