package com.billbox.category;

import com.billbox.category.CategoryDtos.Response;
import com.billbox.category.CategoryDtos.UpsertRequest;
import com.billbox.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<Response> list() {
        return categoryService.list(SecurityUtils.current());
    }

    @PostMapping
    public Response create(@Valid @RequestBody UpsertRequest request) {
        return categoryService.create(SecurityUtils.current(), request);
    }

    @PutMapping("/{id}")
    public Response update(@PathVariable UUID id, @Valid @RequestBody UpsertRequest request) {
        return categoryService.update(SecurityUtils.current(), id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        categoryService.delete(SecurityUtils.current(), id);
    }
}
