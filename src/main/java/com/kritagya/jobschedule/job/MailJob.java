package com.kritagya.jobschedule.job;

import com.kritagya.jobschedule.service.MailService;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@DisallowConcurrentExecution
public class MailJob extends QuartzJobBean {

    @Autowired
    private MailService mailService;
    private final Dotenv dotenv = Dotenv.load();

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try{
            log.info("Mail Job Started..........................");
            mailService.sendEmail(dotenv.get("RECEIVER_EMAIL"), "Mail", "Hello this is mail");
        }catch(Exception e){
            log.error(e.getMessage(), e);
        }
    }
}
