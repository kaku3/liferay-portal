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

package com.liferay.vulcan.jax.rs.writer.json.internal;

import static org.osgi.service.component.annotations.ReferenceCardinality.AT_LEAST_ONE;
import static org.osgi.service.component.annotations.ReferencePolicyOption.GREEDY;

import com.liferay.vulcan.error.VulcanDeveloperError;
import com.liferay.vulcan.list.FunctionalList;
import com.liferay.vulcan.message.RequestInfo;
import com.liferay.vulcan.message.SingleModelJSONMessageMapper;
import com.liferay.vulcan.message.json.JSONObjectBuilder;
import com.liferay.vulcan.pagination.Page;
import com.liferay.vulcan.representor.ModelRepresentorMapper;
import com.liferay.vulcan.wiring.osgi.RelatedModel;
import com.liferay.vulcan.wiring.osgi.RepresentorManager;
import com.liferay.vulcan.wiring.osgi.URIResolver;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import java.net.URI;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alejandro Hernández
 * @author Carlos Sierra Andrés
 * @author Jorge Ferrer
 */
@Component(
	immediate = true, property = "liferay.vulcan.message.body.writer=true"
)
@Provider
public class SingleModelMessageBodyWriter<T> implements MessageBodyWriter<T> {

	@Override
	public long getSize(
		T t, Class<?> type, Type genericType, Annotation[] annotations,
		MediaType mediaType) {

		return -1;
	}

	@Override
	public boolean isWriteable(
		Class<?> type, Type genericType, Annotation[] annotations,
		MediaType mediaType) {

		try {
			Class<T> modelClass = (Class<T>)genericType;

			if (modelClass.isAssignableFrom(Page.class)) {
				return false;
			}

			Optional<ModelRepresentorMapper<T>> optional =
				_representorManager.getModelRepresentorMapperOptional(
					modelClass);

			if (!optional.isPresent()) {
				return false;
			}

			return true;
		}
		catch (ClassCastException cce) {
			return false;
		}
	}

	@Override
	public void writeTo(
			T model, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream)
		throws IOException, WebApplicationException {

		Class<T> modelClass = (Class<T>)genericType;

		JSONObjectBuilder jsonObjectBuilder = new JSONObjectBuilderImpl();

		RequestInfo requestInfo = new RequestInfoImpl(mediaType, httpHeaders);

		Stream<SingleModelJSONMessageMapper<T>> stream =
			_singleModelJSONMessageMappers.stream();

		SingleModelJSONMessageMapper<T> singleModelJSONMessageMapper =
			stream.filter(
				messageMapper ->
					mediaType.toString().equals(messageMapper.getMediaType()) &&
						messageMapper.supports(model, modelClass, requestInfo)
			).findFirst().orElseThrow(
				() -> new VulcanDeveloperError.CannotFindMessageMapper(
					mediaType.toString(), modelClass)
			);

		_writeModel(
			singleModelJSONMessageMapper, jsonObjectBuilder, model, modelClass,
			requestInfo);

		PrintWriter printWriter = new PrintWriter(entityStream, true);

		printWriter.println(jsonObjectBuilder.build().toString());

		printWriter.close();
	}

	@Context
	protected HttpServletRequest httpServletRequest;

	@Context
	protected UriInfo uriInfo;

	private String _getAbsoluteURL(String relativeURI) {
		UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().clone();

		URI uri = uriBuilder.path(relativeURI).build();

		return uri.toString();
	}

	private <U> void _writeEmbeddedModelFields(
		SingleModelJSONMessageMapper<?> singleModelJSONMessageMapper,
		JSONObjectBuilder jsonObjectBuilder, U model, Class<U> modelClass,
		FunctionalList<String> embeddedPathElements) {

		Map<String, Function<U, Object>> fieldFunctions =
			_representorManager.getFieldFunctions(modelClass);

		for (String field : fieldFunctions.keySet()) {
			Object data = fieldFunctions.get(field).apply(model);

			if (data != null) {
				singleModelJSONMessageMapper.mapEmbeddedResourceField(
					jsonObjectBuilder, embeddedPathElements, field, data);
			}
		}
	}

	private <U> void _writeEmbeddedModelLinks(
		SingleModelJSONMessageMapper<?> singleModelJSONMessageMapper,
		JSONObjectBuilder jsonObjectBuilder, Class<U> modelClass,
		FunctionalList<String> embeddedPathElements) {

		Map<String, String> links = _representorManager.getLinks(modelClass);

		for (String key : links.keySet()) {
			singleModelJSONMessageMapper.mapEmbeddedResourceLink(
				jsonObjectBuilder, embeddedPathElements, key, links.get(key));
		}
	}

	private <U> void _writeEmbeddedModelTypes(
		SingleModelJSONMessageMapper<?> singleModelJSONMessageMapper,
		JSONObjectBuilder jsonObjectBuilder, Class<U> modelClass,
		FunctionalList<String> embeddedPathElements) {

		List<String> types = _representorManager.getTypes(modelClass);

		singleModelJSONMessageMapper.mapEmbeddedResourceTypes(
			jsonObjectBuilder, embeddedPathElements, types);
	}

	private <U, V> void _writeEmbeddedRelatedModel(
		SingleModelJSONMessageMapper<?> singleModelJSONMessageMapper,
		JSONObjectBuilder jsonObjectBuilder, RelatedModel<U, V> relatedModel,
		U parentModel, FunctionalList<String> parentEmbeddedPathElements) {

		Optional<V> modelOptional =
			relatedModel.getModelFunction().apply(parentModel);

		if (!modelOptional.isPresent()) {
			return;
		}

		V model = modelOptional.get();

		Class<V> modelClass = relatedModel.getModelClass();

		Optional<String> uriOptional =
			_uriResolver.getSingleResourceURIOptional(modelClass, model);

		uriOptional.ifPresent(
			uri -> {
				String key = relatedModel.getKey();

				FunctionalList<String> embeddedPathElements =
					new StringFunctionalList(parentEmbeddedPathElements, key);

				String url = _getAbsoluteURL(uri);

				_writeEmbeddedModelFields(
					singleModelJSONMessageMapper, jsonObjectBuilder, model,
					modelClass, embeddedPathElements);

				_writeEmbeddedModelLinks(
					singleModelJSONMessageMapper, jsonObjectBuilder, modelClass,
					embeddedPathElements);

				_writeEmbeddedModelTypes(
					singleModelJSONMessageMapper, jsonObjectBuilder, modelClass,
					embeddedPathElements);

				singleModelJSONMessageMapper.mapEmbeddedResourceURL(
					jsonObjectBuilder, embeddedPathElements, url);

				List<RelatedModel<V, ?>> embeddedRelatedModels =
					_representorManager.getEmbeddedRelatedModels(modelClass);

				_writeEmbeddedRelatedModels(
					singleModelJSONMessageMapper, jsonObjectBuilder,
					embeddedRelatedModels, model, embeddedPathElements);

				List<RelatedModel<V, ?>> linkedRelatedModels =
					_representorManager.getLinkedRelatedModels(modelClass);

				_writeLinkedRelatedModels(
					singleModelJSONMessageMapper, jsonObjectBuilder,
					linkedRelatedModels, model, embeddedPathElements);
			});
	}

	private <U> void _writeEmbeddedRelatedModels(
		SingleModelJSONMessageMapper<?> singleModelJSONMessageMapper,
		JSONObjectBuilder jsonObjectBuilder,
		List<RelatedModel<U, ?>> relatedModels, U parentModel,
		FunctionalList<String> embeddedPathElements) {

		for (RelatedModel<U, ?> relatedModel : relatedModels) {
			_writeEmbeddedRelatedModel(
				singleModelJSONMessageMapper, jsonObjectBuilder, relatedModel,
				parentModel, embeddedPathElements);
		}
	}

	private <U, V> void _writeLinkedRelatedModel(
		SingleModelJSONMessageMapper<?> singleModelJSONMessageMapper,
		JSONObjectBuilder jsonObjectBuilder, RelatedModel<U, V> relatedModel,
		U parentModel, FunctionalList<String> parentEmbeddedPathElements) {

		Optional<V> modelOptional =
			relatedModel.getModelFunction().apply(parentModel);

		if (!modelOptional.isPresent()) {
			return;
		}

		V model = modelOptional.get();

		Class<V> modelClass = relatedModel.getModelClass();

		Optional<String> uriOptional =
			_uriResolver.getSingleResourceURIOptional(modelClass, model);

		uriOptional.ifPresent(
			uri -> {
				String key = relatedModel.getKey();

				FunctionalList<String> embeddedPathElements =
					new StringFunctionalList(parentEmbeddedPathElements, key);

				String url = _getAbsoluteURL(uri);

				singleModelJSONMessageMapper.mapLinkedResourceURL(
					jsonObjectBuilder, embeddedPathElements, url);
			});
	}

	private <U> void _writeLinkedRelatedModels(
		SingleModelJSONMessageMapper<?> singleModelJSONMessageMapper,
		JSONObjectBuilder jsonObjectBuilder,
		List<RelatedModel<U, ?>> relatedModels, U parentModel,
		FunctionalList<String> embeddedPathElements) {

		for (RelatedModel<U, ?> relatedModel : relatedModels) {
			_writeLinkedRelatedModel(
				singleModelJSONMessageMapper, jsonObjectBuilder, relatedModel,
				parentModel, embeddedPathElements);
		}
	}

	private <U> void _writeModel(
		SingleModelJSONMessageMapper<U> singleModelJSONMessageMapper,
		JSONObjectBuilder jsonObjectBuilder, U model, Class<U> modelClass,
		RequestInfo requestInfo) {

		singleModelJSONMessageMapper.onStart(
			jsonObjectBuilder, model, modelClass, requestInfo);

		_writeModelFields(
			singleModelJSONMessageMapper, jsonObjectBuilder, model, modelClass);

		_writeModelLinks(
			singleModelJSONMessageMapper, jsonObjectBuilder, modelClass);

		_writeModelTypes(
			singleModelJSONMessageMapper, jsonObjectBuilder, modelClass);

		_writeSelfURL(
			singleModelJSONMessageMapper, jsonObjectBuilder, model, modelClass);

		List<RelatedModel<U, ?>> embeddedRelatedModels =
			_representorManager.getEmbeddedRelatedModels(modelClass);

		_writeEmbeddedRelatedModels(
			singleModelJSONMessageMapper, jsonObjectBuilder,
			embeddedRelatedModels, model, null);

		List<RelatedModel<U, ?>> linkedRelatedModels =
			_representorManager.getLinkedRelatedModels(modelClass);

		_writeLinkedRelatedModels(
			singleModelJSONMessageMapper, jsonObjectBuilder,
			linkedRelatedModels, model, null);

		singleModelJSONMessageMapper.onFinish(
			jsonObjectBuilder, model, modelClass, requestInfo);
	}

	private <U> void _writeModelFields(
		SingleModelJSONMessageMapper<U> singleModelJSONMessageMapper,
		JSONObjectBuilder jsonObjectBuilder, U model, Class<U> modelClass) {

		Map<String, Function<U, Object>> fieldFunctions =
			_representorManager.getFieldFunctions(modelClass);

		for (String field : fieldFunctions.keySet()) {
			Object data = fieldFunctions.get(field).apply(model);

			if (data != null) {
				singleModelJSONMessageMapper.mapField(
					jsonObjectBuilder, field, data);
			}
		}
	}

	private <U> void _writeModelLinks(
		SingleModelJSONMessageMapper<U> singleModelJSONMessageMapper,
		JSONObjectBuilder jsonObjectBuilder, Class<U> modelClass) {

		Map<String, String> links = _representorManager.getLinks(modelClass);

		for (String key : links.keySet()) {
			singleModelJSONMessageMapper.mapLink(
				jsonObjectBuilder, key, links.get(key));
		}
	}

	private <U> void _writeModelTypes(
		SingleModelJSONMessageMapper<U> singleModelJSONMessageMapper,
		JSONObjectBuilder jsonObjectBuilder, Class<U> modelClass) {

		List<String> types = _representorManager.getTypes(modelClass);

		singleModelJSONMessageMapper.mapTypes(jsonObjectBuilder, types);
	}

	private <U> void _writeSelfURL(
		SingleModelJSONMessageMapper<U> singleModelJSONMessageMapper,
		JSONObjectBuilder jsonObjectBuilder, U model, Class<U> modelClass) {

		Optional<String> optional = _uriResolver.getSingleResourceURIOptional(
			modelClass, model);

		String uri = optional.orElseThrow(
			() -> new VulcanDeveloperError.CannotCalculateURI(modelClass));

		String url = _getAbsoluteURL(uri);

		singleModelJSONMessageMapper.mapSelfURL(jsonObjectBuilder, url);
	}

	@Reference
	private RepresentorManager _representorManager;

	@Reference(cardinality = AT_LEAST_ONE, policyOption = GREEDY)
	private List<SingleModelJSONMessageMapper<T>>
		_singleModelJSONMessageMappers;

	@Reference
	private URIResolver _uriResolver;

}