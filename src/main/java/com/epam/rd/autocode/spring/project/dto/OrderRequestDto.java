package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.enums.DeliveryType;
import com.epam.rd.autocode.spring.project.validation.annotation.RequiredForDelivery;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RequiredForDelivery
public class OrderRequestDto {

    @NotNull(message = "Delivery type is required")
    private DeliveryType deliveryType;

    private String deliveryAddress;

    private String comment;
}