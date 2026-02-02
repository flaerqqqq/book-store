package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.enums.DeliveryType;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.validation.annotation.ValidDateTimeRange;
import com.epam.rd.autocode.spring.project.validation.annotation.ValidDecimalRange;
import com.epam.rd.autocode.spring.project.validation.validator.DateTimeRangeAware;
import com.epam.rd.autocode.spring.project.validation.validator.DecimalRangeAware;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UUID;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidDecimalRange(message = "Minimum total amount cannot be greater than maximum")
@ValidDateTimeRange
public class OrderFilterDto implements DecimalRangeAware, DateTimeRangeAware {

    @UUID(message = "The ID must be a valid UUID format")
    private String orderPublicId;

    @UUID(message = "The ID must be a valid UUID format")
    private String clientPublicId;

    @UUID(message = "The ID must be a valid UUID format")
    private String employeePublicId;

    private LocalDateTime startOrderDate;

    private LocalDateTime endOrderDate;

    @Min(value = 0, message = "Price must not be negative")
    private BigDecimal minTotalAmount;

    @Min(value = 0, message = "Price must not be negative")
    private BigDecimal maxTotalAmount;

    private DeliveryType deliveryType;

    private OrderStatus status;

    @Override
    public LocalDateTime getDateTimeFrom() {
        return startOrderDate;
    }

    @Override
    public LocalDateTime getDateTimeTo() {
        return endOrderDate;
    }

    @Override
    public String getDateTimeFieldName() {
        return "startOrderDate";
    }

    @Override
    public BigDecimal getMin() {
        return minTotalAmount;
    }

    @Override
    public BigDecimal getMax() {
        return maxTotalAmount;
    }

    @Override
    public String getDecimalFieldName() {
        return "minTotalAmount";
    }
}