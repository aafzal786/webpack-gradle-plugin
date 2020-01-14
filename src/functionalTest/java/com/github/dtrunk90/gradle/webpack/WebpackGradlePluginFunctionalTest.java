package com.github.dtrunk90.gradle.webpack;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

public class WebpackGradlePluginFunctionalTest {
	@TempDir
	public Path projectDir;

	private WireMockServer server;

	@BeforeEach
	public void startServer() {
		server = new WireMockServer(options().dynamicPort());
	}

	@AfterEach
	public void stopServer() {
		server.stop();
	}

	@BeforeEach
	public void setupProject() throws IOException {
		Files.createFile(projectDir.resolve("settings.gradle"));

		try (InputStream input = WebpackGradlePluginFunctionalTest.class.getResourceAsStream("build.gradle")) {
			Files.copy(input, projectDir.resolve("build.gradle"));
		}
	}

	//@Test
	public void shouldInstallNode() throws IOException {
		String archive = Os.isFamily(Os.FAMILY_WINDOWS) ? "archive.zip" : "archive.tgz";

		try (InputStream input = WebpackGradlePluginFunctionalTest.class.getResourceAsStream(archive)) {
			byte[] body = new byte[input.available()];
			input.read(body);

			server.stubFor(get(urlMatching("/some.specific.version/node-.*")).willReturn(aResponse().withStatus(200).withBody(body)));
		}

		GradleRunner.create()
				.withPluginClasspath()
				.withEnvironment(Collections.singletonMap("URL", server.baseUrl()))
				.withArguments("installNode")
				.withProjectDir(projectDir.toFile())
				.build();

		assertThat(projectDir.resolve(".gradle/node/archive")).isNotEmptyDirectory();
		assertThat(projectDir.resolve(".gradle/node/bin/node")).hasContent("foo\nbar\nbaz\n");
	}

	//@Test
	public void shouldExecWebpack() throws IOException {
		Path out = createExecutable();

		GradleRunner.create()
				.withPluginClasspath()
				.withArguments("execWebpack")
				.withProjectDir(projectDir.toFile())
				.build();

		assertThat(out).hasContent(String.format("webpack --config %1$s/webpack.config.js", projectDir.toRealPath()));
	}

	private Path createExecutable() throws IOException {
		Path out = projectDir.resolve("build/out");
		Path nodeDir = projectDir.resolve(".gradle/node/bin");

		Files.createDirectories(nodeDir);
		Files.createDirectories(projectDir.resolve("src/main/sass"));

		if (Os.isFamily(Os.FAMILY_WINDOWS)) {
			Path npx = nodeDir.resolve("npx.bat");

			try (Writer writer = Files.newBufferedWriter(npx, StandardOpenOption.CREATE)) {
				writer.write(String.format("@echo npx %%* > %s\n", out));
			}
		} else {
			Path npx = nodeDir.resolve("npx");
			Files.createFile(npx, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-xr-x")));
			try (Writer writer = Files.newBufferedWriter(npx)) {
				writer.write("#!/bin/sh\n");
				writer.write(String.format("echo npx $@ > %s\n", out));
			}
		}

		return out;
	}
}
