package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.ClientMapper;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientServiceImpl implements ClientService {

    private static final Integer DEFAULT_PAGE_SIZE = 10;

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<ClientDTO> getAllClients(Pageable pageable) {
        pageable = Objects.requireNonNullElse(pageable, Pageable.ofSize(DEFAULT_PAGE_SIZE));

        return clientRepository.findAll(pageable).map(clientMapper::entityToDto);
    }

    @Override
    public ClientDTO getClientByEmail(String email) {
        Objects.requireNonNull(email, "Email must not be null");

        Client client = getClientOrThrow(email);
        return clientMapper.entityToDto(client);
    }

    @Override
    @Transactional
    public ClientDTO updateClientByEmail(String email, ClientDTO client) {
        Objects.requireNonNull(email, "Email must not be null");
        Objects.requireNonNull(client, "Client data must not be null");

        Client existingClient = getClientOrThrow(email);
        clientMapper.updateClientFromDto(client, existingClient);
        Client savedClient = clientRepository.save(existingClient);

        return clientMapper.entityToDto(savedClient);
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

        if (clientRepository.existsByEmail(client.getEmail())) {
            throw new AlreadyExistException(Client.class, "email", client.getEmail());
        }

        String passwordHash = passwordEncoder.encode(client.getPassword());

        Client clientEntity = clientMapper.dtoToEntity(client);
        clientEntity.setBalance(BigDecimal.ZERO);
        clientEntity.setPassword(passwordHash);

        Client savedClient = clientRepository.save(clientEntity);

        return clientMapper.entityToDto(savedClient);
    }

    private Client getClientOrThrow(String email) {
        return clientRepository.findByEmail(email).orElseThrow(() ->
                new NotFoundException(Client.class, "email", email));
    }
}