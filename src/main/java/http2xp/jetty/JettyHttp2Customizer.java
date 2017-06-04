package http2xp.jetty;

import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http2.HTTP2Cipher;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

/**
 * This {@link WebServerFactoryCustomizer} will customize embedded Jetty
 * configuration to:
 * <ul>
 *     <li>update the SSLContextFactory to select the appropriate TLS cipher for HTTP/2 using {@code HTTP2Cipher.COMPARATOR}
 *     <li>replace the ConnectionFactories configured by Boot by {@link ALPNServerConnectionFactory} and {@link HTTP2ServerConnectionFactory}
 * </ul>
 * @author Brian Clozel
 */
@Component
public class JettyHttp2Customizer implements WebServerFactoryCustomizer<JettyServletWebServerFactory> {

	private final ServerProperties serverProperties;

	@Autowired
	public JettyHttp2Customizer(ServerProperties serverProperties) {
		this.serverProperties = serverProperties;
	}

	@Override
	public void customize(JettyServletWebServerFactory factory) {

		factory.addServerCustomizers(new JettyServerCustomizer() {
			@Override
			public void customize(Server server) {
				if (serverProperties.getSsl() != null && serverProperties.getSsl().isEnabled()) {
					ServerConnector connector = (ServerConnector) server.getConnectors()[0];
					int port = connector.getPort();
					SslContextFactory sslContextFactory = connector
							.getConnectionFactory(SslConnectionFactory.class).getSslContextFactory();
					HttpConfiguration httpConfiguration = connector
							.getConnectionFactory(HttpConnectionFactory.class).getHttpConfiguration();

					configureSslContextFactory(sslContextFactory);
					ConnectionFactory[] connectionFactories = createConnectionFactories(sslContextFactory, httpConfiguration);

					ServerConnector serverConnector = new ServerConnector(server, connectionFactories);
					serverConnector.setPort(port);
					// override existing connectors with new ones
					server.setConnectors(new Connector[] {serverConnector});
				}
			}

			private void configureSslContextFactory(SslContextFactory sslContextFactory) {
				sslContextFactory.setCipherComparator(HTTP2Cipher.COMPARATOR);
				sslContextFactory.setUseCipherSuitesOrder(true);
			}

			private ConnectionFactory[] createConnectionFactories(SslContextFactory sslContextFactory,
					HttpConfiguration httpConfiguration) {
				SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, "alpn");
				ALPNServerConnectionFactory alpnServerConnectionFactory =
						new ALPNServerConnectionFactory("h2", "h2-17", "h2-16", "h2-15", "h2-14");

				HTTP2ServerConnectionFactory http2ServerConnectionFactory =
						new HTTP2ServerConnectionFactory(httpConfiguration);

				return new ConnectionFactory[] {sslConnectionFactory, alpnServerConnectionFactory,
						http2ServerConnectionFactory};
			}
		});

	}
}
