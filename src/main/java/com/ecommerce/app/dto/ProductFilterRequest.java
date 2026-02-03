package com.ecommerce.app.dto;

import java.util.List;

public record ProductFilterRequest(
    List<Long> categories,
    List<Integer> ratings, 
    String sort
) {}
