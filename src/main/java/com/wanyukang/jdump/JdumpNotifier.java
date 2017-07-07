package com.wanyukang.jdump;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

/**
 * Created by wanyukang on 17/6/12.
 */
public class JdumpNotifier extends Notifier {

    private boolean onStart;

    private boolean onSuccess;

    private boolean onFailed;

    private String testResultFile;

    private String alarmWay;

    private String alarmUserEmails;

    private String alarmUserPhones;

    private String alarmUserErps;
    // 返回接收的Jenkins前端配置的参数
    public boolean isOnStart() {
        return onStart;
    }

    public boolean isOnSuccess() {
        return onSuccess;
    }

    public boolean isOnFailed() {
        return onFailed;
    }

    public String getTestResultFile() {
        return testResultFile;
    }

    public String getAlarmWay() {
        return alarmWay;
    }

    public String getAlarmUserEmails() {
        return alarmUserEmails;
    }

    public String getAlarmUserPhones() {
        return alarmUserPhones;
    }

    public String getAlarmUserErps() {
        return alarmUserErps;
    }

    @DataBoundConstructor
    public JdumpNotifier(
            String alarmWay,
            String alarmUserEmails,
            String alarmUserPhones,
            String alarmUserErps,
            String testResultFile,
            boolean onStart,
            boolean onSuccess,
            boolean onFailed) {

        super();
        this.alarmWay = alarmWay;
        this.alarmUserEmails = alarmUserEmails;
        this.alarmUserPhones = alarmUserPhones;
        this.alarmUserErps = alarmUserErps;
        this.testResultFile = testResultFile;
        this.onStart = onStart;
        this.onSuccess = onSuccess;
        this.onFailed = onFailed;
    }

    public JdumpService newJdumpService(AbstractBuild build, TaskListener listener) {
        return new JdumpServiceImpl(alarmWay,
                                    alarmUserEmails,
                                    alarmUserPhones,
                                    alarmUserErps,
                                    testResultFile,
                                    onStart,
                                    onSuccess,
                                    onFailed,
                                    listener,
                                    build);
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        return true;
    }

    @Override
    public JdumpNotifierDescriptor getDescriptor() {
        return (JdumpNotifierDescriptor) super.getDescriptor();
    }

    @Extension
    public static class JdumpNotifierDescriptor extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Jdump Notifier";
        }
    }
}
