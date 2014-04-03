package br.com.caelum.vraptor.console.command;

import br.com.caelum.vraptor.console.command.parser.ParsedCommand;
import br.com.caelum.vraptor.undertown.builder.ServerBuilder;

import com.google.inject.Inject;

public class Start implements Command {
	
	private final Maven maven;

	@Inject
	public Start(Maven maven) {
		this.maven = maven;
	}

	@Override
	public void execute(ParsedCommand parsedCommand) throws Exception {
		maven.execute(new CommandLine("clean"), new CommandLine("compile"), 
				WatchPom.COPY_DEPENDENCIES);
		ServerBuilder.context("/app").webAppFolder("src/main/webapp")
			.warName("app").port(8080).address("localhost").start();
	}

}
