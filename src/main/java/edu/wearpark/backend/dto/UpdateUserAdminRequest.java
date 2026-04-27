package edu.wearpark.backend.dto;

public record UpdateUserAdminRequest(
        String firstName,
        String lastName,
        String role,
        String gender,
        Boolean hasDiagnosis,
        String diagnosis
) {}