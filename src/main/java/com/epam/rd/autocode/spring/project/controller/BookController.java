package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookFilterDto;
import com.epam.rd.autocode.spring.project.dto.BookRequestDto;
import com.epam.rd.autocode.spring.project.dto.BookResponseDto;
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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
    public String getBookListPage(@PageableDefault(sort = "name") Pageable pageable,
                                  @ModelAttribute("bookFilter") @Valid BookFilterDto bookFilter,
                                  BindingResult bindingResult,
                                  Model model) {
        Page<BookResponseDto> bookPage = bookService.findFilteredBooks(bookFilter, pageable)
                .map(bookMapper::dtoToResponseDto);
        model.addAttribute("bookPage", bookPage);
        model.addAttribute("currentPage", pageable.getPageNumber());
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("bookFilter", bookFilter == null ? new BookFilterDto() : bookFilter);

        return "book/book-list";
    }

    @GetMapping("/{publicId}/update")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public String getUpdateBookPage(@PathVariable("publicId") UUID publicId,
                                    Model model) {
        BookDTO foundBook = bookService.getBookByPublicId(publicId);
        model.addAttribute("updateBook", bookMapper.dtoToRequestDto(foundBook));
        model.addAttribute("currentPublicId", publicId);
        return "book/update-book-form";
    }

    @PostMapping("/{publicId}/update")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public String updateBook(@PathVariable("publicId") UUID publicId,
                             @ModelAttribute("updateBook") @Valid BookRequestDto requestDto,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("currentPublicId", publicId);
            return "book/update-book-form";
        }

        BookDTO bookDto = bookMapper.requestDtoToDto(requestDto);
        bookService.updateBookByPublicId(publicId, bookDto);

        return "redirect:/books";
    }

    @PostMapping("/{publicId}/delete")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public String deleteBook(@PathVariable("publicId") UUID publicId) {
        bookService.deleteBookByPublicId(publicId);

        return "redirect:/books";
    }

    @ModelAttribute("languages")
    public Language[] getLanguages() {
        return Language.values();
    }

    @ModelAttribute("ageGroups")
    public AgeGroup[] getAgeGroups() {
        return AgeGroup.values();
    }
}