package example.toyshop.model;

/**
 * Статусы корзины.
 * <p>
 * Определяет текущее состояние корзины пользователя:
 * <ul>
 *     <li>{@link #ACTIVE} — корзина активна, пользователь может добавлять и изменять товары</li>
 *     <li>{@link #COMPLETED} — корзина завершена, заказ оформлен</li>
 * </ul>
 */
public enum CartStatus {
    ACTIVE,
    COMPLETED
}
