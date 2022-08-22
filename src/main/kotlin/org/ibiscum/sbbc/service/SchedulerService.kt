package org.ibiscum.sbbc.service

import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.kcrypto.cert.dsl.*
import org.bouncycastle.kcrypto.dsl.*
import org.bouncycastle.kcrypto.pkcs.dsl.*
import org.bouncycastle.kutil.findBCProvider
import org.bouncycastle.kutil.writePEMObject
import org.bouncycastle.oer.its.ieee1609dot2.basetypes.HashAlgorithm.sha256
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.OutputStreamWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class SchedulerService {
    private val logger = LoggerFactory.getLogger(SchedulerService::class.java)

    @Scheduled(fixedRate = 3000)
    fun fixedRateScheduledTask() {
        using(findBCProvider())

        logger.info("The time is now ${DateTimeFormatter.ISO_LOCAL_TIME.format(LocalDateTime.now())}")

        val kp = signingKeyPair {
            rsa {
                keySize = 2048
            }
        }

        val name = x500Name {
            rdn(BCStyle.C, "AU")
            rdn(BCStyle.O, "The Legion of the Bouncy Castle")
            rdn(BCStyle.L, "Melbourne")
            rdn(BCStyle.CN, "Eric H. Echidna")
            rdn(BCStyle.EmailAddress, "feedback-crypto@bouncycastle.org")
        }

        val extensions = extensions {
            critical(extension {
                extOid = Extension.basicConstraints
                extValue = BasicConstraints(true)
            })
            critical(extension {
                extOid = Extension.keyUsage
                extValue = KeyUsage(KeyUsage.keyCertSign or KeyUsage.cRLSign)
            })
            subjectKeyIdentifierExtension {
                subjectKey = kp.verificationKey
            }
        }

        val pkcs10 = pkcs10Request {
            subject = name
            subjectKey = kp.verificationKey
            attributes = attributes {
                attribute {
                    attrType = PKCSObjectIdentifiers.pkcs_9_at_extensionRequest
                    attrValue = extensions
                }
            }
            signature {
                PKCS1v1dot5 with sha256 using kp.signingKey
            }
        }

        OutputStreamWriter(System.out).writePEMObject(kp.signingKey)

        OutputStreamWriter(System.out).writePEMObject(pkcs10)
    }

    @Scheduled(fixedDelay = 3000)
    fun fixedDelayScheduledTask() {
        //Some code here
    }

    @Scheduled(initialDelay = 2000, fixedDelay = 3000)
    fun initialDelayScheduledTask() {
        //Some code here
    }
    @Scheduled(cron = "0/3 * * * * *")
    fun cronScheduledTask() {
        //Some code here
    }

    @Scheduled(cron = "\${my.cron.value}")
    fun cronScheduledTaskUsingProperties() {
        //Some code here
    }
}