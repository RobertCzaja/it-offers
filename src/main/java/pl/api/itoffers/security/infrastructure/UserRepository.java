package pl.api.itoffers.security.infrastructure;

import org.springframework.stereotype.Repository;
import pl.api.itoffers.security.application.User;

@Repository
public class UserRepository {
    public User findUserByEmail(String email){
        return new User(email,"123456", "FirstName", "LastName");
    }
}
