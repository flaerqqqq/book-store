package com.epam.rd.autocode.spring.project.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@ControllerAdvice
public class GlobalViewExceptionHandler {

    @ExceptionHandler(value = {
            NotFoundException.class
    })
    public String handleNotFound(NotFoundException ex,
                                 HttpServletRequest req,
                                 HttpServletResponse res,
                                 Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorPath", req.getRequestURI());
        res.setStatus(HttpStatus.NOT_FOUND.value());

        return "error/404";
    }


    @ExceptionHandler(value = {
            MethodArgumentTypeMismatchException.class
    })
    public String handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                   HttpServletRequest req,
                                                   HttpServletResponse res,
                                                   Model model) {
        String errorMessage = "The provided identifier for a resource is not in a valid format";

        if (ex.getRequiredType() != null && ex.getRequiredType().equals(UUID.class)) {
            errorMessage = "The entered resource ID is not valid";
        }

        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("errorPath", req.getRequestURI());
        res.setStatus(HttpStatus.NOT_FOUND.value());

        return "error/404";
    }

    @ExceptionHandler(AuthenticationException.class)
    public String handleAuthentication(AuthenticationException ex,
                                       RedirectAttributes redirectAttributes) {
        String message;

        if (ex instanceof UsernameNotFoundException) {
            message = "User not found";
        } else if (ex instanceof BadCredentialsException) {
            message = "Invalid email or password";
        } else {
            message = "Authentication failed";
        }

        redirectAttributes.addFlashAttribute("errorMessage", message);

        return "redirect:/login";
    }

    @ExceptionHandler(AlreadyExistException.class)
    public String handleAlreadyExists(AlreadyExistException ex,
                                      HttpServletResponse res,
                                      Model model) {
        String entityName = ex.getEntityClass() != null
                ? ex.getEntityClass().getSimpleName()
                : "resource";

        model.addAttribute("errorMessage",
                "Request cannot be completed because it conflicts with existing %s data"
                        .formatted(entityName)
        );

        res.setStatus(HttpStatus.CONFLICT.value());
        return "error/409";
    }

    @ExceptionHandler(IllegalOrderStateException.class)
    public String handleIllegalOrderState(IllegalOrderStateException ex,
                                          HttpServletResponse res,
                                          Model model) {
        model.addAttribute("errorMessage", ex.getMessage());

        res.setStatus(HttpStatus.BAD_REQUEST.value());
        return "error/400";
    }

    @ExceptionHandler(EmptyCartException.class)
    public String handleEmptyCart(EmptyCartException ex,
                                  RedirectAttributes redirectAttributes) {
        redirectAttributes.addAttribute("errorMessage",
                "Your shopping cart empty, add some books before trying to checkout");

        return "redirect:/shopping-cart";
    }
}