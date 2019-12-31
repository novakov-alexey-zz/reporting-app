package com.reporting.search

import java.security.{KeyStore, SecureRandom}

import akka.http.scaladsl.{ConnectionContext, HttpsConnectionContext}
import com.typesafe.scalalogging.StrictLogging
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

object SslContext extends StrictLogging {
  def createContext(): HttpsConnectionContext = {
    val password =
      sys.env
        .getOrElse(
          "SSL_KS_PWD",
          sys.error("Please specify 'SSL_KS_PWD' environment variable as password for Java Keystore")
        )
        .toCharArray

    val ks = KeyStore.getInstance("PKCS12")
    val jksFileName = sys.env.getOrElse("SSL_KS_NAME", "keystore.jks")
    logger.debug("Loading java keystore file: {}", jksFileName)
    val keystore = getClass.getClassLoader.getResourceAsStream(jksFileName)

    require(keystore != null, "Keystore required!")
    ks.load(keystore, password)

    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(ks, password)

    val tmf = TrustManagerFactory.getInstance("SunX509")
    tmf.init(ks)

    val context = SSLContext.getInstance("TLS")
    context.init(keyManagerFactory.getKeyManagers, tmf.getTrustManagers, new SecureRandom)
    ConnectionContext.https(context)
  }
}
