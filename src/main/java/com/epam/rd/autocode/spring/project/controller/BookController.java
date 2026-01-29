package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookRequestDto;
import com.epam.rd.autocode.spring.project.mapper.BookMapper;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final BookMapper bookMapper;

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public String getBookCreatePage(Model model) {
        model.addAttribute("bookRequest", new BookRequestDto());
        model.addAttribute("languages", Language.values());
        model.addAttribute("ageGroups", AgeGroup.values());

        return "book/add-book-form";
    }

    @PostMapping("/new")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public String addBook(@ModelAttribute("bookRequest") @Valid BookRequestDto requestDto,
                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "book/add-book-form";
        }

        BookDTO bookDto = bookMapper.requestDtoToDto(requestDto);
        bookService.addBook(bookDto);

        return "redirect:/books";
    }

    @GetMapping
    public String getBookListPage(@PageableDefault(size = 5, sort = "name") Pageable pageable,
                                  Model model) {
        Page<BookDTO> bookPage = bookService.getAllBooks(pageable);
        model.addAttribute("bookPage", bookPage);
        model.addAttribute("currentPage", pageable.getPageNumber());
        model.addAttribute("totalPages", bookPage.getTotalPages());

        return "book/book-list";
    }

}