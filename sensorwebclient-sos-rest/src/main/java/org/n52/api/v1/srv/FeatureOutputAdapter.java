package org.n52.api.v1.srv;

import static org.n52.server.mgmt.ConfigurationContext.getSOSMetadatas;

import java.util.ArrayList;
import java.util.List;

import org.n52.api.v1.io.FeatureConverter;
import org.n52.io.v1.data.FeatureOutput;
import org.n52.shared.serializable.pojos.sos.Feature;
import org.n52.shared.serializable.pojos.sos.SOSMetadata;
import org.n52.shared.serializable.pojos.sos.TimeseriesParametersLookup;
import org.n52.web.v1.ctrl.QueryMap;
import org.n52.web.v1.srv.ParameterService;

public class FeatureOutputAdapter implements ParameterService<FeatureOutput> {

	@Override
	public FeatureOutput[] getExpandedParameters(QueryMap map) {
		List<FeatureOutput> allFeatures = new ArrayList<FeatureOutput>();
		for (SOSMetadata metadata : getSOSMetadatas()) {
		    FeatureConverter converter = new FeatureConverter(metadata);
			TimeseriesParametersLookup lookup = metadata.getTimeseriesParametersLookup();
			allFeatures.addAll(converter.convertExpanded(lookup.getFeaturesAsArray()));
		}
		return allFeatures.toArray(new FeatureOutput[0]);
	}


    @Override
    public FeatureOutput[] getCondensedParameters(QueryMap map) {
        List<FeatureOutput> allFeatures = new ArrayList<FeatureOutput>();
        for (SOSMetadata metadata : getSOSMetadatas()) {
            FeatureConverter converter = new FeatureConverter(metadata);
            TimeseriesParametersLookup lookup = metadata.getTimeseriesParametersLookup();
            allFeatures.addAll(converter.convertCondensed(lookup.getFeaturesAsArray()));
        }
        return allFeatures.toArray(new FeatureOutput[0]);
    }

    
	@Override
	public FeatureOutput getParameter(String featureId) {
		for (SOSMetadata metadata : getSOSMetadatas()) {
			TimeseriesParametersLookup lookup = metadata.getTimeseriesParametersLookup();
			for (Feature feature : lookup.getFeatures()) {
				if (feature.getGlobalId().equals(featureId)) {
					FeatureConverter converter = new FeatureConverter(metadata);
					return converter.convertExpanded(feature);
				}
			}
		}
		return null;
	}

}
