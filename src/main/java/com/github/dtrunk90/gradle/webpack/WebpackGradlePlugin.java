package com.github.dtrunk90.gradle.webpack;

import de.undercouch.gradle.tasks.download.Download;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WebpackGradlePlugin implements Plugin<Project> {
	public void apply(Project project) {
		WebpackGradlePluginExtension extension = project.getExtensions().create("webpack", WebpackGradlePluginExtension.class, project);

		String baseName = String.format("node-%s-%s-%s", extension.getNodeVersion(), archiveOperatingSystem(), archiveArchitecture());
		String archiveName = String.format("%s.%s", baseName, archiveExtension());

		File archive = extension.getDirectory().toPath().resolve("archive").resolve(archiveName).toFile();

		final FileTree downloadedFiles = Os.isFamily(Os.FAMILY_WINDOWS) ? project.zipTree(archive) : project.tarTree(archive);

		TaskProvider<Download> downloadNode = project.getTasks().register("downloadNode", Download.class, task -> {
			task.setDescription("Download a node archive");
			task.src(String.format("%s/%s/%s", extension.getBaseUrl(), extension.getNodeVersion(), archiveName));
			task.dest(archive);
			task.tempAndMove(true);
			task.overwrite(false);
		});

		// work around symlink problem
		TaskProvider<? extends Task> installNode;
		if (Os.isFamily(Os.FAMILY_WINDOWS)) {
			installNode = project.getTasks().register("installNode", Copy.class, task -> {
				task.setDescription("Unpack and install a node archive.");
				task.dependsOn(downloadNode);
				task.from(downloadedFiles);
				task.into(extension.getDirectory());
			});
		} else {
			installNode = project.getTasks().register("installNode", Exec.class, task -> {
				task.setDescription("Unpack and install a node archive.");
				task.dependsOn(downloadNode);
				task.setWorkingDir(extension.getDirectory());
				task.setCommandLine("tar", "xzf", archive);
			});
		}

		TaskProvider<Exec> installWebpack = project.getTasks().register("installWebpack", Exec.class, task -> {
			List<String> args = new ArrayList<>(Os.isFamily(Os.FAMILY_WINDOWS) ? Arrays.asList("cmd", "/c", baseName + "/bin/npm.bat") : Collections.singleton("./" + baseName + "/bin/npm"));
			args.addAll(Arrays.asList("install", "--save-dev", "webpack@" + extension.getWebpackVersion(), "webpack-cli@" + extension.getWebpackCliVersion()));

			task.setDescription("Install node modules for webpack");
			task.setWorkingDir(extension.getDirectory());
			task.dependsOn(installNode);
			task.setCommandLine(args);
		});

		TaskProvider<Webpack> execWebpack = project.getTasks().register("execWebpack", Webpack.class, task -> {
			task.setDescription("Execute webpack.");
			task.dependsOn(installWebpack);
		});
	}

	private String archiveOperatingSystem() {
		return Os.isFamily(Os.FAMILY_WINDOWS) ? "windows" : (Os.isFamily(Os.FAMILY_MAC) ? "darwin" : "linux");
	}

	private String archiveArchitecture() {
		return "x64";
	}

	private String archiveExtension() {
		return Os.isFamily(Os.FAMILY_WINDOWS) ? "zip" : "tar.gz";
	}
}
