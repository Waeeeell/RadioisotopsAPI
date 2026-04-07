package radioisotops.api.com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import radioisotops.api.com.example.demo.model.User;
import java.util.List; // Importante para devolver varios usuarios
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    List<User> findByRol(String rol);
}