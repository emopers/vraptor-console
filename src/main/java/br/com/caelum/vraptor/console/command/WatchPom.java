package br.com.caelum.vraptor.console.command;

import static br.com.caelum.vraptor.console.command.CommandLine.command;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.console.command.jetty.Jetty8VRaptorServer;

public class WatchPom implements Command {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(WatchPom.class);

	private final static CommandLine[] commands = new CommandLine[] {
			command("compile"),
			command("dependency:copy-dependencies",
					"-DoutputDirectory=src/main/webapp/WEB-INF/lib",
					"-DincludeScope=runtime",
					"-Dsilent=true",
					"-DprependGroupId=true"
					) };

	private final static Maven mvn = new Maven();

	@Override
	public void execute(String[] args) throws Exception {
		WatchService service = FileSystems.getDefault().newWatchService();
		configureWatcher(new File("."), service, false);
		configureWatcher(new File("src/main/webapp/WEB-INF/classes"), service, true);
		configureWatcher(new File("target/classes"), service, true);
		watchForChanges(service, null);
	}

	private static void configureWatcher(File listeningTo, WatchService service, boolean recursive)
			throws IOException {
		if(!listeningTo.exists()) return;
		LOGGER.debug("Listening to " + listeningTo.getPath());
		Path path = listeningTo.toPath();
		path.register(service, StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_DELETE,
				StandardWatchEventKinds.ENTRY_MODIFY);
		if (recursive) {
			for (File f : listeningTo.listFiles()) {
				if (f.isDirectory() && f.exists()) {
					configureWatcher(f, service, recursive);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void watchForChanges(final WatchService watcher,
			final Jetty8VRaptorServer server) {
		Runnable onChange = new Runnable() {
			public void run() {
				while (true) {
					try {
						WatchKey key = watcher.take();
						for (WatchEvent<?> event : key.pollEvents()) {
							WatchEvent<Path> ev = (WatchEvent<Path>) event;
							Path filename = ev.context();
							String name = filename.toFile().getName();
							if (name.equals("pom.xml")) {
								LOGGER.debug("pom changed");
								runCommands();
							} else if(name.endsWith(".class")) {
								LOGGER.info("Needs to restart (class " + name + " changed)");
							} else {
								LOGGER.info("Change to " + name + " was ignored");
							}
						}
						key.reset();
					} catch (InterruptedException e) {
						LOGGER.warn("Unable to detect change");
					}
				}
			}

			private void runCommands() {
				for (CommandLine command : commands) {
					runCommand(command);
				}
			}

			private void runCommand(CommandLine command) {
				try {
					mvn.execute(command);
				} catch (Exception e) {
					LOGGER.error("Unable to run " + command, e);
				}
			}
		};
		new Thread(onChange).start();
	}

}
