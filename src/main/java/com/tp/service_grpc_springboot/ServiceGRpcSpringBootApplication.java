package com.tp.service_grpc_springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServiceGRpcSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceGRpcSpringBootApplication.class, args);
        System.out.println("Serveur gRPC démarré sur le port 9090");
    }

}
