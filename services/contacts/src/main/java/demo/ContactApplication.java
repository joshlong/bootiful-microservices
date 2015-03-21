package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Arrays;
import java.util.Collection;

interface ContactRepository extends JpaRepository<Contact, Long> {
    Collection<Contact> findByUserId(String userId);
}

@SpringCloudApplication
//@EnableOAuth2Resource
public class ContactApplication {

    public static void main(String args[]) throws Throwable {
        SpringApplication.run(ContactApplication.class, args);
    }

    @Bean
    CommandLineRunner init(ContactRepository cr) {
        return args ->
                Arrays.asList("jlong,rwinch,dsyer,pwebb,sgibb".split(",")).forEach(
                        userId -> Arrays.asList("Dave,Syer;Phil,Webb;Juergen,Hoeller".split(";"))
                                .stream()
                                .map(n -> n.split(","))
                                .forEach(name -> cr.save(new Contact(
                                        userId, name[0], name[1], name[0].toLowerCase() + "@email.com"))));
    }
}

@RestController
class ContactRestController {
    @Autowired
    private ContactRepository contactRepository;

    @RequestMapping("/{userId}/contacts")
    Collection<Contact> contacts(@PathVariable String userId) {
        return this.contactRepository.findByUserId(userId);
    }
}

@Entity
class Contact {

    @Id
    @GeneratedValue
    private Long id;
    private String userId, firstName, lastName, email;

    public Contact() {
    }

    public Contact(String userId, String firstName, String lastName, String email) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}