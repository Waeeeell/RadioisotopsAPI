package radioisotops.api.com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import radioisotops.api.com.example.demo.model.User;
import java.util.Optional; // Esto es vital para que no dé error al buscar

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // busca en la base de datos si el email existe
    Optional<User> findByEmail(String email);
}