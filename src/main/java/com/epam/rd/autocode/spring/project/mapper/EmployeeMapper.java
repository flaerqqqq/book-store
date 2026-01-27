package com.epam.rd.autocode.spring.project.mapper;

import com.epam.rd.autocode.spring.project.conf.GlobalMapperConfig;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.Role;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(config = GlobalMapperConfig.class)
public interface EmployeeMapper {

    @Mapping(target = "password", ignore = true)
    EmployeeDTO entityToDto(Employee entity);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    Employee dtoToEntity(EmployeeDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateEmployeeFromDto(EmployeeDTO dto, @MappingTarget Employee entity);

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