package com.epam.rd.autocode.spring.project.controller.advice;

import com.epam.rd.autocode.spring.project.model.enums.DeliveryType;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.security.SecurityUtils;
import com.epam.rd.autocode.spring.project.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.math.BigDecimal;
import java.util.UUID;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final ClientService clientService;

    @ModelAttribute("clientBalance")
    public BigDecimal injectBalance() {
        if (SecurityUtils.isClient()) {
            UUID clientPublicId = SecurityUtils.getCurrentUserPublicId();
            return clientService.getClientByPublicId(clientPublicId).getBalance();
        }

        return null;
    }

    @ModelAttribute("deliveryTypes")
    public DeliveryType[] getDeliveryTypes() {
        return DeliveryType.values();
    }

    @ModelAttribute("orderStatuses")
    public OrderStatus[] getOrderStatuses() {
        return OrderStatus.values();
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }
}