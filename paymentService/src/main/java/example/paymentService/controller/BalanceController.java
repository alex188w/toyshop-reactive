package example.paymentService.controller;

import com.example.openapi.client.model.BalanceResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BalanceController {

    @GetMapping("/balance")
    public BalanceResponse getBalance() {
        BalanceResponse response = new BalanceResponse();
        response.setBalance(1000.00); // тут пока мок
        return response;
    }
}
