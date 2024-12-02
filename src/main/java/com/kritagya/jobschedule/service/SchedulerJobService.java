package com.kritagya.jobschedule.service;

import com.kritagya.jobschedule.config.SchedulerConfig;
import com.kritagya.jobschedule.entity.SchedulerJobInfo;
import com.kritagya.jobschedule.job.SimpleCronJob;
import com.kritagya.jobschedule.job.SimpleJob;
import com.kritagya.jobschedule.repository.SchedulerRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Transactional
@Service
public class SchedulerJobService {
    @Autowired
    private Scheduler scheduler;

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    @Autowired
    private SchedulerRepository schedulerRepository;

    @Autowired
    private ApplicationContext context;

    public void saveOrUpdate(SchedulerJobInfo schedulerJobInfo) throws Exception {
        if(schedulerJobInfo.getCronExpression().length() > 0){
            schedulerJobInfo.setJobClass(SimpleCronJob.class.getName());
            schedulerJobInfo.setCronJob(true);
        }else{
            schedulerJobInfo.setJobClass(SimpleJob.class.getName());
            schedulerJobInfo.setCronJob(false);
            schedulerJobInfo.setRepeatTime((long) 1);
        }
        if(StringUtils.isEmpty(schedulerJobInfo.getJobId())){
            log.info("Job Info: {}", schedulerJobInfo);
            // schedule new job
        }else{
            // update schedule job
        }
        schedulerJobInfo.setDescription("I am job number "+ schedulerJobInfo.getJobId());
        schedulerJobInfo.setInterfaceName("Interface_"+schedulerJobInfo.getJobId());
        log.info(">>>>> jobName = [{}] created.", schedulerJobInfo.getJobName());
    }
}
