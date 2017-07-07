package com.wanyukang.jdump;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.junit.SuiteResult;
import hudson.tasks.junit.TestResult;
import org.jenkinsci.plugins.tokenmacro.DataBoundTokenMacro;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An EmailContent for failing tests. Only shows tests that have failed.
 *
 * @author markltbaker
 */
@Extension
public class FailedTestsContent extends DataBoundTokenMacro {

    private Logger logger = LoggerFactory.getLogger(FailedTestsContent.class);

    @Parameter
    public boolean showStack = false;

    @Parameter
    public boolean showMessage = true;

    @Parameter
    public int maxTests = Integer.MAX_VALUE;

    @Parameter
    public boolean onlyRegressions = false;

    @Parameter
//    public int maxLength = Integer.MAX_VALUE;
    public int maxLength = 140;//短信容量

    public static final String MACRO_NAME = "FAILED_TESTS";

    @Override
    public boolean acceptsMacroName(String macroName) {
        return macroName.equals(MACRO_NAME);
    }

    @Override
    public String evaluate(AbstractBuild<?, ?> build, TaskListener listener, String testResultFile)
            throws MacroEvaluationException, IOException, InterruptedException {
//        return evaluate(build, build.getWorkspace(), listener, macroName);
        return evaluate(build, build.getWorkspace(), listener, testResultFile);
    }

    @Override
    public String evaluate(Run<?, ?> run, FilePath filePath, TaskListener listener, String testResultFile)
            throws MacroEvaluationException, IOException, InterruptedException {

        StringBuilder buffer = new StringBuilder();
        TestResult testResult = new TestResult();
        File junitFile = new File(testResultFile);
        testResult.parse(junitFile);
        SuiteResult suite = testResult.getSuites().iterator().next();
        int failCount = getFailedTestCount(suite);

        if (null == testResult) {
            return "No tests ran.";
        }

        if (failCount == 0) {
            buffer.append("All tests passed");
        } else {
            buffer.append(failCount).append(" tests failed.\n");

            boolean showOldFailures = !onlyRegressions;
//            if(maxLength < Integer.MAX_VALUE) {
//                maxLength *= 1024;
//            }

            if (maxTests > 0) {
                int printedTests = 0;
                int printedLength = 0;

                List<CaseResult> cases = suite.getCases();
                for (CaseResult caseResult : cases) {
                    if (!caseResult.isPassed() && !caseResult.isSkipped() && caseResult.getErrorDetails() != null) { // Any error, invalidates the suite result
                        if (showOldFailures || getTestAge(caseResult) == 1) {
                            if (printedTests < maxTests && printedLength <= maxLength) {
                                printedLength += outputTest(buffer, caseResult, showStack, showMessage, maxLength - printedLength);
                                printedTests++;
                            }
                        }
                    }
                }

                if (failCount > printedTests) {
                    buffer.append("... and ").append(failCount - printedTests).append(" other failed tests.\n\n");
                }
                if (printedLength >= maxLength) {
                    buffer.append("\n\n... output truncated.\n\n");
                }
            }
        }
        return buffer.toString();
    }

    private int getTestAge(CaseResult result) {
        if (result.isPassed())
            return 0;
        else if (result.getRun() != null) {
            return result.getRun().getNumber() - result.getFailedSince() + 1;
        } else {
            return 0;
        }
    }

    private int outputTest(StringBuilder buffer, CaseResult failedTest,
                           boolean showStack, boolean showMessage, int lengthLeft) {
        StringBuilder local = new StringBuilder();

        local.append(failedTest.isPassed() ? "PASSED" : "FAILED").append(": ");

//        if(failedTest instanceof CaseResult) {
//            local.append(failedTest.getClassName());
//        } else {
//            local.append(failedTest.getFullName());
//        }
//        local.append('.').append(failedTest.getDisplayName()).append('\n');

        if (showMessage) {
            local.append("\nError Message:\n").append(failedTest.getErrorDetails()).append("\n");
        }

        if (showStack) {
            local.append("\nStack Trace:\n").append(failedTest.getErrorStackTrace()).append('\n');
        }

        if (showMessage || showStack) {
            local.append('\n');
        }

        if (local.length() > lengthLeft) {
            local.setLength(lengthLeft);
        }

        buffer.append(local.toString());
        return local.length();
    }

    public int getFailedTestCount(SuiteResult suiteResult) {//work!

        int failedTestCount = 0;
        List<CaseResult> cases = suiteResult.getCases();
        for (CaseResult caseResult : cases) {
            if (!caseResult.isPassed() && !caseResult.isSkipped()) { // Any error, invalidates the suite result
                failedTestCount++;
            }
        }
        return failedTestCount;
    }

//    public String getFailedTestsContent(SuiteResult suiteResult) {
//
//        StringBuilder context = new StringBuilder();
//        List<CaseResult> cases = suiteResult.getCases();
//        for(CaseResult caseResult : cases) {
//            if(!caseResult.isPassed() && !caseResult.isSkipped() && caseResult.getErrorDetails() != null) { // Any error, invalidates the suite result
//                context.append("\nError Message:\n").append(caseResult.getErrorDetails()).append('\n');
//            }
//        }
//        return context.toString();
//    }

    public String TestResultFileFileter(String filePath, String fileter) {

        File fileDir = new File(filePath);
        File[] listFiles = fileDir.listFiles();
        List<String> testResultFiles = null;
        for (File file : listFiles) {
            if (file.isDirectory()) {
                String[] list = file.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(fileter);
                    }
                });
            testResultFiles.add(file.getName());
            }
        }
        logger.info(testResultFiles.iterator().next());
        return testResultFiles.iterator().next();
    }
}
