package com.billbox.category;

import com.billbox.common.enums.CategoryKind;
import com.billbox.organization.Organization;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategorySeeder {

    private record Seed(String name, CategoryKind kind, String color, String icon) {
    }

    private static final List<Seed> DEFAULTS = List.of(
            new Seed("Elektrik", CategoryKind.EXPENSE, "#f59e0b", "bolt"),
            new Seed("Su", CategoryKind.EXPENSE, "#38bdf8", "tint"),
            new Seed("Doğalgaz", CategoryKind.EXPENSE, "#fb7185", "fire"),
            new Seed("İnternet", CategoryKind.EXPENSE, "#818cf8", "wifi"),
            new Seed("Telefon", CategoryKind.EXPENSE, "#a78bfa", "mobile"),
            new Seed("Kira", CategoryKind.EXPENSE, "#f97316", "home"),
            new Seed("Aidat", CategoryKind.EXPENSE, "#fb923c", "building"),
            new Seed("Market", CategoryKind.EXPENSE, "#34d399", "shopping-cart"),
            new Seed("Elektronik", CategoryKind.EXPENSE, "#6366f1", "mobile"),
            new Seed("Beyaz Eşya", CategoryKind.EXPENSE, "#0ea5e9", "box"),
            new Seed("Ev Eşyası", CategoryKind.EXPENSE, "#14b8a6", "home"),
            new Seed("Giyim", CategoryKind.EXPENSE, "#e11d48", "tag"),
            new Seed("Ulaşım", CategoryKind.EXPENSE, "#22d3ee", "car"),
            new Seed("Sağlık", CategoryKind.EXPENSE, "#f43f5e", "heart"),
            new Seed("Sigorta", CategoryKind.EXPENSE, "#64748b", "shield"),
            new Seed("Abonelik", CategoryKind.EXPENSE, "#8b5cf6", "replay"),
            new Seed("Vergi", CategoryKind.EXPENSE, "#ef4444", "percentage"),
            new Seed("Diğer Gider", CategoryKind.EXPENSE, "#94a3b8", "tag"),
            new Seed("Satış", CategoryKind.INCOME, "#10b981", "chart-line"),
            new Seed("Hizmet", CategoryKind.INCOME, "#14b8a6", "briefcase"),
            new Seed("Kira Geliri", CategoryKind.INCOME, "#0ea5e9", "home"),
            new Seed("Diğer Gelir", CategoryKind.INCOME, "#64748b", "wallet")
    );

    private final CategoryRepository categoryRepository;

    public CategorySeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public void seedFor(Organization organization) {
        List<Category> categories = DEFAULTS.stream().map(seed -> {
            Category category = new Category();
            category.setOrganization(organization);
            category.setName(seed.name());
            category.setKind(seed.kind());
            category.setColor(seed.color());
            category.setIcon(seed.icon());
            return category;
        }).toList();
        categoryRepository.saveAll(categories);
    }
}
