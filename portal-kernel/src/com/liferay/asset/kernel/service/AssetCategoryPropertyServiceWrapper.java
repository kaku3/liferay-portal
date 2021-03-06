/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.asset.kernel.service;

import aQute.bnd.annotation.ProviderType;

import com.liferay.portal.kernel.service.ServiceWrapper;

/**
 * Provides a wrapper for {@link AssetCategoryPropertyService}.
 *
 * @author Brian Wing Shun Chan
 * @see AssetCategoryPropertyService
 * @deprecated As of Judson, replaced by {@link
com.liferay.asset.category.property.service.impl.AssetCategoryPropertyServiceImpl}
 * @generated
 */
@Deprecated
@ProviderType
public class AssetCategoryPropertyServiceWrapper
	implements AssetCategoryPropertyService,
		ServiceWrapper<AssetCategoryPropertyService> {
	public AssetCategoryPropertyServiceWrapper(
		AssetCategoryPropertyService assetCategoryPropertyService) {
		_assetCategoryPropertyService = assetCategoryPropertyService;
	}

	@Override
	public com.liferay.asset.kernel.model.AssetCategoryProperty addCategoryProperty(
		long entryId, String key, String value)
		throws com.liferay.portal.kernel.exception.PortalException {
		return _assetCategoryPropertyService.addCategoryProperty(entryId, key,
			value);
	}

	@Override
	public void deleteCategoryProperty(long categoryPropertyId)
		throws com.liferay.portal.kernel.exception.PortalException {
		_assetCategoryPropertyService.deleteCategoryProperty(categoryPropertyId);
	}

	@Override
	public java.util.List<com.liferay.asset.kernel.model.AssetCategoryProperty> getCategoryProperties(
		long entryId) {
		return _assetCategoryPropertyService.getCategoryProperties(entryId);
	}

	@Override
	public java.util.List<com.liferay.asset.kernel.model.AssetCategoryProperty> getCategoryPropertyValues(
		long companyId, String key) {
		return _assetCategoryPropertyService.getCategoryPropertyValues(companyId,
			key);
	}

	/**
	* Returns the OSGi service identifier.
	*
	* @return the OSGi service identifier
	*/
	@Override
	public String getOSGiServiceIdentifier() {
		return _assetCategoryPropertyService.getOSGiServiceIdentifier();
	}

	@Override
	public com.liferay.asset.kernel.model.AssetCategoryProperty updateCategoryProperty(
		long userId, long categoryPropertyId, String key, String value)
		throws com.liferay.portal.kernel.exception.PortalException {
		return _assetCategoryPropertyService.updateCategoryProperty(userId,
			categoryPropertyId, key, value);
	}

	@Override
	public com.liferay.asset.kernel.model.AssetCategoryProperty updateCategoryProperty(
		long categoryPropertyId, String key, String value)
		throws com.liferay.portal.kernel.exception.PortalException {
		return _assetCategoryPropertyService.updateCategoryProperty(categoryPropertyId,
			key, value);
	}

	@Override
	public AssetCategoryPropertyService getWrappedService() {
		return _assetCategoryPropertyService;
	}

	@Override
	public void setWrappedService(
		AssetCategoryPropertyService assetCategoryPropertyService) {
		_assetCategoryPropertyService = assetCategoryPropertyService;
	}

	private AssetCategoryPropertyService _assetCategoryPropertyService;
}