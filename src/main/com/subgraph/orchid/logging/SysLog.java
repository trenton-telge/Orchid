package com.subgraph.orchid.logging;

@SuppressWarnings("unused")
public class SysLog {
    public static SysLog EMERGENCY = new SysLog(0, "EMERGENCY");
    public static SysLog ALERT = new SysLog(1, "ALERT");
    public static SysLog CRITICAL = new SysLog(2, "CRITICAL");
    public static SysLog ERROR = new SysLog(3, "ERROR");
    public static SysLog WARNING = new SysLog(4, "WARNING");
    public static SysLog NOTICE = new SysLog(5, "NOTICE");
    public static SysLog INFORMATIONAL = new SysLog(6, "INFORMATIONAL");
    public static SysLog DEBUG = new SysLog(7, "DEBUG");
    
    private final Integer value;
    private final String severity;
    
    private SysLog(Integer value, String severity){
        this.value = value;
        this.severity = severity;
    }

    public Integer getValue() {
        return value;
    }

    public String getSeverity() {
        return severity;
    }
    
    public Boolean isGreaterOrEqual(SysLog other){
        if(this.value!=null && other!=null && other.getValue()!=null){
            return this.value<=other.value;
        } else{
            return false;
        }
    }
}
