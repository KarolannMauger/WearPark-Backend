package edu.wearpark.backend.dto;

public record PatchUserAdminRequest(
        String firstName,
        String lastName,
        String role,
        String gender,
        Boolean hasDiagnosis,
        String diagnosis
) {}