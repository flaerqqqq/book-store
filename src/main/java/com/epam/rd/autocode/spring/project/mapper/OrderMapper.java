package com.epam.rd.autocode.spring.project.mapper;

import com.epam.rd.autocode.spring.project.conf.GlobalMapperConfig;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.dto.OrderSummaryDto;
import com.epam.rd.autocode.spring.project.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class, uses = {
        OrderItemMapper.class
})
public interface OrderMapper {

    @Mapping(target = "clientPublicId", source = "client.publicId")
    @Mapping(target = "employeePublicId", source = "employee.publicId")
    @Mapping(target = "cancelledByPublicId", source = "cancelledBy.publicId")
    OrderDTO entityToDto(Order entity);

    Order dtoToEntity(OrderDTO dto);

    @Mapping(target = "totalItems", expression = "java(entity.getOrderItems() != null ? entity.getOrderItems().size() : 0)")
    @Mapping(target = "clientPublicId", source = "client.publicId")
    @Mapping(target = "employeePublicId", source = "employee.publicId")
    @Mapping(target = "cancelledByPublicId", source = "cancelledBy.publicId")
    OrderSummaryDto entityToSummaryDto(Order entity);
}