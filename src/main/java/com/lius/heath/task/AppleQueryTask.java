package com.lius.heath.task;

import com.lius.heath.Heath1TaskApplication;
import com.lius.heath.service.INoticeService;
import com.lius.heath.vo.MailInfoInput;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 同步导出字段
 *
 * @author xd
 */
@Configuration
@Component
@EnableScheduling
public class AppleQueryTask {

    private static final Logger logger = LoggerFactory.getLogger(AppleQueryTask.class);

    @Autowired
    private INoticeService noticeService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${mail.receive.email}")
    private String receiveEmail;

    @Value("${connect.url:https://www.apple.com/hk/shop/fulfillment-messages?pl=true&mt=compact&parts.0=MYW03CH/A&searchNearby=true&store=R639}")
    private String connectUrl;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd HH:mm:ss");
    private static final Set<String> stores = new HashSet<>(Arrays.asList("R639", "R577"));

    private static final AtomicInteger ERROR_COUNT = new AtomicInteger(0);

    @Scheduled(cron = "0/3 * * * * ? ")
    public void handle() {
        ResponseEntity<String> response;
        try {
            response = restTemplate.getForEntity(connectUrl, String.class);
        } catch (RestClientException e) {
            logger.error("request error:{}", dateTimeFormatter.format(LocalDateTime.now()));
            return;
        }
        String str = response.getBody();
        JSONObject jsonObject = JSONObject.fromObject(str);
        JSONObject jsonObject1 = jsonObject.getJSONObject("body").getJSONObject("content").getJSONObject("pickupMessage");
        JSONArray jsonArray = jsonObject1.getJSONArray("stores");
        String storeName = null;
        for (Object o : jsonArray) {
            JSONObject object = (JSONObject) o;
            if (stores.contains(object.getString("storeNumber"))) {
                String string = object.getJSONObject("partsAvailability").getJSONObject("MQ0W3CH/A").getString("pickupDisplay");
                if ("available".equals(string)) {
                    storeName = object.getString("storeName");
                    break;
                }
            }
        }
        if (storeName != null) {
            if (SystemTray.isSupported()) {
                try {
                    Heath1TaskApplication.displayTray(storeName);
                } catch (AWTException ignored) {
                }
            }
            //发送邮件通知
            MailInfoInput mailInfoInput = new MailInfoInput();
            mailInfoInput.setSubject("可预约通知");
            mailInfoInput.setToEmail(receiveEmail);
            mailInfoInput.setReceiveName("刘硕");
            mailInfoInput.setMsgContent("可预约通知： " + storeName);
            logger.info("开始发送邮件通知给：" + receiveEmail);
            noticeService.sendEmail(mailInfoInput);
            logger.info("完成邮件发送通知");
            logger.info(str);
        }
        LocalDateTime now = LocalDateTime.now();
        logger.info("当前时间" + dateTimeFormatter.format(now));
    }
}
