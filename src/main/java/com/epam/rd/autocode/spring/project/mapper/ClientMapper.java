package com.epam.rd.autocode.spring.project.mapper;

import com.epam.rd.autocode.spring.project.conf.GlobalMapperConfig;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientRegisterRequestDto;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Role;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(config = GlobalMapperConfig.class)
public interface ClientMapper {

    @Mapping(target = "password", ignore = true)
    ClientDTO entityToDto(Client entity);

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    Client dtoToEntity(ClientDTO dto);

    ClientDTO registerDtoToDto(ClientRegisterRequestDto registerDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "balance", ignore = true)
    void updateClientFromDto(ClientDTO dto, @MappingTarget Client client);

    default String mapRoleToString(Role role) {
        if (role == null) {
            return null;
        }

        return role.getName().name();
    }

    default Set<String> mapRoles(Set<Role> roles) {
        if (roles == null) {
            return null;
        }

        return roles.stream()
                .map(this::mapRoleToString)
                .collect(Collectors.toSet());
    }
}