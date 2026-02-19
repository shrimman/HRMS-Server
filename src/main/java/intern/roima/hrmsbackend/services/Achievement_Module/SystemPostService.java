package intern.roima.hrmsbackend.services.Achievement_Module;

import java.util.List;

import intern.roima.hrmsbackend.dtos.Responses.AchievementPostDto;

public interface SystemPostService {

    AchievementPostDto createBirthdayPost(Long employeeId);
    AchievementPostDto createWorkAnniversaryPost(Long employeeId);
    List<String> getBirthdays();
    List<String> getWorkAnniversaries();
}
