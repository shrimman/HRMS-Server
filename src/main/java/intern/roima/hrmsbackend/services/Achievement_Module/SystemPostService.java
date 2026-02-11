package intern.roima.hrmsbackend.services.Achievement_Module;

import intern.roima.hrmsbackend.dtos.Responses.AchievementPostDto;

public interface SystemPostService {

    AchievementPostDto createBirthdayPost(Long employeeId);
    AchievementPostDto createWorkAnniversaryPost(Long employeeId);

}
