package example.paymentService;

import example.paymentService.service.BalanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BalanceServiceTest {

    private BalanceService balanceService;

    @BeforeEach
    void setUp() {
        balanceService = new BalanceService(1000.0);
    }

    @Test
    void getBalance_shouldReturnInitialBalance() {
        assertEquals(1000.0, balanceService.getBalance());
    }

    @Test
    void deduct_shouldReduceBalance_whenEnoughFunds() {
        boolean result = balanceService.deduct(300.0);
        assertTrue(result);
        assertEquals(700.0, balanceService.getBalance());
    }

    @Test
    void deduct_shouldThrow_whenNotEnoughFunds() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> balanceService.deduct(1500.0));
        assertEquals("Недостаточно средств на балансе", ex.getMessage());
        assertEquals(1000.0, balanceService.getBalance()); // баланс не изменился
    }

    @Test
    void deduct_multipleOperations_shouldUpdateBalanceCorrectly() {
        balanceService.deduct(200.0);
        balanceService.deduct(300.0);
        assertEquals(500.0, balanceService.getBalance());
    }
}
