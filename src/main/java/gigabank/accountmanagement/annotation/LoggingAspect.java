package gigabank.accountmanagement.annotation;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * @author artem.scheredin
 */

@Aspect
public class LoggingAspect {
    private static final ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Before("@annotation(LogExecutionTime)")
    public void logBefore(JoinPoint joinPoint) {
        System.out.println("Вызов метода: " + joinPoint.getSignature().getName());
        startTime.set(System.currentTimeMillis());
        System.out.println("Время начала: " + startTime.get());
    }

    @After("@annotation(LogExecutionTime)")
    public void logAfter(JoinPoint joinPoint) {
        System.out.println("Завершение метода: " + joinPoint.getSignature().getName());
        Long start = startTime.get();
        if (start != null) {
            long duration = System.currentTimeMillis() - start;
            System.out.println("Продолжительность: " + duration + " мс");
            startTime.remove(); // Очищаем ThreadLocal после использования
        }
    }
}