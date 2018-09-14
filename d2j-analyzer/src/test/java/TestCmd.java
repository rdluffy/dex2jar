import org.junit.Test;

public class TestCmd {
    @Test
    public void testMain(){

        String []args= new String[]{"-o","C:/tmp/watson/analysis/apk/out/tmp",
                "C:/tmp/watson/analysis/apk/test.apk"};
        new com.googlecode.d2j.analyzer.AnalyzeCmd().doMain(args);
    }
}
