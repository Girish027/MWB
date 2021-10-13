
import React, { Component } from 'react';
import MainLayout from 'layouts/MainLayout';

class SettingsLayout extends Component {
  constructor(props, context) {
    super(props, context);

    this.props = props;
  }

  render() {
    const { children, ...others } = this.props;

    return (
      <MainLayout
        isSettingsPage
        showSidebar={false}
        {...others}
      >
        {children}
      </MainLayout>
    );
  }
}

export default SettingsLayout;
