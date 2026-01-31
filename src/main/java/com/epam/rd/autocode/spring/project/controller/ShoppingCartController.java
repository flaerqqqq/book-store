package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.mapper.ShoppingCartMapper;
import com.epam.rd.autocode.spring.project.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/shopping-cart")
@RequiredArgsConstructor
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;
    private final ShoppingCartMapper shoppingCartMapper;
}