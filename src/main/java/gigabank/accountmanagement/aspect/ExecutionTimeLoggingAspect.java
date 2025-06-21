package gigabank.accountmanagement.aspect;

import java.util.logging.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class ExecutionTimeLoggingAspect {
    private static final Logger logger = Logger.getLogger("gigabank.accountmanagement.aspect.ExecutionTimeLoggingAspect");

    @Around("@annotation(gigabank.accountmanagement.annotation.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.info("Aspect triggered for: " + joinPoint.getSignature().toShortString()); // Отладочный лог
        long start = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        logger.info(methodName + " started");
        Object proceed = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;
        logger.info(methodName + " finished, duration: " + executionTime + " ms");
        return proceed;
    }
}