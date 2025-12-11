package uj.wmii.pwj.anns;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class InvalidAnswerException extends Exception
{

    public InvalidAnswerException(String message)
    {
        super(message);
    }
}


class DifferentInputOutputLengthsException extends Exception
{
    public DifferentInputOutputLengthsException(String message)
    {
        super(message);
    }
}

class NoOutputException extends Exception
{
    public NoOutputException(String message)
    {
        super(message);
    }
}

public class MyTestEngine
{

    private final String className;
    private final static String LINE_SEPARATOR = "=".repeat(80);

    public static void main(String[] args)
    {
        if (args.length < 1)
        {
            System.out.println("Please specify test class name");
            System.exit(-1);
        }

        String className = args[0].trim();
        System.out.printf("Testing class: %s\n", className);
        MyTestEngine engine = new MyTestEngine(className);

        engine.runTests();


    }

    public MyTestEngine(String className)
    {
        this.className = className;
    }

    public void runTests()
    {
        printTestEngine();

        final Object unit = getObject(className);
        List<Method> testMethods = getTestMethods(unit);

        printInfo(testMethods);
        int successCount = 0;
        int failCount = 0;
        int errorCount = 0;
        for (Method m : testMethods)
        {
            TestResult result = launchSingleMethod(m, unit);
            if (result == TestResult.SUCCESS)
            {
                successCount++;
            }
            else if (result == TestResult.FAIL)
            {
                failCount++;
            }
            else
            {
                errorCount++;
            }
        }
        System.out.printf("Engine launched %d tests.\n", testMethods.size());
        System.out.printf("%d of them passed, %d failed, %d ended with an error\n", successCount, failCount, errorCount);
    }

    private TestResult launchSingleMethod(Method m, Object unit)
    {
        try
        {
            if (m.isAnnotationPresent(MyTest.class))
            {
                String[] params = m.getAnnotation(MyTest.class)
                                   .params();

                if (params.length == 0)
                {
                    m.invoke(unit);
                }
                else
                {
                    for (String param : params)
                    {
                        m.invoke(unit, param);
                    }
                }
            }
            else if (m.isAnnotationPresent(MyTestString.class))
            {
                final String noExpectedOutputProvidedMessage = "Tested method: " + m.getName() + " test failed with an error\n" + "No expected output provided";
                MyTestString annotation = m.getAnnotation(MyTestString.class);
                String[] params = annotation.params();
                String[] expected = annotation.expected();

                checkNoExpectedValue(expected.length, noExpectedOutputProvidedMessage);
                checkInputOutputLength(params.length, expected.length, m);


                for (int i = 0; i < expected.length; i++)
                {
                    Object result = m.invoke(unit, params[i]);
                    if (!String.valueOf(result).equals(expected[i]))
                    {
                        throw new InvalidAnswerException("Tested method: " + m.getName() + " test failed - wrong answer\n" + "EXPECTED: " + expected[i] + " RETURNED: " + result);
                    }
                }

            }

            System.out.printf("%s%nTested method: %s test successful. \tPASSED%n%s%n", LINE_SEPARATOR, m.getName(), LINE_SEPARATOR);
            return TestResult.SUCCESS;
        }
        catch (ReflectiveOperationException e)
        {
            e.printStackTrace();
            return TestResult.ERROR;
        }
        catch (DifferentInputOutputLengthsException | NoOutputException e)
        {
            System.out.println(LINE_SEPARATOR + "\n" + e.getMessage() + "\tERROR\n" + LINE_SEPARATOR);
            return TestResult.ERROR;
        }
        catch (InvalidAnswerException e)
        {
            System.out.println(LINE_SEPARATOR + "\n" + e.getMessage() + "\tFAILED\n" + LINE_SEPARATOR);
            return TestResult.FAIL;
        }
        catch (Exception e)
        {
            System.out.println(LINE_SEPARATOR + "\n" + e.getMessage() + "\t ERROR\n" + LINE_SEPARATOR);
            return TestResult.ERROR;
        }
    }

    private void checkNoExpectedValue(int outputLen, String errorMsg) throws NoOutputException
    {
        if (outputLen == 0)
        {
            throw new NoOutputException(errorMsg);
        }
    }

    private void checkInputOutputLength(int inputLen, int outputLen, Method m) throws DifferentInputOutputLengthsException
    {
        if (inputLen != outputLen)
        {
            throw new DifferentInputOutputLengthsException("Tested method: " + m.getName() + " test failed with an error\n" + "Input length: " + inputLen + " Output length: " + outputLen);
        }
    }

    private static List<Method> getTestMethods(Object unit)
    {
        Method[] methods = unit.getClass()
                               .getDeclaredMethods();
        return Arrays.stream(methods)
                     .filter(
                             m -> m.getAnnotation(MyTest.class) != null || m.getAnnotation(MyTestString.class) != null)
                     .collect(Collectors.toList());
    }

    private static Object getObject(String className)
    {
        try
        {
            Class<?> unitClass = Class.forName(className);
            return unitClass.getConstructor()
                            .newInstance();
        }
        catch (ReflectiveOperationException e)
        {
            e.printStackTrace();
            return new Object();
        }
    }

    private void printTestEngine()
    {
        System.out.println("""
                                   ___  ____ ____ ___\s
                                    |   |___ [__   | \s
                                    |   |___ ___]  | \s
                                   
                                   ____ _  _ ____ _ _  _ ____
                                   |___ |\\ | | __ | |\\ | |___
                                   |___ | \\| |__] | | \\| |___
                                   """);
    }

    private void printInfo(List<Method> testMethods)
    {
        System.out.printf("%s%nTESTING CLASS: %s%nTOTAL NUMBER OF TESTS: %d TESTS%n%s%n", LINE_SEPARATOR, this.className, testMethods.size(), LINE_SEPARATOR);
    }

}
