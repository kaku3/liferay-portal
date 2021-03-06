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

package com.liferay.category.apio.internal.architect.router;

import com.liferay.apio.architect.router.NestedCollectionRouter;
import com.liferay.apio.architect.routes.NestedCollectionRoutes;
import com.liferay.apio.architect.routes.NestedCollectionRoutes.Builder;
import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.category.apio.architect.identifier.CategoryIdentifier;
import com.liferay.category.apio.internal.architect.form.NestedCategoryForm;
import com.liferay.category.apio.internal.architect.router.base.BaseCategoryNestedCollectionRouter;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleService;
import com.liferay.portal.apio.permission.HasPermission;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.web.page.element.apio.architect.identifier.WebPageElementIdentifier;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Provides the information necessary to expose the {@code Category} resources
 * contained inside a <a href="http://schema.org/WebPageElement">WebPageElement
 * </a> through a web API. The resources are mapped from the internal model
 * {@link AssetCategory} and {@code JournalArticle}.
 *
 * @author Eduardo Perez
 * @review
 */
@Component(immediate = true)
public class WebPageElementCategoryNestedCollectionRouter extends
	BaseCategoryNestedCollectionRouter<WebPageElementIdentifier>
	implements NestedCollectionRouter
		<AssetCategory, Long, CategoryIdentifier, Long,
			WebPageElementIdentifier> {

	@Override
	public NestedCollectionRoutes<AssetCategory, Long, Long> collectionRoutes(
		Builder<AssetCategory, Long, Long> builder) {

		return builder.addGetter(
			this::getPageItems
		).addCreator(
			this::addAssetCategory,
			_hasPermission.forAddingIn(WebPageElementIdentifier.class),
			NestedCategoryForm::buildForm
		).build();
	}

	@Override
	protected String getClassName() {
		return JournalArticle.class.getName();
	}

	@Override
	protected long getResourcePrimKey(long classPK) throws PortalException {
		JournalArticle journalArticle = _journalArticleService.getArticle(
			classPK);

		return journalArticle.getResourcePrimKey();
	}

	@Reference(
		target = "(model.class.name=com.liferay.asset.kernel.model.AssetCategory)"
	)
	private HasPermission<Long> _hasPermission;

	@Reference
	private JournalArticleService _journalArticleService;

}