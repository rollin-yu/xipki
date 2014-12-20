/*
 *
 * This file is part of the XiPKI project.
 * Copyright (c) 2014 Lijun Liao
 * Author: Lijun Liao
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * THE AUTHOR LIJUN LIAO. LIJUN LIAO DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the XiPKI software without
 * disclosing the source code of your own applications.
 *
 * For more information, please contact Lijun Liao at this
 * address: lijun.liao@gmail.com
 */

package org.xipki.ca.qa.certprofile.x509;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.SchemaFactory;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1StreamParser;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.DirectoryString;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.CertPolicyId;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.GeneralSubtree;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.asn1.x509.PolicyQualifierId;
import org.bouncycastle.asn1.x509.PolicyQualifierInfo;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.UserNotice;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xipki.ca.api.BadCertTemplateException;
import org.xipki.ca.api.CertProfileException;
import org.xipki.ca.api.CertValidity;
import org.xipki.ca.api.profile.ExtensionOccurrence;
import org.xipki.ca.api.profile.RDNOccurrence;
import org.xipki.ca.api.profile.x509.CertificatePolicyInformation;
import org.xipki.ca.api.profile.x509.CertificatePolicyQualifier;
import org.xipki.ca.qa.ValidationIssue;
import org.xipki.ca.qa.cainfo.X509CAInfo;
import org.xipki.ca.qa.certprofile.GeneralNameMode;
import org.xipki.ca.qa.certprofile.GeneralNameTag;
import org.xipki.ca.qa.certprofile.KeyParamRange;
import org.xipki.ca.qa.certprofile.KeyParamRanges;
import org.xipki.ca.qa.certprofile.SubjectDNOption;
import org.xipki.ca.qa.certprofile.x509.jaxb.AlgorithmType;
import org.xipki.ca.qa.certprofile.x509.jaxb.CertificatePolicyInformationType;
import org.xipki.ca.qa.certprofile.x509.jaxb.CertificatePolicyInformationType.PolicyQualifiers;
import org.xipki.ca.qa.certprofile.x509.jaxb.ConstantExtensionType;
import org.xipki.ca.qa.certprofile.x509.jaxb.CurveType;
import org.xipki.ca.qa.certprofile.x509.jaxb.CurveType.Encodings;
import org.xipki.ca.qa.certprofile.x509.jaxb.ECParameterType;
import org.xipki.ca.qa.certprofile.x509.jaxb.ExtensionType;
import org.xipki.ca.qa.certprofile.x509.jaxb.ExtensionsType;
import org.xipki.ca.qa.certprofile.x509.jaxb.ExtensionsType.Admission;
import org.xipki.ca.qa.certprofile.x509.jaxb.ExtensionsType.CertificatePolicies;
import org.xipki.ca.qa.certprofile.x509.jaxb.ExtensionsType.ConstantExtensions;
import org.xipki.ca.qa.certprofile.x509.jaxb.ExtensionsType.InhibitAnyPolicy;
import org.xipki.ca.qa.certprofile.x509.jaxb.ExtensionsType.NameConstraints;
import org.xipki.ca.qa.certprofile.x509.jaxb.ExtensionsType.PolicyConstraints;
import org.xipki.ca.qa.certprofile.x509.jaxb.ExtensionsType.PolicyMappings;
import org.xipki.ca.qa.certprofile.x509.jaxb.GeneralNameType;
import org.xipki.ca.qa.certprofile.x509.jaxb.GeneralSubtreeBaseType;
import org.xipki.ca.qa.certprofile.x509.jaxb.GeneralSubtreesType;
import org.xipki.ca.qa.certprofile.x509.jaxb.KeyUsageType;
import org.xipki.ca.qa.certprofile.x509.jaxb.ObjectFactory;
import org.xipki.ca.qa.certprofile.x509.jaxb.OidWithDescType;
import org.xipki.ca.qa.certprofile.x509.jaxb.ParameterType;
import org.xipki.ca.qa.certprofile.x509.jaxb.PolicyIdMappingType;
import org.xipki.ca.qa.certprofile.x509.jaxb.ProfileType;
import org.xipki.ca.qa.certprofile.x509.jaxb.RdnType;
import org.xipki.ca.qa.certprofile.x509.jaxb.SubjectInfoAccessType.Access;
import org.xipki.common.HashAlgoType;
import org.xipki.common.HashCalculator;
import org.xipki.common.LogUtil;
import org.xipki.common.LruCache;
import org.xipki.common.ObjectIdentifiers;
import org.xipki.common.SecurityUtil;
import org.xml.sax.SAXException;

/**
 * @author Lijun Liao
 */

public class QAX509CertProfile
{
    private static final Logger LOG = LoggerFactory.getLogger(QAX509CertProfile.class);
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final long SECOND = 1000L;
    private static final char GENERALNAME_SEP = '|';
    public static final String MODULUS_LENGTH = "moduluslength";
    public static final String P_LENGTH = "plength";
    public static final String Q_LENGTH = "qlength";

    private static final Map<ASN1ObjectIdentifier, String> extOidNameMap;

    static
    {
        extOidNameMap = new HashMap<>();
        extOidNameMap.put(Extension.auditIdentity, "auditIdentity");
        extOidNameMap.put(Extension.authorityInfoAccess, "authorityInfoAccess");
        extOidNameMap.put(Extension.authorityKeyIdentifier, "authorityKeyIdentifier");
        extOidNameMap.put(Extension.basicConstraints, "basicConstraints");
        extOidNameMap.put(Extension.biometricInfo, "biometricInfo");
        extOidNameMap.put(Extension.certificateIssuer, "certificateIssuer");
        extOidNameMap.put(Extension.certificatePolicies, "certificatePolicies");
        extOidNameMap.put(Extension.cRLDistributionPoints, "cRLDistributionPoints");
        extOidNameMap.put(Extension.deltaCRLIndicator, "deltaCRLIndicator");
        extOidNameMap.put(Extension.extendedKeyUsage, "extendedKeyUsage");
        extOidNameMap.put(Extension.inhibitAnyPolicy, "inhibitAnyPolicy");
        extOidNameMap.put(Extension.instructionCode, "instructionCode");
        extOidNameMap.put(Extension.issuerAlternativeName, "issuerAlternativeName");
        extOidNameMap.put(Extension.issuingDistributionPoint, "issuingDistributionPoint");
        extOidNameMap.put(Extension.keyUsage, "keyUsage");
        extOidNameMap.put(Extension.logoType, "logoType");
        extOidNameMap.put(Extension.nameConstraints, "nameConstraints");
        extOidNameMap.put(Extension.policyConstraints, "policyConstraints");
        extOidNameMap.put(Extension.policyMappings, "policyMappings");
        extOidNameMap.put(Extension.privateKeyUsagePeriod, "privateKeyUsagePeriod");
        extOidNameMap.put(Extension.qCStatements, "qCStatements");
        extOidNameMap.put(Extension.subjectAlternativeName, "subjectAlternativeName");
        extOidNameMap.put(Extension.subjectDirectoryAttributes, "subjectDirectoryAttributes");
        extOidNameMap.put(Extension.subjectInfoAccess, "subjectInfoAccess");
        extOidNameMap.put(Extension.subjectKeyIdentifier, "subjectKeyIdentifier");
        extOidNameMap.put(Extension.targetInformation, "targetInformation");
    }

    private static final Set<ASN1ObjectIdentifier> ignoreRDNs;

    private final static Object jaxbUnmarshallerLock = new Object();
    private static Unmarshaller jaxbUnmarshaller;

    protected final ProfileType profileConf;

    private Map<ASN1ObjectIdentifier, Set<Byte>> allowedEcCurves;
    private Map<ASN1ObjectIdentifier, List<KeyParamRanges>> nonEcKeyAlgorithms;

    private Map<ASN1ObjectIdentifier, SubjectDNOption> subjectDNOptions;
    private Map<ASN1ObjectIdentifier, RDNOccurrence> subjectDNOccurrences;
    private Map<ASN1ObjectIdentifier, ExtensionOccurrence> extensionOccurences;

    private CertValidity validity;
    private int syntaxVersion;
    private boolean ca;
    private boolean notBeforeMidnight;
    private Integer pathLen;
    private org.bouncycastle.asn1.x509.KeyUsage keyusage;
    private Set<String> extendedKeyusages;
    private Set<GeneralNameMode> allowedSubjectAltNameModes;
    private Map<String, Set<GeneralNameMode>> allowedSubjectInfoAccessModes;

    private AuthorityKeyIdentifierOption akiOption;
    private Set<String> sigantureAlgorithms;
    private CertificatePolicies certificatePolicies;
    private PolicyMappings policyMappings;
    private NameConstraints nameConstraints;
    private PolicyConstraints policyConstraints;
    private InhibitAnyPolicy inhibitAnyPolicy;
    private Admission admission;

    private Map<ASN1ObjectIdentifier, byte[]> constantExtensions;
    private static LruCache<ASN1ObjectIdentifier, Integer> ecCurveFieldSizes = new LruCache<>(100);

    static
    {
        ignoreRDNs = new HashSet<>(2);
        ignoreRDNs.add(Extension.subjectAlternativeName);
        ignoreRDNs.add(Extension.subjectInfoAccess);
    }

    public QAX509CertProfile(String xmlConf)
    throws CertProfileException
    {
        try
        {
            ProfileType conf = parse(xmlConf);
            this.profileConf = conf;

            this.syntaxVersion = conf.getVersion();
            if(conf.getSignatureAlgorithms() != null)
            {
                List<OidWithDescType> _algorithms = conf.getSignatureAlgorithms().getAlgorithm();
                Set<String> oids = new HashSet<>(_algorithms.size());
                for(OidWithDescType _algorithm : _algorithms)
                {
                    oids.add(_algorithm.getValue());
                }
                this.sigantureAlgorithms = Collections.unmodifiableSet(oids);
            }

            this.validity = CertValidity.getInstance(conf.getValidity());
            this.ca = conf.isCa();
            this.notBeforeMidnight = "midnight".equalsIgnoreCase(conf.getNotBeforeTime());

            // KeyAlgorithms
            if(conf.getKeyAlgorithms() != null)
            {
                List<AlgorithmType> types = conf.getKeyAlgorithms().getAlgorithm();
                this.nonEcKeyAlgorithms = new HashMap<>();
                this.allowedEcCurves = new HashMap<>();

                for(AlgorithmType type : types)
                {
                    ASN1ObjectIdentifier oid = new ASN1ObjectIdentifier(type.getAlgorithm().getValue());
                    if(X9ObjectIdentifiers.id_ecPublicKey.equals(oid))
                    {
                        ECParameterType params = type.getEcParameter();
                        if(params != null)
                        {
                            for(CurveType curveType :params.getCurve())
                            {
                                ASN1ObjectIdentifier curveOid = new ASN1ObjectIdentifier(curveType.getOid().getValue());
                                Encodings encodingsType = curveType.getEncodings();
                                Set<Byte> encodings = new HashSet<>();
                                if(encodingsType != null)
                                {
                                    encodings.addAll(encodingsType.getEncoding());
                                }
                                this.allowedEcCurves.put(curveOid, encodings);
                            }
                        }
                    }
                    else
                    {
                        KeyParamRanges ranges = null;

                        List<ParameterType> paramTypes = type.getParameter();
                        if(paramTypes.isEmpty() == false)
                        {
                            Map<String, List<KeyParamRange>> map = new HashMap<>(paramTypes.size());
                            for(ParameterType paramType : paramTypes)
                            {
                                if(paramType.getMin() != null || paramType.getMax() != null)
                                {
                                    List<KeyParamRange> list = map.get(paramType.getName());
                                    if(list == null)
                                    {
                                        list = new LinkedList<>();
                                        map.put(paramType.getName(), list);
                                    }

                                    list.add(new KeyParamRange(paramType.getMin(), paramType.getMax()));
                                }
                            }

                            if(map.isEmpty() == false)
                            {
                                ranges = new KeyParamRanges(map);
                            }
                        }

                        List<KeyParamRanges> list = this.nonEcKeyAlgorithms.get(oid);
                        if(list == null)
                        {
                            list = new LinkedList<>();
                            this.nonEcKeyAlgorithms.put(oid, list);
                        }

                        if(ranges != null)
                        {
                            list.add(ranges);
                        }
                    }
                }

                if(allowedEcCurves.isEmpty())
                {
                    allowedEcCurves = null;
                }

                if(nonEcKeyAlgorithms.isEmpty())
                {
                    nonEcKeyAlgorithms = null;
                }
            }

            // Subject
            if(conf.getSubject() != null)
            {
                this.subjectDNOccurrences = new HashMap<>();
                this.subjectDNOptions = new HashMap<>();

                for(RdnType t : conf.getSubject().getRdn())
                {
                    ASN1ObjectIdentifier type = new ASN1ObjectIdentifier(t.getType().getValue());
                    RDNOccurrence occ = new RDNOccurrence(type,
                            getInt(t.getMinOccurs(), 1), getInt(t.getMaxOccurs(), 1));
                    this.subjectDNOccurrences.put(type, occ);

                    List<Pattern> patterns = null;
                    if(t.getRegex().isEmpty() == false)
                    {
                        patterns = new LinkedList<>();
                        for(String regex : t.getRegex())
                        {
                            Pattern pattern = Pattern.compile(regex);
                            patterns.add(pattern);
                        }
                    }

                    SubjectDNOption option = new SubjectDNOption(t.getPrefix(), t.getSuffix(), patterns);
                    this.subjectDNOptions.put(type, option);
                }
            }

            // Extensions
            ExtensionsType extensionsType = conf.getExtensions();
            this.pathLen = extensionsType.getPathLen();

            // Extension Occurrences
            Map<ASN1ObjectIdentifier, ExtensionOccurrence> occurrences = new HashMap<>();
            for(ExtensionType extensionType : extensionsType.getExtension())
            {
                String oid = extensionType.getValue();
                boolean required = extensionType.isRequired();
                Boolean b = extensionType.isCritical();

                boolean critical = b == null ? false : b.booleanValue();

                occurrences.put(new ASN1ObjectIdentifier(oid),
                        ExtensionOccurrence.getInstance(critical, required));
            }

            this.extensionOccurences = Collections.unmodifiableMap(occurrences);

            // Extension KeyUsage
            ExtensionOccurrence occurrence = extensionOccurences.get(Extension.keyUsage);
            if(occurrence != null && extensionsType.getKeyUsage() != null)
            {
                List<KeyUsageType> keyUsageTypeList = extensionsType.getKeyUsage().getUsage();

                int keyusage = 0;
                for(KeyUsageType type : keyUsageTypeList)
                {
                    switch(type)
                    {
                    case CRL_SIGN:
                        keyusage |= org.bouncycastle.asn1.x509.KeyUsage.cRLSign;
                        break;
                    case DATA_ENCIPHERMENT:
                        keyusage |= org.bouncycastle.asn1.x509.KeyUsage.dataEncipherment;
                        break;
                    case CONTENT_COMMITMENT:
                        keyusage |= org.bouncycastle.asn1.x509.KeyUsage.nonRepudiation;
                        break;
                    case DECIPHER_ONLY:
                        keyusage |= org.bouncycastle.asn1.x509.KeyUsage.decipherOnly;
                        break;
                    case ENCIPHER_ONLY:
                        keyusage |= org.bouncycastle.asn1.x509.KeyUsage.encipherOnly;
                        break;
                    case DIGITAL_SIGNATURE:
                        keyusage |= org.bouncycastle.asn1.x509.KeyUsage.digitalSignature;
                        break;
                    case KEY_AGREEMENT:
                        keyusage |= org.bouncycastle.asn1.x509.KeyUsage.keyAgreement;
                        break;
                    case KEYCERT_SIGN:
                        keyusage |= org.bouncycastle.asn1.x509.KeyUsage.keyCertSign;
                        break;
                    case KEY_ENCIPHERMENT:
                        keyusage |= org.bouncycastle.asn1.x509.KeyUsage.keyEncipherment;
                        break;
                    }
                }

                this.keyusage = new org.bouncycastle.asn1.x509.KeyUsage(keyusage);
            }

            // ExtendedKeyUsage
            occurrence = extensionOccurences.get(Extension.extendedKeyUsage);
            if(occurrence != null && extensionsType.getExtendedKeyUsage() != null)
            {
                List<OidWithDescType> extKeyUsageTypeList = extensionsType.getExtendedKeyUsage().getUsage();
                Set<String> extendedKeyusageSet = new HashSet<String>();
                for(OidWithDescType t : extKeyUsageTypeList)
                {
                    extendedKeyusageSet.add(t.getValue());
                }

                this.extendedKeyusages = Collections.unmodifiableSet(extendedKeyusageSet);
            }

            // AuthorityKeyIdentifier
            occurrence = extensionOccurences.get(Extension.authorityKeyIdentifier);
            if(occurrence != null)
            {
                boolean includeIssuerAndSerial = true;

                org.xipki.ca.qa.certprofile.x509.jaxb.ExtensionsType.AuthorityKeyIdentifier akiType =
                        extensionsType.getAuthorityKeyIdentifier();
                if(akiType != null)
                {
                    Boolean B = akiType.isIncludeIssuerAndSerial();
                    if(B != null)
                    {
                        includeIssuerAndSerial = B.booleanValue();
                    }
                }

                this.akiOption = new AuthorityKeyIdentifierOption(includeIssuerAndSerial, occurrence);
            }
            else
            {
                this.akiOption = null;
            }

            // Certificate Policies
            occurrence = occurrences.get(Extension.certificatePolicies);
            if(occurrence != null && extensionsType.getCertificatePolicies() != null)
            {
                this.certificatePolicies = extensionsType.getCertificatePolicies();
            }

            // Policy Mappings
            occurrence = occurrences.get(Extension.policyMappings);
            if(occurrence != null && extensionsType.getPolicyMappings() != null)
            {
                this.policyMappings = extensionsType.getPolicyMappings();
            }

            // Name Constrains
            occurrence = occurrences.get(Extension.nameConstraints);
            if(occurrence != null && extensionsType.getNameConstraints() != null)
            {
                this.nameConstraints = extensionsType.getNameConstraints();
            }

            // Policy Constraints
            occurrence = occurrences.get(Extension.policyConstraints);
            if(occurrence != null && extensionsType.getPolicyConstraints() == null)
            {
                this.policyConstraints = extensionsType.getPolicyConstraints();
            }

            // Inhibit anyPolicy
            occurrence = occurrences.get(Extension.inhibitAnyPolicy);
            if(occurrence != null && extensionsType.getInhibitAnyPolicy() == null)
            {
                this.inhibitAnyPolicy = extensionsType.getInhibitAnyPolicy();
            }

            // admission
            occurrence = occurrences.get(ObjectIdentifiers.id_extension_admission);
            if(occurrence != null && extensionsType.getAdmission() == null)
            {
                this.admission = extensionsType.getAdmission();
            }

            // SubjectAltNameMode
            occurrence = occurrences.get(Extension.subjectAlternativeName);
            if(occurrence != null && extensionsType.getSubjectAltName() != null)
            {
                this.allowedSubjectAltNameModes = buildGeneralNameMode(extensionsType.getSubjectAltName());
            }

            // SubjectInfoAccess
            occurrence = occurrences.get(Extension.subjectInfoAccess);
            if(occurrence != null && extensionsType.getSubjectInfoAccess() != null)
            {
                List<Access> list = extensionsType.getSubjectInfoAccess().getAccess();
                this.allowedSubjectInfoAccessModes = new HashMap<>();
                for(Access entry : list)
                {
                    this.allowedSubjectInfoAccessModes.put(
                            entry.getAccessMethod().getValue(),
                            buildGeneralNameMode(entry.getAccessLocation()));
                }
            }

            // constant extensions
            ConstantExtensions cess = extensionsType.getConstantExtensions();
            if(cess != null && cess.getConstantExtension().isEmpty() == false)
            {
                Map<ASN1ObjectIdentifier, byte[]> map = new HashMap<>();
                for(ConstantExtensionType ce : cess.getConstantExtension())
                {
                        ASN1ObjectIdentifier type = new ASN1ObjectIdentifier(ce.getType().getValue());
                        occurrence = occurrences.get(type);
                        if(occurrence != null)
                        {
                            try
                            {
                                ASN1StreamParser parser = new ASN1StreamParser(ce.getValue());
                                parser.readObject();
                            } catch (IOException e)
                            {
                                throw new CertProfileException("Could not parse the constant extension value", e);
                            }
                            map.put(type, ce.getValue());
                        }
                }

                if(map.isEmpty() == false)
                {
                    this.constantExtensions = new HashMap<>(map.size());
                    this.constantExtensions = Collections.unmodifiableMap(map);
                }
            }
        }catch(RuntimeException e)
        {
            final String message = "RuntimeException";
            if(LOG.isErrorEnabled())
            {
                LOG.error(LogUtil.buildExceptionLogFormat(message), e.getClass().getName(), e.getMessage());
            }
            LOG.debug(message, e);
            throw new CertProfileException("RuntimeException thrown while initializing certprofile: " + e.getMessage());
        }
    }

    public List<ValidationIssue> checkCert(
            byte[] certBytes)
    {
        return checkCert(certBytes, null, null, null);
    }

    public List<ValidationIssue> checkCert(
            byte[] certBytes,
            X509CAInfo caInfo,
            X500Name requestedSubject,
            SubjectPublicKeyInfo requestedPublicKey)
    {
        List<ValidationIssue> result = new LinkedList<ValidationIssue>();

        Certificate bcCert;
        X509Certificate cert;

        // certificate encoding
        {
            ValidationIssue issue = new ValidationIssue("X509.ENCODING", "certificate encoding");
            result.add(issue);
            try
            {
                bcCert = Certificate.getInstance(certBytes);
                cert = SecurityUtil.parseCert(certBytes);
            } catch (CertificateException | IOException e)
            {
                issue.setFailureMessage("certificate is not corrected encoded");
                return result;
            }
        }

        // syntax version
        {
            ValidationIssue issue = new ValidationIssue("X509.VERSION", "certificate version");
            result.add(issue);
            int versionNumber = cert.getVersion();
            if(versionNumber != syntaxVersion)
            {
                issue.setFailureMessage("expected='" + syntaxVersion + "', is='" + versionNumber + "'");
            }
        }

        // signatureAlgorithm
        if(sigantureAlgorithms != null)
        {
            ValidationIssue issue = new ValidationIssue("X509.SIGALG", "signature algorithm");
            result.add(issue);

            AlgorithmIdentifier sigAlgId = bcCert.getSignatureAlgorithm();
            AlgorithmIdentifier tbsSigAlgId = bcCert.getTBSCertificate().getSignature();
            if(tbsSigAlgId.equals(sigAlgId) == false)
            {
                issue.setFailureMessage("Certificate.tbsCertificate.signature != Certificate.signatureAlgorithm");
            } else
            {
                String sigAlgo = sigAlgId.getAlgorithm().getId();
                if(sigantureAlgorithms.contains(sigAlgo) == false)
                {
                    issue.setFailureMessage("signatureAlgorithm '" + sigAlgo + "' is not allowed");
                }
            }
        }

        // notBefore
        if(notBeforeMidnight)
        {
            ValidationIssue issue = new ValidationIssue("X509.NOTBEFORE", "not before midnight");
            result.add(issue);
            Calendar c = Calendar.getInstance(UTC);
            c.setTime(cert.getNotBefore());
            int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            int second = c.get(Calendar.SECOND);

            if(hourOfDay != 0 || minute != 0 || second != 0)
            {
                issue.setFailureMessage(" '" + cert.getNotBefore() + "' is not midnight time (UTC)");
            }
        }

        // validity
        {
            ValidationIssue issue = new ValidationIssue("X509.VALIDITY", "cert validity");
            result.add(issue);

            Date expectedNotAfter = validity.add(cert.getNotBefore());
            if(Math.abs(expectedNotAfter.getTime() - cert.getNotAfter().getTime()) > 60 * SECOND)
            {
                issue.setFailureMessage("cert validity is not within " + validity.toString());
            }
        }

        // public key
        SubjectPublicKeyInfo publicKey = bcCert.getSubjectPublicKeyInfo();
        if(((nonEcKeyAlgorithms == null || nonEcKeyAlgorithms.isEmpty())
                && (allowedEcCurves == null || allowedEcCurves.isEmpty())) == false)
        {
            ValidationIssue issue = new ValidationIssue("X509.PUBKEY.SYN", "whether public key is permitted");
            result.add(issue);
            try
            {
                checkPublicKey(publicKey);
            }catch(BadCertTemplateException e)
            {
                issue.setFailureMessage(e.getMessage());
            }
        }

        if(requestedPublicKey != null)
        {
            ValidationIssue issue = new ValidationIssue("X509.PUBKEY.REQ", "whether public key matches the request one");
            result.add(issue);
            SubjectPublicKeyInfo c14nRequestedPublicKey = SecurityUtil.toRfc3279Style(requestedPublicKey);
            if(c14nRequestedPublicKey.equals(publicKey) == false)
            {
                issue.setFailureMessage("public in the certificate does not equal the requested one");
            }
        }

        // Signature
        if(caInfo != null)
        {
            ValidationIssue issue = new ValidationIssue("X509.SIG", "whether certificate is signed by CA");
            result.add(issue);
            try
            {
                cert.verify(caInfo.getCert().getPublicKey(), "BC");
            }catch(Exception e)
            {
                issue.setFailureMessage("invalid signature");
            }
        }

        // issuer
        if(caInfo != null)
        {
            ValidationIssue issue = new ValidationIssue("X509.ISSUER", "certificate issuer");
            result.add(issue);
            if(cert.getIssuerX500Principal().equals(caInfo.getCert().getSubjectX500Principal()) == false)
            {
                issue.setFailureMessage("issue in certificate does not equal the subject of CA certificate");
            }
        }

        // subject
        X500Name subject = bcCert.getTBSCertificate().getSubject();
        result.addAll(checkSubject(subject, requestedSubject));

        // extensions
        result.addAll(checkExtensions(bcCert, caInfo, requestedSubject));

        return result;
    }

    private List<ValidationIssue> checkExtensions(Certificate bcCert, X509CAInfo caInfo, X500Name requestedSubject)
    {
        List<ValidationIssue> result = new LinkedList<>();

        Extensions extensions = bcCert.getTBSCertificate().getExtensions();
        ASN1ObjectIdentifier[] oids = extensions.getExtensionOIDs();
        if(oids == null)
        {
            ValidationIssue issue = new ValidationIssue("X509.EXT.GEN", "extension general");
            result.add(issue);
            issue.setFailureMessage("no extension is present");
            return result;
        }

        for(ASN1ObjectIdentifier extType : extensionOccurences.keySet())
        {
            ExtensionOccurrence extOccurrence = extensionOccurences.get(extType);
            if(extOccurrence.isRequired() == false)
            {
                continue;
            }

            boolean present = false;
            for(ASN1ObjectIdentifier oid : oids)
            {
                if(oid.equals(extType))
                {
                    present = true;
                    break;
                }
            }

            if(present == false)
            {
                ValidationIssue issue = createExtensionIssue(extType);
                result.add(issue);
                issue.setFailureMessage("extension is absent but is required");
            }
        }

        for(ASN1ObjectIdentifier oid : oids)
        {
            ValidationIssue issue = createExtensionIssue(oid);
            result.add(issue);

            Extension ext = extensions.getExtension(oid);
            StringBuilder failureMsg = new StringBuilder();
            ExtensionOccurrence extOccurrence = extensionOccurences.get(oid);
            if(extOccurrence == null)
            {
                failureMsg.append("extension is present but is not permitted");
                failureMsg.append("; ");
            } else if(extOccurrence.isCritical() != ext.isCritical())
            {
                failureMsg.append("critical is '" + ext.isCritical() + "' but expected '" + extOccurrence.isCritical() + "'");
                failureMsg.append("; ");
            }

            byte[] extValue = ext.getExtnValue().getOctets();

            try
            {
                if(constantExtensions.containsKey(oid))
                {
                    byte[] expectedExtValue = constantExtensions.get(oid);
                    if(Arrays.equals(expectedExtValue, extValue) == false)
                    {
                        failureMsg.append("extension valus is '" + hex(extValue) +
                                "' but expected '" + hex(expectedExtValue) + "'");
                        failureMsg.append("; ");
                    }
                }
                else if(Extension.basicConstraints.equals(oid))
                {
                    //  basicConstrains
                    BasicConstraints bc =  BasicConstraints.getInstance(extValue);
                    if(ca != bc.isCA())
                    {
                        failureMsg.append("ca is '" + bc.isCA() + "' but expected '" + ca + "'");
                        failureMsg.append("; ");
                    }

                    if(bc.isCA())
                    {
                        BigInteger _pathLen = bc.getPathLenConstraint();
                        if(pathLen == null)
                        {
                            if(_pathLen != null)
                            {
                                failureMsg.append("pathLen is '" + _pathLen + "' but expected 'absent'");
                                failureMsg.append("; ");
                            }
                        }
                        else
                        {
                            if(_pathLen == null)
                            {
                                failureMsg.append("pathLen is 'null' but expected '" +  pathLen + "'");
                                failureMsg.append("; ");
                            }
                            else if(BigInteger.valueOf(pathLen).equals(pathLen)== false)
                            {
                                failureMsg.append("pathLen is '" + _pathLen + "' but expected '" +  pathLen + "'");
                                failureMsg.append("; ");
                            }
                        }
                    }
                } else if(Extension.subjectKeyIdentifier.equals(oid))
                {
                    // subjectKeyIdentifier
                    SubjectKeyIdentifier asn1 = SubjectKeyIdentifier.getInstance(extValue);
                    byte[] ski = asn1.getKeyIdentifier();
                    byte[] pkData = bcCert.getSubjectPublicKeyInfo().getPublicKeyData().getBytes();
                    byte[] expectedSki = HashCalculator.hash(HashAlgoType.SHA1, pkData);
                    if(Arrays.equals(expectedSki, ski) == false)
                    {
                        failureMsg.append("SKI is '" + hex(ski) + "' but expected is '" + hex(expectedSki) + "'");
                        failureMsg.append("; ");
                    }
                } else if(Extension.authorityKeyIdentifier.equals(oid))
                {
                    // authorityKeyIdentifier
                    AuthorityKeyIdentifier asn1 = AuthorityKeyIdentifier.getInstance(extValue);
                    byte[] keyIdentifier = asn1.getKeyIdentifier();
                    if(keyIdentifier == null)
                    {
                        failureMsg.append("keyIdentifier is 'absent' but expected 'present'");
                        failureMsg.append("; ");
                    }
                    else if(Arrays.equals(caInfo.getSubjectKeyIdentifier(), keyIdentifier) == false)
                    {
                        failureMsg.append("keyIdentifier is '" + hex(keyIdentifier) + "' but expected "
                                + "'" + hex(caInfo.getSubjectKeyIdentifier()) + "'");
                        failureMsg.append("; ");
                    }

                    BigInteger serialNumber = asn1.getAuthorityCertSerialNumber();
                    GeneralNames names = asn1.getAuthorityCertIssuer();
                    if(serialNumber != null)
                    {
                        if(names == null)
                        {
                            failureMsg.append("authorityCertIssuer is 'absent' but expected 'present'");
                            failureMsg.append("; ");
                        }
                        if(caInfo.getCert().getSerialNumber().equals(serialNumber) == false)
                        {
                            failureMsg.append("authorityCertSerialNumber is '" + serialNumber + "' but expected '" +
                                    caInfo.getCert().getSerialNumber() + "'");
                            failureMsg.append("; ");
                        }
                    }

                    if(names != null)
                    {
                        if(serialNumber == null)
                        {
                            failureMsg.append("authorityCertSerialNumber is 'absent' but expected 'present'");
                            failureMsg.append("; ");
                        }
                        GeneralName[] genNames = names.getNames();
                        X500Name x500GenName = null;
                        for(GeneralName genName : genNames)
                        {
                            if(genName.getTagNo() == GeneralName.directoryName)
                            {
                                if(x500GenName != null)
                                {
                                    failureMsg.append("authorityCertIssuer contains at least two directoryName "
                                            + "but expected one");
                                    failureMsg.append("; ");
                                    break;
                                }
                                else
                                {
                                    x500GenName = (X500Name) genName.getName();
                                }
                            }
                        }

                        if(x500GenName == null)
                        {
                            failureMsg.append("authorityCertIssuer does not contain directoryName but expected one");
                            failureMsg.append("; ");
                        }
                        else
                        {
                            X500Name caSubject = caInfo.getBcCert().getTBSCertificate().getSubject();
                            if(caSubject.equals(x500GenName) == false)
                            {
                                failureMsg.append("authorityCertIssuer is '" + x500GenName.toString()
                                        + "' but expected '" + caSubject.toString() + "'");
                                failureMsg.append("; ");
                            }
                        }
                    }
                } else if(Extension.keyUsage.equals(oid))
                {
                    if(keyusage != null)
                    {
                        org.bouncycastle.asn1.x509.KeyUsage asn1 = org.bouncycastle.asn1.x509.KeyUsage.getInstance(extValue);
                        int _keyusage = ((DERBitString) asn1.toASN1Primitive()).intValue();
                        if(keyusage.equals(_keyusage) == false)
                        {
                            failureMsg.append("is '" + asn1 + "' but expected '" + "'");
                            failureMsg.append("; ");
                        }
                    }
                } else if(Extension.certificatePolicies.equals(oid))
                {
                    if(certificatePolicies != null)
                    {
                        org.bouncycastle.asn1.x509.CertificatePolicies asn1 =
                                org.bouncycastle.asn1.x509.CertificatePolicies.getInstance(extValue);
                        PolicyInformation[] iPolicyInformations = asn1.getPolicyInformation();

                        for(PolicyInformation iPolicyInformation : iPolicyInformations)
                        {
                            ASN1ObjectIdentifier iPolicyId = iPolicyInformation.getPolicyIdentifier();
                            CertificatePolicyInformationType eCp = null;
                            for(CertificatePolicyInformationType cp : certificatePolicies.getCertificatePolicyInformation())
                            {
                                if(cp.getPolicyIdentifier().getValue().equals(iPolicyId.getId()))
                                {
                                    eCp = cp;
                                    break;
                                }
                            }

                            if(eCp == null)
                            {
                                failureMsg.append("certificate policy '" + iPolicyId + "' is not expected");
                            } else
                            {
                                PolicyQualifiers eCpPq = eCp.getPolicyQualifiers();
                                if(eCpPq != null)
                                {
                                    ASN1Sequence iPolicyQualifiers = iPolicyInformation.getPolicyQualifiers();
                                    List<String> iCpsUris = new LinkedList<>();
                                    List<String> iUserNotices = new LinkedList<>();

                                    int n = iPolicyQualifiers.size();
                                    for(int i = 0; i < n; i++)
                                    {
                                        PolicyQualifierInfo iPolicyQualifierInfo =
                                                (PolicyQualifierInfo) iPolicyQualifiers.getObjectAt(i);
                                        ASN1ObjectIdentifier iPolicyQualifierId = iPolicyQualifierInfo.getPolicyQualifierId();
                                        ASN1Encodable iQualifier = iPolicyQualifierInfo.getQualifier();
                                        if(PolicyQualifierId.id_qt_cps.equals(iPolicyQualifierId))
                                        {
                                            String iCpsUri = ((DERIA5String) iQualifier).getString();
                                            iCpsUris.add(iCpsUri);
                                        } else if (PolicyQualifierId.id_qt_unotice.equals(iPolicyQualifierId))
                                        {
                                            UserNotice iUserNotice = UserNotice.getInstance(iQualifier);
                                            if(iUserNotice.getExplicitText() != null)
                                            {
                                                iUserNotices.add(iUserNotice.getExplicitText().getString());
                                            }
                                        }
                                    }

                                    List<JAXBElement<String>> elements = eCpPq.getCpsUriOrUserNotice();
                                    for(JAXBElement<String> element : elements)
                                    {
                                        String localPart = element.getName().getLocalPart();
                                        String eleValue = element.getValue();

                                        if("cpsUri".equals(localPart))
                                        {
                                            if(iCpsUris.contains(eleValue) == false)
                                            {
                                                failureMsg.append("CPSuri '" + eleValue + "' is absent but is required");
                                            }
                                        }else if("userNotice".equals(localPart))
                                        {
                                            if(iUserNotices.contains(eleValue) == false)
                                            {
                                                failureMsg.append("userNotice '" + eleValue + "' is absent but is required");
                                            }
                                        }else
                                        {
                                            throw new RuntimeException("should not reach here");
                                        }
                                    }
                                }
                            }
                        }

                        for(CertificatePolicyInformationType cp : certificatePolicies.getCertificatePolicyInformation())
                        {
                            boolean present = false;
                            for(PolicyInformation iPolicyInformation : iPolicyInformations)
                            {
                                if(iPolicyInformation.getPolicyIdentifier().getId().equals(cp.getPolicyIdentifier().getValue()))
                                {
                                    present = true;
                                    break;
                                }
                            }

                            if(present == false)
                            {
                                failureMsg.append("certificate policy '" + cp.getPolicyIdentifier().getValue() + "' is "
                                        + "absent but is required");
                                failureMsg.append("; ");
                            }
                        }
                    }
                } else if(Extension.policyMappings.equals(oid))
                {
                    if(policyMappings != null)
                    {
                        ASN1Sequence iPolicyMappings = DERSequence.getInstance(extValue);
                        Map<String, String> iMap = new HashMap<>();
                        int size = iPolicyMappings.size();
                        for(int i = 0; i < size; i++)
                        {
                            ASN1Sequence seq = (ASN1Sequence) iPolicyMappings.getObjectAt(i);

                            CertPolicyId issuerDomainPolicy = CertPolicyId.getInstance(seq.getObjectAt(0));
                            CertPolicyId subjectDomainPolicy = CertPolicyId.getInstance(seq.getObjectAt(1));
                            iMap.put(issuerDomainPolicy.getId(), subjectDomainPolicy.getId());
                        }

                        for(PolicyIdMappingType type : policyMappings.getMapping())
                        {
                            String eIssuerDomainPolicy = type.getIssuerDomainPolicy().getValue();
                            String eSubjectDomainPolicy = type.getSubjectDomainPolicy().getValue();

                            String iSubjectDomainPolicy = iMap.get(eIssuerDomainPolicy);
                            if(iSubjectDomainPolicy == null)
                            {
                                failureMsg.append("issuerDomainPolicy '" + eIssuerDomainPolicy + "' is absent but is required");
                                failureMsg.append("; ");
                            } else if(iSubjectDomainPolicy.equals(eSubjectDomainPolicy) == false)
                            {
                                failureMsg.append("subjectDomainPolicy for issuerDomainPolicy is '" + iSubjectDomainPolicy +
                                        "' but expected '" + eSubjectDomainPolicy + "'");
                                failureMsg.append("; ");
                            }
                        }
                    }

                } else if(Extension.nameConstraints.equals(oid))
                {
                    // TODO
                } else if(Extension.policyConstraints.equals(oid))
                {
                    // TODO
                } else if(Extension.inhibitAnyPolicy.equals(oid))
                {
                    // TODO
                } else if(Extension.subjectAlternativeName.equals(oid))
                {
                    // TODO
                } else if(Extension.issuerAlternativeName.equals(oid))
                {
                    Extension caSubjectAltExtension = caInfo.getBcCert().getTBSCertificate().getExtensions().getExtension(
                            Extension.subjectAlternativeName);
                    if(caSubjectAltExtension == null)
                    {
                        failureMsg.append("issuerAlternativeName is present but expected 'none'");
                        failureMsg.append("; ");
                    }
                    else
                    {
                        byte[] caSubjectAltExtensionValue = caSubjectAltExtension.getExtnValue().getOctets();
                        if(Arrays.equals(caSubjectAltExtensionValue, extValue) == false)
                        {
                            failureMsg.append("is '" + hex(extValue) + "' but expected '" +
                                    hex(caSubjectAltExtensionValue) + "'");
                            failureMsg.append("; ");
                        }
                    }
                } else if(Extension.authorityInfoAccess.equals(oid))
                {
                    // TODO
                } else if(Extension.cRLDistributionPoints.equals(oid))
                {
                    // TODO
                } else if(Extension.deltaCRLIndicator.equals(oid))
                {
                    // TODO
                } else if(ObjectIdentifiers.id_extension_admission.equals(oid))
                {
                    // TODO
                } else
                {
                    // TODO
                }
            }catch(IllegalArgumentException | ClassCastException | ArrayIndexOutOfBoundsException e)
            {
                issue.setFailureMessage("extension value does not have correct syntax");
            }

            if(failureMsg.length() > 0)
            {
                issue.setFailureMessage(failureMsg.toString());
            }
        }

        return result;
    }

    private static ProfileType parse(String xmlConf)
    throws CertProfileException
    {
        synchronized (jaxbUnmarshallerLock)
        {
            JAXBElement<?> rootElement;
            try
            {
                if(jaxbUnmarshaller == null)
                {
                    JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
                    jaxbUnmarshaller = context.createUnmarshaller();

                    final SchemaFactory schemaFact = SchemaFactory.newInstance(
                            javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
                    URL url = QAX509CertProfile.class.getResource("/xsd/certprofile.xsd");
                    jaxbUnmarshaller.setSchema(schemaFact.newSchema(url));
                }

                rootElement = (JAXBElement<?>) jaxbUnmarshaller.unmarshal(
                        new ByteArrayInputStream(xmlConf.getBytes()));
            }
            catch(JAXBException | SAXException e)
            {
                throw new CertProfileException("parse profile failed, message: " + e.getMessage(), e);
            }

            Object rootType = rootElement.getValue();
            if(rootType instanceof ProfileType)
            {
                return (ProfileType) rootElement.getValue();
            }
            else
            {
                throw new CertProfileException("invalid root element type");
            }
        }
    }

    private void checkPublicKey(SubjectPublicKeyInfo publicKey)
    throws BadCertTemplateException
    {
        if((nonEcKeyAlgorithms == null || nonEcKeyAlgorithms.isEmpty())
                && (allowedEcCurves == null || allowedEcCurves.isEmpty()))
        {
            return;
        }

        ASN1ObjectIdentifier keyType = publicKey.getAlgorithm().getAlgorithm();
        if(X9ObjectIdentifiers.id_ecPublicKey.equals(keyType))
        {
            ASN1ObjectIdentifier curveOid;
            try
            {
                ASN1Encodable algParam = publicKey.getAlgorithm().getParameters();
                curveOid = ASN1ObjectIdentifier.getInstance(algParam);
            } catch(IllegalArgumentException e)
            {
                throw new BadCertTemplateException("Only named EC public key is supported");
            }

            if(allowedEcCurves != null && allowedEcCurves.isEmpty() == false)
            {
                if(allowedEcCurves.containsKey(curveOid) == false)
                {
                    throw new BadCertTemplateException("EC curve " + SecurityUtil.getCurveName(curveOid) +
                            " (OID: " + curveOid.getId() + ") is not allowed");
                }
            }

            byte[] keyData = publicKey.getPublicKeyData().getBytes();

            Set<Byte> allowedEncodings = allowedEcCurves.get(curveOid);
            if(allowedEncodings != null && allowedEncodings.isEmpty() == false)
            {
                if(allowedEncodings.contains(keyData[0]) == false)
                {
                    throw new BadCertTemplateException("Unaccepted EC point encoding " + keyData[0]);
                }
            }

            try
            {
                checkECSubjectPublicKeyInfo(curveOid, publicKey.getPublicKeyData().getBytes());
            }catch(BadCertTemplateException e)
            {
                throw e;
            }catch(Exception e)
            {
                LOG.debug("populateFromPubKeyInfo", e);
                throw new BadCertTemplateException("Invalid public key: " + e.getMessage());
            }

            return;
        }
        else
        {
            if(nonEcKeyAlgorithms == null || allowedEcCurves.isEmpty())
            {
                return;
            }

            if(nonEcKeyAlgorithms.containsKey(keyType))
            {
                List<KeyParamRanges> list = nonEcKeyAlgorithms.get(keyType);
                if(list.isEmpty())
                {
                    return;
                }

                if(PKCSObjectIdentifiers.rsaEncryption.equals(keyType))
                {
                    ASN1Sequence seq = ASN1Sequence.getInstance(publicKey.getPublicKeyData().getBytes());
                    ASN1Integer modulus = ASN1Integer.getInstance(seq.getObjectAt(0));
                    int modulusLength = modulus.getPositiveValue().bitLength();
                    for(KeyParamRanges ranges : list)
                    {
                        if(satisfy(modulusLength, MODULUS_LENGTH, ranges))
                        {
                            return;
                        }
                    }
                }
                else if(X9ObjectIdentifiers.id_dsa.equals(keyType))
                {
                    ASN1Encodable params = publicKey.getAlgorithm().getParameters();
                    if(params == null)
                    {
                        throw new BadCertTemplateException("null Dss-Parms is not permitted");
                    }

                    int pLength;
                    int qLength;

                    try
                    {
                        ASN1Sequence seq = ASN1Sequence.getInstance(params);
                        ASN1Integer p = ASN1Integer.getInstance(seq.getObjectAt(0));
                        ASN1Integer q = ASN1Integer.getInstance(seq.getObjectAt(1));
                        pLength = p.getPositiveValue().bitLength();
                        qLength = q.getPositiveValue().bitLength();
                    } catch(IllegalArgumentException | ArrayIndexOutOfBoundsException e)
                    {
                        throw new BadCertTemplateException("illegal Dss-Parms");
                    }

                    for(KeyParamRanges ranges : list)
                    {
                        boolean match = satisfy(pLength, P_LENGTH, ranges);
                        if(match)
                        {
                            match = satisfy(qLength, Q_LENGTH, ranges);
                        }

                        if(match)
                        {
                            return;
                        }
                    }
                }
                else
                {
                    throw new BadCertTemplateException("Unknown key type " + keyType.getId());
                }
            }
        }

        throw new BadCertTemplateException("the given publicKey is not permitted");
    }

    private static void checkECSubjectPublicKeyInfo(ASN1ObjectIdentifier curveOid, byte[] encoded)
    throws BadCertTemplateException
    {
        Integer expectedLength = ecCurveFieldSizes.get(curveOid);
        if(expectedLength == null)
        {
            X9ECParameters ecP = ECUtil.getNamedCurveByOid(curveOid);
            ECCurve curve = ecP.getCurve();
            expectedLength = (curve.getFieldSize() + 7) / 8;
            ecCurveFieldSizes.put(curveOid, expectedLength);
        }

        switch (encoded[0])
        {
            case 0x02: // compressed
            case 0x03: // compressed
            {
                if (encoded.length != (expectedLength + 1))
                {
                    throw new BadCertTemplateException("Incorrect length for compressed encoding");
                }
                break;
            }
            case 0x04: // uncompressed
            case 0x06: // hybrid
            case 0x07: // hybrid
            {
                if (encoded.length != (2 * expectedLength + 1))
                {
                    throw new BadCertTemplateException("Incorrect length for uncompressed/hybrid encoding");
                }
                break;
            }
            default:
                throw new BadCertTemplateException("Invalid point encoding 0x" + Integer.toString(encoded[0], 16));
        }
    }

    private static boolean satisfy(int len, String paramName, KeyParamRanges ranges)
    {
        List<KeyParamRange> rangeList = ranges.getRanges(paramName);
        if(rangeList == null || rangeList.isEmpty())
        {
            return true;
        }

        for(KeyParamRange range : rangeList)
        {
            if(range.match(len))
            {
                return true;
            }
        }

        return false;
    }

    private List<ValidationIssue> checkSubject(X500Name subject, X500Name requestedSubject)
    {
        List<ValidationIssue> result = new LinkedList<>();

        ASN1ObjectIdentifier[] oids = subject.getAttributeTypes();

        for(ASN1ObjectIdentifier type : subjectDNOccurrences.keySet())
        {
            RDNOccurrence rdnOccurrence = subjectDNOccurrences.get(type);
            if(rdnOccurrence.getMinOccurs() == 0)
            {
                continue;
            }

            boolean present = false;
            for(ASN1ObjectIdentifier oid : oids)
            {
                if(oid.equals(type))
                {
                    present = true;
                    break;
                }
            }

            if(present == false)
            {
                ValidationIssue issue = createSubjectIssue(type);
                result.add(issue);
                issue.setFailureMessage("attribute is 'absent' but expected 'present'");
            }
        }

        for(ASN1ObjectIdentifier oid : oids)
        {
            ValidationIssue issue = createSubjectIssue(oid);
            result.add(issue);

            RDN[] rdns = subject.getRDNs(oid);

            StringBuilder failureMsg = new StringBuilder();
            RDNOccurrence occurrence = subjectDNOccurrences.get(oid);
            if(occurrence == null)
            {
                issue.setFailureMessage("extension is 'present' but expected 'absent'");
                continue;
            }

            SubjectDNOption option = subjectDNOptions.get(oid);
            if(option == null)
            {
                // if not specified, SN and C should be encoded as PrintableString, otherwise UTF8String
            } else
            {

            }

        }

        // check whether subject matches requestedSubject

        // requestedSubject

        // FIXME
        return null;
    }

    public boolean includeIssuerAndSerialInAKI()
    {
        return akiOption == null ? false : akiOption.isIncludeIssuerAndSerial();
    }

    private static List<CertificatePolicyInformation> buildCertificatePolicies(ExtensionsType.CertificatePolicies type)
    {
        List<CertificatePolicyInformationType> policyPairs = type.getCertificatePolicyInformation();
        if(policyPairs == null || policyPairs.isEmpty())
        {
            return null;
        }

        List<CertificatePolicyInformation> policies = new ArrayList<CertificatePolicyInformation>(policyPairs.size());
        for(CertificatePolicyInformationType policyPair : policyPairs)
        {
            List<CertificatePolicyQualifier> qualifiers = null;

            PolicyQualifiers policyQualifiers = policyPair.getPolicyQualifiers();
            if(policyQualifiers != null)
            {
                List<JAXBElement<String>> cpsUriOrUserNotice = policyQualifiers.getCpsUriOrUserNotice();

                qualifiers = new ArrayList<CertificatePolicyQualifier>(cpsUriOrUserNotice.size());
                for(JAXBElement<String> element : cpsUriOrUserNotice)
                {
                    String elementValue = element.getValue();
                    CertificatePolicyQualifier qualifier = null;
                    String elementName = element.getName().getLocalPart();
                    if("cpsUri".equals(elementName))
                    {
                        qualifier = CertificatePolicyQualifier.getInstanceForCpsUri(elementValue);
                    }
                    else
                    {
                        qualifier = CertificatePolicyQualifier.getInstanceForUserNotice(elementValue);
                    }
                    qualifiers.add(qualifier);
                }
            }

            CertificatePolicyInformation cpi = new CertificatePolicyInformation(
                    policyPair.getPolicyIdentifier().getValue(), qualifiers);

            policies.add(cpi);
        }

        return policies;
    }

    private static org.bouncycastle.asn1.x509.PolicyMappings buildPolicyMappings(
            org.xipki.ca.qa.certprofile.x509.jaxb.ExtensionsType.PolicyMappings type)
    {
        List<PolicyIdMappingType> mappings = type.getMapping();
        final int n = mappings.size();

        CertPolicyId[] issuerDomainPolicy = new CertPolicyId[n];
        CertPolicyId[] subjectDomainPolicy = new CertPolicyId[n];

        for(int i = 0; i < n; i++)
        {
            PolicyIdMappingType mapping = mappings.get(i);
            ASN1ObjectIdentifier oid = new ASN1ObjectIdentifier(mapping.getIssuerDomainPolicy().getValue());
            issuerDomainPolicy[i] = CertPolicyId.getInstance(oid);

            oid = new ASN1ObjectIdentifier(mapping.getSubjectDomainPolicy().getValue());
            subjectDomainPolicy[i] = CertPolicyId.getInstance(oid);
        }

        return new org.bouncycastle.asn1.x509.PolicyMappings(issuerDomainPolicy, subjectDomainPolicy);
    }

    private static org.bouncycastle.asn1.x509.NameConstraints buildNameConstrains(
            org.xipki.ca.qa.certprofile.x509.jaxb.ExtensionsType.NameConstraints type)
    throws CertProfileException
    {
        GeneralSubtree[] permitted = buildGeneralSubtrees(type.getPermittedSubtrees());
        GeneralSubtree[] excluded = buildGeneralSubtrees(type.getExcludedSubtrees());
        if(permitted == null && excluded == null)
        {
            return null;
        }
        return new org.bouncycastle.asn1.x509.NameConstraints(permitted, excluded);
    }

    private static GeneralSubtree[] buildGeneralSubtrees(GeneralSubtreesType subtrees)
    throws CertProfileException
    {
        if(subtrees == null || subtrees.getBase().isEmpty())
        {
            return null;
        }

        List<GeneralSubtreeBaseType> list = subtrees.getBase();
        final int n = list.size();
        GeneralSubtree[] ret = new GeneralSubtree[n];
        for(int i = 0; i < n; i++)
        {
            ret[i] = buildGeneralSubtree(list.get(i));
        }

        return ret;
    }

    private static GeneralSubtree buildGeneralSubtree(GeneralSubtreeBaseType type)
    throws CertProfileException
    {
        GeneralName base = null;
        if(type.getDirectoryName() != null)
        {
            base = new GeneralName(SecurityUtil.reverse(
                    new X500Name(type.getDirectoryName())));
        }
        else if(type.getDNSName() != null)
        {
            base = new GeneralName(GeneralName.dNSName, type.getDNSName());
        }
        else if(type.getIpAddress() != null)
        {
            base = new GeneralName(GeneralName.iPAddress, type.getIpAddress());
        }
        else if(type.getRfc822Name() != null)
        {
            base = new GeneralName(GeneralName.rfc822Name, type.getRfc822Name());
        }
        else if(type.getUri() != null)
        {
            base = new GeneralName(GeneralName.uniformResourceIdentifier, type.getUri());
        }
        else
        {
            throw new RuntimeException("should not reach here");
        }

        Integer i = type.getMinimum();
        if(i != null && i < 0)
        {
            throw new CertProfileException("negative minimum is not allowed: " + i);
        }

        BigInteger minimum = (i == null) ? null : BigInteger.valueOf(i.intValue());

        i = type.getMaximum();
        if(i != null && i < 0)
        {
            throw new CertProfileException("negative maximum is not allowed: " + i);
        }

        BigInteger maximum = (i == null) ? null : BigInteger.valueOf(i.intValue());

        return new GeneralSubtree(base, minimum, maximum);
    }

    private static int getInt(Integer i, int dfltValue)
    {
        return i == null ? dfltValue : i.intValue();
    }

    private static GeneralName createGeneralName(String value, Set<GeneralNameMode> modes)
    throws BadCertTemplateException
    {
        int idxTagSep = value.indexOf(GENERALNAME_SEP);
        if(idxTagSep == -1 || idxTagSep == 0 || idxTagSep == value.length() - 1)
        {
            throw new BadCertTemplateException("invalid generalName " + value);
        }
        String s = value.substring(0, idxTagSep);

        int tag;
        try
        {
            tag = Integer.parseInt(s);
        }catch(NumberFormatException e)
        {
            throw new BadCertTemplateException("invalid generalName tag " + s);
        }

        GeneralNameMode mode = null;

        for(GeneralNameMode m : modes)
        {
            if(m.getTag().getTag() == tag)
            {
                mode = m;
                break;
            }
        }

        if(mode == null)
        {
            throw new BadCertTemplateException("generalName tag " + tag + " is not allowed");
        }

        String name = value.substring(idxTagSep + 1);

        switch(mode.getTag())
        {
            case otherName:
            {
                int idxSep = name.indexOf(GENERALNAME_SEP);
                if(idxSep == -1 || idxSep == 0 || idxSep == name.length() - 1)
                {
                    throw new BadCertTemplateException("invalid otherName " + name);
                }
                String otherTypeOid = name.substring(0, idxSep);
                ASN1ObjectIdentifier type = new ASN1ObjectIdentifier(otherTypeOid);
                if(mode.getAllowedTypes().contains(type) == false)
                {
                    throw new BadCertTemplateException("otherName.type " + otherTypeOid + " is not allowed");
                }
                String otherValue = name.substring(idxSep + 1);

                ASN1EncodableVector vector = new ASN1EncodableVector();
                vector.add(type);
                vector.add(new DERTaggedObject(true, 0, new DERUTF8String(otherValue)));
                DERSequence seq = new DERSequence(vector);

                return new GeneralName(GeneralName.otherName, seq);
            }
            case rfc822Name:
                return new GeneralName(tag, name);
            case dNSName:
                return new GeneralName(tag, name);
            case directoryName:
            {
                X500Name x500Name = SecurityUtil.reverse(new X500Name(name));
                return new GeneralName(GeneralName.directoryName, x500Name);
            }
            case ediPartyName:
            {
                int idxSep = name.indexOf(GENERALNAME_SEP);
                if(idxSep == -1 || idxSep == name.length() - 1)
                {
                    throw new BadCertTemplateException("invalid ediPartyName " + name);
                }
                String nameAssigner = idxSep == 0 ? null : name.substring(0, idxSep);
                String partyName = name.substring(idxSep + 1);
                ASN1EncodableVector vector = new ASN1EncodableVector();
                if(nameAssigner != null)
                {
                    vector.add(new DERTaggedObject(false, 0, new DirectoryString(nameAssigner)));
                }
                vector.add(new DERTaggedObject(false, 1, new DirectoryString(partyName)));
                ASN1Sequence seq = new DERSequence(vector);
                return new GeneralName(GeneralName.ediPartyName, seq);
            }
            case uniformResourceIdentifier:
                return new GeneralName(tag, name);
            case iPAddress:
                return new GeneralName(tag, name);
            case registeredID:
                return new GeneralName(tag, name);
            default:
                throw new RuntimeException("should not reach here");
        }
    }

    private static Set<GeneralNameMode> buildGeneralNameMode(GeneralNameType name)
    {
        Set<GeneralNameMode> ret = new HashSet<>();
        if(name.getOtherName() != null)
        {
            List<OidWithDescType> list = name.getOtherName().getType();
            Set<ASN1ObjectIdentifier> set = new HashSet<>();
            for(OidWithDescType entry : list)
            {
                set.add(new ASN1ObjectIdentifier(entry.getValue()));
            }
            ret.add(new GeneralNameMode(GeneralNameTag.otherName, set));
        }

        if(name.getRfc822Name() != null)
        {
            ret.add(new GeneralNameMode(GeneralNameTag.rfc822Name));
        }

        if(name.getDNSName() != null)
        {
            ret.add(new GeneralNameMode(GeneralNameTag.dNSName));
        }

        if(name.getDirectoryName() != null)
        {
            ret.add(new GeneralNameMode(GeneralNameTag.directoryName));
        }

        if(name.getEdiPartyName() != null)
        {
            ret.add(new GeneralNameMode(GeneralNameTag.ediPartyName));
        }

        if(name.getUniformResourceIdentifier() != null)
        {
            ret.add(new GeneralNameMode(GeneralNameTag.uniformResourceIdentifier));
        }

        if(name.getIPAddress() != null)
        {
            ret.add(new GeneralNameMode(GeneralNameTag.iPAddress));
        }

        if(name.getRegisteredID() != null)
        {
            ret.add(new GeneralNameMode(GeneralNameTag.registeredID));
        }

        return ret;
    }

    private ValidationIssue createSubjectIssue(ASN1ObjectIdentifier subjectAttrType)
    {
        ValidationIssue issue;
        String attrName = ObjectIdentifiers.getName(subjectAttrType);
        if(attrName == null)
        {
            attrName = subjectAttrType.getId().replace('.', '_');
            issue = new ValidationIssue("X509.SUBJECT." + attrName, "attribute " + subjectAttrType.getId());
        }
        else
        {
            issue = new ValidationIssue("X509.SUBJECT." + attrName, "extension " + attrName +
                    " (" + subjectAttrType.getId() + ")");
        }
        return issue;
    }

    private ValidationIssue createExtensionIssue(ASN1ObjectIdentifier extId)
    {
        ValidationIssue issue;
        String extName = extOidNameMap.get(extId);
        if(extName == null)
        {
            extName = extId.getId().replace('.', '_');
            issue = new ValidationIssue("X509.EXT." + extName, "extension " + extId.getId());
        }
        else
        {
            issue = new ValidationIssue("X509.EXT." + extName, "extension " + extName + " (" + extId.getId() + ")");
        }
        return issue;
    }

    private static String hex(byte[] bytes)
    {
        return Hex.toHexString(bytes);
    }

}