package intern.roima.hrmsbackend.bootstrap;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import intern.roima.hrmsbackend.entities.Achievement_Module.ModerationType;
import intern.roima.hrmsbackend.entities.Achievement_Module.ModerationTypes;
import intern.roima.hrmsbackend.repositories.Achievement_Module.ModerationTypeRepository;

@Component
public class ModerationTypeSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final ModerationTypeRepository moderationTypeRepository;

    public ModerationTypeSeeder(ModerationTypeRepository moderationTypeRepository) {
        this.moderationTypeRepository = moderationTypeRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.loadModerationTypes();
    }

    private void loadModerationTypes() {
        ModerationType[] typeNames = new ModerationType[]{
                ModerationType.POST_DELETE,
                ModerationType.COMMENT_DELETE
        };

        for (ModerationType typeName : typeNames) {
            if (moderationTypeRepository.findByTypeName(typeName.name()).isEmpty()) {
                ModerationTypes moderationType = new ModerationTypes();
                moderationType.setTypeName(typeName.name());
                moderationType.setDescription("Moderation type for " + typeName.name().toLowerCase().replace("_", " "));
                moderationType.setActive(true);
                moderationType.setCreatedAt(LocalDateTime.now());
                moderationType.setUpdatedAt(LocalDateTime.now());
                moderationTypeRepository.save(moderationType);
            }
        }
    }
}
