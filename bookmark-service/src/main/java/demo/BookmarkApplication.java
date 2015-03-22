package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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

@SpringCloudApplication
public class BookmarkApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookmarkApplication.class, args);
    }

    private String descriptionForBookmark(String mask, String userId, String href) {
        return mask.replaceFirst("_L_", href)
                .replaceFirst("_U_", userId);
    }

    @Autowired
    void setMessage(@Value("${message}") String m) {
        System.out.println("message=" + m);
     }

    @Bean
    CommandLineRunner init(@Value("${bookmark.mask}") String bookmarkMask, BookmarkRepository br) {
        return args ->
                Arrays.asList("jlong,rwinch,dsyer,pwebb,sgibb".split(",")).forEach(userId -> {
                    String href = String.format("http://%s-link.com", userId);
                    String descriptionForBookmark = this.descriptionForBookmark(bookmarkMask, userId, href);
                    br.save(new Bookmark(href, userId, descriptionForBookmark));
                });
    }
}

@RefreshScope
@RestController
class MessageRestController {

    @Value("${message}")
    private String message;

    @RequestMapping("/message")
    String msg() {
        return this.message;
    }

}


interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Collection<Bookmark> findByUserId(String userId);
}


@RestController
class BookmarkRestController {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @RequestMapping("/{userId}/bookmarks")
    Collection<Bookmark> bookmarks(@PathVariable String userId) {
        return this.bookmarkRepository.findByUserId(userId);
    }
}

@Entity
class Bookmark {

    @Id
    @GeneratedValue
    private Long id;
    private String href, userId, description;

    Bookmark() {
    }

    public Bookmark(
            String href,
            String userId,
            String description) {

        this.href = href;
        this.userId = userId;
        this.description = description;
    }

    @Override
    public String toString() {
        return "Bookmark{" +
                "id=" + id +
                ", href='" + href + '\'' +
                ", userId='" + userId + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public Long getId() {

        return id;
    }

    public String getHref() {
        return href;
    }

    public String getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }
}