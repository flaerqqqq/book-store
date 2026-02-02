package com.epam.rd.autocode.spring.project.mapper;

import com.epam.rd.autocode.spring.project.conf.GlobalMapperConfig;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class, uses = {
        OrderItemMapper.class
})
public interface OrderMapper {

    @Mapping(target = "clientPublicId", source = "client.publicId")
    @Mapping(target = "employeePublicId", source = "employee.publicId")
    OrderDTO entityToDto(Order entity);

    Order dtoToEntity(OrderDTO dto);
}