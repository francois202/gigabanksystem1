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
    // Хранит время начала для каждого потока
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


















//
//import java.util.logging.Logger;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//
//@Aspect
//public class ExecutionTimeLoggingAspect {
//    private static final Logger logger = Logger.getLogger("gigabank.accountmanagement.aspect.ExecutionTimeLoggingAspect");
//
//    @Around("@annotation(gigabank.accountmanagement.annotation.LogExecutionTime)")
//    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
//        logger.info("Aspect triggered for: " + joinPoint.getSignature().toShortString()); // Отладочный лог
//        long start = System.currentTimeMillis();
//        String methodName = joinPoint.getSignature().toShortString();
//        logger.info(methodName + " started");
//        Object proceed = joinPoint.proceed();
//        long executionTime = System.currentTimeMillis() - start;
//        logger.info(methodName + " finished, duration: " + executionTime + " ms");
//        return proceed;
//    }
//}