package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ClientService {

    Page<ClientDTO> getAllClients(Pageable pageable);

    ClientDTO addClient(ClientDTO client);

    ClientDTO getClientByPublicId(UUID publicId);
}