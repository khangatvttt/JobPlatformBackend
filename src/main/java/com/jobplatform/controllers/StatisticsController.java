package com.jobplatform.controllers;

import com.jobplatform.models.dto.CountDto;
import com.jobplatform.models.dto.OverallStatisticsDto;
import com.jobplatform.services.StatisticsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }


    @GetMapping("/overall")
    public ResponseEntity<OverallStatisticsDto> getOverall(@RequestParam long days){
        return new ResponseEntity<>(statisticsService.getOverall(days), HttpStatus.OK);
    }

    @GetMapping("/top-5-industry-jobs")
    public ResponseEntity<List<CountDto>> getTop5IndustryJobs(@RequestParam long days){
        return new ResponseEntity<>(statisticsService.getTop5IndustriesOfJob(days), HttpStatus.OK);
    }

    @GetMapping("/top-5-industry-applications")
    public ResponseEntity<List<CountDto>> getTop5IndustryApplications(@RequestParam long days){
        return new ResponseEntity<>(statisticsService.getTop5IndustriesOfApplication(days), HttpStatus.OK);
    }

    @GetMapping("/application-status")
    public ResponseEntity<List<CountDto>> getCountOfStatusApplications(@RequestParam long days){
        return new ResponseEntity<>(statisticsService.getApplicationStatusCount(days), HttpStatus.OK);
    }

    @GetMapping("/statistic-by-time")
    public ResponseEntity<List<List<CountDto>>> getDataThroughTime(@RequestParam long days){
        return new ResponseEntity<>(statisticsService.getDataThroughTime(days), HttpStatus.OK);
    }
}
