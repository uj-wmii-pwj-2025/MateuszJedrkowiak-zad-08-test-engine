package uj.wmii.pwj.anns;

public class MyBeautifulTestSuite {

    @MyTest
    public void testSoemthing() {
        System.out.println("I'm testing something!");
    }

    @MyTest(params = {"a param", "b param", "c param. Long, long C param."})
    public void testWithParam(String param) {
        System.out.printf("I was invoked with parameter: %s\n", param);
    }

    public void notATest() {
        System.out.println("I'm not a test.");
    }

    @MyTest
    public void imFailue() {
        System.out.println("I AM EVIL.");
        throw new NullPointerException();
    }

    @MyTestString(params={"John"}, expected = {"John"})
    public String SuccessStringTest(String param) {
        return param;
    }
    @MyTestString(params={"Jhon", "Jnae"}, expected = {"John", "Jane"})
    public String MisspelledNamesStringTest(String param) {
        return param;
    }
    @MyTestString(params={"cat"})
    public String NoExpectedOutputStringTest(String param) {
        return param;
    }

    @MyTestString(params = {"3", "2"}, expected = {"3", "2"})
    public int returnIntTest(String param) {
        return Integer.parseInt(param);
    }

}
