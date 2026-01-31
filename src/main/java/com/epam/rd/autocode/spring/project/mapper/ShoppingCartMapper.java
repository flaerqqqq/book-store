package com.epam.rd.autocode.spring.project.mapper;

import com.epam.rd.autocode.spring.project.conf.GlobalMapperConfig;
import com.epam.rd.autocode.spring.project.dto.ShoppingCartDto;
import com.epam.rd.autocode.spring.project.model.ShoppingCart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class, uses = {
        ShoppingCartItemMapper.class
})
public interface ShoppingCartMapper {

    @Mapping(target = "userPublicId", source = "user.publicId")
    ShoppingCartDto entityToDto(ShoppingCart entity);

    ShoppingCart dtoToEntity(ShoppingCartDto dto);
}