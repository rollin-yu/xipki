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

package org.xipki.security.speed.shell.completer;

import java.util.LinkedList;
import java.util.List;

import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.xipki.shell.completer.AbstractEnumCompleter;

/**
 * TODO.
 * @author Lijun Liao
 * @since 2.0.0
 */

@Service
//CHECKSTYLE:SKIP
public class ECDSASigAlgCompleter extends AbstractEnumCompleter {

  public ECDSASigAlgCompleter() {
    String[] hashAlgs = {"SHA1", "SHA224", "SHA256", "SHA384", "SHA512",
      "SHA3-224", "SHA3-256", "SHA3-384", "SHA3-512"};
    List<String> enums = new LinkedList<>();
    for (String hashAlg : hashAlgs) {
      enums.add(hashAlg + "withECDSA");
    }
    hashAlgs = new String[]{"SHA1", "SHA224", "SHA256", "SHA384", "SHA512"};
    for (String hashAlg : hashAlgs) {
      enums.add(hashAlg + "withPlainECDSA");
    }
    setTokens(enums);
  }

}
