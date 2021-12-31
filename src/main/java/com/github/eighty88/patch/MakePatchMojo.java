package com.github.eighty88.patch;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.eclipse.jgit.diff.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Objects;

@Mojo(name = "make", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
public class MakePatchMojo extends MavenPatcher {
    @Override
    void doTask() {
        run(new File(root));
    }

    void run(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                makePatch(file.getParentFile().getAbsolutePath(), file.getName());
            } else {
                Arrays.stream(Objects.requireNonNull(file.listFiles())).forEach(this::run);
            }
        }
    }

    void makePatch(String rootPath, String name) {
        String targetPath = rootPath.replace(root, target);
        String outputPath = rootPath.replace(root, patches);

        try {
            RawText rootText = new RawText(Files.readAllBytes(new File(rootPath, name).toPath()));
            RawText targetText;

            if ((new File(targetPath)).exists()) {
                targetText = new RawText(Files.readAllBytes(new File(targetPath, name).toPath()));
            } else {
                targetText = new RawText("".getBytes());
            }

            OutputStream out = new ByteArrayOutputStream();
            EditList diff = new HistogramDiff().diff(RawTextComparator.DEFAULT, rootText, targetText);
            new DiffFormatter(out).format(diff, rootText, targetText);
            if (!StringUtils.isEmpty(out.toString()) && !StringUtils.isBlank(out.toString())) {
                if (!(new File(outputPath)).exists()) {
                    if (!(new File(outputPath)).mkdirs()) {
                        return;
                    }
                }
                Files.write(new File(outputPath, name + ".patch").toPath(), out.toString().getBytes(), StandardOpenOption.CREATE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
