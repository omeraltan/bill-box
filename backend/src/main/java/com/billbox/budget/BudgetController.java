package com.billbox.budget;

import com.billbox.budget.BudgetDtos.Response;
import com.billbox.budget.BudgetDtos.UpsertRequest;
import com.billbox.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public List<Response> list(@RequestParam(required = false) Integer year) {
        return budgetService.list(SecurityUtils.current(), year);
    }

    @PostMapping
    public Response create(@Valid @RequestBody UpsertRequest request) {
        return budgetService.create(SecurityUtils.current(), request);
    }

    @PutMapping("/{id}")
    public Response update(@PathVariable UUID id, @Valid @RequestBody UpsertRequest request) {
        return budgetService.update(SecurityUtils.current(), id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        budgetService.delete(SecurityUtils.current(), id);
    }
}
