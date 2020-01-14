package com.github.dtrunk90.gradle.webpack;

import de.undercouch.gradle.tasks.download.Download;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.gradle.api.Project;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.Exec;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WebpackGradlePluginTest {
	@Test
	public void pluginRegistersAllTasks() {
		Project project = ProjectBuilder.builder().build();
		project.getPlugins().apply("com.github.dtrunk90.webpack");

		assertThat(project.getTasks().findByName("downloadNode")).isInstanceOf(Download.class);
		assertThat(project.getTasks().findByName("installNode")).isInstanceOf(Os.isFamily(Os.FAMILY_WINDOWS) ? Copy.class : Exec.class);
		assertThat(project.getTasks().findByName("installWebpack")).isInstanceOf(Exec.class);
		assertThat(project.getTasks().findByName("execWebpack")).isInstanceOf(Webpack.class);
	}
}
