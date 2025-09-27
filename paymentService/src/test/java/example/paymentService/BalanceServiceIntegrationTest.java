package example.paymentService;

import example.paymentService.service.BalanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

@SpringBootTest(properties = "balance.initial=5000")
class BalanceServiceIntegrationTest {

    @Autowired
    private BalanceService balanceService;

    @Test
    void getBalance_shouldReturnInitialBalanceFromProperties() {
        assertEquals(BigDecimal.valueOf(5000), balanceService.getBalance());
    }

    @Test
    void deduct_shouldReduceBalance() {
        boolean result = balanceService.deduct(BigDecimal.valueOf(1500));
        assertTrue(result);
        assertEquals(BigDecimal.valueOf(3500), balanceService.getBalance());
    }

    @Test
    void deduct_shouldThrow_whenNotEnoughFunds() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> balanceService.deduct(BigDecimal.valueOf(6000)));
        assertEquals("Недостаточно средств на балансе", ex.getMessage());
    }

    @Test
    void add_shouldIncreaseBalance() {
        balanceService.add(BigDecimal.valueOf(2000));
        assertEquals(BigDecimal.valueOf(7000), balanceService.getBalance());
    }
}
