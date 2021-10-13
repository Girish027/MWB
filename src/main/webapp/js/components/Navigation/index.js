import { connect } from 'react-redux';
import NavigationBar from './NavigationBar';

const mapStateToProps = (state, ownProps) => ({});
const mapDispatchToProps = dispatch => ({
  dispatch,
});

export default connect(mapStateToProps, mapDispatchToProps)(NavigationBar);
