package com.example.openapi.client.api;

import com.example.openapi.client.ApiClient;
import com.example.openapi.client.model.ConfirmRequest;
import com.example.openapi.client.model.ConfirmResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class ConfirmApi {
    private ApiClient apiClient;

    public ConfirmApi() {
        this(new ApiClient());
    }

    @Autowired
    public ConfirmApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Подтвердить оформление заказа
     * Эмулирует процесс оформления заказа после успешной оплаты. Возвращает сведения о заказе и статус подтверждения. 
     * <p><b>200</b> - Заказ успешно оформлен
     * <p><b>400</b> - Ошибка в запросе
     * <p><b>500</b> - Ошибка сервиса
     * @param confirmRequest The confirmRequest parameter
     * @return ConfirmResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec confirmPostRequestCreation(ConfirmRequest confirmRequest) throws WebClientResponseException {
        Object postBody = confirmRequest;
        // verify the required parameter 'confirmRequest' is set
        if (confirmRequest == null) {
            throw new WebClientResponseException("Missing the required parameter 'confirmRequest' when calling confirmPost", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
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
        final String[] localVarContentTypes = { 
            "application/json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<ConfirmResponse> localVarReturnType = new ParameterizedTypeReference<ConfirmResponse>() {};
        return apiClient.invokeAPI("/confirm", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Подтвердить оформление заказа
     * Эмулирует процесс оформления заказа после успешной оплаты. Возвращает сведения о заказе и статус подтверждения. 
     * <p><b>200</b> - Заказ успешно оформлен
     * <p><b>400</b> - Ошибка в запросе
     * <p><b>500</b> - Ошибка сервиса
     * @param confirmRequest The confirmRequest parameter
     * @return ConfirmResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ConfirmResponse> confirmPost(ConfirmRequest confirmRequest) throws WebClientResponseException {
        ParameterizedTypeReference<ConfirmResponse> localVarReturnType = new ParameterizedTypeReference<ConfirmResponse>() {};
        return confirmPostRequestCreation(confirmRequest).bodyToMono(localVarReturnType);
    }

    /**
     * Подтвердить оформление заказа
     * Эмулирует процесс оформления заказа после успешной оплаты. Возвращает сведения о заказе и статус подтверждения. 
     * <p><b>200</b> - Заказ успешно оформлен
     * <p><b>400</b> - Ошибка в запросе
     * <p><b>500</b> - Ошибка сервиса
     * @param confirmRequest The confirmRequest parameter
     * @return ResponseEntity&lt;ConfirmResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<ConfirmResponse>> confirmPostWithHttpInfo(ConfirmRequest confirmRequest) throws WebClientResponseException {
        ParameterizedTypeReference<ConfirmResponse> localVarReturnType = new ParameterizedTypeReference<ConfirmResponse>() {};
        return confirmPostRequestCreation(confirmRequest).toEntity(localVarReturnType);
    }

    /**
     * Подтвердить оформление заказа
     * Эмулирует процесс оформления заказа после успешной оплаты. Возвращает сведения о заказе и статус подтверждения. 
     * <p><b>200</b> - Заказ успешно оформлен
     * <p><b>400</b> - Ошибка в запросе
     * <p><b>500</b> - Ошибка сервиса
     * @param confirmRequest The confirmRequest parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec confirmPostWithResponseSpec(ConfirmRequest confirmRequest) throws WebClientResponseException {
        return confirmPostRequestCreation(confirmRequest);
    }
}
