package com.kritagya.jobschedule.controller;

import com.kritagya.jobschedule.dto.request.SchedulerRequestDTO;
import com.kritagya.jobschedule.service.SchedulerJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
public class SchedulerController {

    private final SchedulerJobService schedulerJobService;

    public SchedulerController(SchedulerJobService schedulerJobService) {
        this.schedulerJobService = schedulerJobService;
    }

    @PostMapping("/create")
    public String createScheduler(@RequestBody SchedulerRequestDTO schedulerJobInfoRequest) {
        try{
            schedulerJobService.saveOrUpdate(schedulerJobInfoRequest);
            return "success";
        }catch(Exception e){
            log.error(e.getMessage(), e);
            return "error";
        }
    }

    @PostMapping("/pause")
    public String pauseScheduler(@RequestBody SchedulerRequestDTO schedulerJobInfoRequest) {
        try{
            boolean isPaused = schedulerJobService.pauseJob(schedulerJobInfoRequest);
            if(isPaused){
                return "success";
            }else{
                return "error";
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "error";
        }
    }
}
