package com.wanyukang.jdump;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.tasks.Publisher;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Created by wanyukang on 17/6/12.
 */
@Extension
public class JobListener extends RunListener<AbstractBuild> {

    public JobListener() {
        super(AbstractBuild.class);
    }

    @Override
    public void onStarted(AbstractBuild builder, TaskListener listener) {

        getService(builder, listener).start();
    }

    @Override
    public void onCompleted(AbstractBuild builder, @Nonnull TaskListener listener) {

        Result result = builder.getResult();
        if (null != result && result.equals(Result.SUCCESS)) {
            getService(builder, listener).success();
        } else {
            getService(builder, listener).failed();
        }
    }

    private JdumpService getService(AbstractBuild builder, TaskListener listener) {

        Map<Descriptor<Publisher>, Publisher> map = builder.getProject().getPublishersList().toMap();
        for (Publisher publisher : map.values()) {
            if (publisher instanceof JdumpNotifier) {
                return ((JdumpNotifier) publisher).newJdumpService(builder, listener);
            }
        }
        return null;
    }
}
