package example.paymentService;

import example.paymentService.service.BalanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

class BalanceServiceTest {

private BalanceService balanceService;

    @BeforeEach
    void setUp() {
        // Инициализация баланса 5000
        balanceService = new BalanceService(BigDecimal.valueOf(5000));
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
