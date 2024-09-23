package com.lius.heath.service;

import com.lius.heath.vo.MailInfoInput;

public interface INoticeService {

    String sendEmail(MailInfoInput input);
}