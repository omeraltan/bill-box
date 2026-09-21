package com.billbox.category;

import com.billbox.common.enums.CategoryKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public final class CategoryDtos {

    private CategoryDtos() {
    }

    public record UpsertRequest(
            @NotBlank @Size(max = 80) String name,
            @NotNull CategoryKind kind,
            @NotBlank @Size(max = 16) String color,
            @Size(max = 40) String icon
    ) {
    }

    public record Response(UUID id, String name, CategoryKind kind, String color, String icon) {
        public static Response from(Category category) {
            return new Response(
                    category.getId(),
                    category.getName(),
                    category.getKind(),
                    category.getColor(),
                    category.getIcon()
            );
        }
    }
}
