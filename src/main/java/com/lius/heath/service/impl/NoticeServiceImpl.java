package com.lius.heath.service.impl;

import com.lius.heath.service.INoticeService;
import com.lius.heath.vo.MailInfoInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class NoticeServiceImpl implements INoticeService {

    private static final Logger logger = LoggerFactory.getLogger(NoticeServiceImpl.class);

    @Value("${spring.mail.username}")
    private String mailForm;//邮件发件人

    @Autowired
    private JavaMailSender javaMailSender;

    @Override
    public String sendEmail(MailInfoInput input) {
        try {
            this.sendMimeMessage(input.getToEmail(), input.getSubject(), input.getMsgContent(), input.getReceiveName());
            return "success";
        } catch (Exception e) {
            logger.error("邮件发送失败,", e);
        }
        return "";
    }


    private void sendMimeMessage(String toEmail, String subject, String msgContent, String receiveName) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setFrom(mailForm);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(msgContent);

        mimeMessageHelper.setTo(toEmail);
        javaMailSender.send(mimeMessage);
    }

}
