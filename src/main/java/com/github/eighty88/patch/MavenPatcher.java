package com.github.eighty88.patch;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

public abstract class MavenPatcher extends AbstractMojo {
    @Parameter(required = true)
    String root = "";

    @Parameter(required = true)
    String target = "";

    @Parameter(required = true)
    String patches = "";

    @Override
    public void execute() {
        tweakPath();
        doTask();
    }

    void tweakPath() {
        root = new File(root).getAbsolutePath();
        target = new File(target).getAbsolutePath();
        patches = new File(patches).getAbsolutePath();
    }

    abstract void doTask();
}
