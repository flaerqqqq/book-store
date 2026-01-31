package com.epam.rd.autocode.spring.project.mapper;

import com.epam.rd.autocode.spring.project.conf.GlobalMapperConfig;
import com.epam.rd.autocode.spring.project.dto.ShoppingCartItemDto;
import com.epam.rd.autocode.spring.project.model.ShoppingCartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class, uses = {
        BookMapper.class
})
public interface ShoppingCartItemMapper {

    @Mapping(target = "cartPublicId", source = "cart.publicId")
    ShoppingCartItemDto entityToDto(ShoppingCartItem entity);

    ShoppingCartItem dtoToEntity(ShoppingCartItemDto dto);
}