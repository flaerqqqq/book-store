package com.epam.rd.autocode.spring.project.mapper;

import com.epam.rd.autocode.spring.project.conf.GlobalMapperConfig;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.model.Client;
import org.mapstruct.Mapper;

@Mapper(config = GlobalMapperConfig.class)
public interface ClientMapper {

    ClientDTO entityToDto(Client entity);

    Client dtoToEntity(ClientDTO dto);
}