package com.exemple.ecommerce.store.stock;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.exemple.ecommerce.store.common.InsufficientStockException;
import com.exemple.ecommerce.store.common.NoFoundStockException;

public interface StockService {

    Long update(@NotNull String company, @NotBlank String store, @NotBlank String product, int quantity) throws InsufficientStockException;

    Long get(@NotNull String company, @NotBlank String store, @NotBlank String product) throws NoFoundStockException;

}
