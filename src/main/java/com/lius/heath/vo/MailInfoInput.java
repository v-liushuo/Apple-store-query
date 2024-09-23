package com.lius.heath.vo;

public class MailInfoInput {
    private String toEmail;
    private String subject;
    private String msgContent;
    private String receiveName;

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }

    public String getReceiveName() {
        return receiveName;
    }

    public void setReceiveName(String receiveName) {
        this.receiveName = receiveName;
    }

    public MailInfoInput(String toEmail, String subject, String msgContent, String receiveName) {
        this.toEmail = toEmail;
        this.subject = subject;
        this.msgContent = msgContent;
        this.receiveName = receiveName;
    }

    public MailInfoInput() {
    }
}
