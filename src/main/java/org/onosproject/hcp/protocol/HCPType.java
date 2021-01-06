package org.onosproject.hcp.protocol;


public enum  HCPType {
    HCP_HELLO(0,"Hello"),
    HCP_ERROR(1,"Error"),
    HCP_ECHO_REQUEST(2,"EchoRequest"),
    HCP_ECHO_REPLY(3,"EchoReply"),
    HCP_FEATURES_REQUEST(4,"FeaturesRequest"),
    HCP_FEATURES_REPLY(5,"FeaturesReply"),
    HCP_GET_CONFIG_REQUEST(6,"GetConfigRequest"),
    HCP_GET_CONFIG_REPLY(7,"GetConfigReply"),
    HCP_SET_CONFIG(8,"SetConfig"),
    HCP_TOPO_REQUEST(9,"TopoRequest"),
    HCP_TOPO_REPLY(10,"TopoReply"),
    HCP_HOST_REQUEST(11,"HostRequest"),
    HCP_HOST_REPLY(12,"HostReply"),
    HCP_HOST_UPDATE(13,"HostUpdate"),
    HCP_VPORT_STATUS(14,"VportStatus"),
    HCP_SBP(15,"SBP"),
    HCP_IOT_REQUEST(16,"IoTRequest"),
    HCP_IOT_REPLY(17,"IoTReply"),
    HCP_IOT_UPDATE(18,"IoTUpdate");
    private int value;
    private String name;

    HCPType(int value,String name){
        this.value=value;
        this.name=name;
    }

    public static HCPType valueof(int value){
        switch(value){
            case 0:
                return HCP_HELLO;
            case 1:
                return HCP_ERROR;
            case 2:
                return HCP_ECHO_REQUEST;
            case 3:
                return HCP_ECHO_REPLY;
            case 4:
                return HCP_FEATURES_REQUEST;
            case 5:
                return HCP_FEATURES_REPLY;
            case 6:
                return HCP_GET_CONFIG_REQUEST;
            case 7:
                return HCP_GET_CONFIG_REPLY;
            case 8:
                return HCP_SET_CONFIG;
            case 9:
                return HCP_TOPO_REQUEST;
            case 10:
                return HCP_TOPO_REPLY;
            case 11:
                return HCP_HOST_REQUEST;
            case 12:
                return HCP_HOST_REPLY;
            case 13:
                return HCP_HOST_UPDATE;
            case 14:
                return HCP_VPORT_STATUS;
            case 15:
                return HCP_SBP;
            case 16:
                return HCP_IOT_REQUEST;
            case 17:
                return HCP_IOT_REPLY;
            case 18:
                return HCP_IOT_UPDATE;
            default:
                throw new IllegalArgumentException("Illegal value for type HCPType in version 1.0"+value);        }
    }

    public int value(){
        return value;
    }
    public String getName(){
        return name;
    }
}
