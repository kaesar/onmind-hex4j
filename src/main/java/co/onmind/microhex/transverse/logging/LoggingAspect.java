package co.onmind.microhex.transverse.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspect for cross-cutting logging concerns.
 * 
 * This aspect provides automatic logging for:
 * - Method entry and exit
 * - Method parameters and return values
 * - Execution time measurement
 * - Exception logging
 * 
 * @author OnMind (Cesar Andres Arcila Buitrago)
 * @version 1.0.0
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Pointcut for all methods in the application layer.
     */
    @Pointcut("execution(* co.onmind.microhex.application..*(..))")
    public void applicationLayer() {}

    /**
     * Pointcut for all methods in the domain layer.
     */
    @Pointcut("execution(* co.onmind.microhex.domain..*(..))")
    public void domainLayer() {}

    /**
     * Pointcut for all methods in the infrastructure layer controllers.
     */
    @Pointcut("execution(* co.onmind.microhex.infrastructure.controllers..*(..))")
    public void controllerLayer() {}

    /**
     * Around advice for measuring execution time and logging method calls.
     * 
     * @param joinPoint the join point
     * @return the result of method execution
     * @throws Throwable if method execution fails
     */
    @Around("applicationLayer() || domainLayer() || controllerLayer()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        logger.debug("Entering method: {}.{}() with arguments: {}", 
                    className, methodName, Arrays.toString(args));

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            logger.debug("Exiting method: {}.{}() with result: {} (execution time: {}ms)", 
                        className, methodName, result, executionTime);
            
            return result;
        } catch (Exception ex) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Exception in method: {}.{}() after {}ms: {}", 
                        className, methodName, executionTime, ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * After throwing advice for logging exceptions.
     * 
     * @param joinPoint the join point
     * @param exception the thrown exception
     */
    @AfterThrowing(pointcut = "applicationLayer() || domainLayer() || controllerLayer()", 
                   throwing = "exception")
    public void logException(JoinPoint joinPoint, Throwable exception) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        logger.error("Exception thrown in {}.{}(): {}", 
                    className, methodName, exception.getMessage(), exception);
    }
}