import React, { Component } from 'react';

class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = {
      hasError: false,
      info: {
        componentStack: '',
      },
    };
  }

  componentDidCatch(error, info) {
    // Display fallback UI
    this.setState({
      hasError: true,
      error,
      info,
    });
  }

  render() {
    if (this.state.hasError) {
      // You can render any custom fallback UI
      return (
        <React.Fragment>

          <div
            style={{
              textAlign: 'center',
              fontSize: '20px',
              marginTop: '30px',
              marginBottom: '30px',
            }}
          >
            An error has occurred.
          </div>
          <div
            data-error={this.state.info.componentStack}
            data-error-message={this.state.error.message}
            data-error-stack={this.state.error.stack}
          />
        </React.Fragment>
      );
    }
    return this.props.children;
  }
}

export default ErrorBoundary;
