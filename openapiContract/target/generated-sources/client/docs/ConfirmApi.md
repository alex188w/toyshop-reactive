# ConfirmApi

All URIs are relative to *http://localhost:8081*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**confirmPost**](ConfirmApi.md#confirmPost) | **POST** /confirm | Подтвердить оформление заказа |



## confirmPost

> ConfirmResponse confirmPost(confirmRequest)

Подтвердить оформление заказа

Эмулирует процесс оформления заказа после успешной оплаты. Возвращает сведения о заказе и статус подтверждения. 

### Example

```java
// Import classes:
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.ApiException;
import com.example.openapi.client.Configuration;
import com.example.openapi.client.models.*;
import com.example.openapi.client.api.ConfirmApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8081");

        ConfirmApi apiInstance = new ConfirmApi(defaultClient);
        ConfirmRequest confirmRequest = new ConfirmRequest(); // ConfirmRequest | 
        try {
            ConfirmResponse result = apiInstance.confirmPost(confirmRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ConfirmApi#confirmPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **confirmRequest** | [**ConfirmRequest**](ConfirmRequest.md)|  | |

### Return type

[**ConfirmResponse**](ConfirmResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Заказ успешно оформлен |  -  |
| **400** | Ошибка в запросе |  -  |
| **500** | Ошибка сервиса |  -  |

