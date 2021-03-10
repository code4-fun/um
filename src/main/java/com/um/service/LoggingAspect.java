package com.um.service;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Aspect
public class LoggingAspect {

    /**
     * Шаблон, определяющий методы, к которым будут применятся advice-ы
     */
    @Pointcut("execution(* com.um.*.*.*(..))")
    public void logging(){
    }

    /**
     * Метод, выполняющий логирование в файл logging.txt успешно выполненных методов
     */
    @AfterReturning(value = "logging()", returning = "returningValue")
    public void recordSuccessfulExecution(JoinPoint joinPoint, Object returningValue) throws IOException {

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss.SSS");
        FileWriter fw = new FileWriter("logging.txt", true);

        if (returningValue != null) {
            String logString = String.format(
                    sdf.format(date) + "  УСПЕШНО ВЫПОЛНЕН МЕТОД - %s(), КЛАССА - %s, С РЕЗУЛЬТАТОМ ВЫПОЛНЕНИЯ - %s\n",
                    joinPoint.getSignature().getName(),
                    getClassName(joinPoint),
                    returningValue);

            fw.write(logString);
            fw.close();
        }
        else {
            String str = String.format(
                    sdf.format(date) + "  УСПЕШНО ВЫПОЛНЕН МЕТОД - %s(), КЛАССА - %s\n",
                    joinPoint.getSignature().getName(),
                    getClassName(joinPoint));
            fw.write(str);
            fw.close();
        }
    }

    /**
     * Метод, выполняющий логирование в файл logging.txt методов, закончившихся с ошибкой
     */
    @AfterThrowing(value = "logging()", throwing = "exception")
    public void recordFailedExecution(JoinPoint joinPoint, Exception exception) throws IOException {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss.SSS");
        FileWriter fw = new FileWriter("logging.txt", true);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String stackTrace = sw.toString();

        String logString = String.format(sdf.format(date) + "  МЕТОД - %s(), КЛАССА - %s, БЫЛ АВАРИЙНО ЗАВЕРШЕН С ОШИБКОЙ - %s\n" +
                        "Stacktrace:\n %s",
                joinPoint.getSignature().getName(),
                getClassName(joinPoint),
                exception,
                stackTrace);

        fw.write(logString);
        fw.close();
    }

    /**
     * Метод вырезает имя класса из полного пути к данному классу.
     * Используется только в методах данного класса
     */
    private String getClassName(JoinPoint joinPoint){
        String pathToClass = joinPoint.getSourceLocation().getWithinType().getName();
        int indexOfLastPoint = joinPoint.getSourceLocation().getWithinType().getName().lastIndexOf(".");
        return  pathToClass.substring(indexOfLastPoint + 1);
    }
}