package org.onosproject.hcp.protocol;

import java.text.Normalizer;

public enum HCPSbpCmpType {
    NORMAL,
    FLOW_FORWARDING_REQUEST,
    FLOW_FORWARDING_REPLY,
    PACKET_IN,
    PACKET_OUT,
    RESOURCE_REQUEST,
    RESOURCE_REPLY;
}
