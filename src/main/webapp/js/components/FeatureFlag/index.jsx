import React, { Component } from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';

class FeatureFlag extends Component {
  constructor(props) {
    super(props);
  }

  render() {
    const { featureFlagValue, components, defaultComponent } = this.props;
    const componentToRender = components[featureFlagValue];

    if (!_.isNil(componentToRender)) {
      return componentToRender;
    }

    return defaultComponent;
  }
}

FeatureFlag.defaultProps = {
  defaultComponent: '',
};

FeatureFlag.propTypes = {
  defaultComponent: PropTypes.node,
  featureFlagValue: PropTypes.string.isRequired,
  components: PropTypes.object.isRequired,
};

export default FeatureFlag;
