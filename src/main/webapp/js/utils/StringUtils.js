
export const escapeRegExp = (str) => {
  // Referring to the table here:
  // https://developer.mozilla.org/en/JavaScript/Reference/Global_Objects/regexp
  // these characters should be escaped
  // \ ^ $ * + ? . ( ) | { } [ ]
  // These characters only have special meaning inside of brackets
  // they do not need to be escaped, but they MAY be escaped
  // without any adverse effects (to the best of my knowledge and casual testing)
  // : ! , =
  // my test "~!@#$%^&*(){}[]`/=?+\|-_;:'\",<.>".match(/[\#]/g)

  const specials = [
    // order matters for these
    '-',
    '[',
    ']',
    // order doesn't matter for any of these
    '/',
    '{',
    '}',
    '(',
    ')',
    '*',
    '+',
    '?',
    '.',
    '\\',
    '^',
    '$',
    '|',
  ];

  // I choose to escape every character with '\'
  // even though only some strictly require it when inside of []
  const regex = RegExp(`[${specials.join('\\')}]`, 'g');
  return (`${str}`).replace(regex, '\\$&');
  // test escapeRegExp("/path/to/res?search=this.that")
};

export const capitalizeFirstLetter = string => string.charAt(0).toUpperCase() + string.slice(1);

export const titleCase = str => str.toLowerCase().split(' ').map(word => word.replace(word[0], word[0].toUpperCase())).join(' ');
