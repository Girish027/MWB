import React from 'react';
import { Route } from 'react-router-dom';

const AppRoute = ({
  component: AppComponent,
  layout: AppLayout,
  ...rest
}) => (
  <Route
    {...rest}
    render={props => (
      <AppLayout
        {...props}
      >
        <AppComponent {...props} />
      </AppLayout>
    )
    }
  />
);

export default AppRoute;
