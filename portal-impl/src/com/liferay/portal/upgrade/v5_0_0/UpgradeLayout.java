/**
 * Copyright (c) 2000-2009 Liferay, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.liferay.portal.upgrade.v5_0_0;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.upgrade.UpgradeException;
import com.liferay.portal.upgrade.UpgradeProcess;

/**
 * <a href="UpgradeLayout.java.html"><b><i>View Source</i></b></a>
 *
 * @author Samuel Kong
 *
 */
public class UpgradeLayout extends UpgradeProcess {

	public void upgrade() throws UpgradeException {
		_log.info("Upgrading");

		try {
			doUpgrade();
		}
		catch (Exception e) {
			throw new UpgradeException(e);
		}
	}

	protected void doUpgrade() throws Exception {
		String languageId = LocaleUtil.toLanguageId(LocaleUtil.getDefault());

		StringBuilder sb = new StringBuilder();

		sb.append("update Layout set typeSettings = replace(replace(replace(");
		sb.append("typeSettings, 'meta-description=', 'meta-description_");
		sb.append(languageId);
		sb.append("='), 'meta-keywords=', 'meta-keywords_");
		sb.append(languageId);
		sb.append("='), 'meta-robots=', 'meta-robots_");
		sb.append(languageId);
		sb.append("=') where typeSettings like '%meta-description=%'");

		runSQL(sb.toString());
	}

	private static Log _log = LogFactoryUtil.getLog(UpgradeLayout.class);

}