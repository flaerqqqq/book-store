package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.ClientMapper;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Role;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.RoleRepository;
import com.epam.rd.autocode.spring.project.service.fixture.ClientTestFixture;
import com.epam.rd.autocode.spring.project.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private ShoppingCartService shoppingCartService;
    @Mock
    private PasswordEncoder passwordEncoder;
    private ClientMapper clientMapper = Mappers.getMapper(ClientMapper.class);

    private ClientServiceImpl clientService;

    private static final int DEFAULT_PAGE_SIZE = 10;
    private final Role.RoleName CLIENT_ROLE_NAME = Role.RoleName.ROLE_CLIENT;

    private String clientEmail;
    private UUID clientPublicId;
    private Client clientEntity;
    private ClientDTO clientDto;

    @BeforeEach
    public void setUpFixture() {
        clientEmail = ClientTestFixture.DEFAULT_EMAIL;
        clientPublicId = ClientTestFixture.DEFAULT_PUBLIC_ID;
        clientEntity = ClientTestFixture.getDefaultClient();
        clientDto = ClientTestFixture.getDefaultClientDto();
    }

    @BeforeEach
    public void setUpService() {
        clientService = new ClientServiceImpl(
                clientRepository,
                roleRepository,
                clientMapper,
                passwordEncoder,
                shoppingCartService
        );
    }

    @Test
    void getAllClients_ShouldReturnRequestedPage_WhenPageableIsPresent() {
        Pageable inputPageable = PageRequest.of(0, 1);
        Page<Client> clientPage = new PageImpl<>(Collections.singletonList(clientEntity), inputPageable, 1);

        when(clientRepository.findAll(any(Pageable.class))).thenReturn(clientPage);

        Page<ClientDTO> actualReturnPage = clientService.getAllClients(inputPageable);

        assertThat(actualReturnPage).isNotNull();
        assertThat(actualReturnPage.getTotalElements()).isEqualTo(1);
        assertThat(actualReturnPage.getContent()).hasSize(1);

        ClientDTO actualClientDto = actualReturnPage.getContent().get(0);
        assertThat(actualClientDto.getEmail()).isEqualTo(clientEmail);

        verify(clientRepository, times(1)).findAll(inputPageable);
    }

    @Test
    void getAllClient_ShouldReturnDefaultPage_WhenPageableIsNull() {
        Page<Client> clientPage = Page.empty();
        when(clientRepository.findAll(any(Pageable.class))).thenReturn(clientPage);

        clientService.getAllClients(null);

        ArgumentCaptor<Pageable> argCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(clientRepository).findAll(argCaptor.capture());

        Pageable capturedPageable = argCaptor.getValue();

        assertThat(capturedPageable).isNotNull();
        assertThat(capturedPageable.getPageSize()).isEqualTo(DEFAULT_PAGE_SIZE);
    }

    @Test
    void addClient_ShouldSuccessfullyReturn_WhenRoleFound() {
        String passwordHash = "passwordHash";
        Role role = new Role(1L, CLIENT_ROLE_NAME);

        when(roleRepository.findByName(CLIENT_ROLE_NAME)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(clientDto.getPassword())).thenReturn(passwordHash);
        when(clientRepository.save(any(Client.class))).thenReturn(clientEntity);

        ClientDTO actualReturnClientDto = clientService.addClient(clientDto);

        ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository, times(1)).save(clientCaptor.capture());
        Client capturedClient = clientCaptor.getValue();

        assertThat(capturedClient).isNotNull();
        assertThat(capturedClient.getPassword()).isEqualTo(passwordHash);

        assertThat(actualReturnClientDto.getEmail()).isEqualTo(clientEntity.getEmail());

        verify(clientRepository, times(1)).existsByEmail(clientEmail);
        verify(passwordEncoder, times(1)).encode(clientDto.getPassword());
    }

    @Test
    void addClient_ShouldThrowNotFound_WhenRoleNotFound() {
        when(roleRepository.findByName(CLIENT_ROLE_NAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.addClient(clientDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(CLIENT_ROLE_NAME.toString());
    }

    @Test
    void addClient_ShouldThrowAlreadyExist_WhenEmailIsNotUnique() {
        when(clientRepository.existsByEmail(clientEmail)).thenReturn(true);

        AlreadyExistException thrown = assertThrows(AlreadyExistException.class, () ->
                clientService.addClient(ClientDTO.builder().email(clientEmail).build()));
        assertThat(thrown.getMessage()).contains(clientEmail);
    }

    @Test
    void addClient_ShouldThrowNullPointer_WhenInputClientDtoIsNull() {
        assertThrows(NullPointerException.class, () ->
                clientService.addClient(null));
    }

    @Test
    void getClientByPublicId_ShouldReturnCorrectClient() {
        when(clientRepository.findByPublicId(clientPublicId)).thenReturn(Optional.of(clientEntity));

        ClientDTO actualReturnClient = clientService.getClientByPublicId(clientPublicId);

        assertThat(actualReturnClient).isNotNull()
                .extracting(ClientDTO::getPublicId)
                .isEqualTo(clientPublicId);
    }

    @Test
    void getClientByPublicId_ShouldThrowNotFound() {
        when(clientRepository.findByPublicId(clientPublicId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.getClientByPublicId(clientPublicId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(clientPublicId.toString());
    }
}