package fr.mossaab.docmultilang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DocMultilangApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocMultilangApplication.class, args);
        System.out.println("get swagger http://localhost:8080/swagger-ui/index.html");
    }
}
