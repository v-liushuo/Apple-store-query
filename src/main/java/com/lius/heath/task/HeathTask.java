package com.lius.heath.task;

import com.lius.heath.service.INoticeService;
import com.lius.heath.vo.MailInfoInput;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 同步导出字段
 *
 * @author xd
 */
@Configuration
@Component
@EnableScheduling
public class HeathTask {

    private static final Logger logger = LoggerFactory.getLogger(HeathTask.class);

    @Autowired
    private INoticeService noticeService;

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @Value("${mail.receive.email}")
    private String receiveEmail;

    @Value("${task.run.time}")
    private String runTime;

    @Value("${connect.url:https://www.apple.com.cn/xc/cn/vieworder/W1048351962/1062654154@qq.com}")
    private String connectUrl;

    private LocalTime nextExecuteTime = LocalTime.now();

    private int exceptionTimes = 0;

    private static final String[] ua = {"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.87 Safari/537.36 OPR/37.0.2178.32",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.57.2 (KHTML, like Gecko) Version/5.1.7 Safari/534.57.2",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Safari/537.36 Edge/13.10586",
            "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko",
            "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)",
            "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)",
            "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0)",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 BIDUBrowser/8.3 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36 Core/1.47.277.400 QQBrowser/9.4.7658.400",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 UBrowser/5.6.12150.8 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 Safari/537.36 SE 2.X MetaSr 1.0",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36 TheWorld 7",
            "Mozilla/5.0 (Windows NT 6.1; W…) Gecko/20100101 Firefox/60.0"};

//    @Scheduled(cron = "0 0/20 * * * ?")
    public void handle() {
//        String html = restTemplate.getForObject("http://www.kwh.org.mo/", String.class);
        List<String> receiveEmails = Arrays.asList(receiveEmail.split(","));
        String[] runTimes = runTime.split("-");
        LocalTime startTime = LocalTime.parse(runTimes[0]);
        LocalTime endTime = LocalTime.parse(runTimes[1]);
        LocalTime now = LocalTime.now();
        if (now.isAfter(startTime) && now.isBefore(endTime)) {
            if (now.isAfter(nextExecuteTime)) {
                logger.info("query time :" + dateTimeFormatter.format(LocalDateTime.now()));
                try {
                    execute(receiveEmails);
                    Random random = new Random();
                    int min = random.nextInt(4);
                    nextExecuteTime = now.plusMinutes(min);
                    logger.info("下次执行间隔" + min + "分钟");
                    exceptionTimes = 0;
                } catch (Exception e) {
                    logger.error("刷新异常", e);
                }
            } else {
                logger.info("当前时间" + timeFormatter.format(now) + "下次执行时间" + timeFormatter.format(nextExecuteTime));
            }
        } else {
            logger.info("当前时间" + timeFormatter.format(now) + "执行时间段" + runTime + "下次执行时间" + timeFormatter.format(nextExecuteTime));
            nextExecuteTime = now;
        }

    }

    private void execute(List<String> receiveEmails) throws IOException {
        //建立连接
        Random random = new Random();
        int uaCode = random.nextInt(ua.length);
        Connection connection = Jsoup.connect(connectUrl).timeout(10000).userAgent(ua[uaCode]);

        //获取数据
        Document document = connection.get();
        //分析数据
        Elements elements = document.select("a[href]");
        new ArrayList<>(elements).stream()
                .map(element -> element.attr("href"))
                .filter(href -> href.startsWith("attachments/pdf/有貨通知"))
                .forEach(s -> {
                    System.out.println(s);
                    receiveEmails.forEach(s1 -> {
                        //发送邮件通知
                        MailInfoInput mailInfoInput = new MailInfoInput();
                        mailInfoInput.setSubject("可预约通知");
                        mailInfoInput.setToEmail(s1);
                        mailInfoInput.setReceiveName("刘硕");
                        mailInfoInput.setMsgContent("可预约通知" + connectUrl + s);
                        logger.info("开始发送邮件通知给：" + s1);
                        noticeService.sendEmail(mailInfoInput);
                        logger.info("完成邮件发送通知");
                    });
                });
    }

    public static void main(String[] args) throws IOException {
        String connectUrl = "https://www.apple.com.cn/xc/cn/vieworder/W1048351962/1062654154@qq.com";
        Connection connection = Jsoup.connect(connectUrl);
        //获取数据
        Document document = connection.get();
        //分析数据
        Elements elements = document.select("a[href]");
        System.out.println();
    }

}
