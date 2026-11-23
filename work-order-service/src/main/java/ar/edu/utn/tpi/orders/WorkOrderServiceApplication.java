package ar.edu.utn.tpi.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "ar.edu.utn.tpi")
public class WorkOrderServiceApplication {
    public static void main(String[] args) { SpringApplication.run(WorkOrderServiceApplication.class, args); }
}
