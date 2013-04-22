package br.com.caelum.vraptor.console.command;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class Restart implements Command {

	@Override
	public void execute(String[] args, File output) throws Exception {
		new Maven().execute(output, new CommandLine("compile"),WatchPom.COPY_DEPENDENCIES);
		if (new File("src/jetty").exists()) {
			customJetty();
		} else {
			new StartJetty().execute(args, output);
		}
	}

	private void customNotImplementedJetty() throws MalformedURLException,
			NoSuchMethodException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		// use esse diretorio
		URLClassLoader loader = new URLClassLoader(
				new URL[] { new File("jetty").toURL() }, this.getClass()
						.getClassLoader());
		Class<?> type;
		try {
			type = loader.loadClass(this.getClass().getPackage().getName()
					+ ".Main");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		Method method = type.getMethod("main", new Class[] { String[].class });
		Object instance = type.newInstance();
		method.invoke(instance, new String[] {});
	}

	private void customJetty() {
		throw new UnsupportedOperationException(
				"/jetty dir found, but we haven't implemented "
						+ "custom jetty servers support, contact the developers for more info");
	}

}
