package com.wanyukang.jdump;

import hudson.model.*;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.AggregatedTestResultAction;
import hudson.tasks.test.TestResult;
import hudson.util.RunList;
import org.jenkinsci.plugins.tokenmacro.DataBoundTokenMacro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by wanyukang on 17/6/12.
 */
public class JdumpServiceImpl implements JdumpService {

    private Logger logger = LoggerFactory.getLogger(JdumpService.class);

    private TaskListener listener;

    private AbstractBuild build;

    private Job project;

    private boolean onStart;

    private boolean onSuccess;

    private boolean onFailed;

//    private String operateType;
//
//    private String type;
//
//    private String token;
//
//    private String key;
//
//    private String appName;
//
//    private String remark;
//
//    private String interval;

    private String alarmWay;

    private String alarmUserEmails;

    private String alarmUserPhones;

    private String alarmUserErps;

    private String testResultFile;

    public static final String MACRO_NAME = "FAILED_TESTS";

    private FailedTestsContent failedTestContent;

    public JdumpServiceImpl(String alarmWay,
                            String alarmUserEmails,
                            String alarmUserPhones,
                            String alarmUserErps,
                            String testResultFile,
                            boolean onStart,
                            boolean onSuccess,
                            boolean onFailed,
                            TaskListener listener,
                            AbstractBuild build) {

//        this.operateType = operateType;
//        this.type = type;
//        this.token = token;
//        this.key = key;
//        this.appName = appName;

        this.alarmWay = alarmWay;
        this.alarmUserEmails = alarmUserEmails;
        this.alarmUserPhones = alarmUserPhones;
        this.alarmUserErps = alarmUserErps;
        this.testResultFile = testResultFile;
        this.onStart = onStart;
        this.onSuccess = onSuccess;
        this.onFailed = onFailed;
        this.listener = listener;
        this.build = build;
    }



    @Override
    public void start() {

        if (onStart) {
            logger.info("Start notify by Jdump from " + listener.toString());
        }
    }

    @Override
    public void success() {

        if (onSuccess) {
            logger.info("Success notify by Jdump from " + listener.toString());
        }
    }

    @Override
    public void failed() {

        if (onFailed) {
            logger.info("Failed notify by Jdump from " + listener.toString());
            try {
                notifyByJdump();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyByJdump() throws Exception {

        String title = String.format("%s%s构建失败", build.getProject().getDisplayName(), build.getDisplayName());
        String content = String.format("项目[%s%s]构建失败, summary:%s, duration:%s", build.getProject().getDisplayName(), build.getDisplayName(), build.getBuildStatusSummary().message, build.getDurationString());
        logger.info("test fail log : " + "title :" + title + "content :" + content);

        FailedTestsContent failedTestContent = new FailedTestsContent();
        String failContent = failedTestContent.evaluate( build, listener, testResultFile );
        logger.info("failContent :" + failContent);
        //实现通知
    }
}
