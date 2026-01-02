package com.tp.service_grpc_springboot.controllers;

import com.tp.service_grpc_springboot.entities.Compte;
import com.tp.service_grpc_springboot.services.CompteService;
import com.tp.service_grpc_springboot.stubs.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class CompteServiceImpl extends CompteServiceGrpc.CompteServiceImplBase {

    private final CompteService compteService;

    @Autowired
    public CompteServiceImpl(CompteService compteService) {
        this.compteService = compteService;
    }

    /**
     * Récupérer tous les comptes
     */
    @Override
    public void allComptes(GetAllComptesRequest request,
                           StreamObserver<GetAllComptesResponse> responseObserver) {
        try {
            List<Compte> comptesEntity = compteService.findAllComptes();

            List<com.tp.service_grpc_springboot.stubs.Compte> comptes = comptesEntity.stream()
                    .map(compte -> com.tp.service_grpc_springboot.stubs.Compte.newBuilder()
                            .setId(compte.getId())
                            .setSolde(compte.getSolde())
                            .setDateCreation(compte.getDateCreation())
                            .setType(TypeCompte.valueOf(compte.getType()))
                            .build())
                    .collect(Collectors.toList());

            GetAllComptesResponse response = GetAllComptesResponse.newBuilder()
                    .addAllComptes(comptes)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    /**
     * Récupérer un compte par son ID
     */
    @Override
    public void compteById(GetCompteByIdRequest request,
                           StreamObserver<GetCompteByIdResponse> responseObserver) {
        try {
            Compte compteEntity = compteService.findCompteById(request.getId());

            if (compteEntity != null) {
                com.tp.service_grpc_springboot.stubs.Compte compte =
                        com.tp.service_grpc_springboot.stubs.Compte.newBuilder()
                                .setId(compteEntity.getId())
                                .setSolde(compteEntity.getSolde())
                                .setDateCreation(compteEntity.getDateCreation())
                                .setType(TypeCompte.valueOf(compteEntity.getType()))
                                .build();

                GetCompteByIdResponse response = GetCompteByIdResponse.newBuilder()
                        .setCompte(compte)
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(
                        new Throwable("Compte avec l'ID " + request.getId() + " non trouvé")
                );
            }
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    /**
     * Obtenir les statistiques des soldes
     */
    @Override
    public void totalSolde(GetTotalSoldeRequest request,
                           StreamObserver<GetTotalSoldeResponse> responseObserver) {
        try {
            List<Compte> comptes = compteService.findAllComptes();

            int count = comptes.size();
            float sum = compteService.calculateTotalSolde();
            float average = count > 0 ? sum / count : 0;

            SoldeStats stats = SoldeStats.newBuilder()
                    .setCount(count)
                    .setSum(sum)
                    .setAverage(average)
                    .build();

            GetTotalSoldeResponse response = GetTotalSoldeResponse.newBuilder()
                    .setStats(stats)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    /**
     * Sauvegarder un nouveau compte
     */
    @Override
    public void saveCompte(SaveCompteRequest request,
                           StreamObserver<SaveCompteResponse> responseObserver) {
        try {
            CompteRequest compteReq = request.getCompte();

            // Créer l'entité JPA
            Compte compteEntity = new Compte();
            compteEntity.setSolde(compteReq.getSolde());
            compteEntity.setDateCreation(compteReq.getDateCreation());
            compteEntity.setType(compteReq.getType().name());

            // Sauvegarder dans la base de données
            Compte savedCompte = compteService.saveCompte(compteEntity);

            // Créer le message gRPC de réponse
            com.tp.service_grpc_springboot.stubs.Compte grpcCompte =
                    com.tp.service_grpc_springboot.stubs.Compte.newBuilder()
                            .setId(savedCompte.getId())
                            .setSolde(savedCompte.getSolde())
                            .setDateCreation(savedCompte.getDateCreation())
                            .setType(TypeCompte.valueOf(savedCompte.getType()))
                            .build();

            SaveCompteResponse response = SaveCompteResponse.newBuilder()
                    .setCompte(grpcCompte)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}