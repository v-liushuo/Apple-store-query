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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

    @Value("${connect.url:https://www.apple.com/hk/shop/fulfillment-messages?pl=true&mt=compact" +
            "&parts.0=MYTN3ZA/A" +
            "&parts.1=MYTP3ZA/A" +
            "&parts.2=MYTQ3ZA/A" +
            "&parts.3=MYTM3ZA/A" +
            "&searchNearby=true&store=R409}")
    private String connectUrl;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd HH:mm:ss");
    private static final Set<String> stores = new HashSet<>(Collections.emptyList());

    private static final AtomicInteger ERROR_COUNT = new AtomicInteger(0);

    @Scheduled(cron = "0/3 * * * * ? ")
    public void handle() {
        LocalDateTime now1 = LocalDateTime.now();
        LocalTime endDate = LocalTime.of(23, 0);
        LocalTime startDate = LocalTime.of(6, 0);

        if (LocalTime.now().isAfter(endDate) || LocalTime.now().isBefore(startDate)) {
            logger.warn("暂未到开始时间,执行时间段为：{} 点到{}点", startDate.getHour(), endDate.getHour());
            return;
        }
        ResponseEntity<String> response;
        try {
            response = restTemplate.getForEntity(connectUrl, String.class);
        } catch (RestClientException e) {
            logger.error("request error:{}", dateTimeFormatter.format(now1));
            return;
        }
        String str = response.getBody();
        JSONObject jsonObject = JSONObject.fromObject(str);
        JSONObject jsonObject1 = jsonObject.getJSONObject("body").getJSONObject("content").getJSONObject("pickupMessage");
        JSONArray jsonArray = jsonObject1.getJSONArray("stores");
        Map<String, List<String>> storeHasProduct = new ConcurrentHashMap<>();
        for (Object o : jsonArray) {
            JSONObject object = (JSONObject) o;
            if (stores.isEmpty() || stores.contains(object.getString("storeNumber"))) {
                JSONObject partsAvailability = object.getJSONObject("partsAvailability");
                Set<String> set = partsAvailability.keySet();
                for (String key : set) {
                    JSONObject jsonObject2 = partsAvailability.getJSONObject(key);
                    String string = jsonObject2.getString("pickupDisplay");
                    if ("available".equals(string)) {
                        String storeName = object.getString("storeName");
                        List<String> orDefault = storeHasProduct.getOrDefault(storeName, new ArrayList<>());
                        String name = jsonObject2.getJSONObject("messageTypes").getJSONObject("compact").getString("storePickupProductTitle");
                        orDefault.add(name);
                        storeHasProduct.put(storeName, orDefault);
                    }
                }
            }
        }
        if (!storeHasProduct.isEmpty()) {
            String noticeContent = storeHasProduct.entrySet().parallelStream().map(stringListEntry -> {
                String storeNameKey = stringListEntry.getKey();
                String value = stringListEntry.getValue().parallelStream().collect(Collectors.joining("\n"));
                return String.format("%s\n%s", storeNameKey, value);
            }).collect(Collectors.joining("\n"));
            if (SystemTray.isSupported()) {
                try {
                    Heath1TaskApplication.displayTray(noticeContent);
                } catch (AWTException ignored) {
                }
            }
            //发送邮件通知
            MailInfoInput mailInfoInput = new MailInfoInput();
            mailInfoInput.setSubject("可预约通知");
            mailInfoInput.setToEmail(receiveEmail);
            mailInfoInput.setReceiveName("刘硕");
            mailInfoInput.setMsgContent("可预约通知： " + noticeContent);
            logger.info("开始发送邮件通知给：" + receiveEmail);
            noticeService.sendEmail(mailInfoInput);
            logger.info("完成邮件发送通知");
            logger.info(str);
        } else {
            LocalDateTime now = now1;
            logger.info("当前时间" + dateTimeFormatter.format(now));
        }
    }
}
