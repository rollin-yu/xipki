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

package org.xipki.ca.api.profile;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.xipki.util.ParamUtil;

/**
 * TODO.
 * @author Lijun Liao
 * @since 2.0.0
 */

public class ExtKeyUsageControl {

  private final ASN1ObjectIdentifier extKeyUsage;

  private final boolean required;

  public ExtKeyUsageControl(ASN1ObjectIdentifier extKeyUsage, boolean required) {
    this.extKeyUsage = ParamUtil.requireNonNull("extKeyUsage", extKeyUsage);
    this.required = required;
  }

  public ASN1ObjectIdentifier getExtKeyUsage() {
    return extKeyUsage;
  }

  public boolean isRequired() {
    return required;
  }

}
