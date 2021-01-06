package org.onosproject.hcp.protocol;

/**
 * @Author ldy
 * @Date: 2021/1/6 下午4:16
 * @Version 1.0
 */
public enum HCPIoTState {
    ACTIVE(true),
    INACTIVE(false);

    private final boolean activeState;

    private HCPIoTState(boolean activeState){
        this.activeState=activeState;
    }

    public boolean isActive(){
        return activeState;
    }
}
