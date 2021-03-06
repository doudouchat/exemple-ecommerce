package com.exemple.ecommerce.resource.stock;

import javax.validation.constraints.NotBlank;

public interface StockResource {

    void update(@NotBlank String store, @NotBlank String product, long quantity);

    long get(@NotBlank String store, @NotBlank String product);

}
