package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.ClientMapper;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private ClientMapper clientMapper = Mappers.getMapper(ClientMapper.class);

    private ClientServiceImpl clientService;

    @BeforeEach
    public void setUp() {
        clientService = new ClientServiceImpl(
                clientRepository,
                clientMapper,
                passwordEncoder
        );
    }

    @Test
    void getAllClients_ShouldReturnRequestedPage_WhenPageableIsPresent() {
        Client client = ClientTestFixture.getDefaultClient();
        Pageable inputPageable = PageRequest.of(0, 1);
        Page<Client> clientPage = new PageImpl<>(Collections.singletonList(client), inputPageable, 1);

        when(clientRepository.findAll(any(Pageable.class))).thenReturn(clientPage);

        Page<ClientDTO> actualReturnPage = clientService.getAllClients(inputPageable);

        assertThat(actualReturnPage).isNotNull();
        assertThat(actualReturnPage.getTotalElements()).isEqualTo(1);
        assertThat(actualReturnPage.getContent()).hasSize(1);

        ClientDTO actualClientDto = actualReturnPage.getContent().get(0);
        assertThat(actualClientDto.getEmail()).isEqualTo(client.getEmail());

        verify(clientRepository, times(1)).findAll(inputPageable);
    }

    @Test
    void getAllClient_ShouldReturnDefaultPage_WhenPageableIsNull() {
        int expectedDefaultSize = 10;

        Page<Client> clientPage = new PageImpl<>(Collections.emptyList());
        when(clientRepository.findAll(any(Pageable.class))).thenReturn(clientPage);

        clientService.getAllClients(null);

        ArgumentCaptor<Pageable> argCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(clientRepository).findAll(argCaptor.capture());

        Pageable capturedPageable = argCaptor.getValue();

        assertThat(capturedPageable).isNotNull();
        assertThat(capturedPageable.getPageSize()).isEqualTo(expectedDefaultSize);
    }

    @Test
    void getClientByEmail_ShouldReturnClient_WhenClientExistsByEmail() {
        String inputEmail = ClientTestFixture.DEFAULT_EMAIL;
        Client client = ClientTestFixture.getDefaultClient();

        when(clientRepository.findByEmail(inputEmail)).thenReturn(Optional.of(client));

        ClientDTO actualReturnClient = clientService.getClientByEmail(inputEmail);

        assertThat(actualReturnClient).isNotNull();
        assertThat(actualReturnClient.getEmail()).isEqualTo(inputEmail);
        assertThat(actualReturnClient.getName()).isEqualTo(client.getName());

        verify(clientRepository, times(1)).findByEmail(inputEmail);
    }

    @Test
    void getClientByEmail_ShouldThrowNotFound_WhenClientDoesNotExistByEmail() {
        String inputEmail = ClientTestFixture.DEFAULT_EMAIL;

        when(clientRepository.findByEmail(inputEmail)).thenReturn(Optional.empty());

        NotFoundException thrown = assertThrows(NotFoundException.class, () ->
                clientService.getClientByEmail(inputEmail));

        assertThat(thrown.getMessage()).contains(inputEmail);

        verify(clientRepository, times(1)).findByEmail(inputEmail);
    }

    @Test
    void getClientByEmail_ShouldThrowNullPointer_WhenEmailIsNull() {
        String inputEmail = null;

        assertThrows(NullPointerException.class, () ->
                clientService.getClientByEmail(inputEmail));
    }

    @Test
    void updateClientByEmail_ShouldUpdateAndReturnClient_whenClientExistsByEmail() {
        String updateClientEmail = ClientTestFixture.DEFAULT_EMAIL;
        Client clientToBeUpdated = ClientTestFixture.getDefaultClient();
        ClientDTO updateClientDto = ClientDTO.builder()
                        .name("name2")
                        .build();

        when(clientRepository.findByEmail(updateClientEmail)).thenReturn(Optional.of(clientToBeUpdated));
        when(clientRepository.save(clientToBeUpdated)).thenReturn(clientToBeUpdated);

        ClientDTO actualReturnClientDto = clientService.updateClientByEmail(updateClientEmail, updateClientDto);

        assertThat(actualReturnClientDto).isNotNull();
        assertThat(actualReturnClientDto.getEmail()).isEqualTo(updateClientEmail);
        assertThat(actualReturnClientDto.getName()).isEqualTo(updateClientDto.getName());
        assertThat(actualReturnClientDto.getBalance()).isEqualTo(clientToBeUpdated.getBalance());

        verify(clientRepository, times(1)).findByEmail(updateClientEmail);
        verify(clientRepository, times(1)).save(clientToBeUpdated);
    }

    @Test
    void updateClientByEmail_ShouldThrowNullPointer_WhenEmailIsNull() {
        assertThrows(NullPointerException.class, () ->
                clientService.updateClientByEmail(null, ClientDTO.builder().build()));
    }

    @Test
    void updateClientByEmail_ShouldThrowNullPointer_WhenClientDtoIsNull() {
        assertThrows(NullPointerException.class, () ->
                clientService.updateClientByEmail(ClientTestFixture.DEFAULT_EMAIL, null));
    }

    @Test
    void updateClientByEmail_ShouldThrowNotFond_WhenClientIsNotFoundByEmail() {
        String inputEmail = ClientTestFixture.DEFAULT_EMAIL;

        when(clientRepository.findByEmail(inputEmail)).thenReturn(Optional.empty());

        NotFoundException thrown = assertThrows(NotFoundException.class, () ->
                clientService.updateClientByEmail(inputEmail, ClientDTO.builder().build()));
        assertThat(thrown.getMessage()).contains(inputEmail);

        verify(clientRepository, times(1)).findByEmail(inputEmail);
    }

    @Test
    void deleteClientByEmail_ShouldDelete_WhenClientExistsByEmail() {
        String inputEmail = ClientTestFixture.DEFAULT_EMAIL;
        Long deletedCount = 1L;

        when(clientRepository.deleteByEmail(inputEmail)).thenReturn(deletedCount);

        clientService.deleteClientByEmail(inputEmail);

        verify(clientRepository, times(1)).deleteByEmail(inputEmail);
    }

    @Test
    void deleteClientByEmail_ShouldThrowNotFound_WhenClientIsNotFound() {
        String inputEmail = ClientTestFixture.DEFAULT_EMAIL;
        Long deletedCount = 0L;

        when(clientRepository.deleteByEmail(inputEmail)).thenReturn(deletedCount);

        NotFoundException thrown = assertThrows(NotFoundException.class, () ->
                clientService.deleteClientByEmail(inputEmail));
        assertThat(thrown.getMessage()).contains(inputEmail);
    }

    @Test
    void deleteClientByEmail_ShouldThrowNullPointer_WhenInputEmailIsNull() {
        assertThrows(NullPointerException.class, () ->
                clientService.deleteClientByEmail(null));
    }

    @Test
    void addClient_ShouldSuccessfullyReturn_WhenEmailIsUnique() {
        String email = ClientTestFixture.DEFAULT_EMAIL;
        String passwordHash = "passwordHash";
        Client savedClient = ClientTestFixture.getDefaultClient();
        ClientDTO inputClientDto = ClientTestFixture.getDefaultClientDto();

        when(clientRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(inputClientDto.getPassword())).thenReturn(passwordHash);
        when(clientRepository.save(any(Client.class))).thenReturn(savedClient);

        ClientDTO actualReturnClientDto = clientService.addClient(inputClientDto);

        ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository, times(1)).save(clientCaptor.capture());
        Client capturedClient = clientCaptor.getValue();

        assertThat(capturedClient).isNotNull();
        assertThat(capturedClient.getPassword()).isEqualTo(passwordHash);

        assertThat(actualReturnClientDto.getEmail()).isEqualTo(savedClient.getEmail());

        verify(clientRepository, times(1)).existsByEmail(email);
        verify(passwordEncoder, times(1)).encode(inputClientDto.getPassword());
    }

    @Test
    void addClient_ShouldThrowAlreadyExist_WhenEmailIsNotUnique() {
        String inputEmail = ClientTestFixture.DEFAULT_EMAIL;

        when(clientRepository.existsByEmail(inputEmail)).thenReturn(true);

        AlreadyExistException thrown = assertThrows(AlreadyExistException.class, () ->
                clientService.addClient(ClientDTO.builder().email(inputEmail).build()));
        assertThat(thrown.getMessage()).contains(inputEmail);
    }

    @Test
    void addClient_ShouldThrowNullPointer_WhenInputClientDtoIsNull() {
        assertThrows(NullPointerException.class, () ->
                clientService.addClient(null));
    }
}