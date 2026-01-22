package com.epam.rd.autocode.spring.project.mapper;

import com.epam.rd.autocode.spring.project.conf.GlobalMapperConfig;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.model.Employee;
import org.mapstruct.Mapper;

@Mapper(config = GlobalMapperConfig.class)
public interface EmployeeMapper {

    Employee dtoToEntity(EmployeeDTO dto);

    EmployeeDTO entityToDto(Employee entity);
}