package com.exemple.ecommerce.application.detail;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.exemple.ecommerce.application.common.model.ApplicationDetail;

public interface ApplicationDetailService {

    void put(@NotBlank String application, @Valid @NotNull ApplicationDetail detail);

    ApplicationDetail get(String application);

}
