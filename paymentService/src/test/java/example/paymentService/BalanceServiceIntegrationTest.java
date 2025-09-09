package example.paymentService;

import example.paymentService.service.BalanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "balance.initial=5000")
class BalanceServiceIntegrationTest {

    @Autowired
    private BalanceService balanceService;

    @Test
    void getBalance_shouldReturnInitialBalanceFromProperties() {
        assertEquals(5000.0, balanceService.getBalance());
    }

    @Test
    void deduct_shouldReduceBalance() {
        boolean result = balanceService.deduct(1500.0);
        assertTrue(result);
        assertEquals(3500.0, balanceService.getBalance());
    }

    @Test
    void deduct_shouldThrow_whenNotEnoughFunds() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> balanceService.deduct(6000.0));
        assertEquals("Недостаточно средств на балансе", ex.getMessage());
    }
}
