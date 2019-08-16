import hubitat.zigbee.clusters.iaszone.ZoneStatus

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


def getClusters() { 
     "zdo active 0x${device.deviceNetworkId}" 
       log.debug "Get Clusters Called";
}




def parse(String description) {

  if (description?.startsWith("enroll request")) {
    if (logEnable) log.debug "RECEVED ENROLL REQUEST: ${description}"
        List cmds = zigbee.enrollResponse(1200)
        result = cmds?.collect { new hubitat.device.HubAction(it, hubitat.device.Protocol.ZIGBEE) }
  } else {
    if (description?.startsWith("zone status")){
      ZoneStatus zs = zigbee.parseZoneStatus(description)
      if (logEnable) log.debug zs
    } else {

      def descMap = zigbee.parseDescriptionAsMap(description)
      if (logEnable) log.debug "zigbee.parseDescriptionAsMap-read attr: ${descMap}"
      def cluster = descMap.clusterId
      if (logEnable) log.debug "cluster: ${cluster}"
      
      def sourceEndpoint = descMap.sourceEndpoint
      if (logEnable) log.debug "sourceEndpoint: ${sourceEndpoint}"
      
      def button = descMap.data[0]
      if (logEnable) log.debug "button: ${button}"
    }
}
}

def configure(){
 String zigbeeId = swapEndianHex(device.hub.zigbeeId)
      log.debug "Config Called"
    def configCmds = [
  "zcl global write 0x500 0x10 0xf0 {${zigbeeId}}", "delay 200",
    "send 0x${device.deviceNetworkId} 0x08 1", "delay 1500",
    "zdo bind 0x${device.deviceNetworkId} 0x08 0x01 0x0501 {${device.zigbeeId}} {}", "delay 500",
      "zdo bind 0x${device.deviceNetworkId} 0x08 1 1 {${device.zigbeeId}} {}"
    ] 
    return configCmds


}

private String swapEndianHex(String hex) {
    reverseArray(hex.decodeHex()).encodeHex()
}
private byte[] reverseArray(byte[] array) {
    int i = 0;
    int j = array.length - 1;
    byte tmp;
    while (j > i) {
        tmp = array[j];
        array[j] = array[i];
        array[i] = tmp;
        j--;
        i++;
    }
    return array
}
