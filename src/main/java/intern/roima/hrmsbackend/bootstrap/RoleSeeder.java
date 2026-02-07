package intern.roima.hrmsbackend.bootstrap;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import intern.roima.hrmsbackend.entities.Employee_Module.RoleEnum;
import intern.roima.hrmsbackend.entities.Employee_Module.Roles;
import intern.roima.hrmsbackend.repositories.Employee_Module.RoleRepository;

@Component
public class RoleSeeder implements ApplicationListener<ContextRefreshedEvent> {
   private final RoleRepository RoleRepository;

   public RoleSeeder(RoleRepository RoleRepository) {
       this.RoleRepository = RoleRepository;
   }

   @Override
   public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
       this.loadRoles();
   }

   private void loadRoles() {
       RoleEnum[] roleNames = new RoleEnum[]{
               RoleEnum.EMPLOYEE,
               RoleEnum.HR,
               RoleEnum.MANAGER
       };

       for (RoleEnum RoleName : roleNames) {
           if (!RoleRepository.existsByRoleName(RoleName.name())) {
               Roles role = new Roles();
               role.setRoleName(RoleName.name());
               RoleRepository.save(role);
           }
       }
   }
}