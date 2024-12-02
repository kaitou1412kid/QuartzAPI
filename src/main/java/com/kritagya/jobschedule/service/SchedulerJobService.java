package com.kritagya.jobschedule.service;

import com.kritagya.jobschedule.config.SchedulerConfig;
import com.kritagya.jobschedule.dto.mapper.SchedulerMapper;
import com.kritagya.jobschedule.dto.request.SchedulerRequestDTO;
import com.kritagya.jobschedule.entity.SchedulerJobInfo;
import com.kritagya.jobschedule.job.SimpleCronJob;
import com.kritagya.jobschedule.job.SimpleJob;
import com.kritagya.jobschedule.repository.SchedulerRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;

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

    private SchedulerMapper schedulerMapper = new SchedulerMapper();

    public void saveOrUpdate(SchedulerRequestDTO schedulerRequestDTO) throws Exception {
        SchedulerJobInfo schedulerJobInfo = new SchedulerJobInfo();
        schedulerJobInfo = schedulerMapper.mapData(schedulerRequestDTO);
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
            scheduleNewJob(schedulerJobInfo);
        }else{
            updateScheduleJob(schedulerJobInfo);
        }
        schedulerJobInfo.setDescription("I am job number "+ schedulerJobInfo.getJobId());
        schedulerJobInfo.setInterfaceName("Interface_"+schedulerJobInfo.getJobId());
        log.info(">>>>> jobName = [{}] created.", schedulerJobInfo.getJobName());
    }

    private void scheduleNewJob(SchedulerJobInfo schedulerJobInfo){
        try{
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            JobDetail jobDetail = JobBuilder
                    .newJob((Class<? extends QuartzJobBean>) Class.forName(schedulerJobInfo.getJobClass()))
                    .withIdentity(schedulerJobInfo.getJobName(), schedulerJobInfo.getJobGroup())
                    .build();

            if(!scheduler.checkExists(jobDetail.getKey())){
                jobDetail = SchedulerConfig.createJob(
                        (Class<? extends QuartzJobBean>) Class.forName(schedulerJobInfo.getJobClass()), false,context,
                        schedulerJobInfo.getJobName(),
                        schedulerJobInfo.getJobGroup()
                );
                Trigger trigger;
                if(schedulerJobInfo.getCronJob()){
                    trigger = SchedulerConfig.createCronTrigger(
                            schedulerJobInfo.getCronExpression(),
                            new Date(),
                            schedulerJobInfo.getJobName(),
                            SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW
                    );
                }else{
                    trigger = SchedulerConfig.createSimpleTrigger(
                            schedulerJobInfo.getJobName(),
                            new Date(),
                            schedulerJobInfo.getRepeatTime(),
                            SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW
                    );
                }
                scheduler.scheduleJob(jobDetail, trigger);
                schedulerJobInfo.setJobStatus("SCHEDULED");
                schedulerRepository.save(schedulerJobInfo);
                log.info(">>>>> jobName = [{}] scheduled.", schedulerJobInfo.getJobName());
            }else{
                log.error("scheduleNewJobRequest.jobAlreadyExist");
            }
        }catch (ClassNotFoundException e){
            log.error("Class not found- {}", schedulerJobInfo.getJobClass(), e);
        }catch (SchedulerException e){
            log.error(e.getMessage(), e);
        }
    }

    private void updateScheduleJob(SchedulerJobInfo schedulerJobInfo){
        Trigger newTrigger;
        if(schedulerJobInfo.getCronJob()){
            newTrigger = SchedulerConfig.createCronTrigger(
                    schedulerJobInfo.getCronExpression(),
                    new Date(),
                    schedulerJobInfo.getJobName(),
                    SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW
            );
        }else{
            newTrigger = SchedulerConfig.createSimpleTrigger(
                    schedulerJobInfo.getJobName(),
                    new Date(),
                    schedulerJobInfo.getRepeatTime(),
                    SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW
            );
        }
        try{
            schedulerFactoryBean.getScheduler().rescheduleJob(TriggerKey.triggerKey(schedulerJobInfo.getJobName()), newTrigger);
            schedulerJobInfo.setJobStatus("EDITED & SCHEDULED");
            schedulerRepository.save(schedulerJobInfo);
            log.info(">>>>> jobName = [{}] updated and scheduled.", schedulerJobInfo.getJobName());
        }catch (SchedulerException e){
            log.error(e.getMessage(), e);
        }
    }

    public boolean pauseJob(SchedulerJobInfo schedulerJobInfo){
        try{
            SchedulerJobInfo getJobInfo = schedulerRepository.findByJobName(schedulerJobInfo.getJobName());
            getJobInfo.setJobStatus("PAUSED");
            schedulerRepository.save(getJobInfo);
            schedulerFactoryBean.getScheduler().pauseJob(new JobKey(schedulerJobInfo.getJobName(), schedulerJobInfo.getJobGroup()));
            log.info(">>>>>>> jobName = [{}] paused", schedulerJobInfo.getJobName());
            return true;
        }catch(SchedulerException e){
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean resumeJob(SchedulerJobInfo schedulerJobInfo){
        try{
            SchedulerJobInfo getJobInfo = schedulerRepository.findByJobName(schedulerJobInfo.getJobName());
            getJobInfo.setJobStatus("RESUMED");
            schedulerRepository.save(getJobInfo);
            schedulerFactoryBean.getScheduler().resumeJob(new JobKey(schedulerJobInfo.getJobName(), schedulerJobInfo.getJobGroup()));
            log.info(">>>>>>> jobName = [{}] resumed", schedulerJobInfo.getJobName());
            return true;
        }catch (SchedulerException e){
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean deleteJob(SchedulerJobInfo schedulerJobInfo){
        try{
            SchedulerJobInfo getJobInfo = schedulerRepository.findByJobName(schedulerJobInfo.getJobName());
            schedulerRepository.delete(getJobInfo);
            log.info(">>>>> jobName = [{}] deleted", schedulerJobInfo.getJobName());
            return schedulerFactoryBean.getScheduler().deleteJob(new JobKey(schedulerJobInfo.getJobName(), schedulerJobInfo.getJobGroup()));
        }catch(SchedulerException e){
            log.error(e.getMessage(), e);
            return false;
        }
    }
}
