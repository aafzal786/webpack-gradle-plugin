package com.github.dtrunk90.gradle.webpack;

import lombok.Setter;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;
import java.util.Objects;

public class Webpack extends DefaultTask {
	private final WorkerExecutor workerExecutor;

	@Setter
	private File configFile = new File(getProject().getProjectDir(), "webpack.config.js");

	@Inject
	public Webpack(WorkerExecutor workerExecutor) {
		super();
		this.workerExecutor = workerExecutor;
	}

	@TaskAction
	public void execWebpack() {
		File executable = Objects.requireNonNull(getProject().getExtensions().findByType(WebpackGradlePluginExtension.class)).getDirectory().toPath()
				.resolve("node_modules/.bin/webpack").toFile();

		WorkQueue workQueue = workerExecutor.noIsolation();
		workQueue.submit(WebpackWorkAction.class, webpackWorkParameters -> {
			webpackWorkParameters.getExecutable().set(executable);
			webpackWorkParameters.getConfigFile().set(configFile);
		});
	}
}
