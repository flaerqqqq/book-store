package com.epam.rd.autocode.spring.project.repo.specification;

import com.epam.rd.autocode.spring.project.dto.OrderFilterDto;
import com.epam.rd.autocode.spring.project.model.Order;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderSpecifications {

    public static Specification<Order> withFilters(OrderFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.isNotBlank(filter.getOrderPublicId())) {
                predicates.add(
                        cb.equal(root.get("publicId"), UUID.fromString(filter.getOrderPublicId()))
                );
            }

            if (StringUtils.isNotBlank(filter.getClientPublicId())) {
                predicates.add(
                        cb.equal(root.join("client", JoinType.LEFT).get("publicId"), UUID.fromString(filter.getClientPublicId()))
                );
            }

            if (StringUtils.isNotBlank(filter.getEmployeePublicId())) {
                predicates.add(
                        cb.equal(root.join("employee", JoinType.LEFT).get("publicId"), UUID.fromString(filter.getEmployeePublicId()))
                );
            }

            if (filter.getStartOrderDate() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("orderDate"), filter.getStartOrderDate())
                );
            }

            if (filter.getEndOrderDate() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("orderDate"), filter.getEndOrderDate())
                );
            }

            if (filter.getMinTotalAmount() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("totalAmount"), filter.getMinTotalAmount())
                );
            }

            if (filter.getMaxTotalAmount() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("totalAmount"), filter.getMaxTotalAmount())
                );
            }

            if (filter.getDeliveryType() != null) {
                predicates.add(
                        cb.equal(root.get("deliveryType"), filter.getDeliveryType())
                );
            }

            if (filter.getStatus() != null) {
                predicates.add(
                        cb.equal(root.get("status"), filter.getStatus())
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}