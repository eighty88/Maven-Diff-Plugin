package com.github.eighty88.patch;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.eclipse.jgit.api.errors.PatchApplyException;
import org.eclipse.jgit.api.errors.PatchFormatException;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.patch.Patch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Mojo(name = "apply", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
public class ApplyPatchMojo extends MavenPatcher {
    @Override
    void doTask() {
        run(new File(patches));
    }

    void run(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                applyPatch(file.getParentFile().getAbsolutePath(), file.getName());
            } else {
                Arrays.stream(Objects.requireNonNull(file.listFiles())).forEach(this::run);
            }
        }
    }

    void applyPatch(String patchPath, String name) {
        String rootPath = patchPath.replace(patches, root);
        String targetPath = patchPath.replace(patches, target);

        List<String> output = new ArrayList<>();

        try {
            Patch patch = new Patch();
            patch.parse(new FileInputStream(new File(patchPath, name)));

            if (!patch.getErrors().isEmpty())
                throw new PatchFormatException(patch.getErrors());
            for (FileHeader fh : patch.getFiles()) {
                File file = new File(rootPath);
                List<String> apply = apply(file, fh);
                output.addAll(apply);
            }

            if (!(new File(targetPath)).exists()) {
                if (!(new File(targetPath)).mkdirs()) {
                    return;
                }
            }
            Files.write(new File(targetPath, name.replace(".patch", "")).toPath(), output, StandardOpenOption.CREATE);
        } catch (IOException | PatchApplyException | PatchFormatException e) {
            e.printStackTrace();
        }
    }

    private List<String> apply(File f, FileHeader fh) throws IOException, PatchApplyException {
        RawText rt = new RawText(f);
        List<String> oldLines = new ArrayList<>(rt.size());
        for (int i = 0; i < rt.size(); i++)
            oldLines.add(rt.getString(i));
        List<String> newLines = new ArrayList<>(oldLines);
        List<String> newLineToBeAdded = new ArrayList<>();
        for (HunkHeader hh : fh.getHunks()) {

            byte[] b = new byte[hh.getEndOffset() - hh.getStartOffset()];
            System.arraycopy(hh.getBuffer(), hh.getStartOffset(), b, 0, b.length);
            RawText hrt = new RawText(b);

            List<String> hunkLines = new ArrayList<>(hrt.size());
            for (int i = 0; i < hrt.size(); i++)
                hunkLines.add(hrt.getString(i));
            int pos = 0;
            for (int j = 1; j < hunkLines.size(); j++) {
                String hunkLine = hunkLines.get(j);
                switch (hunkLine.charAt(0)) {
                    case '-':
                        if (hh.getNewStartLine() == 0) {
                            newLines.clear();
                        } else {
                            if (!(newLines.get(hh.getNewStartLine() - 1 + pos).replaceAll("([\\r\\n])", "")).equals(hunkLine.substring(1))) {
                                throw new PatchApplyException(MessageFormat.format(JGitText.get().patchApplyException, hh));
                            }
                            newLines.remove(hh.getNewStartLine() - 1 + pos);
                        }
                        break;
                    case '+':
                        newLines.add(hh.getNewStartLine() - 1 + pos, hunkLine.substring(1));
                        newLineToBeAdded.add(hunkLine.substring(1));
                        pos++;
                        break;
                }
            }
        }
        return newLineToBeAdded;
    }
}