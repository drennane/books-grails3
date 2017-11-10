package books3.clientauth

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.apache.catalina.Context
import org.apache.catalina.connector.Connector
import org.apache.coyote.http11.Http11NioProtocol
import org.apache.tomcat.util.descriptor.web.LoginConfig
import org.apache.tomcat.util.descriptor.web.SecurityCollection
import org.apache.tomcat.util.descriptor.web.SecurityConstraint
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory
import org.springframework.context.annotation.Bean

class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    @Bean
    EmbeddedServletContainerCustomizer containerCustomizer() throws Exception {

        return new EmbeddedServletContainerCustomizer() {
            @Override
            public void customize(ConfigurableEmbeddedServletContainer container) {
                TomcatEmbeddedServletContainerFactory tomcat = (TomcatEmbeddedServletContainerFactory) container
                tomcat.addConnectorCustomizers(
                        new TomcatConnectorCustomizer() {
                            @Override
                            public void customize(Connector connector) {
                                connector.setPort(8443)
                                connector.setSecure(true)
                                connector.setScheme("https")

                                Http11NioProtocol proto = (Http11NioProtocol) connector.getProtocolHandler()
                                proto.setMinSpareThreads(5)
                                proto.setSSLEnabled(true)
                                proto.setClientAuth("false")

                                proto.setKeystoreFile("/home/me/keys/cbgui.jks")
                                proto.setKeystorePass("changeit")
                                proto.setKeystoreType("JKS")
                                proto.setKeyAlias("ssl_server")
                                proto.setTruststoreFile("/home/me/keys/cbgui.jts")
                                proto.setTruststoreType("JKS")
                                proto.setTruststorePass("changeit")
                                proto.setCiphers("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,TLS_ECDHE_RSA_WITH_RC4_128_SHA,TLS_RSA_WITH_AES_128_CBC_SHA256,TLS_RSA_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_256_CBC_SHA256,TLS_RSA_WITH_AES_256_CBC_SHA,SSL_RSA_WITH_RC4_128_SHA")
                            }
                        })
                tomcat.addContextCustomizers(new TomcatContextCustomizer() {
                    @Override
                    public void customize(Context context) {
                        context.setPath("/books3")
                        SecurityConstraint sc = new SecurityConstraint()
                        sc.addAuthRole("secureConnection")

                        SecurityCollection securityCollection = new SecurityCollection()
                        securityCollection.setName("Protected")
                        securityCollection.addPattern("/book/*")
                        sc.setUserConstraint("CONFIDENTIAL")
                        sc.addCollection(securityCollection)

                        LoginConfig loginConfig = new LoginConfig()
                        loginConfig.setAuthMethod("CLIENT-CERT")

                        context.addConstraint(sc)
                        context.setLoginConfig(loginConfig)
                        context.addSecurityRole("secureConnection")

                    }
                });
            }
        }
    }
}