/*
 * Copyright (c) 2014 Lijun Liao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 *
 */

package org.xipki.ca.client.shell;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.bouncycastle.asn1.x500.X500Name;
import org.xipki.ca.common.CertIDOrError;
import org.xipki.ca.common.PKIStatusInfo;
import org.xipki.security.common.CRLReason;
import org.xipki.security.common.IoCertUtil;

/**
 * @author Lijun Liao
 */

@Command(scope = "caclient", name = "revoke", description="Revoke certificate")
public class RARevokeCertCommand extends ClientCommand
{
    public static List<CRLReason> permitted_reasons = Collections.unmodifiableList(
            Arrays.asList(    new CRLReason[]
            {
                CRLReason.UNSPECIFIED, CRLReason.KEY_COMPROMISE,
                CRLReason.AFFILIATION_CHANGED, CRLReason.SUPERSEDED, CRLReason.CESSATION_OF_OPERATION,
                CRLReason.CERTIFICATE_HOLD,    CRLReason.PRIVILEGE_WITHDRAWN}));

    @Option(name = "-cert",
            description = "Certificate file")
    protected String            certFile;

    @Option(name = "-cacert",
            description = "CA Certificate file")
    protected String            caCertFile;

    @Option(name = "-serial",
            description = "Serial number")
    protected String            serialNumber;

    @Option(name = "-reason",
            required = true,
            description = "Required. Reason, valid values are \n" +
                    "  0: unspecified\n" +
                    "  1: keyCompromise\n" +
                    "  3: affiliationChanged\n" +
                    "  4: superseded\n" +
                    "  5: cessationOfOperation\n" +
                    "  6: certificateHold\n" +
                    "  9: privilegeWithdrawn")
    protected String           reason;

    @Override
    protected Object doExecute()
    throws Exception
    {
        if(certFile == null && (caCertFile == null || serialNumber == null))
        {
            System.err.println("either cert or (cacert, serial) must be specified");
            return null;
        }

        CRLReason crlReason = CRLReason.getInstance(reason);
        if(crlReason == null)
        {
            System.out.println("invalid reason " + reason);
            return null;
        }

        if(permitted_reasons.contains(crlReason) == false)
        {
            System.err.println("reason " + reason + " is not permitted");
            return null;
        }

        CertIDOrError certIdOrError;
        if(certFile != null)
        {
            X509Certificate cert = IoCertUtil.parseCert(certFile);
            certIdOrError = raWorker.revokeCert(cert, crlReason.getCode());
        }
        else
        {
            X509Certificate caCert = IoCertUtil.parseCert(caCertFile);
            X500Name issuer = X500Name.getInstance(caCert.getSubjectX500Principal().getEncoded());
            certIdOrError = raWorker.revokeCert(issuer, new BigInteger(serialNumber), crlReason.getCode());
        }

        if(certIdOrError.getError() != null)
        {
            PKIStatusInfo error = certIdOrError.getError();
            System.err.println("Revocation failed: " + error);
        }
        else
        {
            System.out.println("Revoked certificate");
        }
        return null;
    }

}
