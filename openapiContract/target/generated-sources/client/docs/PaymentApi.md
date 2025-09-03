# PaymentApi

All URIs are relative to *http://localhost:8081*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**payPost**](PaymentApi.md#payPost) | **POST** /pay | Инициировать оплату заказа |



## payPost

> PaymentResponse payPost(paymentRequest)

Инициировать оплату заказа

Получает данные заказа и возвращает результат оплаты.

### Example

```java
// Import classes:
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.ApiException;
import com.example.openapi.client.Configuration;
import com.example.openapi.client.models.*;
import com.example.openapi.client.api.PaymentApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8081");

        PaymentApi apiInstance = new PaymentApi(defaultClient);
        PaymentRequest paymentRequest = new PaymentRequest(); // PaymentRequest | 
        try {
            PaymentResponse result = apiInstance.payPost(paymentRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling PaymentApi#payPost");
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
| **paymentRequest** | [**PaymentRequest**](PaymentRequest.md)|  | |

### Return type

[**PaymentResponse**](PaymentResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Успешная оплата |  -  |
| **400** | Ошибка в запросе |  -  |
| **402** | Недостаточно средств |  -  |
| **500** | Ошибка сервиса |  -  |

