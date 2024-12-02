package com.kritagya.jobschedule.dto.mapper;

import com.kritagya.jobschedule.dto.request.SchedulerRequestDTO;
import com.kritagya.jobschedule.entity.SchedulerJobInfo;
import lombok.Data;

public class SchedulerMapper {

    public SchedulerJobInfo mapData(SchedulerRequestDTO schedulerRequestDTO){
        SchedulerJobInfo schedulerJobInfo = new SchedulerJobInfo();
        schedulerJobInfo.setJobName(schedulerRequestDTO.getJobName());
        schedulerJobInfo.setJobGroup(schedulerRequestDTO.getJobGroup());
        schedulerJobInfo.setCronExpression(schedulerRequestDTO.getCronExpression());
        schedulerJobInfo.setDescription(schedulerRequestDTO.getJobDescription());
        schedulerJobInfo.setJobStatus(schedulerRequestDTO.getJobStatus());
        schedulerJobInfo.setCronJob(null);
        schedulerJobInfo.setJobClass(null);
        schedulerJobInfo.setInterfaceName(null);
        schedulerJobInfo.setRepeatTime(null);
        return schedulerJobInfo;
    }
}
