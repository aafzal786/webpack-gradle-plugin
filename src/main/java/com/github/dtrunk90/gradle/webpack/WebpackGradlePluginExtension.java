package com.github.dtrunk90.gradle.webpack;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.Project;

import java.io.File;

@Getter
@Setter
public class WebpackGradlePluginExtension {
	private String nodeVersion;
	private String webpackVersion;
	private String webpackCliVersion;

	private File directory;
	private String baseUrl;

	public WebpackGradlePluginExtension(Project project) {
		nodeVersion = "v12.14.1";
		webpackVersion = "4.41.5";
		webpackCliVersion = "3.3.10";

		directory = project.getRootDir().toPath().resolve(".gradle").resolve("node").toFile();
		baseUrl = "https://nodejs.org/dist";
	}
}
