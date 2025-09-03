# BalanceApi

All URIs are relative to *http://localhost:8081*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**balanceGet**](BalanceApi.md#balanceGet) | **GET** /balance | Получить баланс счёта |



## balanceGet

> BalanceResponse balanceGet()

Получить баланс счёта

Возвращает текущий баланс пользователя.

### Example

```java
// Import classes:
import com.example.openapi.client.ApiClient;
import com.example.openapi.client.ApiException;
import com.example.openapi.client.Configuration;
import com.example.openapi.client.models.*;
import com.example.openapi.client.api.BalanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8081");

        BalanceApi apiInstance = new BalanceApi(defaultClient);
        try {
            BalanceResponse result = apiInstance.balanceGet();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling BalanceApi#balanceGet");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

[**BalanceResponse**](BalanceResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Баланс успешно получен |  -  |
| **500** | Ошибка сервиса |  -  |

