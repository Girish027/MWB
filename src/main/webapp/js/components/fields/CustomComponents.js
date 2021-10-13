import CustomTextArea from './CustomTextArea';
import CustomTextField from './CustomTextField';
import CustomDropDown from './CustomDropDown';

export default {
  text: CustomTextField,
  textarea: CustomTextArea,
  select: CustomDropDown,
  noop: () => null,
};
