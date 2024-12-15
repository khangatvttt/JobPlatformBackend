package com.jobplatform.services;

import com.jobplatform.models.UserAccount;
import com.jobplatform.models.dto.CountDto;
import com.jobplatform.models.dto.OverallStatisticsDto;
import com.jobplatform.repositories.ApplicationRepository;
import com.jobplatform.repositories.JobRepository;
import com.jobplatform.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    public OverallStatisticsDto getOverall(long days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime daysAgo;
        if (days!=0) {
            daysAgo = now.minusDays(days);
        }
        else{
            daysAgo = LocalDateTime.of(1970, 1, 1, 0, 0, 0);;
        }


        long numberOfJobs = jobRepository.countByCreateAtBetween(daysAgo, now);
        long numberOfApplications = applicationRepository.countByAppliedAtBetween(daysAgo, now);
        long numberOfJobSeekers = userRepository.countByRoleAndCreatedAtBetween(UserAccount.Role.ROLE_JOB_SEEKER, daysAgo, now);
        long numberOfRecruiters = userRepository.countByRoleAndCreatedAtBetween(UserAccount.Role.ROLE_RECRUITER, daysAgo, now);
        return new OverallStatisticsDto(numberOfJobs, numberOfApplications, numberOfJobSeekers, numberOfRecruiters);
    }

    public List<CountDto> getTop5IndustriesOfJob(long days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime daysAgo;
        if (days!=0) {
            daysAgo = now.minusDays(days);
        }
        else{
            daysAgo = LocalDateTime.of(1970, 1, 1, 0, 0, 0);;
        }

        List<Object[]> result = jobRepository.findTop5IndustriesBetweenDates(daysAgo, now);

        return result
                .stream()
                .map(record -> new CountDto(
                        (String) record[0],
                        (Long) record[1]))
                .collect(Collectors.toList());
    }

    public List<CountDto> getTop5IndustriesOfApplication(long days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime daysAgo;
        if (days!=0) {
            daysAgo = now.minusDays(days);
        }
        else{
            daysAgo = LocalDateTime.of(1970, 1, 1, 0, 0, 0);;
        }

        List<Object[]> result = applicationRepository.findTop5IndustriesByApplications(daysAgo, now);

        return result
                .stream()
                .map(record -> new CountDto(
                        (String) record[0],
                        (Long) record[1]))
                .collect(Collectors.toList());
    }

    public List<CountDto> getApplicationStatusCount(long days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime daysAgo = now.minusDays(days);
        if (days!=0) {
            daysAgo = now.minusDays(days);
        }
        else{
            daysAgo = LocalDateTime.of(1970, 1, 1, 0, 0, 0);;
        }

        List<Object[]> result = applicationRepository.countApplicationsByStatus(daysAgo, now);

        return result
                .stream()
                .map(record -> new CountDto(
                        record[0].toString(),
                        (Long) record[1]))
                .collect(Collectors.toList());
    }

    public List<List<CountDto>> getDataThroughTime(long days) {
        DateTimeFormatter formatter;
        if (days < 180) {
            formatter = DateTimeFormatter.ofPattern("dd/MM");
        } else {
            formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        }

        List<LocalDateTime> intervals = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime daysAgo;
        if (days!=0) {
            daysAgo = now.minusDays(days);
        }
        else{
            daysAgo = LocalDateTime.of(2020, 1, 1, 0, 0, 0);;
        }

        // Calculate the duration between start and end times
        Duration totalDuration = Duration.between(daysAgo, now);
        long intervalMillis = totalDuration.toMillis() / 6;

        for (int i = 0; i <= 5; i++) {
            intervals.add(daysAgo.plus(Duration.ofMillis(intervalMillis * i)));
        }

        intervals.add(now);

        for (LocalDateTime time : intervals){
            System.out.println(time.toString());
        }

        List<CountDto> applicationList = new ArrayList<>();
        List<CountDto> jobList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            if (i == 0) {
                CountDto countDtoApplications = new CountDto(intervals.get(0).format(formatter), 0);
                CountDto countDtoJobs = new CountDto(intervals.get(0).format(formatter), 0);

                applicationList.add(countDtoApplications);
                jobList.add(countDtoJobs);
            } else {
                LocalDateTime start = intervals.get(i - 1);
                LocalDateTime end = intervals.get(i);

                long numberOfApplications = applicationRepository.countByAppliedAtBetween(start, end);
                long numberOfJobs = jobRepository.countByCreateAtBetween(start, end);

                CountDto countDtoApplications = new CountDto(end.format(formatter), numberOfApplications);
                CountDto countDtoJobs = new CountDto(end.format(formatter), numberOfJobs);

                applicationList.add(countDtoApplications);
                jobList.add(countDtoJobs);
            }
        }


        List<List<CountDto>> countDtoList = new ArrayList<>();
        countDtoList.add(applicationList);
        countDtoList.add(jobList);

        return countDtoList;

    }

}
