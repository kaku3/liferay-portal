<%--
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
--%>

<%@ include file="/blogs/init.jsp" %>

<%
SearchContainer searchContainer = (SearchContainer)request.getAttribute("view_entry_content.jsp-searchContainer");

BlogsEntry entry = (BlogsEntry)request.getAttribute("view_entry_content.jsp-entry");

RatingsEntry ratingsEntry = (RatingsEntry)request.getAttribute("view_entry_content.jsp-ratingsEntry");
RatingsStats ratingsStats = (RatingsStats)request.getAttribute("view_entry_content.jsp-ratingsStats");
%>

<c:choose>
	<c:when test="<%= BlogsEntryPermission.contains(permissionChecker, entry, ActionKeys.VIEW) && (entry.isVisible() || (entry.getUserId() == user.getUserId()) || BlogsEntryPermission.contains(permissionChecker, entry, ActionKeys.UPDATE)) %>">
		<portlet:renderURL var="viewEntryURL">
			<portlet:param name="mvcRenderCommandName" value="/blogs/view_entry" />
			<portlet:param name="redirect" value="<%= currentURL %>" />

			<c:choose>
				<c:when test="<%= Validator.isNotNull(entry.getUrlTitle()) %>">
					<portlet:param name="urlTitle" value="<%= entry.getUrlTitle() %>" />
				</c:when>
				<c:otherwise>
					<portlet:param name="entryId" value="<%= String.valueOf(entry.getEntryId()) %>" />
				</c:otherwise>
			</c:choose>
		</portlet:renderURL>

		<div class="widget-mode-simple-entry">
			<div class="autofit-row widget-topbar">
				<div class="autofit-col autofit-col-expand">
					<h3 class="title">
						<aui:a cssClass="title-link" href="<%= viewEntryURL %>"><%= HtmlUtil.escape(BlogsEntryUtil.getDisplayTitle(resourceBundle, entry)) %></aui:a>
					</h3>

					<%
					String subtitle = entry.getSubtitle();
					%>

					<c:if test="<%= blogsPortletInstanceConfiguration.displayStyle().equals(BlogsUtil.DISPLAY_STYLE_FULL_CONTENT) && Validator.isNotNull(subtitle) %>">
						<h4 class="sub-title"><%= HtmlUtil.escape(subtitle) %></h4>
					</c:if>
				</div>

				<div class="autofit-col visible-interaction">
					<div class="dropdown dropdown-action">
						<liferay-util:include page="/blogs/entry_action.jsp" servletContext="<%= application %>" />
					</div>
				</div>
			</div>

			<div class="autofit-row widget-metadata">
				<div class="autofit-col inline-item-before">

					<%
					User entryUser = UserLocalServiceUtil.fetchUser(entry.getUserId());

					String entryUserURL = StringPool.BLANK;

					if ((entryUser != null) && !entryUser.isDefaultUser()) {
						entryUserURL = entryUser.getDisplayURL(themeDisplay);
					}
					%>

					<liferay-ui:user-portrait
						cssClass="user-icon-lg"
						user="<%= entryUser %>"
					/>
				</div>

				<div class="autofit-col autofit-col-expand">
					<div class="autofit-row">
						<div class="autofit-col autofit-col-expand">
							<div class="text-truncate-inline">
								<a class="text-truncate username" href="<%= entryUserURL %>"><%= entry.getUserName() %></a>
							</div>

							<div class="text-secondary">
								<span class="hide-accessible"><liferay-ui:message key="published-date" /></span><liferay-ui:message arguments="<%= LanguageUtil.getTimeDescription(request, System.currentTimeMillis() - entry.getStatusDate().getTime(), true) %>" key="x-ago" translateArguments="<%= false %>" />

								<c:if test="<%= blogsPortletInstanceConfiguration.enableReadingTime() %>">
									- <liferay-reading-time:reading-time displayStyle="descriptive" model="<%= entry %>" />
								</c:if>

								<c:if test="<%= blogsPortletInstanceConfiguration.enableViewCount() %>">

									<%
									AssetEntry assetEntry = _getAssetEntry(request, entry);
									%>

									- <liferay-ui:message arguments="<%= assetEntry.getViewCount() %>" key='<%= (assetEntry.getViewCount() == 1) ? "x-view" : "x-views" %>' />
								</c:if>
							</div>
						</div>
					</div>
				</div>
			</div>

			<div class="widget-content" id="<portlet:namespace /><%= entry.getEntryId() %>">

				<%
				String coverImageURL = entry.getCoverImageURL(themeDisplay);
				%>

				<c:if test="<%= Validator.isNotNull(coverImageURL) %>">
					<a href="<%= viewEntryURL.toString() %>">
						<div class="aspect-ratio aspect-ratio-8-to-3 aspect-ratio-bg-cover cover-image" style="background-image: url(<%= coverImageURL %>)"></div>
					</a>
				</c:if>

				<c:choose>
					<c:when test="<%= blogsPortletInstanceConfiguration.displayStyle().equals(BlogsUtil.DISPLAY_STYLE_ABSTRACT) %>">

						<%
						String summary = entry.getDescription();

						if (Validator.isNull(summary)) {
							summary = entry.getContent();
						}
						%>

						<p>
							<%= StringUtil.shorten(HtmlUtil.stripHtml(summary), pageAbstractLength) %>
						</p>
					</c:when>
					<c:when test="<%= blogsPortletInstanceConfiguration.displayStyle().equals(BlogsUtil.DISPLAY_STYLE_FULL_CONTENT) %>">
						<%= entry.getContent() %>
					</c:when>
				</c:choose>

				<div class="autofit-float autofit-row autofit-row-center widget-toolbar">
					<c:if test="<%= blogsPortletInstanceConfiguration.enableComments() %>">
						<div class="autofit-col">

							<%
							int messagesCount = CommentManagerUtil.getCommentsCount(BlogsEntry.class.getName(), entry.getEntryId());
							%>

							<portlet:renderURL var="viewEntryCommentsURL">
								<portlet:param name="mvcRenderCommandName" value="/blogs/view_entry" />
								<portlet:param name="scroll" value='<%= renderResponse.getNamespace() + "discussionContainer" %>' />

								<c:choose>
									<c:when test="<%= Validator.isNotNull(entry.getUrlTitle()) %>">
										<portlet:param name="urlTitle" value="<%= entry.getUrlTitle() %>" />
									</c:when>
									<c:otherwise>
										<portlet:param name="entryId" value="<%= String.valueOf(entry.getEntryId()) %>" />
									</c:otherwise>
								</c:choose>
							</portlet:renderURL>

							<liferay-util:whitespace-remover>
								<a class="btn btn-outline-borderless btn-outline-secondary btn-sm" href="<%= viewEntryCommentsURL.toString() %>">
									<span class="inline-item inline-item-before">
										<clay:icon
											symbol="comments"
										/>
									</span>

									<liferay-ui:message arguments="<%= messagesCount %>" key="comment-x" />
								</a>
							</liferay-util:whitespace-remover>
						</div>
					</c:if>

					<c:if test="<%= blogsPortletInstanceConfiguration.enableFlags() && blogsPortletInstanceConfiguration.displayStyle().equals(BlogsUtil.DISPLAY_STYLE_FULL_CONTENT) %>">
						<div class="autofit-col">
							<div class="flags">
								<liferay-flags:flags
									className="<%= BlogsEntry.class.getName() %>"
									classPK="<%= entry.getEntryId() %>"
									contentTitle="<%= BlogsEntryUtil.getDisplayTitle(resourceBundle, entry) %>"
									enabled="<%= !entry.isInTrash() %>"
									message='<%= entry.isInTrash() ? "flags-are-disabled-because-this-entry-is-in-the-recycle-bin" : StringPool.BLANK %>'
									reportedUserId="<%= entry.getUserId() %>"
								/>
							</div>
						</div>
					</c:if>

					<c:if test="<%= blogsPortletInstanceConfiguration.enableRatings() %>">
						<div class="autofit-col">
							<div class="ratings">
								<liferay-ui:ratings
									className="<%= BlogsEntry.class.getName() %>"
									classPK="<%= entry.getEntryId() %>"
									inTrash="<%= entry.isInTrash() %>"
									ratingsEntry="<%= ratingsEntry %>"
									ratingsStats="<%= ratingsStats %>"
								/>
							</div>
						</div>
					</c:if>

					<div class="autofit-col autofit-col-end">
						<%@ include file="/blogs/social_bookmarks.jspf" %>
					</div>
				</div>
			</div>

			<c:if test="<%= blogsPortletInstanceConfiguration.displayStyle().equals(BlogsUtil.DISPLAY_STYLE_FULL_CONTENT) %>">
				<liferay-asset:asset-tags-available
					className="<%= BlogsEntry.class.getName() %>"
					classPK="<%= entry.getEntryId() %>"
				>
					<div class="entry-tags">
						<liferay-asset:asset-tags-summary
							className="<%= BlogsEntry.class.getName() %>"
							classPK="<%= entry.getEntryId() %>"
							portletURL="<%= renderResponse.createRenderURL() %>"
						/>
					</div>
				</liferay-asset:asset-tags-available>

				<c:if test="<%= blogsPortletInstanceConfiguration.enableRelatedAssets() %>">
					<div class="entry-links">

						<%
						AssetEntry assetEntry = _getAssetEntry(request, entry);
						%>

						<liferay-asset:asset-links
							assetEntryId="<%= (assetEntry != null) ? assetEntry.getEntryId() : 0 %>"
							className="<%= BlogsEntry.class.getName() %>"
							classPK="<%= entry.getEntryId() %>"
						/>
					</div>
				</c:if>

				<liferay-asset:asset-categories-available
					className="<%= BlogsEntry.class.getName() %>"
					classPK="<%= entry.getEntryId() %>"
				>
					<div class="entry-categories">
						<liferay-asset:asset-categories-summary
							className="<%= BlogsEntry.class.getName() %>"
							classPK="<%= entry.getEntryId() %>"
							portletURL="<%= renderResponse.createRenderURL() %>"
						/>
					</div>
				</liferay-asset:asset-categories-available>
			</c:if>
		</div>
	</c:when>
	<c:otherwise>

		<%
		if (searchContainer != null) {
			searchContainer.setTotal(searchContainer.getTotal() - 1);
		}
		%>

	</c:otherwise>
</c:choose>

<%!
private AssetEntry _getAssetEntry(HttpServletRequest request, BlogsEntry entry) throws PortalException, SystemException {
	AssetEntry assetEntry = (AssetEntry)request.getAttribute("view_entry_content.jsp-assetEntry");

	if (assetEntry == null) {
		assetEntry = AssetEntryLocalServiceUtil.getEntry(BlogsEntry.class.getName(), entry.getEntryId());

		request.setAttribute("view_entry_content.jsp-assetEntry", assetEntry);
	}

	return assetEntry;
}
%>