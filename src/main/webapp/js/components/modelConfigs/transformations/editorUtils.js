
import _ from 'lodash';

// Update state under the following conditions.
// 1) Initial selection of transformation - no currentTransformation
// 2) Transformations have different names - may be the same type or
//    different types
// 3) Transformations have different types
// 4) No unsaved changes
export const shouldUpdateState = (nextProps, state) => {
  const nextTransformation = nextProps.transformation;
  const currentTransformation = state.transformation;
  let shouldUpdate = false;

  if (!_.isNil(nextTransformation)) {
    if (_.isNil(currentTransformation)) {
      shouldUpdate = true;
    } else {
      if (!state.unsavedChanges) {
        shouldUpdate = true;
      }

      const nextName = Object.keys(nextTransformation)[0];
      const currentName = Object.keys(currentTransformation)[0];

      if ((nextTransformation.type !== currentTransformation.type)
        || (nextName !== currentName)) {
        shouldUpdate = true;
      }
    }
  }

  return shouldUpdate;
};
