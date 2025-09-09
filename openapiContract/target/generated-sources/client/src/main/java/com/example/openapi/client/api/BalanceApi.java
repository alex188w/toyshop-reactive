package com.example.openapi.client.api;

import com.example.openapi.client.ApiClient;

import com.example.openapi.client.model.BalanceResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class BalanceApi {
    private ApiClient apiClient;

    public BalanceApi() {
        this(new ApiClient());
    }

    @Autowired
    public BalanceApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Получить баланс счёта
     * Возвращает текущий баланс пользователя.
     * <p><b>200</b> - Баланс успешно получен
     * <p><b>500</b> - Ошибка сервиса
     * @return BalanceResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec balanceGetRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<BalanceResponse> localVarReturnType = new ParameterizedTypeReference<BalanceResponse>() {};
        return apiClient.invokeAPI("/balance", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Получить баланс счёта
     * Возвращает текущий баланс пользователя.
     * <p><b>200</b> - Баланс успешно получен
     * <p><b>500</b> - Ошибка сервиса
     * @return BalanceResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BalanceResponse> balanceGet() throws WebClientResponseException {
        ParameterizedTypeReference<BalanceResponse> localVarReturnType = new ParameterizedTypeReference<BalanceResponse>() {};
        return balanceGetRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Получить баланс счёта
     * Возвращает текущий баланс пользователя.
     * <p><b>200</b> - Баланс успешно получен
     * <p><b>500</b> - Ошибка сервиса
     * @return ResponseEntity&lt;BalanceResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BalanceResponse>> balanceGetWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<BalanceResponse> localVarReturnType = new ParameterizedTypeReference<BalanceResponse>() {};
        return balanceGetRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Получить баланс счёта
     * Возвращает текущий баланс пользователя.
     * <p><b>200</b> - Баланс успешно получен
     * <p><b>500</b> - Ошибка сервиса
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec balanceGetWithResponseSpec() throws WebClientResponseException {
        return balanceGetRequestCreation();
    }
}
