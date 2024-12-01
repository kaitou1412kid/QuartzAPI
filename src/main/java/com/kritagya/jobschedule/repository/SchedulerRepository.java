package com.kritagya.jobschedule.repository;

import com.kritagya.jobschedule.entity.SchedulerJobInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchedulerRepository extends JpaRepository<SchedulerJobInfo, Integer> {
    SchedulerJobInfo findByJobName(String jobName);
}
