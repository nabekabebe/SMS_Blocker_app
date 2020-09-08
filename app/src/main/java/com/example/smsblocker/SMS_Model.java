package com.example.smsblocker;

public class SMS_Model {
    private int sms_id;
    private String sms_source;
    private String sms_content;

    public SMS_Model(){}

    public SMS_Model(int sms_id, String sms_source, String sms_content) {
        this.sms_id = sms_id;
        this.sms_source = sms_source;
        this.sms_content = sms_content;
    }

    public int getSms_id() {
        return sms_id;
    }

    public String getSms_source() {
        return sms_source;
    }

    public String getSms_content() {
        return sms_content;
    }

    public void setSms_id(int sms_id) {
        this.sms_id = sms_id;
    }

    public void setSms_source(String sms_source) {
        this.sms_source = sms_source;
    }

    public void setSms_content(String sms_content) {
        this.sms_content = sms_content;
    }
}
