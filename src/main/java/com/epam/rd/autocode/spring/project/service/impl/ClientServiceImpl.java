package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.ClientMapper;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Role;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.RoleRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientServiceImpl implements ClientService {

    private static final Integer DEFAULT_PAGE_SIZE = 10;

    private final ClientRepository clientRepository;
    private final RoleRepository roleRepository;
    private final ClientMapper clientMapper;
    private final PasswordEncoder passwordEncoder;
    private final ShoppingCartService shoppingCartService;

    @Override
    public Page<ClientDTO> getAllClients(Pageable pageable) {
        pageable = Objects.requireNonNullElse(pageable, Pageable.ofSize(DEFAULT_PAGE_SIZE));

        return clientRepository.findAll(pageable).map(clientMapper::entityToDto);
    }

    @Override
    @Transactional
    public ClientDTO addClient(ClientDTO client) {
        Objects.requireNonNull(client, "Client data cannot be null");

        if (clientRepository.existsByEmail(client.getEmail())) {
            throw new AlreadyExistException(Client.class, "email", client.getEmail());
        }

        Role clientRole = roleRepository.findByName(Role.RoleName.ROLE_CLIENT).orElseThrow(() ->
                new NotFoundException(Role.class, "name", Role.RoleName.ROLE_CLIENT));

        String passwordHash = passwordEncoder.encode(client.getPassword());

        Client clientEntity = clientMapper.dtoToEntity(client);
        clientEntity.setPassword(passwordHash);
        clientEntity.getRoles().add(clientRole);

        Client savedClient = clientRepository.save(clientEntity);

        shoppingCartService.createCart(savedClient.getPublicId());

        return clientMapper.entityToDto(savedClient);
    }

    @Override
    public ClientDTO getClientByPublicId(UUID publicId) {
        Objects.requireNonNull(publicId, "Client public ID must not be null");

        return clientMapper.entityToDto(getClientOrThrow(publicId));
    }

    private Client getClientOrThrow(UUID clientPublicId) {
        return clientRepository.findByPublicId(clientPublicId).orElseThrow(() ->
                new NotFoundException(Client.class, "publicId", clientPublicId));
    }
}