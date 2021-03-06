/**
 * ﻿Copyright (C) 2012-2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as publishedby the Free
 * Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU General Public License version 2
 * and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
package org.n52.api.v1.srv;

import static org.n52.server.mgmt.ConfigurationContext.getSOSMetadatas;
import static org.n52.shared.requests.query.QueryParameters.createEmptyFilterQuery;

import java.util.ArrayList;
import java.util.Collection;

import org.n52.api.v1.io.CategoryConverter;
import org.n52.shared.serializable.pojos.sos.Feature;
import org.n52.shared.serializable.pojos.sos.Offering;
import org.n52.shared.serializable.pojos.sos.Phenomenon;
import org.n52.shared.serializable.pojos.sos.Procedure;
import org.n52.shared.serializable.pojos.sos.SOSMetadata;
import org.n52.shared.serializable.pojos.sos.SosTimeseries;
import org.n52.shared.serializable.pojos.sos.Station;
import org.n52.shared.serializable.pojos.sos.TimeseriesParametersLookup;
import org.n52.web.v1.srv.SearchService;
import org.n52.web.v1.srv.search.CategorySearchResult;
import org.n52.web.v1.srv.search.FeatureSearchResult;
import org.n52.web.v1.srv.search.OfferingSearchResult;
import org.n52.web.v1.srv.search.PhenomenonSearchResult;
import org.n52.web.v1.srv.search.ProcedureSearchResult;
import org.n52.web.v1.srv.search.SearchResult;
import org.n52.web.v1.srv.search.ServiceSearchResult;
import org.n52.web.v1.srv.search.StationSearchResult;
import org.n52.web.v1.srv.search.TimeseriesSearchResult;

public class SearchAdapter implements SearchService {

    @Override
    public Collection<SearchResult> searchResources(String search, String locale) {
        // language specific search is not supported by the aggregation component
        return searchResources(search);
    }

    private Collection<SearchResult> searchResources(String search) {
        
        // TODO extend search logic to support composed search strings

        ArrayList<SearchResult> results = new ArrayList<SearchResult>();
        results.addAll(requestServices(search));
        results.addAll(requestStations(search));
        results.addAll(requestTimeseries(search));
        results.addAll(requestTSParameters(search));
        return results;
    }

    private Collection<SearchResult> requestTimeseries(String search) {
        Collection<SearchResult> results = new ArrayList<SearchResult>();
        for (SOSMetadata metadata : getSOSMetadatas()) {
            SosTimeseries[] timeseries = metadata.getMatchingTimeseries(createEmptyFilterQuery());
            for (SosTimeseries ts : timeseries) {
                if (containsSearchString(ts.getFeature().getLabel(), search)
                        || containsSearchString(ts.getPhenomenon().getLabel(), search)
                        || containsSearchString(ts.getProcedure().getLabel(), search)
                        || containsSearchString(ts.getOffering().getLabel(), search)
                        || containsSearchString(ts.getCategory(), search)) {
                    results.add(new TimeseriesSearchResult(ts.getTimeseriesId(), ts.getLabel()));
                }
            }
        }
        return results;
    }

    private Collection<SearchResult> requestStations(String search) {
        Collection<SearchResult> results = new ArrayList<SearchResult>();
        for (SOSMetadata metadata : getSOSMetadatas()) {
            for (Station station : metadata.getStations()) {
                if (containsSearchString(station.getLabel(), search)) {
                    results.add(new StationSearchResult(station.getGlobalId(), station.getLabel()));
                }
            }
        }
        return results;
    }

    private Collection<SearchResult> requestTSParameters(String search) {
        Collection<SearchResult> results = new ArrayList<SearchResult>();
        for (SOSMetadata metadata : getSOSMetadatas()) {
            TimeseriesParametersLookup lookup = metadata.getTimeseriesParametersLookup();
            // offerings
            for (Offering offering : lookup.getOfferings()) {
                if (containsSearchString(offering.getLabel(), search)) {
                    results.add(new OfferingSearchResult(offering.getGlobalId(), offering.getLabel()));
                }
            }
            // features
            for (Feature feature : lookup.getFeatures()) {
                if (containsSearchString(feature.getLabel(), search)) {
                    results.add(new FeatureSearchResult(feature.getGlobalId(), feature.getLabel()));
                }
            }
            // procedures
            for (Procedure procedure : lookup.getProcedures()) {
                if (containsSearchString(procedure.getLabel(), search)) {
                    results.add(new ProcedureSearchResult(procedure.getGlobalId(), procedure.getLabel()));
                }
            }
            // phenomena
            for (Phenomenon phenomenon : lookup.getPhenomenons()) {
                if (containsSearchString(phenomenon.getLabel(), search)) {
                    results.add(new PhenomenonSearchResult(phenomenon.getGlobalId(), phenomenon.getLabel()));
                }
            }
            // categories
            SosTimeseries[] timeseries = metadata.getMatchingTimeseries(createEmptyFilterQuery());
            for (SosTimeseries sosTimeseries : timeseries) {
                if (containsSearchString(sosTimeseries.getCategory(), search)) {
                    CategoryConverter converter = new CategoryConverter(metadata);
                    String generateCategoryId = converter.generateId(sosTimeseries.getCategory());
                    results.add(new CategorySearchResult(generateCategoryId, sosTimeseries.getCategory()));
                }
            }
        }
        return results;
    }

    private Collection<SearchResult> requestServices(String search) {
        Collection<SearchResult> results = new ArrayList<SearchResult>();
        for (SOSMetadata metadata : getSOSMetadatas()) {
            if (containsSearchString(metadata.getTitle(), search)) {
                results.add(new ServiceSearchResult(metadata.getGlobalId(), metadata.getTitle()));
            }
        }
        return results;
    }

    /**
     * @param label
     *        the label to check.
     * @param searchToken
     *        the input search token.
     * @return <code>true</code> if the <code>label</code> contains the <code>searchToken</code> and ignores
     *         the case.
     */
    private boolean containsSearchString(String label, String searchToken) {
        return label.toLowerCase().contains(searchToken.toLowerCase());
    }

}
