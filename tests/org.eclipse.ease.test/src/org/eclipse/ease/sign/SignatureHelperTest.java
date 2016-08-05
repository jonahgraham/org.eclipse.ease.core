/*******************************************************************************
 * Copyright (c) 2016 Varun Raval and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Varun Raval - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.sign;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import org.junit.Test;

public class SignatureHelperTest {

	private final String fCertificatesWithoutFormat = "MIIDMjCCAvCgAwIBAgIEVr2DrTALBgcqhkjOOAQDBQAwazEL" + "MAkGA1UEBhMCSU4xEDAOBgNVBAgTB1Vua25vd24xEDAOBgNV"
			+ "BAcTB1Vua25vd24xEDAOBgNVBAoTB1Vua25vd24xEDAOBgNV" + "BAsTB1Vua25vd24xFDASBgNVBAMTC1ZhcnVuIFJhdmFsMB4X"
			+ "DTE2MDczMTAyMjYwMVoXDTE2MTAyOTAyMjYwMVowazELMAkG" + "A1UEBhMCSU4xEDAOBgNVBAgTB1Vua25vd24xEDAOBgNVBAcT"
			+ "B1Vua25vd24xEDAOBgNVBAoTB1Vua25vd24xEDAOBgNVBAsT" + "B1Vua25vd24xFDASBgNVBAMTC1ZhcnVuIFJhdmFsMIIBtzCC"
			+ "ASwGByqGSM44BAEwggEfAoGBAP1/U4EddRIpUt9KnC7s5Of2" + "EbdSPO9EAMMeP4C2USZpRV1AIlH7WT2NWPq/xfW6MPbLm1Vs"
			+ "14E7gB00b/JmYLdrmVClpJ+f6AR7ECLCT7up1/63xhv4O1fn" + "xqimFQ8E+4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3R"
			+ "SAHHAhUAl2BQjxUjC8yykrmCouuEC/BYHPUCgYEA9+Gghdab" + "Pd7LvKtcNrhXuXmUr7v6OuqC+VdMCz0HgmdRWVeOutRZT+Zx"
			+ "BxCBgLRJFnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuz" + "pnWRbqN/C/ohNWLx+2J6ASQ7zKTxvqhRkImog9/hWuWfBpKL"
			+ "Zl6Ae1UlZAFMO/7PSSoDgYQAAoGAAv2q73WXSvfTzOMflRsk" + "oq9bdxOf6c0uz5YPHed+NCjztdqWPmNSDcPO2/52a3IXs5Pl"
			+ "NGmZK57mB4bdcNIt9ANm0LRpn7p9DmzKaklKYNb9N3smWmXS" + "/0TTCsZ1OVit8tPv29V158YU4R2c+cVNo1WUsG/zE8HdoBh4"
			+ "YwH2VvijITAfMB0GA1UdDgQWBBSzGq2YIzDUhHXCkxTuxCAN" + "iiecDTALBgcqhkjOOAQDBQADLwAwLAIUVgVBlEjgXkxgPrir"
			+ "SjQM6//0jawCFEVQg9mQCOjkDElPrwd/Z3Ch2+JQ";
	// append : before using for signatureHelper getSignatureInFormat

	@Test
	public void certificateTest() throws ScriptSignatureException, CertificateException {

		CertificateFactory certificateFactory;
		certificateFactory = CertificateFactory.getInstance("X.509");
		final Certificate certificate = certificateFactory
				.generateCertificate(new ByteArrayInputStream(SignatureHelper.convertBase64ToBytes(fCertificatesWithoutFormat)));
		assertTrue(SignatureHelper.isSelfSignedCertificate(certificate));
	}
}
