package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;

import java.util.*;

public interface OrderService {

    List<OrderDTO> getOrdersByClient(UUID clientPublicId);

    List<OrderDTO> getOrdersByEmployee(UUID employeePublicId);

    OrderDTO createFromShoppingCart(UUID clientPublicId);
}