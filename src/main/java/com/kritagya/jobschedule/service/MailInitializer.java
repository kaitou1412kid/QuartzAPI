package com.kritagya.jobschedule.service;

import com.kritagya.jobschedule.config.SchedulerConfig;
import com.kritagya.jobschedule.entity.SchedulerJobInfo;
import com.kritagya.jobschedule.job.MailJob;
import com.kritagya.jobschedule.repository.SchedulerRepository;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class MailInitializer implements CommandLineRunner {

    @Autowired
    private SchedulerRepository schedulerRepository;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private ApplicationContext context;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing mail service");
        try{
            SchedulerJobInfo jobInfo = schedulerRepository.findByJobName("MAIL");
            if(jobInfo != null){
                log.info("JOB CLASS NAME {}", jobInfo.getJobClass());
                try{
                    JobDetail jobDetail = JobBuilder
                            .newJob(MailJob.class)
                            .withIdentity(jobInfo.getJobName(), jobInfo.getJobGroup())
                            .build();
                    if(!scheduler.checkExists(jobDetail.getKey())){
                        JobDataMap jobDataMap = new JobDataMap();
                        jobDataMap.put(jobInfo.getJobClass(), jobInfo.getJobId().toString());
                        jobDetail = SchedulerConfig.createJob(
                                (Class<? extends QuartzJobBean>) Class.forName(jobInfo.getJobClass()),
                                false,
                                context,
                                jobInfo.getJobName(),
                                jobInfo.getJobGroup()
                        );
                        Trigger trigger = SchedulerConfig.createCronTrigger(
                                jobInfo.getCronExpression(),
                                new Date(),
                                jobInfo.getJobName(),
                                SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW
                        );
                        scheduler.scheduleJob(jobDetail, trigger);
                    }
                }catch (SchedulerException e){
                    throw new RuntimeException(e);
                }
            }else{
                log.info("No jonInfos Found");
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }
}
