package edu.wearpark.backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferences {
    @Field(name = "monthly_report_email")
    private Boolean monthlyReportEmail;
    @Field(name = "report_recipients")
    private String[] reportRecipients;
}
