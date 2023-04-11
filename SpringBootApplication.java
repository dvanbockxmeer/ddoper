package nl.minvenw.rws.loadtest;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.http.ResponseEntity;

@org.springframework.boot.autoconfigure.SpringBootApplication
public class SpringBootApplication implements CommandLineRunner {

    private static Logger LOGGER = LoggerFactory.getLogger(SpringBootApplication.class);

    private final MainController controller;

    public SpringBootApplication(MainController controller) {
        this.controller = Objects.requireNonNull(controller);
    }

    public static void main(String[] args) {
        LOGGER.info("STARTING THE APPLICATION");
        SpringApplication.run(SpringBootApplication.class, args);
        LOGGER.info("APPLICATION FINISHED");
    }

    @Override
    public void run(String... args) {
         for (String s : args) {
            LOGGER.info("Parameter {}", s);
        }
        ResponseEntity<String> entity = controller.readDDOper(args);
        LOGGER.info("{}", entity.getStatusCode());
        LOGGER.info("{}", entity.getBody());
        System.exit(0);
    }
}
