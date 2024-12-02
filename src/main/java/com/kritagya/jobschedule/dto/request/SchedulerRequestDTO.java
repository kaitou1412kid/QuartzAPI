package com.kritagya.jobschedule.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SchedulerRequestDTO {
    private String jobName;
    private String jobGroup;
    private String jobDescription;
    private String cronExpression;
    private String jobStatus;
}
