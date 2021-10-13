
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import MainLayout from 'layouts/MainLayout';

class NoSidebarLayout extends Component {
  constructor(props, context) {
    super(props, context);

    this.props = props;
  }

  render() {
    const { children, ...others } = this.props;

    return (
      <MainLayout
        showSidebar={false}
        {...others}
      >
        {children}
      </MainLayout>
    );
  }
}

NoSidebarLayout.propTypes = {

};

export default NoSidebarLayout;
