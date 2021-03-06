/*
 *
 * Copyright (c) 2013 - 2018 Lijun Liao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xipki.ca.client.api.dto;

import org.bouncycastle.asn1.cmp.CMPCertificate;
import org.bouncycastle.asn1.cmp.PKIStatus;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;

/**
 * TODO.
 * @author Lijun Liao
 * @since 2.0.0
 */

public class EnrollCertResultEntry extends ResultEntry {

  private final CMPCertificate cert;

  private final PrivateKeyInfo privateKeyInfo;

  private final int status;

  public EnrollCertResultEntry(String id, CMPCertificate cert, PrivateKeyInfo privateKeyInfo) {
    this(id, cert, privateKeyInfo, PKIStatus.GRANTED);
  }

  public EnrollCertResultEntry(String id, CMPCertificate cert, PrivateKeyInfo privateKeyInfo,
      int status) {
    super(id);
    this.cert = cert;
    this.privateKeyInfo = privateKeyInfo;
    this.status = status;
  }

  public CMPCertificate getCert() {
    return cert;
  }

  public PrivateKeyInfo getPrivateKeyInfo() {
    return privateKeyInfo;
  }

  public int getStatus() {
    return status;
  }

}
