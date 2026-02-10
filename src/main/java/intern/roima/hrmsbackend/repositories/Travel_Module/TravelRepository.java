package intern.roima.hrmsbackend.repositories.Travel_Module;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Travel_Module.Travels;

public interface  TravelRepository extends JpaRepository<Travels, Long>{
    
}
