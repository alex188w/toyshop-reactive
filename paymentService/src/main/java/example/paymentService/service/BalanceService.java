package example.paymentService.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class BalanceService {

    private final AtomicReference<BigDecimal> balance;

    public BalanceService(@Value("${balance.initial:10000}") BigDecimal initialBalance) {
        this.balance = new AtomicReference<>(initialBalance);
    }

    /** Получение текущего баланса */
    public BigDecimal getBalance() {
        return balance.get();
    }

    /**
     * Списание суммы с баланса
     * 
     * @param amount сумма для списания
     * @return true, если операция успешна
     */
    public boolean deduct(BigDecimal amount) {
        return balance.updateAndGet(current -> {
            if (current.compareTo(amount) >= 0) {
                return current.subtract(amount);
            } else {
                throw new IllegalStateException("Недостаточно средств на балансе");
            }
        }).compareTo(BigDecimal.ZERO) >= 0;
    }

    /** Пополнение баланса */
    public void add(BigDecimal amount) {
        balance.updateAndGet(current -> current.add(amount));
    }
}
