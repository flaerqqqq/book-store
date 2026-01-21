package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.ClientMapper;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<ClientDTO> getAllClients() {
        return List.of();
    }

    @Override
    public ClientDTO getClientByEmail(String email) {
        return null;
    }

    @Override
    public ClientDTO updateClientByEmail(String email, ClientDTO client) {
        return null;
    }

    @Override
    @Transactional
    public void deleteClientByEmail(String email) {
        Objects.requireNonNull(email, "Email must not be null");

        Long deletedCount = clientRepository.deleteByEmail(email);

        if (deletedCount == 0) {
            throw new NotFoundException(Client.class, "email", email);
        }
    }

    @Override
    @Transactional
    public ClientDTO addClient(ClientDTO client) {
        Objects.requireNonNull(client, "Method argument cannot be null");

        if (clientRepository.existsByEmail(client.getEmail()))
            throw new AlreadyExistException(Client.class, "email", client.getEmail());

        String passwordHash = passwordEncoder.encode(client.getPassword());

        Client clientEntity = clientMapper.dtoToEntity(client);
        clientEntity.setPassword(passwordHash);

        Client savedClient = clientRepository.save(clientEntity);

        return clientMapper.entityToDto(savedClient);
    }
}