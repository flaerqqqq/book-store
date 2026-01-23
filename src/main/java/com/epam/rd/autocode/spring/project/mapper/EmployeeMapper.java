package com.epam.rd.autocode.spring.project.mapper;

import com.epam.rd.autocode.spring.project.conf.GlobalMapperConfig;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.model.Employee;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class)
public interface EmployeeMapper {

    @Mapping(target = "password", ignore = true)
    EmployeeDTO entityToDto(Employee entity);

    Employee dtoToEntity(EmployeeDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "email", ignore = true)
    void updateEmployeeFromDto(EmployeeDTO dto, @MappingTarget Employee entity);
}