package com.epam.rd.autocode.spring.project.mapper;

import com.epam.rd.autocode.spring.project.conf.GlobalMapperConfig;
import com.epam.rd.autocode.spring.project.dto.OrderItemDto;
import com.epam.rd.autocode.spring.project.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface OrderItemMapper {

    @Mapping(target = "orderPublicId", source = "order.publicId")
    OrderItemDto entityToDto(OrderItem entity);

    OrderItem dtoToEntity(OrderItemDto dto);
}