package com.billbox.vendor;

import com.billbox.common.enums.PartyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public final class VendorDtos {

    private VendorDtos() {
    }

    public record UpsertRequest(
            @NotBlank @Size(max = 200) String name,
            @Size(max = 20) String taxNumber,
            @Size(max = 180) String email,
            @Size(max = 40) String phone,
            @Size(max = 34) String iban,
            @Size(max = 400) String address,
            @NotNull PartyType partyType,
            @Size(max = 1000) String notes
    ) {
    }

    public record Response(
            UUID id,
            String name,
            String taxNumber,
            String email,
            String phone,
            String iban,
            String address,
            PartyType partyType,
            String notes
    ) {
        public static Response from(Vendor vendor) {
            return new Response(
                    vendor.getId(),
                    vendor.getName(),
                    vendor.getTaxNumber(),
                    vendor.getEmail(),
                    vendor.getPhone(),
                    vendor.getIban(),
                    vendor.getAddress(),
                    vendor.getPartyType(),
                    vendor.getNotes()
            );
        }
    }
}
