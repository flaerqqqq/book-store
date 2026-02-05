package com.epam.rd.autocode.spring.project.controller.view;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookFilterDto;
import com.epam.rd.autocode.spring.project.dto.BookRequestDto;
import com.epam.rd.autocode.spring.project.dto.BookResponseDto;
import com.epam.rd.autocode.spring.project.mapper.BookMapper;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.security.CustomUserDetails;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.ShoppingCartService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final ShoppingCartService cartService;
    private final BookMapper bookMapper;
    private final ObjectMapper objectMapper;

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public String getBookCreatePage(Model model) {
        model.addAttribute("bookRequest", new BookRequestDto());

        return "book/add-book-form";
    }

    @PostMapping("/new")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
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
    public String getBookListPage(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @PageableDefault(sort = "name") Pageable pageable,
                                  @ModelAttribute("bookFilter") @Valid BookFilterDto bookFilter,
                                  BindingResult bindingResult,
                                  Model model) {
        Page<BookResponseDto> bookPage = bookService.findFilteredBooks(bookFilter, pageable)
                .map(bookMapper::dtoToResponseDto);

        if (userDetails != null && userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_CLIENT"))) {
            Set<UUID> cartItemBookIds = cartService.getCartItemBookIds(userDetails.getPublicId());
            bookPage.forEach(book -> book.setInCart(cartItemBookIds.contains(book.getPublicId())));
        }

        model.addAttribute("bookPage", bookPage);
        model.addAttribute("currentPage", pageable.getPageNumber());
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("bookFilter", bookFilter == null ? new BookFilterDto() : bookFilter);

        return "book/book-list";
    }

    @GetMapping("/{publicId}/update")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String getUpdateBookPage(@PathVariable("publicId") UUID publicId,
                                    Model model) {
        BookDTO foundBook = bookService.getBookByPublicId(publicId);
        model.addAttribute("updateBook", bookMapper.dtoToRequestDto(foundBook));
        model.addAttribute("currentPublicId", publicId);
        return "book/update-book-form";
    }

    @PostMapping("/{publicId}/update")
    @PreAuthorize("hasRole('EMPLOYEE')")
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
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String deleteBook(@PageableDefault Pageable pageable,
                             @PathVariable("publicId") UUID publicId,
                             @ModelAttribute("bookFilter") @Valid BookFilterDto bookFilter,
                             RedirectAttributes redirectAttributes) {
        addPaginationAttributes(pageable, redirectAttributes);

        bookService.deleteBookByPublicId(publicId);

        long totalItems = bookService.getBooksCountByFilter(bookFilter);
        if (totalItems == (long) pageable.getPageSize() * (pageable.getPageNumber()) && pageable.getPageNumber() > 0) {
            redirectAttributes.addAttribute("page", pageable.getPageNumber() - 1);
        }

        addFiltersToRedirect(redirectAttributes, bookFilter);

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

    private void addPaginationAttributes(Pageable pageable, RedirectAttributes redirectAttributes) {
        redirectAttributes.addAttribute("page", pageable.getPageNumber());
        redirectAttributes.addAttribute("size", pageable.getPageSize());
        if (pageable.getSort().isSorted()) {
            redirectAttributes.addAttribute("sort",
                    pageable.getSort().toString().replace(": ", ","));
        }
    }

    private void addFiltersToRedirect(RedirectAttributes ra, BookFilterDto filter) {
        Map<String, Object> filterMap = objectMapper.convertValue(filter, new TypeReference<Map<String, Object>>() {});

        filterMap.forEach((key, value) -> {
            if (value != null && !value.toString().isBlank()) {
                ra.addAttribute(key, value);
            }
        });
    }
}