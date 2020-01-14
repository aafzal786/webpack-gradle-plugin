package com.github.dtrunk90.gradle.webpack;

import org.gradle.process.ExecOperations;
import org.gradle.workers.WorkAction;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public abstract class WebpackWorkAction implements WorkAction<WebpackWorkParameters> {
	private final ExecOperations execOperations;

	@Inject
	public WebpackWorkAction(ExecOperations execOperations) {
		this.execOperations = execOperations;
	}

	@Override
	public void execute() {
		WebpackWorkParameters parameters = getParameters();

		execOperations.exec(execSpec -> {
			execSpec.executable(parameters.getExecutable().get());

			List<String> args = new ArrayList<>();
			args.add(String.format("--config %s", parameters.getConfigFile().get().toString()));
			execSpec.args(args);
		});
	}
}
