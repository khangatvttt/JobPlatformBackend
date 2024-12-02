package com.jobplatform.services;

import com.jobplatform.models.dto.CountDto;
import com.jobplatform.models.dto.OverallStatisticsDto;
import com.jobplatform.repositories.ApplicationRepository;
import com.jobplatform.repositories.JobRepository;
import com.jobplatform.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public StatisticsService(JobRepository jobRepository, ApplicationRepository applicationRepository, UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    public OverallStatisticsDto getOverall(long days){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime daysAgo = now.minusDays(days);

        long numberOfJobs = jobRepository.countByCreateAtBetween(daysAgo, now);
        long numberOfApplications = applicationRepository.countByAppliedAtBetween(daysAgo, now);
        long numberOfUsers = userRepository.countByCreatedAtBetween(daysAgo, now);
        return new OverallStatisticsDto(numberOfJobs, numberOfApplications, numberOfUsers);
    }

    public List<CountDto> getTop5IndustriesOfJob(long days){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime daysAgo = now.minusDays(days);

        List<Object[]> result = jobRepository.findTop5IndustriesBetweenDates(daysAgo, now);

        return result
                .stream()
                .map(record -> new CountDto(
                (String) record[0],
                (Long) record[1]))
                .collect(Collectors.toList());
    }

    public List<CountDto> getTop5IndustriesOfApplication(long days){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime daysAgo = now.minusDays(days);

        List<Object[]> result = applicationRepository.findTop5IndustriesByApplications(daysAgo, now);

        return result
                .stream()
                .map(record -> new CountDto(
                        (String) record[0],
                        (Long) record[1]))
                .collect(Collectors.toList());
    }

    public List<CountDto> getApplicationStatusCount(long days){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime daysAgo = now.minusDays(days);

        List<Object[]> result = applicationRepository.countApplicationsByStatus(daysAgo, now);

        return result
                .stream()
                .map(record -> new CountDto(
                        record[0].toString(),
                        (Long) record[1]))
                .collect(Collectors.toList());
    }
}
