import isFeatureEnabled, { featureFlagDefinitions } from 'utils/FeatureFlags';

describe('FeatureFlags', () => {
  describe('isFeatureEnabled', () => {
    test('returns false if userFeatureConfiguration is not defined', () => {
      const userFeatureConfiguration = null;
      const val = isFeatureEnabled(featureFlagDefinitions.names.models, userFeatureConfiguration, featureFlagDefinitions.options.show);
      expect(val).toBe(false);
    });
    test('returns false if the requested feature is null', () => {
      const userFeatureConfiguration = {
        [featureFlagDefinitions.names.models]: featureFlagDefinitions.hide,
      };
      const val = isFeatureEnabled(null, userFeatureConfiguration, featureFlagDefinitions.options.show);
      expect(val).toBe(false);
    });
    test('returns false if the requested enabledCheck is null', () => {
      const userFeatureConfiguration = {
        [featureFlagDefinitions.names.models]: featureFlagDefinitions.hide,
      };
      const val = isFeatureEnabled(featureFlagDefinitions.names.models, userFeatureConfiguration, null);
      expect(val).toBe(false);
    });
    test('returns false if the feature is not enabled', () => {
      const userFeatureConfiguration = {
        [featureFlagDefinitions.names.models]: featureFlagDefinitions.hide,
      };
      const val = isFeatureEnabled(featureFlagDefinitions.names.models, userFeatureConfiguration, featureFlagDefinitions.options.show);
      expect(val).toBe(false);
    });
    test('returns false if feature is not defined in the userFeatureConfiguration', () => {
      const userFeatureConfiguration = {
        [featureFlagDefinitions.modelTrainingOutputs]: featureFlagDefinitions.hide,
      };
      const val = isFeatureEnabled(featureFlagDefinitions.names.models, userFeatureConfiguration, featureFlagDefinitions.options.show);
      expect(val).toBe(false);
    });
    test('returns true if the feature is enabled', () => {
      const userFeatureConfiguration = {
        [featureFlagDefinitions.names.models]: featureFlagDefinitions.options.show,
      };
      const val = isFeatureEnabled(featureFlagDefinitions.names.models, userFeatureConfiguration, featureFlagDefinitions.options.show);
      expect(val).toBe(true);
    });
    test('returns true if the feature is not defined', () => {
      const userFeatureConfiguration = {
        [featureFlagDefinitions.names.models]: featureFlagDefinitions.options.show,
      };
      const val = isFeatureEnabled(featureFlagDefinitions.names.models, userFeatureConfiguration);
      expect(val).toBe(true);
    });
  });
});
