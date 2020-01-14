package com.github.dtrunk90.gradle.webpack;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.workers.WorkParameters;

public interface WebpackWorkParameters extends WorkParameters {
	RegularFileProperty getExecutable();
	RegularFileProperty getConfigFile();
}
