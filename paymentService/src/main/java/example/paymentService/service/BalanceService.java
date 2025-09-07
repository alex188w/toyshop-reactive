package example.paymentService.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@Service
public class BalanceService {

    private final AtomicReference<Double> balance;

    public BalanceService(@Value("${balance.initial:10000}") double initialBalance) {
        this.balance = new AtomicReference<>(initialBalance);
    }

    public double getBalance() {
        return balance.get();
    }

    public boolean deduct(double amount) {
        return balance.updateAndGet(current -> {
            if (current >= amount) {
                return current - amount;
            } else {
                throw new IllegalStateException("Недостаточно средств на балансе");
            }
        }) >= 0;
    }
}
